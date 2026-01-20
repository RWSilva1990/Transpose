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
import com.example.domain.model.local_file.LocalFileData
import com.example.domain.model.playable.PlayableItem
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.model.preferences.VideoQuality
import com.example.domain.model.youtube.video.Video
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.media.state_holder.PlaybackError
import com.example.media.state_holder.PlaybackType
import com.example.util.CrashReporter
import com.example.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPlaybackManager @Inject constructor(
    private val controllerProvider: MediaControllerProvider,
    private val nowPlayingStateHolder: NowPlayingStateHolder,
    private val crashReporter: CrashReporter
) {

    private val defaultDispatcher = Dispatchers.Default
    private val mainDispatcher = Dispatchers.Main

    private val scope = CoroutineScope(SupervisorJob() + mainDispatcher)

    private var updateMediaItemJob: Job? = null

    private var lastStateUpdateTime = 0L
    private val stateUpdateThrottleMs = 100L

    private val mediaItemCache = mutableMapOf<String, PlayableItem>()

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
            val currentItem = nowPlayingStateHolder.currentItem.value
            val isPlaylist = nowPlayingStateHolder.playbackType.value == PlaybackType.PLAYLIST
            val hasNext = nowPlayingStateHolder.hasNext()

            Logger.e("🔴 Player Error:")
            Logger.e("  - Item: ${currentItem?.id} - ${currentItem?.title}")
            Logger.e("  - Error code: ${error.errorCode}")
            Logger.e("  - Message: ${error.message}")
            Logger.e("  - Cause: ${error.cause}")

            crashReporter.setCustomKey("error_item_id", currentItem?.id ?: "unknown")
            crashReporter.setCustomKey("error_item_title", currentItem?.title ?: "unknown")
            crashReporter.setCustomKey("error_code", error.errorCode)
            crashReporter.setCustomKey("playback_type", if (isPlaylist) "playlist" else "single")
            crashReporter.setCustomKey("is_local", currentItem?.isLocal?.toString() ?: "unknown")
            crashReporter.log("Playback error: itemId=${currentItem?.id}, errorCode=${error.errorCode}, message=${error.message}")
            crashReporter.recordException(error)

            error.cause?.let { cause ->
                if (cause is BehindLiveWindowException) {
                    Logger.e("  - Behind live window")
                } else if (cause is HttpDataSource.HttpDataSourceException) {
                    Logger.e("  - HTTP error: ${cause.type}")
                }
            }

            scope.launch {
                if (isPlaylist && hasNext) {
                    nowPlayingStateHolder.emitPlaybackError(
                        PlaybackError.PlaylistItemError(
                            itemId = currentItem?.id,
                            itemTitle = currentItem?.title,
                            errorCode = error.errorCode,
                            errorMessage = error.message,
                            cause = error.cause,
                            skippedToNext = true
                        )
                    )
                    delay(500)
                    mediaControllerFlow.value?.seekToNextMediaItem()
                } else {
                    nowPlayingStateHolder.emitPlaybackError(
                        PlaybackError.SingleItemError(
                            itemId = currentItem?.id,
                            itemTitle = currentItem?.title,
                            errorCode = error.errorCode,
                            errorMessage = error.message,
                            cause = error.cause
                        )
                    )
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Logger.d("🎵 isPlaying: $isPlaying")
            nowPlayingStateHolder.setIsPlaying(isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateName = when (playbackState) {
                Player.STATE_IDLE -> "IDLE"
                Player.STATE_BUFFERING -> "BUFFERING"
                Player.STATE_READY -> "READY"
                Player.STATE_ENDED -> "ENDED"
                else -> "UNKNOWN($playbackState)"
            }
            Logger.d("🎵 PlaybackState: $stateName")
            
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
            updateUiForPlayingItem(it)
            return
        }

        withContext(defaultDispatcher) {
            val playlist = nowPlayingStateHolder.currentPlaylist.value

            val matchingItem = playlist.find { it.id == mediaId }
            if (matchingItem != null) {
                mediaItemCache[mediaId] = matchingItem
                withContext(mainDispatcher) {
                    updateUiForPlayingItem(matchingItem)
                }
                return@withContext
            }

            val tag = mediaItem.localConfiguration?.tag
            val playableItem: PlayableItem? = when (tag) {
                is Video -> PlayableItem.Remote(tag)
                is LocalFileData -> PlayableItem.Local(tag)
                is PlayableItem -> tag
                else -> null
            }

            withContext(mainDispatcher) {
                updateUiForPlayingItem(playableItem)
            }
        }
    }

    private fun updateUiForPlayingItem(item: PlayableItem?) {
        if (item == null) return
        val ctrl = mediaControllerFlow.value ?: return

        when (nowPlayingStateHolder.playbackType.value) {
            PlaybackType.SINGLE, PlaybackType.LOCAL -> {
                nowPlayingStateHolder.setCurrentItem(item)
            }
            PlaybackType.PLAYLIST -> {
                nowPlayingStateHolder.setCurrentItem(item)
                nowPlayingStateHolder.setCurrentPlaylistIndex(ctrl.currentMediaItemIndex)
            }
        }
    }

    fun updateMediaItemWithFullInfo(
        itemId: String,
        videoDefaultStreamUrl: String,
        videoOnlyStreamUrl: String?,
        audioOnlyStreamUrl: String?,
        videoQuality: VideoQuality,
        videoManifestString: String?,
        audioManifestsString: String?,
    ) {
        val ctrl = mediaControllerFlow.value ?: return
        val currentPosition = ctrl.currentPosition
        val shouldPlayWhenReady = ctrl.playWhenReady

        updateMediaItemJob?.cancel()

        ctrl.addListener(object : Player.Listener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                if (currentPosition > 5000) {
                    ctrl.seekTo(currentPosition)
                }
                ctrl.removeListener(this)
            }
        })

        if (videoQuality.isAuto) {
            val streamUrl = videoDefaultStreamUrl

            Logger.d("MediaPlaybackManager: Switching to AUTO source (progressive video+audio)")
            Logger.d("streamUrl: $streamUrl")

            updateMediaItemJob = scope.launch {
                val currentIndex = ctrl.currentMediaItemIndex

                if (currentIndex < 0 || currentIndex >= ctrl.mediaItemCount) return@launch

                val currentItem = ctrl.getMediaItemAt(currentIndex)
                if (currentItem.mediaId == itemId) {
                    val currentUri = currentItem.localConfiguration?.uri?.toString()
                    if (currentUri == streamUrl) {
                        Logger.d("MediaItem already has correct URL, skipping update")
                        return@launch
                    }

                    val updatedMetadata = currentItem.mediaMetadata.buildUpon()
                        .setExtras(android.os.Bundle().apply {
                            putString("videoQuality", videoQuality.name)
                        })
                        .build()
                    val updatedMediaItem = currentItem.buildUpon()
                        .setUri(streamUrl)
                        .setMediaMetadata(updatedMetadata)
                        .build()

                    ctrl.replaceMediaItem(currentIndex, updatedMediaItem)
                    ctrl.prepare()
                    if (shouldPlayWhenReady) {
                        ctrl.play()
                    }
                }
            }
        } else {
            Logger.d("MediaPlaybackManager: Switching to ${videoQuality.displayName} (DASH) source")
            updateMediaItemJob = scope.launch {
                val currentIndex = ctrl.currentMediaItemIndex

                if (currentIndex < 0 || currentIndex >= ctrl.mediaItemCount) return@launch

                val currentItem = ctrl.getMediaItemAt(currentIndex)
                if (currentItem.mediaId == itemId) {
                    val currentUri = currentItem.localConfiguration?.uri?.toString()
                    val currentQuality = currentItem.mediaMetadata.extras?.getString("videoQuality")
                    if (currentUri == videoOnlyStreamUrl && currentQuality == videoQuality.name) {
                        Logger.d("MediaItem already has correct URL and quality, skipping update")
                        return@launch
                    }

                    val updatedMetadata = currentItem.mediaMetadata.buildUpon()
                        .setExtras(android.os.Bundle().apply {
                            putString("videoQuality", videoQuality.name)
                            putString("videoManifestString", videoManifestString)
                            putString("audioManifestString", audioManifestsString)
                            putString("videoUrl", videoOnlyStreamUrl)
                            putString("audioUrl", audioOnlyStreamUrl)
                        })
                        .build()

                    val updatedMediaItem = currentItem.buildUpon()
                        .setUri(videoOnlyStreamUrl)
                        .setMediaMetadata(updatedMetadata)
                        .build()

                    ctrl.replaceMediaItem(currentIndex, updatedMediaItem)
                    ctrl.prepare()
                    if (shouldPlayWhenReady) {
                        ctrl.play()
                    }
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

    fun setPlaybackSpeed(rate: Float) {
        val ctrl = mediaControllerFlow.value ?: return
        ctrl.playbackParameters = PlaybackParameters(rate)
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


    fun playSingleVideo(video: Video) {
        Logger.d("MediaPlaybackManager.playSingleVideo - videoId: ${video.id}")
        playSingleItem(PlayableItem.Remote(video))
    }

    fun playSingleItem(item: PlayableItem) {
        Logger.d("MediaPlaybackManager.playSingleItem - itemId: ${item.id}, isLocal: ${item.isLocal}")
        val playbackType = if (item.isLocal) PlaybackType.LOCAL else PlaybackType.SINGLE
        nowPlayingStateHolder.setPlaybackType(playbackType)
        Logger.d("MediaPlaybackManager.playSingleItem - playbackType set to: $playbackType")

        val ctrl = mediaControllerFlow.value
        if (ctrl == null) {
            Logger.e("MediaPlaybackManager.playSingleItem - mediaController is NULL!")
            return
        }
        val isSameItem = (ctrl.currentMediaItem?.mediaId == item.id)
        Logger.d("MediaPlaybackManager.playSingleItem - isSameItem: $isSameItem, currentMediaId: ${ctrl.currentMediaItem?.mediaId}")

        if (isSameItem) {
            Logger.d("MediaPlaybackManager.playSingleItem - same item, toggling play/pause")
            if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
            return
        }

        Logger.d("MediaPlaybackManager.playSingleItem - calling prepareAndPlayItems")
        prepareAndPlayItems(item, emptyList(), 0)
    }

    fun playPlaylist(playlistItems: List<Video>, startIndex: Int = 0) {
        val items = playlistItems.map { PlayableItem.Remote(it) }
        playPlaylistItems(items, startIndex)
    }

    fun playPlaylistItems(playlistItems: List<PlayableItem>, startIndex: Int = 0) {
        nowPlayingStateHolder.setPlaybackType(PlaybackType.PLAYLIST)

        val ctrl = mediaControllerFlow.value ?: return
        val targetItemId = playlistItems[startIndex].id
        val isSameItem = (ctrl.currentMediaItem?.mediaId == targetItemId)

        if (isSameItem) {
            if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
            return
        }

        val existingItemAtIndex = if (startIndex < ctrl.mediaItemCount) {
            ctrl.getMediaItemAt(startIndex)
        } else null

        if (existingItemAtIndex?.mediaId == targetItemId) {
            Logger.d("Item already in player at index $startIndex, using seekTo")
            ctrl.seekTo(startIndex, 0L)
            ctrl.play()
            return
        }

        prepareAndPlayItems(null, playlistItems, startIndex)
    }

    fun playLocalFile(localFile: LocalFileData) {
        playSingleItem(PlayableItem.Local(localFile))
    }

    private fun prepareAndPlayItems(
        singleItem: PlayableItem?,
        playlistItems: List<PlayableItem>,
        startIndex: Int = 0
    ) {
        Logger.d("prepareAndPlayItems START - singleItem: ${singleItem?.id}, playlistSize: ${playlistItems.size}")
        val ctrl = mediaControllerFlow.value
        if (ctrl == null) {
            Logger.e("prepareAndPlayItems - mediaController is NULL!")
            return
        }

        Logger.d("prepareAndPlayItems - calling clearCurrentPlayback")
        clearCurrentPlayback()
        scope.launch {
            val playbackType = nowPlayingStateHolder.playbackType.value
            Logger.d("prepareAndPlayItems - playbackType: $playbackType")
            when (playbackType) {
                PlaybackType.SINGLE, PlaybackType.LOCAL -> {
                    val mediaItem = withContext(defaultDispatcher) {
                        singleItem!!.let {
                            mediaItemCache[it.id] = it
                            createMediaItemFromPlayable(it)
                        }
                    }

                    withContext(mainDispatcher) {
                        ctrl.setMediaItem(mediaItem)
                        nowPlayingStateHolder.updateSingleTrack(singleItem)
                        ctrl.prepare()
                        ctrl.play()
                    }
                }

                PlaybackType.PLAYLIST -> {
                    val mediaItems = withContext(defaultDispatcher) {
                        playlistItems.forEach { item ->
                            mediaItemCache[item.id] = item
                        }
                        createMediaItemsFromPlayable(playlistItems)
                    }

                    withContext(mainDispatcher) {
                        ctrl.setMediaItems(mediaItems, startIndex, 0L)
                        nowPlayingStateHolder.updatePlaylistTrack(
                            item = playlistItems[startIndex],
                            playlist = playlistItems,
                            index = startIndex
                        )
                        ctrl.prepare()
                        ctrl.play()
                    }
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

    private fun createMediaItemFromPlayable(item: PlayableItem): MediaItem {
        return when (item) {
            is PlayableItem.Remote -> createMediaItem(item.video)
            is PlayableItem.Local -> createLocalMediaItem(item.localFile)
        }
    }

    private fun createMediaItemsFromPlayable(items: List<PlayableItem>): List<MediaItem> {
        return items.map { createMediaItemFromPlayable(it) }
    }

    private fun createLocalMediaItem(localFile: LocalFileData): MediaItem {
        return MediaItem.Builder()
            .setMediaId("local_${localFile.id}")
            .setUri(localFile.uri)
            .setTag(localFile)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(localFile.title)
                    .setArtist(localFile.artist ?: "Unknown Artist")
                    .setAlbumTitle(localFile.album)
                    .build()
            )
            .build()
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