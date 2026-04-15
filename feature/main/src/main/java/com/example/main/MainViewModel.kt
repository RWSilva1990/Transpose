package com.example.main

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import com.example.domain.model.local_file.LocalFileData
import com.example.domain.model.playable.PlayableItem
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
import com.example.media.manager.AudioEffectsManager
import com.example.media.manager.MediaPlaybackManager
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.media.state_holder.PlaybackError
import com.example.util.CrashReporter
import com.example.util.Logger
import com.example.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
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
    private val crashReporter: CrashReporter,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _updateDialogState = MutableStateFlow<UpdateDialogState>(UpdateDialogState.Hidden)
    val updateDialogState: StateFlow<UpdateDialogState> = _updateDialogState.asStateFlow()

    private val _videoDetailUiState =
        MutableStateFlow<VideoDetailUiState>(VideoDetailUiState.Loading)
    val videoDetailUiState = _videoDetailUiState.asStateFlow()

    private val _playbackErrorMessage = MutableStateFlow<String?>(null)
    val playbackErrorMessage: StateFlow<String?> = _playbackErrorMessage.asStateFlow()

    private val _toastEvent = Channel<String>(Channel.BUFFERED)
    val toastEvent = _toastEvent.receiveAsFlow()

    private val videoDetailCache = mutableMapOf<String, VideoDetail>()

    private fun fetchCurrentVideoDetailData(video: Video) =
        viewModelScope.launch {
            videoDetailCache[video.id]?.let { cachedDetail ->
                Logger.d("VideoDetail cache hit: ${video.id}")
                _videoDetailUiState.value = VideoDetailUiState.Success(cachedDetail)
                delay(50)
                if (nowPlayingStateHolder.currentVideo.value?.id == video.id) {
                    updateMediaItemWithFullInfo(cachedDetail)
                }
                return@launch
            }

            _videoDetailUiState.value = VideoDetailUiState.Loading
            val result = videoRepository.fetchVideoDetail(video)
            if (result.isSuccess) {
                val videoDetail = result.getOrNull()

                if (videoDetail == null) {
                    _videoDetailUiState.value = VideoDetailUiState.Error("Video detail not found")
                    return@launch
                }

                videoDetailCache[video.id] = videoDetail
                _videoDetailUiState.value = VideoDetailUiState.Success(videoDetail)
                updateMediaItemWithFullInfo(videoDetail)

            } else {
                val error = result.exceptionOrNull()
                val errorMessage = error?.message ?: "Unknown error"

                crashReporter.setCustomKey("video_id", video.id)
                crashReporter.setCustomKey("video_title", video.title)
                crashReporter.log("Video detail fetch failed: $errorMessage")
                error?.let { crashReporter.recordException(it) }

                _videoDetailUiState.value = VideoDetailUiState.Error(message = errorMessage)
                _toastEvent.send(errorMessage)
            }
        }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val selectStreamUseCase = SelectStreamUseCase()

    private fun updateMediaItemWithFullInfo(videoDetail: VideoDetail?) {
        videoDetail ?: return

        val videoDefaultStreamUrl = videoDetail.videoStreamContent
        if (videoDefaultStreamUrl == null) {
            Logger.e("videoStreamContent is null for video: ${videoDetail.id}")
            return
        }

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
                videoDefaultStreamUrl = videoDefaultStreamUrl,
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
                videoDefaultStreamUrl = videoDefaultStreamUrl,
                videoOnlyStreamUrl = null,
                audioOnlyStreamUrl = null,
                videoManifestString = null,
                audioManifestsString = null,
            )
            return
        }

        // DASH manifest 생성 시도, 실패하면 Progressive로 fallback
        try {
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
                videoDefaultStreamUrl = videoDefaultStreamUrl,
                videoOnlyStreamUrl = videoStream.content,
                audioOnlyStreamUrl = audioStream.content,
                videoManifestString = videoManifestString,
                audioManifestsString = audioManifestString,
            )
        } catch (e: Exception) {
            Logger.e("DASH manifest creation failed, falling back to Progressive: ${e.message}")
            mediaPlaybackManager.updateMediaItemWithFullInfo(
                itemId = videoDetail.id,
                videoQuality = currentVideoQuality,
                videoDefaultStreamUrl = videoDefaultStreamUrl,
                videoOnlyStreamUrl = null,
                audioOnlyStreamUrl = null,
                videoManifestString = null,
                audioManifestsString = null,
            )
        }
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

    private val _localSearchQuery = MutableStateFlow("")
    val localSearchQuery: StateFlow<String> = _localSearchQuery.asStateFlow()

    private val _isLocalSearchActive = MutableStateFlow(false)
    val isLocalSearchActive: StateFlow<Boolean> = _isLocalSearchActive.asStateFlow()

    fun updateLocalSearchQuery(query: String) {
        _localSearchQuery.value = query
    }

    fun setLocalSearchActive(active: Boolean) {
        _isLocalSearchActive.value = active
        if (!active) {
            _localSearchQuery.value = ""
        }
    }

    fun clearLocalSearchQuery() {
        _localSearchQuery.value = ""
    }

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    val isPlaying = nowPlayingStateHolder.isPlaying
    val currentVideo = nowPlayingStateHolder.currentVideo
    val currentItem = nowPlayingStateHolder.currentItem
    val currentLocalFile = nowPlayingStateHolder.currentLocalFile
    val currentPlaylist = nowPlayingStateHolder.currentPlaylist
    val currentPlaylistIndex = nowPlayingStateHolder.currentPlaylistIndex
    val currentPlaylistInfo = nowPlayingStateHolder.currentPlaylistInfo

    val repeatMode: StateFlow<RepeatMode> = playbackPreferencesRepository.repeatMode

    val shuffleMode: StateFlow<Boolean> = playbackPreferencesRepository.shuffleMode

    val videoQuality: StateFlow<VideoQuality> = playbackPreferencesRepository.videoQuality

    val audioQuality: StateFlow<AudioQuality> = playbackPreferencesRepository.audioQuality


    init {
        Logger.d("MainViewModel init START")
        viewModelScope.launch {
            Logger.d("MainViewModel - currentVideo observer started")
            nowPlayingStateHolder.currentVideo
                .distinctUntilChanged { old, new ->
                    val same = old?.id == new?.id
                    Logger.d("MainViewModel - distinctUntilChanged: old=${old?.id}, new=${new?.id}, same=$same")
                    same
                }
                .collectLatest { currentVideo ->
                    Logger.d("MainViewModel - collectLatest received: ${currentVideo?.id}, title: ${currentVideo?.title}")
                    currentVideo?.let {
                        Logger.d("MainViewModel - calling fetchCurrentVideoDetailData")
                        _playbackErrorMessage.value = null
                        fetchCurrentVideoDetailData(it)
                    } ?: Logger.d("MainViewModel - currentVideo is null, skipping fetch")
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
        viewModelScope.launch {
            nowPlayingStateHolder.playbackErrorEvent.collect { error ->
                val errorMessage = when (error) {
                    is PlaybackError.PlaylistItemError -> {
                        if (error.skippedToNext) {
                            context.getString(R.string.playback_error_skip_next)
                        } else {
                            context.getString(R.string.playback_error)
                        }
                    }
                    is PlaybackError.SingleItemError -> {
                        context.getString(R.string.playback_error)
                    }
                }
                _playbackErrorMessage.value = errorMessage
            }
        }
    }

    fun start() {
        checkPermissions()
        checkForUpdate()
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

    fun addLocalFileToPlaylist(localFile: LocalFileData, playlistId: Long) =
        viewModelScope.launch {
            myPlaylistDBRepository.addLocalFileToPlaylist(localFile, playlistId)
        }

    fun addItemToPlaylist(item: PlayableItem, playlistId: Long) =
        viewModelScope.launch {
            myPlaylistDBRepository.addPlayableItemToPlaylist(item, playlistId)
        }


    fun playPause() {
        mediaPlaybackManager.playPause()
    }

    fun stopPlayback() {
        mediaPlaybackManager.clearCurrentPlayback()
        videoDetailCache.clear()
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

    fun playVideo(video: Video) {
        Logger.d("MainViewModel.playVideo - videoId: ${video.id}, title: ${video.title}")
        mediaPlaybackManager.playSingleVideo(video)
    }

    fun playLocalFile(localFile: LocalFileData) {
        mediaPlaybackManager.playLocalFile(localFile)
    }

    fun playItem(item: PlayableItem) {
        mediaPlaybackManager.playSingleItem(item)
    }

    fun playPlaylistItems(playlist: List<PlayableItem>, startIndex: Int = 0) {
        mediaPlaybackManager.playPlaylistItems(playlist, startIndex)
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

    fun clearPlaybackError() {
        _playbackErrorMessage.value = null
    }
}