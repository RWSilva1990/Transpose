package com.example.audio.di

import android.content.Context
import com.example.audio.SignalsmithAudioEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideSignalsmithAudioEngine(
        @ApplicationContext context: Context
    ): SignalsmithAudioEngine {
        return SignalsmithAudioEngine(context)
    }
}
