package com.example.data.repository

import com.example.data.local.preferences.PlaybackPreferences
import com.example.domain.model.preferences.AudioQuality
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.model.preferences.VideoQuality
import com.example.domain.repository.PlaybackPreferencesRepository
import kotlinx.coroutines.flow.StateFlow

class PlaybackPreferencesRepositoryImpl(
    private val playbackPreferences: PlaybackPreferences
) : PlaybackPreferencesRepository {

    override suspend fun setRepeatMode(mode: RepeatMode) {
        playbackPreferences.setRepeatMode(mode)
    }

    override suspend fun setShuffleMode(enabled: Boolean) {
        playbackPreferences.setShuffleMode(enabled)
    }

    override suspend fun setVideoQuality(quality: VideoQuality) {
        playbackPreferences.setVideoQuality(quality)
    }

    override suspend fun setAudioQuality(quality: AudioQuality) {
        playbackPreferences.setAudioQuality(quality)
    }

    override val videoQuality: StateFlow<VideoQuality> = playbackPreferences.videoQuality
    override val audioQuality: StateFlow<AudioQuality> = playbackPreferences.audioQuality
    override val repeatMode: StateFlow<RepeatMode> = playbackPreferences.repeatMode
    override val shuffleMode: StateFlow<Boolean> = playbackPreferences.shuffleMode
}