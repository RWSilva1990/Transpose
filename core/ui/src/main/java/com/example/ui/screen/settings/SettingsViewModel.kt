package com.example.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.preferences.VideoQuality
import com.example.domain.repository.PlaybackPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val playbackPreferencesRepository: PlaybackPreferencesRepository
) : ViewModel() {

    val videoQuality: StateFlow<VideoQuality> = playbackPreferencesRepository.videoQuality

    fun setVideoQuality(quality: VideoQuality) {
        viewModelScope.launch {
            playbackPreferencesRepository.setVideoQuality(quality)
        }
    }
}