package com.example.main

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.domain.repository.ChannelRepository
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.domain.repository.PlaybackPreferencesRepository
import com.example.domain.repository.SuggestionKeywordRepository
import com.example.domain.repository.VideoRepository
import com.example.media.manager.AudioEffectsManager
import com.example.media.manager.MediaPlaybackManager
import com.example.util.Logger
import com.example.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val mediaPlaybackManager: MediaPlaybackManager,
    private val audioEffectsManager: AudioEffectsManager,
    private val suggestionKeywordRepository: SuggestionKeywordRepository,
    private val videoRepository: VideoRepository,
    private val myPlaylistDBRepository: MyPlaylistDBRepository,
    private val channelRepository: ChannelRepository,
    private val playbackPreferencesRepository: PlaybackPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()


    val repeatMode: StateFlow<RepeatMode> = playbackPreferencesRepository.repeatMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RepeatMode.NONE
        )

    // 셔플 모드 상태
    val shuffleMode: StateFlow<Boolean> = playbackPreferencesRepository.shuffleMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        checkPermissions()

        viewModelScope.launch {
            mediaPlaybackManager.currentVideoData
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .collectLatest { videoData ->
                    videoData?.let {
                        fetchCurrentVideoDetailData(it.id)
                    }
                }
        }

        viewModelScope.launch {
            repeatMode?.collect { mode ->
                mediaPlaybackManager.setRepeatMode(mode)
            }
        }

        viewModelScope.launch {
            shuffleMode?.collect { enabled ->
                mediaPlaybackManager.setShuffleMode(enabled)
            }
        }
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

    val mediaControllerFlow: StateFlow<MediaController?> = mediaPlaybackManager.mediaControllerFlow
    val isPlaying = mediaPlaybackManager.isPlaying
    val currentVideoData = mediaPlaybackManager.currentVideoData
    val currentPlaylistInfo = mediaPlaybackManager.currentPlaylistInfo
    val currentPlaylistItems = mediaPlaybackManager.currentPlaylist
    val currentPlaylistIndex = mediaPlaybackManager.currentPlaylistIndex

    private val _currentVideoDetail = MutableStateFlow<VideoDetail?>(null)
    val currentVideoDetailData = _currentVideoDetail.asStateFlow()

    private fun fetchCurrentVideoDetailData(videoId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _currentVideoDetail.value = null
            val result = videoRepository.fetchVideoDetail(videoId)
            if (result.isSuccess) {
                _currentVideoDetail.value = result.getOrNull()
                mediaPlaybackManager.updateMediaItemWithFullInfo(videoId, result.getOrNull())
            } else {
                Logger.d("mainViewModel fetchCurrentVideoDetailData fail ${result.exceptionOrNull()}")
            }
        }

    private val _myPlaylists = MutableStateFlow<List<MyPlaylist>>(emptyList())
    val myPlaylists = _myPlaylists.asStateFlow()

    fun getAllMyPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        _myPlaylists.value = myPlaylistDBRepository.getAllPlaylists()
    }

    fun addVideoToPlaylist(video: Video, playlistId: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            myPlaylistDBRepository.addVideoToPlaylist(video, playlistId)
        }


    fun playPause() {
        mediaPlaybackManager.playPause()
    }

    fun stopPlayback() {
        mediaPlaybackManager.clearCurrentPlayback()
    }

    fun onMediaItemClick(
        clickedItem: Video,
        playlistItems: List<Video>? = null,
        clickedIndex: Int = 0
    ) {
        mediaPlaybackManager.onMediaItemClick(clickedItem, playlistItems, clickedIndex)
    }

    val pitchValue = audioEffectsManager.pitchValue
    val tempoValue = audioEffectsManager.tempoValue

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

    fun fetchChannelInfo(channelId: String) = viewModelScope.launch(Dispatchers.IO) {
        val result = channelRepository.fetchChannelDetail(channelId)
        if (result.isSuccess) {
            Logger.d("mainViewModel fetchChannelInfo success ${result.getOrNull()}")
        } else {
            Logger.e("mainViewModel fetchChannelInfo fail ${result.exceptionOrNull()}")
        }
    }




    fun toggleRepeatMode() {
        viewModelScope.launch {
            val currentMode = repeatMode.value
            val newMode = when (currentMode) {
                RepeatMode.NONE -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.NONE
            }

            // 저장소 업데이트
            playbackPreferencesRepository.setRepeatMode(newMode)

            // ExoPlayer에 적용
            mediaPlaybackManager.setRepeatMode(newMode)
        }
    }

    // 셔플 모드 전환 메서드
    fun toggleShuffleMode() {
        viewModelScope.launch {
            val currentMode = shuffleMode.value
            val newMode = !currentMode

            // 저장소 업데이트
            playbackPreferencesRepository.setShuffleMode(newMode)

            // ExoPlayer에 적용
            mediaPlaybackManager.setShuffleMode(newMode)
        }
    }

    // 앱 시작 시 저장된 재생 설정 적용 (init 블록에 추가 또는 별도 호출)
    fun applyPlaybackSettings() {
        viewModelScope.launch {
            // 반복 모드 및 셔플 모드 값 가져오기 위한 코드
            val currentRepeatMode = repeatMode.value
            val currentShuffleMode = shuffleMode.value

            // ExoPlayer에 적용
            mediaPlaybackManager.setRepeatMode(currentRepeatMode)
            mediaPlaybackManager.setShuffleMode(currentShuffleMode)
        }
    }


}