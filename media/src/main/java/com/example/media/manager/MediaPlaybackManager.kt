package com.example.media.manager

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import com.example.domain.model.youtube.video.BasicVideoData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject


class MediaPlaybackManager @Inject constructor(
    private val controllerProvider: MediaControllerProvider,
) {

    val mediaController: MediaController?
        get() = controllerProvider.mediaController.value

    // 재생 중 여부, 현재 위치, 총 길이 등 핵심 상태
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentVideoData = MutableStateFlow<BasicVideoData?>(null)
    val currentVideoData: StateFlow<BasicVideoData?> = _currentVideoData.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<BasicVideoData>?>(null)
    val currentPlaylist: StateFlow<List<BasicVideoData>?> = _currentPlaylist.asStateFlow()

    private val _currentPlaylistIndex = MutableStateFlow<Int>(-1)
    val currentPlaylistIndex: StateFlow<Int> = _currentPlaylistIndex.asStateFlow()


    init {
        CoroutineScope(Dispatchers.Main).launch {
            controllerProvider.mediaController
                .filterNotNull()
                .collect { ctrl ->
                    ctrl.addListener(playerListener)
                    updatePlaybackState(ctrl)
                }
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            super.onPlaybackParametersChanged(playbackParameters)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            handleMediaItemTransition(mediaItem)
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            val ctrl = mediaController ?: return
            updatePlaybackState(ctrl)

            if (playbackState == Player.STATE_ENDED) {
                handleTrackEnded()
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            super.onTracksChanged(tracks)
        }
    }

    private fun updatePlaybackState(controller: MediaController) {
        _isPlaying.value = controller.isPlaying
        _duration.value = controller.duration
        _currentPosition.value = controller.currentPosition
    }

    private fun handleTrackEnded() {
        CoroutineScope(Dispatchers.Main).launch {
            val ctrl = mediaController ?: return@launch
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
    }

    private fun handleMediaItemTransition(mediaItem: MediaItem?) {
        val data = mediaItem
            ?.localConfiguration
            ?.tag as? BasicVideoData
        updateUiForPlayingMediaItem(data)
    }

    private fun updateUiForPlayingMediaItem(metadata: BasicVideoData?) {
        _currentVideoData.value = metadata

    }


    fun onMediaItemClick(
        clickedItem: BasicVideoData,
        playlistItems: List<BasicVideoData>? = null,
        clickedIndex: Int = 0
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val ctrl = mediaController ?: return@launch
            val isSameItem = (ctrl.currentMediaItem?.mediaId == clickedItem.id)

            if (isSameItem) {
                if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
                return@launch
            }

            clearCurrentPlayback(ctrl)
            if (playlistItems != null) {
                val mediaItems = createMediaItems(playlistItems)
                ctrl.setMediaItems(mediaItems, clickedIndex, 0L)
                _currentPlaylist.value = playlistItems
                _currentPlaylistIndex.value = clickedIndex
            } else {
                val singleItem = createMediaItem(clickedItem)
                ctrl.setMediaItem(singleItem)
                _currentPlaylist.value = null
                _currentPlaylistIndex.value = -1
            }
            ctrl.prepare()
            ctrl.play()
        }
    }

    private fun clearCurrentPlayback(controller: MediaController) {
        controller.stop()
        controller.clearMediaItems()
        // 필요하면 추가 로직
    }

    private fun createMediaItem(basicVideoData: BasicVideoData): MediaItem {
        val uri = Uri.parse("asset:///15-seconds-of-silence.mp3")
        return MediaItem.Builder()
            .setMediaId(basicVideoData.id)
            .setUri(uri)
            .setTag(basicVideoData)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(basicVideoData.title)
                    .setArtist(basicVideoData.uploaderName ?: "Unknown Uploader")
                    .setArtworkUri(Uri.parse(basicVideoData.thumbnailUrl))
                    .build()
            )
            .build()
    }

    private fun createMediaItems(basicVideoDataList: List<BasicVideoData>): List<MediaItem> {
        val uri = Uri.parse("asset:///15-seconds-of-silence.mp3")
        return basicVideoDataList.map {
            MediaItem.Builder()
                .setMediaId(it.id)
                .setUri(uri)
                .setTag(it)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(it.title)
                        .setArtist(it.uploaderName ?: "Unknown Uploader")
                        .setArtworkUri(Uri.parse(it.thumbnailUrl))
                        .build()
                )
                .build()
        }
    }


    fun playPause() {
        val ctrl = mediaController ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun release() {
        controllerProvider.mediaController.value?.removeListener(playerListener)
        controllerProvider.release()
    }
}
