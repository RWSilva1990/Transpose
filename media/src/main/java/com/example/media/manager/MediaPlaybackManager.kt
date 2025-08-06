package com.example.media.manager

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.source.BehindLiveWindowException
import androidx.media3.session.MediaController
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.model.youtube.video.Video
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.media.state_holder.PlaybackType
import com.example.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPlaybackManager @Inject constructor(
    private val controllerProvider: MediaControllerProvider,
    private val nowPlayingStateHolder: NowPlayingStateHolder
) {

    private val defaultDispatcher = Dispatchers.Default
    private val mainDispatcher = Dispatchers.Main

    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)

    private var updateMediaItemJob: Job? = null

    private var lastStateUpdateTime = 0L
    private val stateUpdateThrottleMs = 100L

    private val mediaItemCache = mutableMapOf<String, Video>()

    val mediaControllerFlow: StateFlow<MediaController?> = controllerProvider.mediaController

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
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            scope.launch {
                handleMediaItemTransition(mediaItem)
            }
        }

        @OptIn(UnstableApi::class)
        override fun onPlayerError(error: PlaybackException) {
            Logger.e("🔴 Player Error:")
            Logger.e("  - Error code: ${error.errorCode}")
            Logger.e("  - Message: ${error.message}")
            Logger.e("  - Cause: ${error.cause}")

            error.cause?.let { cause ->
                if (cause is BehindLiveWindowException) {
                    Logger.e("  - Behind live window")
                } else if (cause is HttpDataSource.HttpDataSourceException) {
                    Logger.e("  - HTTP error: ${cause.type}")
                } else {
                    Logger.e("  - HTTP error: ${cause.cause}")
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            nowPlayingStateHolder.setIsPlaying(isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val ctrl = mediaControllerFlow.value ?: return
            throttledUpdatePlaybackState(ctrl)

            if (playbackState == Player.STATE_ENDED) {
                handleTrackEnded()
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
        }
    }

    private fun throttledUpdatePlaybackState(controller: MediaController) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastStateUpdateTime >= stateUpdateThrottleMs) {
            updatePlaybackState(controller)
            lastStateUpdateTime = currentTime
        }
    }

    private fun handleTrackEnded() {
        val ctrl = mediaControllerFlow.value ?: return

        ctrl.seekTo(0, 0)
        ctrl.pause()

        nowPlayingStateHolder.setCurrentPlaylistIndex(0)
    }

    private suspend fun handleMediaItemTransition(mediaItem: MediaItem?) {
        val mediaId = mediaItem?.mediaId ?: return

        mediaItemCache[mediaId]?.let {
            updateUiForPlayingMediaItem(it)
            return
        }

        withContext(defaultDispatcher) {
            val playlist = nowPlayingStateHolder.currentPlaylist.value

            val matchingItem = playlist.find { it.id == mediaId }
            if (matchingItem != null) {
                mediaItemCache[mediaId] = matchingItem

                withContext(mainDispatcher) {
                    updateUiForPlayingMediaItem(matchingItem)
                }
                return@withContext
            }

            val data = mediaItem.localConfiguration?.tag as? Video

            withContext(mainDispatcher) {
                updateUiForPlayingMediaItem(data)
            }
        }
    }

    private fun updateUiForPlayingMediaItem(metadata: Video?) {
        if (metadata != null) {
            val ctrl = mediaControllerFlow.value ?: return

            when (nowPlayingStateHolder.playbackType.value) {
                PlaybackType.SINGLE -> nowPlayingStateHolder.setCurrentVideoData(metadata)
                PlaybackType.PLAYLIST -> {
                    nowPlayingStateHolder.setCurrentVideoData(metadata)
                    nowPlayingStateHolder.setCurrentPlaylistIndex(ctrl.currentMediaItemIndex)
                }

                PlaybackType.LOCAL -> {}
            }
        }
    }

    fun updateMediaItemWithFullInfo(
        itemId: String,
        videoDefaultStreamUrl: String,
        videoOnlyStreamUrl: String?,
        audioOnlyStreamUrl: String?,
        videoQuality: String,
        videoManifestString: String,
        videoManifestUrl: String?,
        audioManifestsString: String,
        audioManifestUrl: String?,
    ) {
        val ctrl = mediaControllerFlow.value ?: return
        val currentPosition = ctrl.currentPosition

        updateMediaItemJob?.cancel()


        ctrl.addListener(object : Player.Listener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                ctrl.seekTo(currentPosition)
                ctrl.removeListener(this)
            }
        })

        if (videoQuality == "AUTO") {
            updateMediaItemJob = scope.launch {
                val currentIndex = ctrl.currentMediaItemIndex

                if (currentIndex < 0 || currentIndex >= ctrl.mediaItemCount) return@launch

                val currentItem = ctrl.getMediaItemAt(currentIndex)
                if (currentItem.mediaId == itemId) {
                    val updatedMetadata = currentItem.mediaMetadata.buildUpon()
                        .setExtras(android.os.Bundle().apply {
                            putString("videoQuality", videoQuality)
                        })
                        .build()
                    val updatedMediaItem = currentItem.buildUpon()
                        .setUri(videoDefaultStreamUrl)
                        .build()
                    Logger.d("quality 고정일 때 로그창")

                    ctrl.replaceMediaItem(currentIndex, updatedMediaItem)
                }
            }
        } else {
            updateMediaItemJob = scope.launch {
                val currentIndex = ctrl.currentMediaItemIndex

                if (currentIndex < 0 || currentIndex >= ctrl.mediaItemCount) return@launch

                val currentItem = ctrl.getMediaItemAt(currentIndex)
                if (currentItem.mediaId == itemId) {
                    Logger.d("quality 바뀌었을 때 로그창 $videoManifestString")
                    val updatedMetadata = currentItem.mediaMetadata.buildUpon()
                        .setExtras(android.os.Bundle().apply {
                            putString("videoQuality", videoQuality)
                            putString("videoManifestUrl", videoManifestUrl)
                            putString("videoManifestString", videoManifestString)
                            putString("audioManifestUrl", audioManifestUrl)
                            putString("audioManifestString", audioManifestsString)
                            putString("videoUrl", videoOnlyStreamUrl)
                            putString("audioUrl", audioOnlyStreamUrl)
                        })
                        .build()

                    val updatedMediaItem = currentItem.buildUpon()
                        .setUri(videoOnlyStreamUrl)
                        .setMediaMetadata(updatedMetadata)
                        .build()
                    Logger.d("quality 바뀌었을 때 로그창")

                    ctrl.replaceMediaItem(currentIndex, updatedMediaItem)
                }
            }
        }
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
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }

        ctrl.repeatMode = exoPlayerRepeatMode
    }

    fun setShuffleMode(enabled: Boolean) {
        val ctrl = mediaControllerFlow.value ?: return
        ctrl.shuffleModeEnabled = enabled
    }

    fun getCurrentShuffleMode(): Boolean {
        val ctrl = mediaControllerFlow.value ?: return false
        return ctrl.shuffleModeEnabled
    }

    fun getCurrentRepeatMode(): RepeatMode {
        val ctrl = mediaControllerFlow.value ?: return RepeatMode.OFF

        return when (ctrl.repeatMode) {
            Player.REPEAT_MODE_OFF -> RepeatMode.OFF
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.OFF
        }
    }


    fun playSingleVideo(
        video: Video,
    ) {
        nowPlayingStateHolder.setPlaybackType(PlaybackType.SINGLE)

        val ctrl = mediaControllerFlow.value ?: return
        val isSameItem = (ctrl.currentMediaItem?.mediaId == video.id)

        if (isSameItem) {
            if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
            return
        }

        prepareAndPlay(video, emptyList(), 0)
    }

    fun playPlaylist(
        playlistItems: List<Video>,
        startIndex: Int = 0
    ) {
        nowPlayingStateHolder.setPlaybackType(PlaybackType.PLAYLIST)

        val ctrl = mediaControllerFlow.value ?: return
        val isSameItem = (ctrl.currentMediaItem?.mediaId == playlistItems[startIndex].id)

        if (isSameItem) {
            if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
            return
        }

        prepareAndPlay(null, playlistItems, startIndex)

    }

    private fun prepareAndPlay(video: Video?, playlistItems: List<Video>, startIndex: Int = 0) {
        val ctrl = mediaControllerFlow.value ?: return

        clearCurrentPlayback()
        scope.launch {
            when (nowPlayingStateHolder.playbackType.value) {
                PlaybackType.SINGLE -> {
                    val singleItem = withContext(defaultDispatcher) {
                        video!!.let {
                            mediaItemCache[video.id] = video
                            createMediaItem(video)
                        }
                    }

                    withContext(mainDispatcher) {
                        ctrl.setMediaItem(singleItem)
                        nowPlayingStateHolder.updateSingleTrack(video)
                        ctrl.prepare()
                        ctrl.play()
                    }
                }

                PlaybackType.PLAYLIST -> {
                    val mediaItems = withContext(defaultDispatcher) {
                        playlistItems.forEach { video ->
                            mediaItemCache[video.id] = video
                        }
                        createMediaItems(playlistItems)
                    }

                    withContext(mainDispatcher) {
                        ctrl.setMediaItems(mediaItems, startIndex, 0L)

                        nowPlayingStateHolder.updatePlaylistTrack(
                            video = playlistItems[startIndex],
                            playlist = playlistItems,
                            index = startIndex
                        )
                        ctrl.prepare()
                        ctrl.play()
                    }
                }

                PlaybackType.LOCAL -> {
                }
            }
        }
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

    fun clearCurrentPlayback() {
        val ctrl = mediaControllerFlow.value ?: return
        ctrl.stop()
        ctrl.clearMediaItems()
        nowPlayingStateHolder.clearAll()
        mediaItemCache.clear()
    }

    private fun updatePlaybackState(controller: MediaController) {
        nowPlayingStateHolder.updatePlaybackState(
            isPlaying = controller.isPlaying,
            position = controller.currentPosition,
            duration = controller.duration,
            playlistIndex = controller.currentMediaItemIndex
        )
    }

    fun release() {
        updateMediaItemJob?.cancel()
        scope.launch {
            controllerProvider.mediaController.value?.removeListener(playerListener)
            controllerProvider.release()
            mediaItemCache.clear()
        }
    }
}