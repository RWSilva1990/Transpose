package com.example.media

import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import com.example.util.Logger

@OptIn(UnstableApi::class)
class CustomMediaSourceFactory(
    private val context: Context
) : MediaSource.Factory {

    private val dataSourceFactory = DefaultDataSource.Factory(context)

    private var drmSessionManagerProvider: DrmSessionManagerProvider? = null
    private var loadErrorHandlingPolicy: LoadErrorHandlingPolicy? = null

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
        val videoUri = extras?.getString("videoUrl")
        val audioUri = extras?.getString("audioUrl")
        Logger.d("CustomMediaSourceFactory.createMediaSource: $videoUri $audioUri")

        // ProgressiveMediaSource.Factory를 하나 만들고, DRM 등 옵션을 적용
        val factory = ProgressiveMediaSource.Factory(dataSourceFactory).apply {
            drmSessionManagerProvider?.let { setDrmSessionManagerProvider(it) }
            loadErrorHandlingPolicy?.let { setLoadErrorHandlingPolicy(it) }
        }

        return if (!videoUri.isNullOrEmpty() && !audioUri.isNullOrEmpty()) {
            // (1) 비디오+오디오 두 개 스트림이 모두 있을 때 => MergingMediaSource
            val videoItem = MediaItem.fromUri(videoUri)
            val audioItem = MediaItem.fromUri(audioUri)

            val videoSource = factory.createMediaSource(videoItem)
            val audioSource = factory.createMediaSource(audioItem)

            // adjustPeriodTimeOffsets = false 로 설정해 지연/오프셋 계산 최소화
            MergingMediaSource(
                /* adjustPeriodTimeOffsets = */ false,
                videoSource,
                audioSource
            )
        } else {
            // (2) 하나만 있거나 extras가 없으면 => 그냥 단일 MediaSource
            factory.createMediaSource(mediaItem)
        }
    }
}
