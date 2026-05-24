package com.example.media

import android.net.Uri
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object PendingMediaItemResolver {
    private const val SCHEME = "transpose-pending"
    private const val AUTHORITY = "media"
    private const val TIMEOUT_MS = 30_000L

    private val pendingItems = ConcurrentHashMap<String, PendingItem>()

    fun createPendingUri(mediaId: String): Uri {
        prepare(mediaId)
        return Uri.Builder()
            .scheme(SCHEME)
            .authority(AUTHORITY)
            .appendPath(mediaId)
            .build()
    }

    fun isPendingUri(uri: Uri?): Boolean {
        return uri?.scheme == SCHEME && uri.authority == AUTHORITY
    }

    fun mediaIdFrom(uri: Uri): String? {
        if (!isPendingUri(uri)) return null
        return uri.pathSegments.firstOrNull()
    }

    fun resolve(mediaId: String, streamUrl: String) {
        val pendingItem = pendingItems.computeIfAbsent(mediaId) { PendingItem() }
        pendingItem.resolve(streamUrl)
    }

    fun fail(mediaId: String, message: String) {
        val pendingItem = pendingItems.computeIfAbsent(mediaId) { PendingItem() }
        pendingItem.fail(IOException(message))
    }

    fun awaitResolvedUri(mediaId: String): Uri {
        val pendingItem = pendingItems.computeIfAbsent(mediaId) { PendingItem() }
        return pendingItem.await(TIMEOUT_MS)
    }

    fun clearAll() {
        pendingItems.values.forEach { it.cancel() }
        pendingItems.clear()
    }

    private fun prepare(mediaId: String) {
        pendingItems.remove(mediaId)?.cancel()
        pendingItems[mediaId] = PendingItem()
    }

    private class PendingItem {
        private val latch = CountDownLatch(1)

        @Volatile private var streamUrl: String? = null
        @Volatile private var error: IOException? = null

        fun resolve(streamUrl: String) {
            this.streamUrl = streamUrl
            latch.countDown()
        }

        fun cancel() {
            error = IOException("Pending media item was cancelled")
            latch.countDown()
        }

        fun fail(error: IOException) {
            this.error = error
            latch.countDown()
        }

        fun await(timeoutMs: Long): Uri {
            val completed = try {
                latch.await(timeoutMs, TimeUnit.MILLISECONDS)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("Interrupted while waiting for media URL", e)
            }

            if (!completed) {
                throw IOException("Timed out waiting for media URL")
            }

            error?.let { throw it }

            val resolvedUrl = streamUrl
                ?: throw IOException("Media URL was not resolved")
            return Uri.parse(resolvedUrl)
        }
    }
}
