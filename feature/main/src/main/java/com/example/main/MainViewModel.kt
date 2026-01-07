package com.example.main

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import com.example.domain.model.preferences.AudioQuality
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.model.preferences.VideoQuality
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.domain.repository.ChannelRepository
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.domain.repository.PlaybackPreferencesRepository
import com.example.domain.repository.SuggestionKeywordRepository
import com.example.domain.repository.UpdateInfo
import com.example.domain.repository.UpdateRepository
import com.example.domain.repository.VideoRepository
import com.example.domain.usecase.SelectStreamUseCase
import com.example.main.components.bottomsheet.state.VideoDetailUiState
import com.example.media.audio.PlaybackModeController
import com.example.media.manager.AudioEffectsManager
import com.example.media.manager.MediaPlaybackManager
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.util.Logger
import com.example.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.YoutubeProgressiveDashManifestCreator
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
    private val updateRepository: UpdateRepository,
    private val playbackModeController: PlaybackModeController,
    @ApplicationContext private val context: Context
) : ViewModel() {

    fun onVideoModeSelected() {
        playbackModeController.switchToVideoMode()
    }

    fun onAudioModeSelected() {
        playbackModeController.switchToAudioMode()
    }
    
    fun onVideoWithDspModeSelected() {
        playbackModeController.switchToVideoWithDspMode()
    }

    fun onPitchChanged(pitch: Float) {
        playbackModeController.setPitch(pitch)
    }

    private val _updateDialogState = MutableStateFlow<UpdateDialogState>(UpdateDialogState.Hidden)
    val updateDialogState: StateFlow<UpdateDialogState> = _updateDialogState.asStateFlow()

    private val _videoDetailUiState =
        MutableStateFlow<VideoDetailUiState>(VideoDetailUiState.Loading)
    val videoDetailUiState = _videoDetailUiState.asStateFlow()

    private fun fetchCurrentVideoDetailData(video: Video) =
        viewModelScope.launch {
            _videoDetailUiState.value = VideoDetailUiState.Loading
            val result = videoRepository.fetchVideoDetail(video)
            if (result.isSuccess) {
                val videoDetail = result.getOrNull()

                if (videoDetail == null) {
                    _videoDetailUiState.value = VideoDetailUiState.Error("Video detail not found")
                    return@launch
                }

                _videoDetailUiState.value = VideoDetailUiState.Success(result.getOrNull())

                updateMediaItemWithFullInfo(result.getOrNull())

            } else {
                _videoDetailUiState.value = VideoDetailUiState.Error(
                    message = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val selectStreamUseCase = SelectStreamUseCase()

    private fun updateMediaItemWithFullInfo(videoDetail: VideoDetail?) {
        videoDetail ?: return

        val currentVideoQuality = videoQuality.value
        val currentAudioQuality = audioQuality.value

        Logger.d("Quality change requested: video=${currentVideoQuality.displayName}, audio=${currentAudioQuality.displayName}")

        // 특정 화질 선택: DASH 소스 사용 (video + audio 병합)
        val selectedStreams = selectStreamUseCase.selectStreams(
            videoStreams = videoDetail.videoOnlyStreams,
            audioStreams = videoDetail.audioOnlyStreams,
            videoQuality = currentVideoQuality,
            audioQuality = currentAudioQuality
        )

        val videoStream = selectedStreams.videoStream
        val audioStream = selectedStreams.audioStream

        // AUTO: Progressive 소스 사용 (videoStreamContent만 재생)
        if (currentVideoQuality.isAuto) {

            val audioStream = videoDetail.audioOnlyStreams
                ?.find { it.itag == currentAudioQuality.itag }
                ?: videoDetail.audioOnlyStreams?.firstOrNull()

            Logger.d("Using Progressive source (AUTO)")
            mediaPlaybackManager.updateMediaItemWithFullInfo(
                itemId = videoDetail.id,
                videoQuality = currentVideoQuality,
                videoDefaultStreamUrl = videoDetail.videoStreamContent!!,
                videoOnlyStreamUrl = videoStream?.content,
                audioOnlyStreamUrl = audioStream?.content,
                videoManifestString = null,
                audioManifestsString = null,
            )
            return
        }


        if (videoStream == null || audioStream == null) {
            Logger.d("Selected stream not found, falling back to Progressive")
            mediaPlaybackManager.updateMediaItemWithFullInfo(
                itemId = videoDetail.id,
                videoQuality = currentVideoQuality,
                videoDefaultStreamUrl = videoDetail.videoStreamContent!!,
                videoOnlyStreamUrl = null,
                audioOnlyStreamUrl = null,
                videoManifestString = null,
                audioManifestsString = null,
            )
            return
        }



        Logger.d("Using DASH source: video=${videoStream.itag} ${videoStream.quality}, audio=${audioStream.itag}")

        val videoManifestString = YoutubeProgressiveDashManifestCreator
            .fromProgressiveStreamingUrl(
                videoStream.content,
                videoStream.itagItem!!,
                videoDetail.duration
            )
        val audioManifestString = YoutubeProgressiveDashManifestCreator
            .fromProgressiveStreamingUrl(
                audioStream.content,
                audioStream.itagItem!!,
                videoDetail.duration
            )

        mediaPlaybackManager.updateMediaItemWithFullInfo(
            itemId = videoDetail.id,
            videoQuality = currentVideoQuality,
            videoDefaultStreamUrl = videoDetail.videoStreamContent!!,
            videoOnlyStreamUrl = videoStream.content,
            audioOnlyStreamUrl = audioStream.content,
            videoManifestString = videoManifestString,
            audioManifestsString = audioManifestString,
        )
    }

    @OptIn(FlowPreview::class)
    val suggestionKeywords = searchQuery
        .debounce(100)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
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

    val shuffleMode: StateFlow<Boolean> = playbackPreferencesRepository.shuffleMode

    val videoQuality: StateFlow<VideoQuality> = playbackPreferencesRepository.videoQuality

    val audioQuality: StateFlow<AudioQuality> = playbackPreferencesRepository.audioQuality


    init {
        checkPermissions()
        checkForUpdate()
        viewModelScope.launch {
            nowPlayingStateHolder.currentVideo
                .distinctUntilChanged { old, new -> old?.id == new?.id }
                .collectLatest { currentVideo ->
                    currentVideo?.let {
                        fetchCurrentVideoDetailData(it)
                    }
                }
        }
        viewModelScope.launch {
            videoQuality.collectLatest {
                videoDetailUiState.value.let { state ->
                    if (state is VideoDetailUiState.Success) {
                        updateMediaItemWithFullInfo(state.videoDetail)
                    }
                }
            }
        }
    }

    fun setVideoQuality(quality: VideoQuality) {
        viewModelScope.launch {
            playbackPreferencesRepository.setVideoQuality(quality)
        }
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            updateRepository.checkForUpdate()?.let { updateInfo ->
                Logger.d("Update available: ${updateInfo.latestVersion}")
                if (updateInfo.isUpdateAvailable) {
                    _updateDialogState.value = UpdateDialogState.Visible(updateInfo)
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateDialogState.value = UpdateDialogState.Hidden
    }

    fun onUpdateClicked() {
        (_updateDialogState.value as? UpdateDialogState.Visible)?.let { state ->
            _updateDialogState.value = UpdateDialogState.Hidden
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
        viewModelScope.launch {
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
    ) {
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

    fun fetchChannelInfo(channelId: String) = viewModelScope.launch {
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


    sealed interface UpdateDialogState {
        data object Hidden : UpdateDialogState
        data class Visible(val updateInfo: UpdateInfo) : UpdateDialogState
    }


}