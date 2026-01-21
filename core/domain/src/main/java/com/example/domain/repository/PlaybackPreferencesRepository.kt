package com.example.domain.repository

import com.example.domain.model.preferences.AudioQuality
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.model.preferences.VideoQuality
import kotlinx.coroutines.flow.StateFlow

interface PlaybackPreferencesRepository {
    suspend fun setRepeatMode(mode: RepeatMode)
    suspend fun setShuffleMode(enabled: Boolean)
    suspend fun setVideoQuality(quality: VideoQuality)
    suspend fun setAudioQuality(quality: AudioQuality)
    val videoQuality: StateFlow<VideoQuality>
    val audioQuality: StateFlow<AudioQuality>
    val repeatMode: StateFlow<RepeatMode>
    val shuffleMode: StateFlow<Boolean>
}
