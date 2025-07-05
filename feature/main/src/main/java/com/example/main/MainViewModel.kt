package com.example.main

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.lifecycle.SavedStateHandle
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
import com.example.main.components.bottomsheet.state.VideoDetailUiState
import com.example.media.manager.AudioEffectsManager
import com.example.media.manager.MediaPlaybackManager
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.util.Logger
import com.example.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_QUERY = "search_query"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val mediaPlaybackManager: MediaPlaybackManager,
    private val audioEffectsManager: AudioEffectsManager,
    private val suggestionKeywordRepository: SuggestionKeywordRepository,
    private val videoRepository: VideoRepository,
    private val myPlaylistDBRepository: MyPlaylistDBRepository,
    private val channelRepository: ChannelRepository,
    private val nowPlayingStateHolder: NowPlayingStateHolder,
    private val playbackPreferencesRepository: PlaybackPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _videoDetailUiState = MutableStateFlow<VideoDetailUiState>(VideoDetailUiState.Loading)
    val videoDetailUiState = _videoDetailUiState.asStateFlow()

    private fun fetchCurrentVideoDetailData(videoId: String) =
        viewModelScope.launch(Dispatchers.IO) {
            _videoDetailUiState.value = VideoDetailUiState.Loading
            val result = videoRepository.fetchVideoDetail(videoId)
            if (result.isSuccess) {
                _videoDetailUiState.value = VideoDetailUiState.Success(result.getOrNull())
                mediaPlaybackManager.updateMediaItemWithFullInfo(
                    itemId = videoId,
                    videoDetail = result.getOrNull()
                )
            } else {
                _videoDetailUiState.value = VideoDetailUiState.Error(
                    message = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class)
    val suggestionKeywords = searchQuery
        .debounce (100)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()){
                flowOf(emptyList())
            } else {
                suggestionKeywordRepository.getSuggestionKeywords(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    val isPlaying = nowPlayingStateHolder.isPlaying
    val currentVideo = nowPlayingStateHolder.currentVideo
    val currentPlaylist = nowPlayingStateHolder.currentPlaylist
    val currentPlaylistIndex = nowPlayingStateHolder.currentPlaylistIndex
    val currentPlaylistInfo = nowPlayingStateHolder.currentPlaylistInfo

    val repeatMode: StateFlow<RepeatMode> = playbackPreferencesRepository.repeatMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RepeatMode.OFF
        )

    val shuffleMode: StateFlow<Boolean> = playbackPreferencesRepository.shuffleMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        checkPermissions()

        viewModelScope.launch {
            nowPlayingStateHolder.currentVideo
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .collectLatest { currentVideo ->
                    currentVideo?.let {
                        fetchCurrentVideoDetailData(it.id)
                    }
                }
        }

        viewModelScope.launch {
            repeatMode.collect { mode ->
                mediaPlaybackManager.setRepeatMode(mode)
            }
        }

        viewModelScope.launch {
            shuffleMode.collect { enabled ->
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


    val myPlaylists = myPlaylistDBRepository.getAllPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


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

    fun playPlaylist(
        playlist: List<Video>,
        startIndex: Int,
    ){
        mediaPlaybackManager.playPlaylist(
            playlistItems = playlist,
            startIndex = startIndex
        )
    }

    fun playVideo(
        video: Video,
    ) {
        mediaPlaybackManager.playSingleVideo(video)
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

    fun storeSearchQuery(query: String) {
        viewModelScope.launch {
            savedStateHandle[SEARCH_QUERY] = query
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
                RepeatMode.OFF -> RepeatMode.ONE
                RepeatMode.ONE -> RepeatMode.ALL
                RepeatMode.ALL -> RepeatMode.OFF
            }

            // 저장소 업데이트
            playbackPreferencesRepository.setRepeatMode(newMode)

            // ExoPlayer에 적용
            mediaPlaybackManager.setRepeatMode(newMode)
        }
    }


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