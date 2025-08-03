package com.example.domain.repository

import com.example.domain.model.preferences.RepeatMode
import kotlinx.coroutines.flow.StateFlow

interface PlaybackPreferencesRepository {
    suspend fun setRepeatMode(mode: RepeatMode)
    suspend fun setShuffleMode(enabled: Boolean)
    suspend fun setVideoQuality(quality: String)
    suspend fun setAudioQuality(quality: String)
    val videoQuality: StateFlow<String>
    val audioQuality: StateFlow<String>
    val repeatMode: StateFlow<RepeatMode>
    val shuffleMode: StateFlow<Boolean>
}
