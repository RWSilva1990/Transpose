package com.example.media.state_holder

import com.example.domain.model.local_file.LocalFileData
import com.example.domain.model.playable.PlayerMode
import com.example.domain.model.playable.PlayableItem
import com.example.domain.model.playable.defaultPlayerMode
import com.example.domain.model.playable.supportedPlayerModes
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.util.Logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class PlaybackError(
    val itemId: String?,
    val itemTitle: String?,
    val errorCode: Int,
    val errorMessage: String?,
    val cause: Throwable?
) {
    class SingleItemError(
        itemId: String?,
        itemTitle: String?,
        errorCode: Int,
        errorMessage: String?,
        cause: Throwable?
    ) : PlaybackError(itemId, itemTitle, errorCode, errorMessage, cause)

    class PlaylistItemError(
        itemId: String?,
        itemTitle: String?,
        errorCode: Int,
        errorMessage: String?,
        cause: Throwable?,
        val skippedToNext: Boolean
    ) : PlaybackError(itemId, itemTitle, errorCode, errorMessage, cause)
}

@Singleton
class NowPlayingStateHolder @Inject constructor() {

    private val _playbackErrorEvent = Channel<PlaybackError>(Channel.BUFFERED)
    val playbackErrorEvent = _playbackErrorEvent.receiveAsFlow()

    private val _playbackType = MutableStateFlow(PlaybackType.SINGLE)
    val playbackType: StateFlow<PlaybackType> = _playbackType.asStateFlow()

    private val _playerMode = MutableStateFlow(PlayerMode.VIDEO)
    val playerMode: StateFlow<PlayerMode> = _playerMode.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentPlaylistInfo = MutableStateFlow<Playlist?>(null)
    val currentPlaylistInfo = _currentPlaylistInfo.asStateFlow()

    private val _currentItem = MutableStateFlow<PlayableItem?>(null)
    val currentItem: StateFlow<PlayableItem?> = _currentItem.asStateFlow()

    private val _currentVideo = MutableStateFlow<Video?>(null)
    val currentVideo: StateFlow<Video?> = _currentVideo.asStateFlow()

    private val _currentLocalFile = MutableStateFlow<LocalFileData?>(null)
    val currentLocalFile: StateFlow<LocalFileData?> = _currentLocalFile.asStateFlow()

    private val _currentVideoDetail = MutableStateFlow<VideoDetail?>(null)
    val currentVideoDetail: StateFlow<VideoDetail?> = _currentVideoDetail.asStateFlow()

    private val _currentPlaylistItems = MutableStateFlow<List<PlayableItem>>(emptyList())
    val currentPlaylist: StateFlow<List<PlayableItem>> = _currentPlaylistItems.asStateFlow()

    private val _currentPlaylistIndex = MutableStateFlow(-1)
    val currentPlaylistIndex: StateFlow<Int> = _currentPlaylistIndex.asStateFlow()

    fun setCurrentVideoDetail(videoDetail: VideoDetail?) {
        _currentVideoDetail.value = videoDetail
    }

