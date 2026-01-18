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
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.dash.DefaultDashChunkSource
import androidx.media3.exoplayer.dash.manifest.DashManifest
import androidx.media3.exoplayer.dash.manifest.DashManifestParser
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import com.example.domain.model.preferences.VideoQuality
import com.example.util.Logger
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

@OptIn(UnstableApi::class)
class CustomMediaSourceFactory(
    private val context: Context,
    private val dataSourceFactory: CustomHttpDataSource.Factory
) : MediaSource.Factory {

    private val CACHE_FOLDER_NAME: String = "exoplayer"

    private var cache: SimpleCache? = null
    private var drmSessionManagerProvider: DrmSessionManagerProvider? = null
    private var loadErrorHandlingPolicy: LoadErrorHandlingPolicy? = null
    private var bandwidthMeter: DefaultBandwidthMeter? = null
    private var cacheFactory:
            CacheDataSource.Factory? = null
    private var defaultDashChunkSourceFactory: DefaultDashChunkSource.Factory? = null
    private var youtubeDashMediaSource: DashMediaSource.Factory? = null

    init {
        instantiateCacheIfNeeded(context)

        bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()

        cacheFactory = CacheDataSource.Factory()
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setCache(cache!!)

        defaultDashChunkSourceFactory = DefaultDashChunkSource.Factory(cacheFactory!!)

//        youtubeDashMediaSource = DashMediaSource.Factory(
//            defaultDashChunkSourceFactory!!,
//            cacheFactory
//        )
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
        // 30초 무음 파일인지 확인
        val uri = mediaItem.localConfiguration?.uri?.toString()
        if (uri?.contains("30-seconds-of-silence") == true || uri?.startsWith("asset:///") == true) {
            return ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
                .createMediaSource(mediaItem)
        }

        // extras에 "videoUrl", "audioUrl"가 들어있다면 각각 사용
        val extras: Bundle? = mediaItem.mediaMetadata.extras

        val videoQualityName = extras?.getString("videoQuality")
        val videoQuality = videoQualityName?.let {
            try {
                VideoQuality.valueOf(it)
            } catch (e: IllegalArgumentException) {
                VideoQuality.AUTO
            }
        } ?: VideoQuality.AUTO

        if (videoQuality.isAuto) {
            Logger.d("CustomMediaSourceFactory: Using Progressive source (AUTO quality)")
            return ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
                .createMediaSource(mediaItem)
        }

        Logger.d("CustomMediaSourceFactory: Using DASH source (${videoQuality.displayName})")

        val videoManifestString = extras?.getString("videoManifestString")
        val audioManifestString = extras?.getString("audioManifestString")

        val videoUri = extras?.getString("videoUrl")
        val audioUri = extras?.getString("audioUrl")
        Logger.d("CustomMediaSourceFactory.createMediaSource: videoUri: $videoUri, audioUri: $audioUri")


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

        Logger.d("CustomMediaSourceFactory videoMediaItem: ${videoMediaItem.mediaId}, audioMediaItem: ${audioMediaItem.mediaId}")

        val videoMediaSource = try {
            Logger.d("Creating video media source with:")
            Logger.d("  - videoManifestString: ${videoManifestString?.take(200)}...")
            Logger.d("  - videoUri: ${videoMediaItem.localConfiguration?.uri}")

            val manifest = createDashManifest(videoManifestString, null)
            Logger.d("Video manifest created successfully")
            val source = DashMediaSource.Factory(
                defaultDashChunkSourceFactory!!,
                cacheFactory
            ).createMediaSource(manifest, videoMediaItem)
//             youtubeDashMediaSource?.createMediaSource(manifest, videoMediaItem)
            Logger.d("Video media source created: $source")
            source
        } catch (e: Exception) {
            Logger.e(
                "CustomMediaSourceFactory.createMediaSource: Failed to create video media source",
                e
            )
            Logger.e("Exception type: ${e.javaClass.simpleName}")
            Logger.e("Exception message: ${e.message}")
            Logger.e("Stack trace:", e)
            throw RuntimeException("Failed to create video media source", e)
        }
        val audioMediaSource = if (!audioManifestString.isNullOrEmpty()) {
            try {
                Logger.d("Creating audio media source with:")
                Logger.d("  - audioManifestString: ${audioManifestString.take(200)}...")
                Logger.d("  - audioUri: ${audioMediaItem.localConfiguration?.uri}")

                val manifest = createDashManifest(audioManifestString, null)
                Logger.d("Audio manifest created successfully")

                val source = DashMediaSource.Factory(
                    defaultDashChunkSourceFactory!!,
                    cacheFactory
                ).createMediaSource(manifest, audioMediaItem)

                Logger.d("Audio media source created: $source")
                source
            } catch (e: Exception) {
                Logger.e(
                    "CustomMediaSourceFactory.createMediaSource: Failed to create audio media source",
                    e
                )
                Logger.e("Exception type: ${e.javaClass.simpleName}")
                Logger.e("Exception message: ${e.message}")
                Logger.e("Stack trace:", e)
                throw RuntimeException("Failed to create audio media source", e)
            }
        } else {
            Logger.d("Audio manifest string is null or empty, skipping audio source")
            null
        }

        Logger.d("CustomMediaSourceFactory videoMediaSource: $videoMediaSource, audioMediaSource: $audioMediaSource")


        if (videoMediaSource != null) {
            mediaSourceList.add(videoMediaSource)
        }
        if (audioMediaSource != null) {
            mediaSourceList.add(audioMediaSource)
        }

        if (mediaSourceList.isEmpty()) {
            throw RuntimeException("No media sources could be created")
        }

        val mergingMediaSource = if (mediaSourceList.size > 1) {
            MergingMediaSource(true, *mediaSourceList.toTypedArray())
        } else {
            mediaSourceList.first()
        }

        Logger.d("CustomMediaSourceFactory mergingMediaSource: $mergingMediaSource")
        return mergingMediaSource
    }

    @OptIn(UnstableApi::class)
    private fun createDashManifest(
        manifestContent: String?,
        manifestUrl: String?
    ): DashManifest {
        try {
            // Null 체크 및 검증
            if (manifestContent.isNullOrEmpty()) {
                throw IllegalArgumentException("Manifest content is null or empty")
            }

            Logger.d("Parsing DASH manifest:")
            Logger.d("  - Content length: ${manifestContent.length}")
            Logger.d("  - URL: $manifestUrl")

            val uri = manifestUrlToUri(manifestUrl)
            Logger.d("  - Parsed URI: $uri")

            val inputStream =
                ByteArrayInputStream(manifestContent.toByteArray(StandardCharsets.UTF_8))
            val manifest = DashManifestParser().parse(uri, inputStream)

            Logger.d("Manifest parsed successfully:")
            Logger.d("  - Duration: ${manifest.durationMs}ms")
            Logger.d("  - Period count: ${manifest.periodCount}")

            return manifest
        } catch (e: Exception) {
            Logger.e("CustomMediaSourceFactory.createDashManifest: Failed to parse manifest", e)
            Logger.e("Manifest content preview: ${manifestContent?.take(500)}")
            throw RuntimeException("Failed to parse DASH manifest: ${e.message}", e)
        }
    }

    private fun manifestUrlToUri(manifestUrl: String?): Uri {
        return manifestUrl?.toUri() ?: "".toUri()
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
