package com.example.media

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSourceException
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.HttpDataSource.HttpDataSourceException
import androidx.media3.datasource.HttpUtil
import androidx.media3.datasource.HttpUtil.buildRangeRequestHeader
import androidx.media3.datasource.TransferListener
import com.google.common.net.HttpHeaders
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.NoRouteToHostException
import java.net.URL
import java.util.zip.GZIPInputStream
import kotlin.math.min
import com.example.util.Logger


@OptIn(UnstableApi::class)
class CustomHttpDataSource(
    private val userAgent: String?,
    private val connectTimeoutMs: Int,
    private val readTimeoutMs: Int,
    private val allowCrossProtocolRedirects: Boolean,
    private val crossProtocolRedirectsForceOriginal: Boolean,
    private val defaultRequestProperties: HttpDataSource.RequestProperties?,
    private val contentTypePredicate: ((String) -> Boolean)?,
    private val keepPostFor302Redirects: Boolean,
    private val rangeParameterEnabled: Boolean = false,
    private val rnParameterEnabled: Boolean = false
) : BaseDataSource(true), HttpDataSource {

    class Factory : HttpDataSource.Factory {
        private val defaultRequestProperties = HttpDataSource.RequestProperties()
        private var transferListener: TransferListener? = null
        private var contentTypePredicate: ((String) -> Boolean)? = null
        private var userAgent: String? = null
        private var connectTimeoutMs: Int = DEFAULT_CONNECT_TIMEOUT_MILLIS
        private var readTimeoutMs: Int = DEFAULT_READ_TIMEOUT_MILLIS
        private var allowCrossProtocolRedirects: Boolean = false
        private var crossProtocolRedirectsForceOriginal: Boolean = false
        private var keepPostFor302Redirects: Boolean = false

        private var rangeParameterEnabled: Boolean = false
        private var rnParameterEnabled: Boolean = false

        override fun setDefaultRequestProperties(props: Map<String, String>): Factory = apply {
            defaultRequestProperties.clearAndSet(props)
        }

        fun setUserAgent(userAgent: String?): Factory = apply {
            this.userAgent = userAgent
        }

        fun setConnectTimeoutMs(timeout: Int): Factory = apply {
            this.connectTimeoutMs = timeout
        }

        fun setReadTimeoutMs(timeout: Int): Factory = apply {
            this.readTimeoutMs = timeout
        }

        fun setAllowCrossProtocolRedirects(allow: Boolean): Factory = apply {
            this.allowCrossProtocolRedirects = allow
        }

        fun setCrossProtocolRedirectsForceOriginal(force: Boolean): Factory = apply {
            this.crossProtocolRedirectsForceOriginal = force
        }

        fun setContentTypePredicate(predicate: ((String) -> Boolean)?): Factory = apply {
            this.contentTypePredicate = predicate
        }

        fun setTransferListener(listener: TransferListener?): Factory = apply {
            this.transferListener = listener
        }

        fun setKeepPostFor302Redirects(keep: Boolean): Factory = apply {
            this.keepPostFor302Redirects = keep
        }

        fun setRangeParameterEnabled(enabled: Boolean): Factory = apply {
            this.rangeParameterEnabled = enabled
        }

        fun setRnParameterEnabled(enabled: Boolean): Factory = apply {
            this.rnParameterEnabled = enabled
        }

        override fun createDataSource(): CustomHttpDataSource {
            Logger.d("CustomHttpDataSource.Factory: Creating with rangeParam=$rangeParameterEnabled, rnParam=$rnParameterEnabled, connectTimeout=$connectTimeoutMs, readTimeout=$readTimeoutMs")
            val dataSource = CustomHttpDataSource(
                userAgent,
                connectTimeoutMs,
                readTimeoutMs,
                allowCrossProtocolRedirects,
                crossProtocolRedirectsForceOriginal,
                defaultRequestProperties,
                contentTypePredicate,
                keepPostFor302Redirects,
                rangeParameterEnabled,
                rnParameterEnabled
            )
            transferListener?.let { dataSource.addTransferListener(it) }
            return dataSource
        }
    }

    companion object {
        /** The default connection timeout, in milliseconds. */
        const val DEFAULT_CONNECT_TIMEOUT_MILLIS = 8 * 1000

        /** The default read timeout, in milliseconds. */
        const val DEFAULT_READ_TIMEOUT_MILLIS = 8 * 1000

        private const val TAG = "CustomHttpDataSource"
        private const val MAX_REDIRECTS = 20 // Same limit as okhttp.
        private const val HTTP_STATUS_TEMPORARY_REDIRECT = 307
        private const val HTTP_STATUS_PERMANENT_REDIRECT = 308
        private const val MAX_BYTES_TO_DRAIN = 2048L

        private const val RN_PARAMETER = "&rn="
        private const val YOUTUBE_BASE_URL = "https://www.youtube.com"
        private val POST_BODY = byteArrayOf(0x78, 0)

        const val ANDROID_CLIENT_VERSION: String = "19.28.35"

    }

    private var dataSpec: DataSpec? = null
    private var connection: HttpURLConnection? = null
    private var inputStream: InputStream? = null
    private var opened: Boolean = false
    private var responseCode: Int = 0
    private var bytesToRead: Long = 0
    private var bytesRead: Long = 0
    private var requestProperties = HttpDataSource.RequestProperties()

    private var requestNumber: Long = 0


    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        try {
            return readInternal(buffer, offset, length)
        } catch (e: IOException) {
            throw HttpDataSourceException.createForIOException(
                e, Util.castNonNull(dataSpec), HttpDataSourceException.TYPE_READ
            )
        }
    }

    /**
     * Reads up to `length` bytes of data and stores them into `buffer`, starting at index
     * `offset`.
     *
     *
     * This method blocks until at least one byte of data can be read, the end of the opened range
     * is detected, or an exception is thrown.
     *
     * @param buffer The buffer into which the read data should be stored.
     * @param offset The start offset into `buffer` at which data should be written.
     * @param readLength The maximum number of bytes to read.
     * @return The number of bytes read, or [C.RESULT_END_OF_INPUT] if the end of the opened
     * range is reached.
     * @throws IOException If an error occurs reading from the source.
     */
    private fun readInternal(buffer: ByteArray, offset: Int, readLength: Int): Int {
        var tempReadLength = readLength

        if (readLength == 0) {
            return 0
        }
        if (bytesToRead != C.LENGTH_UNSET.toLong()) {
            val bytesRemaining: Long = bytesToRead - bytesRead
            if (bytesRemaining == 0L) {
                return C.RESULT_END_OF_INPUT
            }
            tempReadLength = min(readLength.toDouble(), bytesRemaining.toDouble()).toInt()
        }

        val read = Util.castNonNull<InputStream>(inputStream).read(buffer, offset, tempReadLength)
        if (read == -1) {
            return C.RESULT_END_OF_INPUT
        }

        bytesRead += read.toLong()
        bytesTransferred(read)
        
        // Log every 1MB of data read
        if (bytesRead % (1024 * 1024) < read) {
            Logger.d("CustomHttpDataSource.read: bytesRead=${bytesRead / 1024}KB, bytesToRead=${if (bytesToRead == C.LENGTH_UNSET.toLong()) "UNSET" else "${bytesToRead / 1024}KB"}")
        }
        
        return read
    }

    override fun open(dataSpec: DataSpec): Long {
        Logger.d("CustomHttpDataSource.open: URL=${dataSpec.uri}, position=${dataSpec.position}, length=${dataSpec.length}")
        this.dataSpec = dataSpec
        bytesRead = 0
        bytesToRead = 0
        transferInitializing(dataSpec)

        val httpURLConnection: HttpURLConnection
        val responseMessage: String
        try {
            connection = makeConnection(dataSpec)
            httpURLConnection = connection!!
            responseCode = httpURLConnection.responseCode
            responseMessage = httpURLConnection.responseMessage
        } catch (e: IOException) {
            closeConnectionQuietly()
            throw HttpDataSourceException.createForIOException(
                e,
                dataSpec,
                HttpDataSourceException.TYPE_OPEN
            )
        }

        if (responseCode < 200 || responseCode > 299) {
            val headers = httpURLConnection.headerFields
            if (responseCode == 416) {
                val documentSize =
                    HttpUtil.getDocumentSize(httpURLConnection.getHeaderField(HttpHeaders.CONTENT_RANGE))
                if (dataSpec.position == documentSize) {
                    opened = true
                    transferStarted(dataSpec)
                    return if (dataSpec.length != C.LENGTH_UNSET.toLong()) dataSpec.length else 0
                }
            }
            val errorStream = httpURLConnection.errorStream
            val errorResponseBody = try {
                errorStream?.readBytes() ?: ByteArray(0)
            } catch (e: IOException) {
                ByteArray(0)
            }
            closeConnectionQuietly()
            val cause =
                if (responseCode == 416) DataSourceException(PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE) else null
            throw HttpDataSource.InvalidResponseCodeException(
                responseCode,
                responseMessage,
                cause,
                headers,
                dataSpec,
                errorResponseBody
            )
        }

        val contentType = httpURLConnection.contentType
        if (contentTypePredicate != null && contentTypePredicate.invoke(contentType)) {
            closeConnectionQuietly()
            throw HttpDataSource.InvalidContentTypeException(contentType, dataSpec)
        }

        val bytesToSkip =
            if (responseCode == 200 && dataSpec.position != 0L) dataSpec.position else 0L

        val isCompressed = isCompressed(httpURLConnection)
        val contentLength = HttpUtil.getContentLength(
            httpURLConnection.getHeaderField(HttpHeaders.CONTENT_LENGTH),
            httpURLConnection.getHeaderField(HttpHeaders.CONTENT_RANGE)
        )
        
        Logger.d("CustomHttpDataSource: Response code=$responseCode, contentLength=$contentLength, isCompressed=$isCompressed, bytesToSkip=$bytesToSkip")
        
        bytesToRead = if (!isCompressed) {
            if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
                dataSpec.length
            } else {
                if (contentLength != C.LENGTH_UNSET.toLong()) contentLength - bytesToSkip else C.LENGTH_UNSET.toLong()
            }
        } else {
            dataSpec.length
        }

        try {
            inputStream = httpURLConnection.inputStream.let {
                if (isCompressed) GZIPInputStream(it) else it
            }
        } catch (e: IOException) {
            closeConnectionQuietly()
            throw HttpDataSourceException(
                e,
                dataSpec,
                PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
                HttpDataSourceException.TYPE_OPEN
            )
        }

        opened = true
        transferStarted(dataSpec)

        try {
            skipFully(bytesToSkip, dataSpec)
        } catch (e: IOException) {
            closeConnectionQuietly()
            if (e is HttpDataSourceException) throw e
            throw HttpDataSourceException(
                e,
                dataSpec,
                PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
                HttpDataSourceException.TYPE_OPEN
            )
        }

        Logger.d("CustomHttpDataSource.open completed: bytesToRead=$bytesToRead")
        return bytesToRead
    }

    private fun skipFully(bytesToSkip: Long, dataSpec: DataSpec) {
        if (bytesToSkip == 0L) {
            return
        }
        Logger.d("CustomHttpDataSource.skipFully: Skipping $bytesToSkip bytes")
        val skipBuffer = ByteArray(4096)
        var remaining = bytesToSkip
        var totalSkipped = 0L
        while (remaining > 0) {
            val readLength = minOf(remaining, skipBuffer.size.toLong()).toInt()
            val read = checkNotNull(inputStream).read(skipBuffer, 0, readLength)
            if (Thread.currentThread().isInterrupted) {
                throw HttpDataSourceException(
                    InterruptedIOException(),
                    dataSpec,
                    PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
                    HttpDataSourceException.TYPE_OPEN
                )
            }
            if (read == -1) {
                throw HttpDataSourceException(
                    dataSpec,
                    PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE,
                    HttpDataSourceException.TYPE_OPEN
                )
            }
            remaining -= read.toLong()
            totalSkipped += read.toLong()
            bytesTransferred(read)
        }
        Logger.d("CustomHttpDataSource.skipFully: Completed skipping $totalSkipped bytes")
    }


    private fun makeConnection(dataSpecToUse: DataSpec): HttpURLConnection {
        var url = URL(dataSpecToUse.uri.toString())
        var httpMethod = dataSpecToUse.httpMethod
        var httpBody = dataSpecToUse.httpBody
        val position = dataSpecToUse.position
        val length = dataSpecToUse.length
        val allowGzip = dataSpecToUse.isFlagSet(DataSpec.FLAG_ALLOW_GZIP)

        if (!allowCrossProtocolRedirects && !keepPostFor302Redirects) {
            // HttpURLConnection disallows cross-protocol redirects, but otherwise performs
            // redirection automatically. This is the behavior we want, so use it.
            return makeConnection(
                url,
                httpMethod,
                httpBody,
                position,
                length,
                allowGzip,
                true,
                dataSpecToUse.httpRequestHeaders
            )
        }

        // We need to handle redirects ourselves to allow cross-protocol redirects or to keep the
        // POST request method for 302.
        var redirectCount = 0
        while (redirectCount++ <= MAX_REDIRECTS) {
            val httpURLConnection = makeConnection(
                url,
                httpMethod,
                httpBody,
                position,
                length,
                allowGzip,
                false,
                dataSpecToUse.httpRequestHeaders
            )
            val httpURLConnectionResponseCode = httpURLConnection.responseCode
            val location = httpURLConnection.getHeaderField("Location")
            if ((httpMethod == DataSpec.HTTP_METHOD_GET || httpMethod == DataSpec.HTTP_METHOD_HEAD) &&
                (httpURLConnectionResponseCode == HttpURLConnection.HTTP_MULT_CHOICE ||
                        httpURLConnectionResponseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        httpURLConnectionResponseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        httpURLConnectionResponseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                        httpURLConnectionResponseCode == HTTP_STATUS_TEMPORARY_REDIRECT ||
                        httpURLConnectionResponseCode == HTTP_STATUS_PERMANENT_REDIRECT)
            ) {
                httpURLConnection.disconnect()
                url = handleRedirect(url, location, dataSpecToUse)
            } else if (httpMethod == DataSpec.HTTP_METHOD_POST &&
                (httpURLConnectionResponseCode == HttpURLConnection.HTTP_MULT_CHOICE ||
                        httpURLConnectionResponseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                        httpURLConnectionResponseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                        httpURLConnectionResponseCode == HttpURLConnection.HTTP_SEE_OTHER)
            ) {
                httpURLConnection.disconnect()
                val shouldKeepPost = keepPostFor302Redirects &&
                        httpURLConnectionResponseCode == HttpURLConnection.HTTP_MOVED_TEMP
                if (!shouldKeepPost) {
                    // POST request follows the redirect and is transformed into a GET request.
                    httpMethod = DataSpec.HTTP_METHOD_GET
                    httpBody = null
                }
                url = handleRedirect(url, location, dataSpecToUse)
            } else {
                return httpURLConnection
            }
        }

        // If we get here we've been redirected more times than are permitted.
        throw HttpDataSourceException(
            NoRouteToHostException("Too many redirects: $redirectCount"),
            dataSpecToUse,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            HttpDataSourceException.TYPE_OPEN
        )
    }

    @Throws(IOException::class)
    private fun makeConnection(
        url: URL,
        httpMethod: Int,
        httpBody: ByteArray?,
        position: Long,
        length: Long,
        allowGzip: Boolean,
        followRedirects: Boolean,
        requestParameters: Map<String, String>
    ): HttpURLConnection {
        var requestUrl = url.toString()

        val isVideoPlaybackUrl = url.path.startsWith("/videoplayback")
        Logger.d("CustomHttpDataSource.makeConnection: Original URL=$requestUrl, isVideoPlayback=$isVideoPlaybackUrl")
        
        if (isVideoPlaybackUrl && rnParameterEnabled && !requestUrl.contains(RN_PARAMETER)) {
            val rnValue = requestNumber
            requestUrl += RN_PARAMETER + rnValue
            requestNumber++
            Logger.d("CustomHttpDataSource: Added RN parameter=$rnValue")
        }

        if (rangeParameterEnabled && isVideoPlaybackUrl) {
            val rangeParameterBuilt = buildRangeParameter(position, length)
            if (rangeParameterBuilt != null) {
                requestUrl += rangeParameterBuilt
                Logger.d("CustomHttpDataSource: Added range parameter=$rangeParameterBuilt")
            }
        }

        val httpURLConnection = openConnection(URL(requestUrl))
        httpURLConnection.connectTimeout = connectTimeoutMs
        httpURLConnection.readTimeout = readTimeoutMs

        val requestHeaders = mutableMapOf<String, String>()
        defaultRequestProperties?.getSnapshot()?.let { requestHeaders.putAll(it) }
        requestHeaders.putAll(requestProperties.getSnapshot())
        requestHeaders.putAll(requestParameters)

        for ((key, value) in requestHeaders) {
            httpURLConnection.setRequestProperty(key, value)
        }

        if (!rangeParameterEnabled) {
            val rangeHeader = buildRangeRequestHeader(position, length)
            if (rangeHeader != null) {
                httpURLConnection.setRequestProperty(HttpHeaders.RANGE, rangeHeader)
            }
        }


        httpURLConnection.setRequestProperty(HttpHeaders.TE, "trailers")


        httpURLConnection.setRequestProperty(HttpHeaders.USER_AGENT, getAndroidUserAgent(null))


        httpURLConnection.setRequestProperty(
            HttpHeaders.ACCEPT_ENCODING,
            if (allowGzip) "gzip" else "identity"
        )
        httpURLConnection.instanceFollowRedirects = followRedirects

        httpURLConnection.requestMethod = "POST"
        httpURLConnection.doOutput = true
        httpURLConnection.setFixedLengthStreamingMode(POST_BODY.size)
        
        Logger.d("CustomHttpDataSource: Making POST request to $requestUrl")
        httpURLConnection.connect()

        val os = httpURLConnection.outputStream
        os.write(POST_BODY)
        os.close()

        return httpURLConnection
    }

    private fun getAndroidUserAgent(localization: Localization?): String {
        return ("com.google.android.youtube/" + ANDROID_CLIENT_VERSION
                + " (Linux; U; Android 15; "
                + (localization ?: Localization.DEFAULT).countryCode
                + ") gzip")
    }


    private fun buildRangeParameter(position: Long, length: Long): String? {
        if (position == 0L && length == C.LENGTH_UNSET.toLong()) {
            return null
        }

        val rangeParameter = StringBuilder()
        rangeParameter.append("&range=")
        rangeParameter.append(position)
        rangeParameter.append("-")
        if (length != C.LENGTH_UNSET.toLong()) {
            rangeParameter.append(position + length - 1)
        }
        return rangeParameter.toString()
    }

    private fun openConnection(url: URL): HttpURLConnection {
        return url.openConnection() as HttpURLConnection
    }


    /**
     * Closes the current connection quietly, if there is one.
     */
    private fun closeConnectionQuietly() {
        connection?.let {
            try {
                it.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error while disconnecting", e)
            }
            connection = null
        }
    }


    private fun isCompressed(connection: HttpURLConnection): Boolean {
        val contentEncoding = connection.getHeaderField("Content-Encoding")
        return "gzip".equals(contentEncoding, ignoreCase = true)
    }

    private fun handleRedirect(
        originalUrl: URL,
        location: String?,
        dataSpecToHandleRedirect: DataSpec
    ): URL {
        if (location == null) {
            throw HttpDataSourceException(
                "Null location redirect",
                dataSpecToHandleRedirect,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                HttpDataSourceException.TYPE_OPEN
            )
        }

        val url = try {
            URL(originalUrl, location)
        } catch (e: MalformedURLException) {
            throw HttpDataSourceException(
                e,
                dataSpecToHandleRedirect,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                HttpDataSourceException.TYPE_OPEN
            )
        }

        val protocol = url.protocol
        if (protocol != "https" && protocol != "http") {
            throw HttpDataSourceException(
                "Unsupported protocol redirect: $protocol",
                dataSpecToHandleRedirect,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                HttpDataSourceException.TYPE_OPEN
            )
        }

        if (!allowCrossProtocolRedirects && protocol != originalUrl.protocol) {
            throw HttpDataSourceException(
                "Disallowed cross-protocol redirect (${originalUrl.protocol} to $protocol)",
                dataSpecToHandleRedirect,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                HttpDataSourceException.TYPE_OPEN
            )
        }

        return url
    }


    override fun getUri(): Uri? {
        return if (connection == null) null else connection!!.url.toString().toUri()
    }

    override fun getResponseHeaders(): MutableMap<String, MutableList<String>> {
        val conn = connection ?: return mutableMapOf()

        return conn.headerFields
            .filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { it.value.toMutableList() }
            .toMutableMap()
    }

    override fun close() {
        Logger.d("CustomHttpDataSource.close: bytesRead=${bytesRead / 1024}KB, bytesToRead=${if (bytesToRead == C.LENGTH_UNSET.toLong()) "UNSET" else "${bytesToRead / 1024}KB"}")
        try {
            val connectionInputStream = inputStream
            if (connectionInputStream != null) {
                val bytesRemaining = if (bytesToRead == C.LENGTH_UNSET.toLong()) {
                    C.LENGTH_UNSET.toLong()
                } else {
                    bytesToRead - bytesRead
                }
                maybeTerminateInputStream(connection, bytesRemaining)

                try {
                    connectionInputStream.close()
                } catch (e: IOException) {
                    throw HttpDataSourceException(
                        e,
                        checkNotNull(dataSpec),
                        PlaybackException.ERROR_CODE_IO_UNSPECIFIED,
                        HttpDataSourceException.TYPE_CLOSE
                    )
                }
            }
        } finally {
            inputStream = null
            closeConnectionQuietly()
            if (opened) {
                opened = false
                transferEnded()
            }
        }
    }

    private fun maybeTerminateInputStream(connection: HttpURLConnection?, bytesRemaining: Long) {
        if (connection == null || Util.SDK_INT < 19 || Util.SDK_INT > 20) {
            return
        }

        try {
            val inputStream = connection.inputStream
            if (bytesRemaining == C.LENGTH_UNSET.toLong()) {
                if (inputStream.read() == -1) {
                    return
                }
            } else if (bytesRemaining <= MAX_BYTES_TO_DRAIN) {
                return
            }

            val className = inputStream.javaClass.name
            if (className == "com.android.okhttp.internal.http.HttpTransport\$ChunkedInputStream" ||
                className == "com.android.okhttp.internal.http.HttpTransport\$FixedLengthInputStream"
            ) {

                val superclass = inputStream.javaClass.superclass
                val unexpectedEndOfInput = superclass?.getDeclaredMethod("unexpectedEndOfInput")
                unexpectedEndOfInput?.isAccessible = true
                unexpectedEndOfInput?.invoke(inputStream)
            }
        } catch (e: Exception) {

        }
    }


    override fun setRequestProperty(name: String, value: String) {
        requestProperties.set(name, value)
    }

    override fun clearRequestProperty(name: String) {
        requestProperties.remove(name)
    }

    override fun clearAllRequestProperties() {
        requestProperties.clear()
    }

    override fun getResponseCode(): Int {
        return if (connection == null || responseCode <= 0) -1 else responseCode
    }


}