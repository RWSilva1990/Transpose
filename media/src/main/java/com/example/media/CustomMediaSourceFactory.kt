package com.example.media

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.dash.DefaultDashChunkSource
import androidx.media3.exoplayer.dash.manifest.DashManifest
import androidx.media3.exoplayer.dash.manifest.DashManifestParser
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import com.example.util.Logger
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Objects

@OptIn(UnstableApi::class)
class CustomMediaSourceFactory(
    private val context: Context,
    private val dataSourceFactory: DefaultDataSource.Factory
) : MediaSource.Factory {

    val CACHE_FOLDER_NAME: String = "exoplayer"

    val USER_AGENT: String =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0"

    private var cache: SimpleCache? = null
    private var drmSessionManagerProvider: DrmSessionManagerProvider? = null
    private var loadErrorHandlingPolicy: LoadErrorHandlingPolicy? = null
    private var bandwidthMeter: DefaultBandwidthMeter? = null
    private var cacheFactory: CacheFactory? = null
    private var defaultDashChunkSourceFactory: DefaultDashChunkSource.Factory? = null
    private var dashMediaSource: DashMediaSource.Factory? = null

    init {
        instantiateCacheIfNeeded(context)

        bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()

        cacheFactory = CacheFactory(
            context,
            bandwidthMeter!!,
            cache!!,
            dataSourceFactory
        )
        defaultDashChunkSourceFactory = DefaultDashChunkSource.Factory(cacheFactory!!)
        dashMediaSource = DashMediaSource.Factory(
            defaultDashChunkSourceFactory!!,
            cacheFactory
        )

    }


    override fun setDrmSessionManagerProvider(
        drmSessionManagerProvider: DrmSessionManagerProvider
    ): MediaSource.Factory {
        this.drmSessionManagerProvider = drmSessionManagerProvider
        return this
    }

    override fun setLoadErrorHandlingPolicy(
        loadErrorHandlingPolicy: LoadErrorHandlingPolicy
    ): MediaSource.Factory {
        this.loadErrorHandlingPolicy = loadErrorHandlingPolicy
        return this
    }

    override fun getSupportedTypes(): IntArray {
        // 필요하면 TYPE_DASH, TYPE_HLS 등 추가
        return intArrayOf(C.TYPE_OTHER)
    }

    override fun createMediaSource(mediaItem: MediaItem): MediaSource {
        // extras에 "videoUrl", "audioUrl"가 들어있다면 각각 사용
        val extras: Bundle? = mediaItem.mediaMetadata.extras
        val videoManifestUrl = extras?.getString("manifestUrl")
        val videoManifestString = extras?.getString("manifestString")
        val audioManifestString = extras?.getString("audioManifestString")
        val audioManifestUrl = extras?.getString("audioManifestUrl")

        val videoUri = extras?.getString("videoUrl")
        val audioUri = extras?.getString("audioUrl")
        Logger.d("CustomMediaSourceFactory.createMediaSource: ${videoUri.isNullOrEmpty()} ${audioUri.isNullOrEmpty()}")

        val mediaSourceList = mutableListOf<MediaSource>()

        val videoMediaItem = MediaItem.Builder()
            .setUri(videoUri?.toUri())
            .setMediaId(mediaItem.mediaId)
            .setMediaMetadata(mediaItem.mediaMetadata)
            .build()

        val audioMediaItem = MediaItem.Builder()
            .setUri(audioUri?.toUri())
            .setMediaId(mediaItem.mediaId)
            .setMediaMetadata(mediaItem.mediaMetadata)
            .build()

        val videoMediaSource = dashMediaSource?.createMediaSource(
            createDashManifest(
                videoManifestString!!,
                videoManifestUrl!!
            ), videoMediaItem
        )
        val audioMediaSource = dashMediaSource?.createMediaSource(
            createDashManifest(
                audioManifestString!!,
                audioManifestUrl!!
            ), audioMediaItem
        )


        mediaSourceList.add(videoMediaSource!!)
        mediaSourceList.add(audioMediaSource!!)

        return MergingMediaSource(true, *mediaSourceList.toTypedArray())
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    private fun createDashManifest(
        manifestContent: String,
        manifestUrl: String
    ): DashManifest {
        return DashManifestParser().parse(
            manifestUrlToUri(manifestUrl),
            ByteArrayInputStream(manifestContent.toByteArray(StandardCharsets.UTF_8))
        )
    }

    private fun manifestUrlToUri(manifestUrl: String): Uri {
        return Objects.requireNonNullElse(manifestUrl, "").toUri()
    }

    private fun instantiateCacheIfNeeded(context: Context) {
        if (cache == null) {
            val cacheDir = File(
                context.externalCacheDir,
                CACHE_FOLDER_NAME
            )

            if (!cacheDir.exists() && !cacheDir.mkdir()) {
                Logger.d("CustomMediaSourceFactory Failed to create cache directory: ${cacheDir.absolutePath}")

            }

            val evictor =
                LeastRecentlyUsedCacheEvictor(64 * 1024 * 1024L)
            cache =
                SimpleCache(cacheDir, evictor, StandaloneDatabaseProvider(context))
        }
    } //endregion
}
