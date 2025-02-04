package com.example.transpose

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.example.domain.repository.SuggestionKeywordRepository
import com.example.media.manager.AudioEffectsManager
import com.example.media.manager.MediaPlaybackManager
import com.example.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val mediaPlaybackManager: MediaPlaybackManager,
    private val audioEffectsManager: AudioEffectsManager,
    private val suggestionKeywordRepository: SuggestionKeywordRepository,
    @ApplicationContext private val context: Context  // Application Context 주입
) : ViewModel() {

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    init {
        checkPermissions()
    }

    private fun checkPermissions() {
        _permissionGranted.value = PermissionUtils.checkPermissions(context)
    }

    fun setPermissionGranted(granted: Boolean) {
        _permissionGranted.value = granted
    }

    fun requestPermissions(launcher: (Array<String>) -> Unit) {
        // 실제 권한 리스트: READ_EXTERNAL_STORAGE or READ_MEDIA_VIDEO 등
        PermissionUtils.requestPermissions(launcher)
    }

    val mediaController = mediaPlaybackManager.mediaController
    val isPlaying = mediaPlaybackManager.isPlaying
    val currentVideoData = mediaPlaybackManager.currentVideoData
    val currentPlaylist = mediaPlaybackManager.currentPlaylist
    val currentPlaylistIndex = mediaPlaybackManager.currentPlaylistIndex

    private val _currentVideoDetailData = MutableStateFlow<VideoDetailData?>(null)
    val currentVideoDetailData = _currentVideoDetailData.asStateFlow()

    fun playPause() {
        mediaPlaybackManager.playPause()
    }

    fun onMediaItemClick(
        clickedItem: BasicVideoData,
        playlistItems: List<BasicVideoData>? = null,
        clickedIndex: Int = 0
    ) {
        mediaPlaybackManager.onMediaItemClick(clickedItem, playlistItems, clickedIndex)
    }

    fun pitchPlusOne() {
        audioEffectsManager.pitchPlusOne()
    }

    fun pitchMinusOne() {
        audioEffectsManager.pitchMinusOne()
    }

    fun initPitchValue() {
        audioEffectsManager.initPitchValue()
    }

    fun tempoPlusOne() {
        audioEffectsManager.tempoPlusOne()
    }

    fun initTempoValue() {
        audioEffectsManager.initTempoValue()
    }

    fun tempoMinusOne() {
        audioEffectsManager.tempoMinusOne()
    }


    private val _suggestionKeywords: MutableStateFlow<List<String>> = MutableStateFlow(
        listOf()
    )
    val suggestionKeywords = _suggestionKeywords.asStateFlow()

    fun clearSuggestionKeywords() {
        _suggestionKeywords.value = arrayListOf()
    }

    fun getSuggestionKeyword(query: String) = viewModelScope.launch {
        suggestionKeywordRepository.getSuggestionKeywords(query)
            .onSuccess { list ->
                _suggestionKeywords.value = list
            }
            .onFailure {
                // 에러 처리
            }
    }

}