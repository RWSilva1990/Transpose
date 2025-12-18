package com.example.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.PlaybackPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val playbackPreferencesRepository: PlaybackPreferencesRepository
) : ViewModel() {

    val videoQuality: StateFlow<String> = playbackPreferencesRepository.videoQuality

    fun setVideoQuality(quality: String) {
        viewModelScope.launch {
            playbackPreferencesRepository.setVideoQuality(quality)
        }
    }
}