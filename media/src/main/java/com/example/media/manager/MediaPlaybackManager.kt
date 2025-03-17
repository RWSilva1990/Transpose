package com.example.media.manager

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPlaybackManager @Inject constructor(
    private val controllerProvider: MediaControllerProvider,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)

    private var updateMediaItemJob: Job? = null

    private var lastStateUpdateTime = 0L
    private val STATE_UPDATE_THROTTLE_MS = 100L

    private val mediaItemCache = mutableMapOf<String, Video>()

    val mediaControllerFlow: StateFlow<MediaController?> = controllerProvider.mediaController

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentPlaylistInfo = MutableStateFlow<Playlist?>(null)
    val currentPlaylistInfo = _currentPlaylistInfo.asStateFlow()

    private val _currentVideoData = MutableStateFlow<Video?>(null)
    val currentVideoData: StateFlow<Video?> = _currentVideoData.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<Video>>(emptyList())
    val currentPlaylist: StateFlow<List<Video>> = _currentPlaylist.asStateFlow()

    private val _currentPlaylistIndex = MutableStateFlow<Int>(-1)
    val currentPlaylistIndex: StateFlow<Int> = _currentPlaylistIndex.asStateFlow()

    init {
        scope.launch {
            controllerProvider.mediaController
                .filterNotNull()
                .collect { ctrl ->
                    ctrl.addListener(playerListener)
                    throttledUpdatePlaybackState(ctrl)
                }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            // 불필요한 super 호출 제거
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            scope.launch {
                handleMediaItemTransition(mediaItem)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            // 오류 로깅이나 처리 로직 추가 가능
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val ctrl = mediaControllerFlow.value ?: return
            throttledUpdatePlaybackState(ctrl)

            if (playbackState == Player.STATE_ENDED) {
                handleTrackEnded()
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            // 불필요한 super 호출 제거
        }
    }

    private fun throttledUpdatePlaybackState(controller: MediaController) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastStateUpdateTime >= STATE_UPDATE_THROTTLE_MS) {
            updatePlaybackState(controller)
            lastStateUpdateTime = currentTime
        }
    }

    private fun updatePlaybackState(controller: MediaController) {
        _isPlaying.value = controller.isPlaying
        _duration.value = controller.duration
        _currentPosition.value = controller.currentPosition
        _currentPlaylistIndex.value = controller.currentMediaItemIndex
    }

    private fun handleTrackEnded() {
        val ctrl = mediaControllerFlow.value ?: return
        val nextIndex = ctrl.currentMediaItemIndex + 1

        if (nextIndex < ctrl.mediaItemCount) {
            ctrl.seekToNext()
            ctrl.play()
            _currentPlaylistIndex.value = nextIndex
        } else {
            ctrl.seekTo(0, 0)
            ctrl.pause()
        }
    }

    private suspend fun handleMediaItemTransition(mediaItem: MediaItem?) {
        val mediaId = mediaItem?.mediaId ?: return

        mediaItemCache[mediaId]?.let {
            updateUiForPlayingMediaItem(it)
            return
        }

        withContext(defaultDispatcher) {
            val matchingItem = _currentPlaylist.value?.find { it.id == mediaId }
            if (matchingItem != null) {
                // 캐시에 추가
                mediaItemCache[mediaId] = matchingItem

                withContext(mainDispatcher) {
                    updateUiForPlayingMediaItem(matchingItem)
                }
                return@withContext
            }

            // 태그에서 데이터 추출
            val data = mediaItem.localConfiguration?.tag as? Video

            withContext(mainDispatcher) {
                updateUiForPlayingMediaItem(data)
            }
        }
    }

    private fun updateUiForPlayingMediaItem(metadata: Video?) {
        if (metadata != null) {
            _currentVideoData.value = metadata
        }
    }

    fun onMediaItemClick(
        clickedItem: Video,
        playlistItems: List<Video>? = null,
        clickedIndex: Int = 0
    ) {
        val ctrl = mediaControllerFlow.value ?: return
        val isSameItem = (ctrl.currentMediaItem?.mediaId == clickedItem.id)

        if (isSameItem) {
            if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
            return
        }

        // 기존 재생 중지 및 정리
        clearCurrentPlayback()

        // 새 작업 시작
        scope.launch {
            if (playlistItems != null) {
                // 백그라운드에서 미디어 아이템 생성
                val mediaItems = withContext(defaultDispatcher) {
                    // 캐시 업데이트
                    playlistItems.forEach { video ->
                        mediaItemCache[video.id] = video
                    }
                    createMediaItems(playlistItems)
                }

                // UI 작업은 메인 스레드에서 수행
                withContext(mainDispatcher) {
                    ctrl.setMediaItems(mediaItems, clickedIndex, 0L)
                    _currentVideoData.value = clickedItem
                    _currentPlaylist.value = playlistItems
                    _currentPlaylistIndex.value = clickedIndex
                }
            } else {
                // 단일 아이템 재생 시
                val singleItem = withContext(defaultDispatcher) {
                    // 캐시 업데이트
                    mediaItemCache[clickedItem.id] = clickedItem
                    createMediaItem(clickedItem)
                }

                withContext(mainDispatcher) {
                    ctrl.setMediaItem(singleItem)
                    _currentVideoData.value = clickedItem
                    _currentPlaylist.value = emptyList()
                    _currentPlaylistIndex.value = 0
                }
            }

            // 재생 시작
            ctrl.prepare()
            ctrl.play()
        }
    }

    fun clearCurrentPlayback() {
        val ctrl = mediaControllerFlow.value ?: return
        ctrl.stop()
        ctrl.clearMediaItems()
        _currentVideoData.value = null
        // 캐시도 정리
        mediaItemCache.clear()
    }

    private fun createMediaItem(video: Video): MediaItem {
        val uri = Uri.parse("asset:///30-seconds-of-silence.mp3")
        return MediaItem.Builder()
            .setMediaId(video.id)
            .setUri(uri)
            .setTag(video)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(video.title)
                    .setArtist(video.uploaderName ?: "Unknown Uploader")
                    .setArtworkUri(Uri.parse(video.thumbnailUrl ?: ""))
                    .build()
            )
            .build()
    }

    private fun createMediaItems(videoList: List<Video>): List<MediaItem> {
        val uri = Uri.parse("asset:///30-seconds-of-silence.mp3")
        // 배치 처리를 통한 최적화 가능
        return videoList.map {
            MediaItem.Builder()
                .setMediaId(it.id)
                .setUri(uri)
                .setTag(it)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(it.title)
                        .setArtist(it.uploaderName ?: "Unknown Uploader")
                        .setArtworkUri(Uri.parse(it.thumbnailUrl ?: ""))
                        .build()
                )
                .build()
        }
    }

    fun updateMediaItemWithFullInfo(itemId: String, videoDetail: VideoDetail?) {
        // 이전 작업이 있으면 취소
        updateMediaItemJob?.cancel()

        val selectedVideoStream = videoDetail?.videoStreamContent ?: return

        // 새 작업 시작 및 추적
        updateMediaItemJob = scope.launch {
            val ctrl = mediaControllerFlow.value ?: return@launch
            val currentIndex = ctrl.currentMediaItemIndex

            if (currentIndex < 0 || currentIndex >= ctrl.mediaItemCount) return@launch

            val currentItem = ctrl.getMediaItemAt(currentIndex)
            if (currentItem.mediaId == itemId) {
                val updatedMediaItem = currentItem.buildUpon()
                    .setUri(selectedVideoStream)
                    .build()
                ctrl.replaceMediaItem(currentIndex, updatedMediaItem)
            }
        }
    }

    fun setCurrentPlaylistInfo(playlist: Playlist?) {
        _currentPlaylistInfo.value = playlist
    }

    fun playPause() {
        val ctrl = mediaControllerFlow.value ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun stopPlayback() {
        val ctrl = mediaControllerFlow.value ?: return
        ctrl.stop()
    }

    fun setRepeatMode(repeatMode: RepeatMode) {
        val ctrl = mediaControllerFlow.value ?: return

        val exoPlayerRepeatMode = when (repeatMode) {
            RepeatMode.NONE -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }

        ctrl.repeatMode = exoPlayerRepeatMode
    }

    fun setShuffleMode(enabled: Boolean) {
        val ctrl = mediaControllerFlow.value ?: return
        ctrl.shuffleModeEnabled = enabled
    }

    fun getCurrentRepeatMode(): RepeatMode {
        val ctrl = mediaControllerFlow.value ?: return RepeatMode.NONE

        return when (ctrl.repeatMode) {
            Player.REPEAT_MODE_OFF -> RepeatMode.NONE
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.NONE
        }
    }

    fun isShuffleModeEnabled(): Boolean {
        val ctrl = mediaControllerFlow.value ?: return false
        return ctrl.shuffleModeEnabled
    }

    fun release() {
        // 모든 작업 정리
        updateMediaItemJob?.cancel()
        scope.launch {
            controllerProvider.mediaController.value?.removeListener(playerListener)
            controllerProvider.release()
            // 캐시 정리
            mediaItemCache.clear()
        }
    }
}