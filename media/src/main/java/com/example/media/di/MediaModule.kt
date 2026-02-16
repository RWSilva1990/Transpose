package com.example.media.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.media.CustomHttpDataSource
import com.example.media.CustomMediaSourceFactory
import com.example.media.audio.SignalsmithAudioProcessor
import com.example.media.audio.ProcessorRenderersFactory
import com.example.media.audio.VocalRemovalProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MediaModule {

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideCustomMediaSourceFactory(@ApplicationContext context: Context): CustomMediaSourceFactory {
        val custom = CustomHttpDataSource.Factory()
            .setRangeParameterEnabled(true)
            .setRnParameterEnabled(true)
            .setKeepPostFor302Redirects(true)
            .setAllowCrossProtocolRedirects(true)

        return CustomMediaSourceFactory(context, custom)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
        customMediaSourceFactory: CustomMediaSourceFactory,
        processorRenderersFactory: ProcessorRenderersFactory
    ): ExoPlayer = ExoPlayer.Builder(context, processorRenderersFactory)
        .setSeekBackIncrementMs(10_000)
        .setSeekForwardIncrementMs(10_000)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    5_000,
                    30_000,
                    1_000,
                    2_000
                )
                .setTargetBufferBytes(5 * 1024 * 1024)
                .setPrioritizeTimeOverSizeThresholds(false)
                .setBackBuffer(10_000, true)
                .build()
        )
        .setMediaSourceFactory(customMediaSourceFactory)
        .setTrackSelector(DefaultTrackSelector(context))
        .build()

    @Provides
    @Singleton
    fun providePlayer(exoPlayer: ExoPlayer): Player = exoPlayer

    @Provides
    @Singleton
    fun provideSignalsmithAudioProcessor(): SignalsmithAudioProcessor {
        return SignalsmithAudioProcessor()
    }

    @Provides
    @Singleton
    fun provideVocalRemovalProcessor(@ApplicationContext context: Context): VocalRemovalProcessor {
        return VocalRemovalProcessor(context)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideProcessorRenderersFactory(
        @ApplicationContext context: Context,
        vocalRemovalProcessor: VocalRemovalProcessor,
        signalsmithProcessor: SignalsmithAudioProcessor
    ): ProcessorRenderersFactory {
        return ProcessorRenderersFactory(context, vocalRemovalProcessor, signalsmithProcessor)
    }
}
