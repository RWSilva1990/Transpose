package com.example.media.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.media.CustomHttpDataSource
import com.example.media.CustomMediaSourceFactory
import com.example.media.audio_effect.AudioEffectHandlerImpl
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

        val defaultDataSourceFactory = DefaultDataSource.Factory(context, custom)

        return CustomMediaSourceFactory(context, defaultDataSourceFactory)

    }


    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
        customMediaSourceFactory: CustomMediaSourceFactory
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setHandleAudioBecomingNoisy(true)
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    5_000,   // minBufferMs: 5초로 줄임 (빠른 시작)
                    30_000,  // maxBufferMs: 30초로 줄임 (메모리 절약)
                    1_000,   // bufferForPlaybackMs: 1초로 줄임 (빠른 재생 시작)
                    2_000    // bufferForPlaybackAfterRebufferMs: 2초로 줄임
                )
                .setTargetBufferBytes(5 * 1024 * 1024) // 5MB로 줄임
                .setPrioritizeTimeOverSizeThresholds(false) // 크기 기반 우선
                .setBackBuffer(10_000, true) // 10초 백버퍼 설정
                .build()
        )
        .setTrackSelector(DefaultTrackSelector(context))
        .build()

    @Provides
    @Singleton
    fun providePlayer(exoPlayer: ExoPlayer): Player = exoPlayer


    @Provides
    @Singleton
    fun provideAudioEffectHandler(player: Player): AudioEffectHandlerImpl =
        AudioEffectHandlerImpl(player as ExoPlayer)

}