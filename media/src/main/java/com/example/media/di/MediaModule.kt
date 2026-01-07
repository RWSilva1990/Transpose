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
import com.example.media.audio.HybridAudioSink
import com.example.media.audio.HybridRenderersFactory
import com.example.media.audio.PlaybackModeController
import com.example.media.audio.SuperpoweredBridge
import com.example.media.audio.SignalsmithBridgeImpl
import com.example.media.audio.SignalsmithAudioProcessor
import com.example.media.audio.ProcessorRenderersFactory
import com.example.audio.SignalsmithAudioEngine
import com.example.media.audio_effect.AudioEffectHandlerImpl
import dagger.Lazy
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
    fun provideCustomMediaSourceFactory(@ApplicationContext context: Context): CustomMediaSourceFactory{

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
        hybridRenderersFactory: HybridRenderersFactory  // 추가
    ): ExoPlayer = ExoPlayer.Builder(context, hybridRenderersFactory)  // 수정
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
    fun provideAudioEffectHandler(
        player: Player,
        playbackModeController: Lazy<PlaybackModeController>,
        signalsmithProcessor: Lazy<SignalsmithAudioProcessor>
    ): AudioEffectHandlerImpl =
        AudioEffectHandlerImpl(player as ExoPlayer, playbackModeController, signalsmithProcessor)

    @Provides
    @Singleton
    fun provideSuperpoweredBridge(engine: SignalsmithAudioEngine): SuperpoweredBridge {
        return SignalsmithBridgeImpl(engine)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideHybridAudioSink(
        @ApplicationContext context: Context,
        superpoweredBridge: SuperpoweredBridge
    ): HybridAudioSink {
        return HybridAudioSink(context, superpoweredBridge)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideHybridRenderersFactory(
        @ApplicationContext context: Context,
        hybridAudioSink: HybridAudioSink
    ): HybridRenderersFactory {
        return HybridRenderersFactory(context, hybridAudioSink)
    }

    @Provides
    @Singleton
    fun provideSignalsmithAudioProcessor(): SignalsmithAudioProcessor {
        return SignalsmithAudioProcessor()
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideProcessorRenderersFactory(
        @ApplicationContext context: Context,
        signalsmithProcessor: SignalsmithAudioProcessor
    ): ProcessorRenderersFactory {
        return ProcessorRenderersFactory(context, signalsmithProcessor)
    }
}