    fun setIsPlaying(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun setCurrentPosition(position: Long) {
        _currentPosition.value = position
    }

    fun setDuration(duration: Long) {
        _duration.value = duration
    }

    fun setCurrentPlaylistInfo(playlist: Playlist?) {
        _currentPlaylistInfo.value = playlist
    }

    fun setCurrentItem(item: PlayableItem?) {
        Logger.d("NowPlayingStateHolder.setCurrentItem - item: ${item?.id}, isLocal: ${item?.isLocal}")
        _currentItem.value = item
        _playerMode.value = item?.defaultPlayerMode() ?: PlayerMode.VIDEO
        when (item) {
            is PlayableItem.Remote -> {
                Logger.d("NowPlayingStateHolder.setCurrentItem - setting currentVideo: ${item.video.id}")
                _currentVideo.value = item.video
                _currentLocalFile.value = null
            }
            is PlayableItem.Local -> {
                Logger.d("NowPlayingStateHolder.setCurrentItem - setting currentLocalFile: ${item.localFile.id}")
                _currentVideo.value = null
                _currentLocalFile.value = item.localFile
            }
            null -> {
                Logger.d("NowPlayingStateHolder.setCurrentItem - clearing all")
                _currentVideo.value = null
                _currentLocalFile.value = null
            }
        }
    }

    fun setPlayerMode(mode: PlayerMode) {
        val item = _currentItem.value ?: return
        if (mode in item.supportedPlayerModes()) {
            _playerMode.value = mode
        }
    }

    fun setCurrentVideoData(video: Video?) {
        _currentVideo.value = video
        if (video != null) {
            val item = PlayableItem.Remote(video)
            _currentItem.value = item
            _playerMode.value = item.defaultPlayerMode()
            _currentLocalFile.value = null
        }
    }

    fun setCurrentLocalFileData(localFile: LocalFileData?) {
        _currentLocalFile.value = localFile
        if (localFile != null) {
            val item = PlayableItem.Local(localFile)
            _currentItem.value = item
            _playerMode.value = item.defaultPlayerMode()
            _currentVideo.value = null
        }
    }

    fun setCurrentPlaylist(playlist: List<PlayableItem>) {
        _currentPlaylistItems.value = playlist
    }

    fun setCurrentPlaylistIndex(index: Int) {
        _currentPlaylistIndex.value = index
    }

    fun setPlaybackType(type: PlaybackType) {
        _playbackType.value = type
    }

    fun clearAll() {
        Logger.d("NowPlayingStateHolder.clearAll - clearing all state")
        _isPlaying.value = false
        _currentVideoDetail.value = null
        _currentPosition.value = 0L
        _duration.value = 0L
        _currentItem.value = null
        _playerMode.value = PlayerMode.VIDEO
        _currentVideo.value = null
        _currentLocalFile.value = null
        _currentPlaylistItems.value = emptyList()
        _currentPlaylistIndex.value = -1
        Logger.d("NowPlayingStateHolder.clearAll - done")
    }

    fun updatePlaybackState(
        isPlaying: Boolean,
        position: Long,
        duration: Long,
        playlistIndex: Int
    ) {
        _isPlaying.value = isPlaying
        _currentPosition.value = position
        _duration.value = duration
        _currentPlaylistIndex.value = playlistIndex
    }

    fun updatePlaylistTrack(item: PlayableItem?, playlist: List<PlayableItem>, index: Int) {
        setCurrentItem(item)
        _currentPlaylistItems.value = playlist
        _currentPlaylistIndex.value = index
    }

    fun updateSingleTrack(item: PlayableItem?) {
        Logger.d("NowPlayingStateHolder.updateSingleTrack - item: ${item?.id}")
        setCurrentItem(item)
        _currentPlaylistItems.value = emptyList()
        _currentPlaylistIndex.value = 0
        Logger.d("NowPlayingStateHolder.updateSingleTrack - done")
    }

    fun hasNext(): Boolean {
        return _currentPlaylistIndex.value < _currentPlaylistItems.value.size - 1
    }

    fun hasPrevious(): Boolean {
        return _currentPlaylistIndex.value > 0
    }

    fun getNextItem(): PlayableItem? {
        val currentIndex = _currentPlaylistIndex.value
        val playlist = _currentPlaylistItems.value
        return if (currentIndex < playlist.size - 1) {
            playlist[currentIndex + 1]
        } else null
    }

    fun getPreviousItem(): PlayableItem? {
        val currentIndex = _currentPlaylistIndex.value
        val playlist = _currentPlaylistItems.value
        return if (currentIndex > 0) {
            playlist[currentIndex - 1]
        } else null
    }

    suspend fun emitPlaybackError(error: PlaybackError) {
        _playbackErrorEvent.send(error)
    }
}
