package com.example.data.repository

import com.example.data.local.preferences.PlaybackPreferences
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.repository.PlaybackPreferencesRepository
import kotlinx.coroutines.flow.Flow

class PlaybackPreferencesRepositoryImpl(
    private val playbackPreferences: PlaybackPreferences
) : PlaybackPreferencesRepository {
    override suspend fun setRepeatMode(mode: RepeatMode) {
        playbackPreferences.setRepeatMode(mode)
    }

    override suspend fun setShuffleMode(enabled: Boolean) {
        playbackPreferences.setShuffleMode(enabled)
    }

    override val repeatMode: Flow<RepeatMode> = playbackPreferences.repeatMode
    override val shuffleMode: Flow<Boolean> = playbackPreferences.shuffleMode
}