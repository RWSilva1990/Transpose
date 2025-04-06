package com.example.domain.repository

import com.example.domain.model.preferences.RepeatMode
import kotlinx.coroutines.flow.Flow

interface PlaybackPreferencesRepository {
    suspend fun setRepeatMode(mode: RepeatMode)
    suspend fun setShuffleMode(enabled: Boolean)
    val repeatMode: Flow<RepeatMode>
    val shuffleMode: Flow<Boolean>
}
