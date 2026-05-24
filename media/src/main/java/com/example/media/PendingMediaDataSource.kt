package com.example.media

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import com.example.util.Logger
import java.io.IOException

@OptIn(UnstableApi::class)
class PendingMediaDataSource(
    upstreamFactory: DataSource.Factory
) : DataSource {

    private val upstream = upstreamFactory.createDataSource()

    override fun addTransferListener(transferListener: TransferListener) {
        upstream.addTransferListener(transferListener)
    }

    @Throws(IOException::class)
    override fun open(dataSpec: DataSpec): Long {
        val mediaId = PendingMediaItemResolver.mediaIdFrom(dataSpec.uri)
            ?: throw IOException("Invalid pending media URI: ${dataSpec.uri}")
        Logger.d("PendingMediaDataSource: waiting for mediaId=$mediaId")
        val resolvedUri = PendingMediaItemResolver.awaitResolvedUri(mediaId)
        Logger.d("PendingMediaDataSource: resolved mediaId=$mediaId")
        return upstream.open(dataSpec.withUri(resolvedUri))
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return upstream.read(buffer, offset, length)
    }

    override fun getUri(): Uri? {
        return upstream.uri
    }

    override fun getResponseHeaders(): Map<String, List<String>> {
        return upstream.responseHeaders
    }

    @Throws(IOException::class)
    override fun close() {
        upstream.close()
    }

    class Factory(
        private val upstreamFactory: DataSource.Factory
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource {
            return PendingMediaDataSource(upstreamFactory)
        }
    }
}
