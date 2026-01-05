package com.example.audio.di

import android.content.Context
import com.example.audio.SuperpoweredAudioEngine
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
    fun provideSuperpoweredAudioEngine(
        @ApplicationContext context: Context
    ): SuperpoweredAudioEngine {
        return SuperpoweredAudioEngine(context)
    }
}