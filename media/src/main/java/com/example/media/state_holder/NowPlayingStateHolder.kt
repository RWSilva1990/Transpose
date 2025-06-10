package com.example.media.state_holder

import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class
NowPlayingStateHolder @Inject constructor() {

    private val _playbackType = MutableStateFlow(PlaybackType.SINGLE)
    val playbackType: StateFlow<PlaybackType> = _playbackType.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentPlaylistInfo = MutableStateFlow<Playlist?>(null)
    val currentPlaylistInfo = _currentPlaylistInfo.asStateFlow()

    private val _currentVideo = MutableStateFlow<Video?>(null)
    val currentVideo: StateFlow<Video?> = _currentVideo.asStateFlow()

    private val _currentVideoDetail = MutableStateFlow<VideoDetail?>(null)
    val currentVideoDetail: StateFlow<VideoDetail?> = _currentVideoDetail.asStateFlow()

    private val _currentPlaylistItems = MutableStateFlow<List<Video>>(emptyList())
    val currentPlaylist: StateFlow<List<Video>> = _currentPlaylistItems.asStateFlow()

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

    fun setCurrentVideoData(video: Video?) {
        _currentVideo.value = video
    }

    fun setCurrentPlaylist(playlist: List<Video>) {
        _currentPlaylistItems.value = playlist
    }

    fun setCurrentPlaylistIndex(index: Int) {
        _currentPlaylistIndex.value = index
    }

    fun setPlaybackType(type: PlaybackType) {
        _playbackType.value = type
    }

    fun clearAll() {
        _isPlaying.value = false
        _currentVideoDetail.value = null
        _currentPosition.value = 0L
        _duration.value = 0L
        _currentPlaylistInfo.value = null
        _currentVideo.value = null
        _currentPlaylistItems.value = emptyList()
        _currentPlaylistIndex.value = -1
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

    fun updatePlaylistTrack(video: Video?, playlist: List<Video>, index: Int) {
        _currentVideo.value = video
        _currentPlaylistItems.value = playlist
        _currentPlaylistIndex.value = index
    }

    fun updateSingleTrack(video: Video?) {
        _currentVideo.value = video
        _currentPlaylistItems.value = emptyList()
        _currentPlaylistIndex.value = 0
    }

    fun hasNext(): Boolean {
        return _currentPlaylistIndex.value < _currentPlaylistItems.value.size - 1
    }

    fun hasPrevious(): Boolean {
        return _currentPlaylistIndex.value > 0
    }

    fun getNextVideo(): Video? {
        val currentIndex = _currentPlaylistIndex.value
        val playlist = _currentPlaylistItems.value
        return if (currentIndex < playlist.size - 1) {
            playlist[currentIndex + 1]
        } else null
    }

    fun getPreviousVideo(): Video? {
        val currentIndex = _currentPlaylistIndex.value
        val playlist = _currentPlaylistItems.value
        return if (currentIndex > 0) {
            playlist[currentIndex - 1]
        } else null
    }
}