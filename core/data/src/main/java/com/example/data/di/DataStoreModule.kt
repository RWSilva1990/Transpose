package com.example.data.di

import android.content.Context
import com.example.data.local.preferences.PlaybackPreferences
import com.example.data.local.preferences.playbackPreferencesDataStore
import com.example.data.repository.PlaybackPreferencesRepositoryImpl
import com.example.domain.repository.PlaybackPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// core:data 모듈
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providePlaybackPreferences(@ApplicationContext context: Context): PlaybackPreferences {
        return PlaybackPreferences(context.playbackPreferencesDataStore)
    }

    @Provides
    @Singleton
    fun providePlaybackPreferencesRepository(
        playbackPreferences: PlaybackPreferences
    ): PlaybackPreferencesRepository {
        return PlaybackPreferencesRepositoryImpl(playbackPreferences)
    }
}