package com.example.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.model.youtube.video.Video
import com.example.domain.repository.PlaybackPreferencesRepository
import com.example.media.manager.AudioEffectsManager
import com.example.media.manager.MediaPlaybackManager
import com.example.media.state_holder.NowPlayingStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainPlayerViewModel @Inject constructor(
    private val mediaPlaybackManager: MediaPlaybackManager,
    private val nowPlayingStateHolder: NowPlayingStateHolder,
    private val audioEffectsManager: AudioEffectsManager,
    private val playbackPreferencesRepository: PlaybackPreferencesRepository,
) : ViewModel() {


    val currentPlaylistItems = nowPlayingStateHolder.currentPlaylist
    val currentPlaylistIndex = nowPlayingStateHolder.currentPlaylistIndex
    val currentPlaylistInfo = nowPlayingStateHolder.currentPlaylistInfo

    val repeatMode = mediaPlaybackManager.getCurrentRepeatMode()
    val shuffleMode = mediaPlaybackManager.getCurrentShuffleMode()

    fun playPlaylist(playlist: List<Video>, playlistIndex: Int) {
        viewModelScope.launch {
            mediaPlaybackManager.playPlaylist(playlist, playlistIndex)
        }
    }

    fun stopPlayback() {
        mediaPlaybackManager.stopPlayback()
    }

    fun toggleRepeatMode() {
        viewModelScope.launch {
            val currentMode = repeatMode.value
            val newMode = when (currentMode) {
                Player.REPEAT_MODE_OFF -> RepeatMode.ONE
                Player.REPEAT_MODE_ONE -> RepeatMode.ALL
                Player.REPEAT_MODE_ALL -> RepeatMode.OFF
                else -> RepeatMode.OFF
            }

            playbackPreferencesRepository.setRepeatMode(newMode)

            mediaPlaybackManager.setRepeatMode(newMode)
        }
    }

    fun toggleShuffleMode() {
        viewModelScope.launch {
            val currentMode = shuffleMode
            val newMode = !currentMode

            playbackPreferencesRepository.setShuffleMode(newMode)

            mediaPlaybackManager.setShuffleMode(newMode)
        }
    }
}