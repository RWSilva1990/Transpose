package com.example.transpose

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.example.domain.repository.NewPipeRepository
import com.example.media.MediaService
import com.example.media.audio_effect.data.equalizer.EqualizerPresets
import com.example.media.audio_effect.data.equalizer.EqualizerSettings
import com.example.media.audio_effect.data.reverb.ReverbPresets
import com.example.transpose.utils.Logger
import com.example.transpose.utils.constants.MediaSessionCallback
import com.example.ui.common.VideoPlayerUiState
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.log2
import kotlin.math.roundToInt

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val application: Application,
    private val newPipeRepository: NewPipeRepository,
) : AndroidViewModel(application) {

    private val _mediaController = MutableStateFlow<MediaController?>(null)
    val mediaController = _mediaController.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _isShowingThumbnail = MutableStateFlow(false)
    val isShowingThumbnail = _isShowingThumbnail.asStateFlow()

    private val _isShowingLoadingIndicator = MutableStateFlow(false)
    val isShowingLoadingIndicator = _isShowingLoadingIndicator.asStateFlow()

    init {
        initializeMediaController()
    }

    private fun initializeMediaController() {
        val sessionToken =
            SessionToken(application, ComponentName(application, MediaService::class.java))
        val controllerFuture = MediaController.Builder(application, sessionToken)
            .buildAsync()
        controllerFuture.addListener(
            {
                _mediaController.value = controllerFuture.get()
                _mediaController.value?.addListener(playerListener)
                updatePlaybackState()
            },
            MoreExecutors.directExecutor()
        )
    }

    private val playerListener = object : Player.Listener {

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            super.onPlaybackParametersChanged(playbackParameters)

            val currentPitch = playbackParameters.pitch

            val semitonesPitch = 12 * log2(currentPitch.toDouble())

            val pitchValue = ((semitonesPitch * 10) + 100).roundToInt().coerceIn(0, 200)
            _pitchValue.value = pitchValue

            val currentTempo = playbackParameters.speed

            val semitonesTempo = 12 * log2(currentTempo.toDouble())

            val tempoValue = ((semitonesTempo * 10) + 100).roundToInt().coerceIn(0, 200)
            _tempoValue.value = tempoValue

        }


        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)

            var basicVideoData: BasicVideoData? = null
            mediaItem?.localConfiguration?.let { localConfiguration ->
                basicVideoData = localConfiguration.tag as? BasicVideoData
            }

            mediaItem?.let {
                handleMediaItemTransition(basicVideoData)
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            super.onMediaMetadataChanged(mediaMetadata)
        }

        override fun onTracksChanged(tracks: Tracks) {
            super.onTracksChanged(tracks)
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            Logger.e("PlaybackError: ${error.message}")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            updatePlaybackState()

            if (playbackState == Player.STATE_ENDED) {
                handleTrackEnded()
            }
        }
    }

    private fun handleTrackEnded() {
        viewModelScope.launch {
            val nextIndex = (mediaController.value?.currentMediaItemIndex ?: -1) + 1
            if (nextIndex < (mediaController.value?.mediaItemCount ?: 0)) {
                // 다음 트랙이 있으면 자동으로 재생
                mediaController.value?.seekToNext()
                mediaController.value?.play()
            } else {
                // 플레이리스트의 마지막 트랙이면 재생 종료 또는 루프 설정에 따라 처리
                // 예: 처음으로 돌아가기
                mediaController.value?.seekTo(0, 0)
                mediaController.value?.pause()
            }
        }
    }

    private fun handleMediaItemTransition(basicVideoData: BasicVideoData?) {
        viewModelScope.launch {
            basicVideoData?.let {
                _currentVideoItemState.value = VideoPlayerUiState.BasicDataLoaded(basicVideoData)
                loadFullItemInfo(basicVideoData.id)
            }
        }
    }

    private fun updatePlaybackState() {
        val controller = _mediaController.value ?: return
        _isPlaying.value = controller.isPlaying
        _duration.value = controller.duration
        _currentPosition.value = controller.currentPosition
    }

    private var lastProcessedMediaId: String? = null
    private val preloadedItems = mutableSetOf<String>()

    private val _currentPlayingIndex = MutableStateFlow<Int?>(null)
    val currentPlayingIndex: StateFlow<Int?> = _currentPlayingIndex


    private val _currentVideoItemState =
        MutableStateFlow<VideoPlayerUiState>(VideoPlayerUiState.Initial)
    val currentVideoItemState = _currentVideoItemState.asStateFlow()


    fun onMediaItemClick(
        clickedItem: BasicVideoData,
        playlistItems: List<BasicVideoData>? = null,
        clickedIndex: Int = 0
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val isPlaylist = playlistItems != null

                // 현재 재생 중인 아이템과 비교
                val isSameItem = mediaController.value?.currentMediaItem?.mediaId == clickedItem.id

                if (isSameItem) {
                    // 같은 아이템을 클릭한 경우 재생/일시정지 토글
                    mediaController.value?.let { controller ->
                        if (controller.isPlaying) controller.pause() else controller.play()
                    }
                    return@launch
                }

                // 새로운 아이템 또는 플레이리스트 재생 준비
                clearCurrentPlayback()

                _currentVideoItemState.value = VideoPlayerUiState.BasicDataLoaded(clickedItem)

                if (isPlaylist) {
                    // 플레이리스트 처리
                    loadPlaylistItems(playlistItems!!, clickedIndex)
                } else {
                    // 단일 아이템 처리
                    val mediaItem = createMediaItem(clickedItem)
                    mediaController.value?.setMediaItem(mediaItem)
                }

                mediaController.value?.prepare()
                mediaController.value?.play()

                loadFullItemInfo(clickedItem.id)
            } catch (e: Exception) {
                Logger.d("onMediaItemClick $e")
            }

        }
    }

    private fun clearCurrentPlayback() {
        mediaController.value?.apply {
            stop()
            clearMediaItems()
        }
        _currentVideoItemState.value = VideoPlayerUiState.Initial
        _currentPlayingIndex.value = null
        lastProcessedMediaId = null
        // 기타 필요한 초기화...
    }

    private fun loadPlaylistItems(items: List<BasicVideoData>, clickedIndex: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            val initialLoadSize = 10
            val startLoadIndex = (clickedIndex - initialLoadSize / 2).coerceAtLeast(0)
            val endLoadIndex = (startLoadIndex + initialLoadSize).coerceAtMost(items.size)

            val initialItems = items.subList(startLoadIndex, endLoadIndex)
                .map { createMediaItem(it) }

            mediaController.value?.setMediaItems(initialItems, clickedIndex - startLoadIndex, 0)

            launch(Dispatchers.Default) {
                val precedingItems = items.subList(0, startLoadIndex)
                val followingItems = items.subList(endLoadIndex, items.size)
                loadRemainingItems(precedingItems, followingItems)
            }
        }
    }

    private fun createMediaItem(basicVideoData: BasicVideoData): MediaItem {
        val uri = Uri.parse("asset:///15-seconds-of-silence.mp3")
        return MediaItem.Builder()
            .setMediaId(basicVideoData.id)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(basicVideoData.title)
                    .setArtist(basicVideoData.uploaderName ?: "Unknown Uploader")
                    .setArtworkUri(Uri.parse(basicVideoData.thumbnailUrl))
                    .build()
            )
            .build()
    }

    private suspend fun loadRemainingItems(
        precedingItems: List<BasicVideoData>,
        followingItems: List<BasicVideoData>
    ) {
        val batchSize = 10

        // 앞쪽 아이템 로드
        precedingItems.chunked(batchSize).forEach { batch ->
            val mediaItems = batch.map { createMediaItem(it) }
            withContext(Dispatchers.Main) {
                mediaController.value?.addMediaItems(0, mediaItems)
            }
        }

        // 뒤쪽 아이템 로드
        followingItems.chunked(batchSize).forEach { batch ->
            val mediaItems = batch.map { createMediaItem(it) }
            withContext(Dispatchers.Main) {
                mediaController.value?.addMediaItems(mediaItems)
            }
        }
    }

    private suspend fun loadFullItemInfo(videoId: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val result = newPipeRepository.fetchVideoDetail(videoId)
            if (result.isSuccess) {
                val videoDetailResult = result.getOrNull()
                videoDetailResult?.let { videoDetail ->
                    _currentVideoItemState.value =
                        VideoPlayerUiState.DetailedDataLoaded(videoDetail)
                    withContext(Dispatchers.Main) {
                        updateMediaItemWithFullInfo(videoId, videoDetail)
                    }
                }
            } else {
                _currentVideoItemState.value =
                    VideoPlayerUiState.LoadError(result.exceptionOrNull()?.message)
            }
        } catch (e: Exception) {
            _currentVideoItemState.value = VideoPlayerUiState.LoadError(e.message)
        }
    }

    private fun updateMediaItemWithFullInfo(itemId: String, streamInfo: VideoDetailData) {
        val selectedVideoStream = streamInfo.videoStream
        if (selectedVideoStream != null) {
            val currentIndex = mediaController.value?.currentMediaItemIndex ?: 0
            val currentItem = mediaController.value?.getMediaItemAt(currentIndex)
            if (currentItem?.mediaId == itemId) {
                val updatedMediaItem = currentItem.buildUpon()
                    .setUri(selectedVideoStream.content)
                    .build()
                mediaController.value?.replaceMediaItem(currentIndex, updatedMediaItem)
            }
        }
    }

//    fun changeResolution(resolution: String) {
//        val selectedVideoStream = currentVideoStreams?.find { it.getResolution() == resolution }
//        val currentItem = mediaController.value?.currentMediaItem
//
//
//        if (selectedVideoStream != null && currentItem != null) {
//            val currentPosition = mediaController.value?.currentPosition ?: 0
//
//            val newMediaItem = MediaItem.Builder()
//                .setMediaId(currentItem.mediaId)
//                .setUri(selectedVideoStream.content)
//                .setMediaMetadata(currentItem.mediaMetadata)
//                .setTag(Bundle().apply {
//                    putString("audioUrl", currentAudioStream?.content)
//                })
//                .build()
//
//            mediaController.value?.setMediaItem(newMediaItem, currentPosition)
//            mediaController.value?.prepare()
//            mediaController.value?.play()
//        }
//    }

    fun playPause() {
        if (isPlaying.value)
            mediaController.value?.pause()
        else
            mediaController.value?.play()
    }


    private val _pitchValue = MutableStateFlow(100)
    val pitchValue = _pitchValue.asStateFlow()

    private val _tempoValue = MutableStateFlow(100)
    val tempoValue = _tempoValue.asStateFlow()

    fun updatePitchValue(value: Int) {
        _pitchValue.value = value
    }

    fun initPitchValue() {
        _pitchValue.value = 100
        setPitch()
    }

    fun pitchPlusOne() {
        val action = MediaSessionCallback.PITCH_PLUS
        val bundle = Bundle()
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    fun pitchMinusOne() {
        val action = MediaSessionCallback.PITCH_MINUS
        val bundle = Bundle()
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    fun setPitch() {
        val action = MediaSessionCallback.SET_PITCH
        val bundle = Bundle().apply {
            putInt("value", pitchValue.value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }


    fun updateTempoValue(value: Int) {
        _tempoValue.value = value
    }

    fun initTempoValue() {
        _tempoValue.value = 100
        setTempo()
    }

    fun tempoPlusOne() {
        val action = MediaSessionCallback.TEMPO_PLUS
        val bundle = Bundle()
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    fun tempoMinusOne() {
        val action = MediaSessionCallback.TEMPO_MINUS
        val bundle = Bundle()
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    fun setTempo() {
        val action = MediaSessionCallback.SET_TEMPO
        val bundle = Bundle().apply {
            putInt("value", tempoValue.value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    private val _isEqualizerEnabled = MutableStateFlow(false)
    val isEqualizerEnabled = _isEqualizerEnabled.asStateFlow()

    private val _equalizerCurrentPreset = MutableStateFlow(EqualizerPresets.PRESET_DEFAULT)
    val equalizerCurrentPreset = _equalizerCurrentPreset.asStateFlow()

    private val _equalizerSettings = MutableStateFlow(EqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings

    fun updateIsEqualizerEnabled() {
        if (isEqualizerEnabled.value)
            initEqualizerValue()
        _isEqualizerEnabled.value = !isEqualizerEnabled.value
    }

    fun initEqualizerValue() {
        updateEqualizerWithPreset(EqualizerPresets.PRESET_DEFAULT)
    }

    fun updateEqualizerWithPreset(presetIndex: Int) {
        _equalizerCurrentPreset.value = presetIndex
        val presetValues = EqualizerPresets.getPresetGainValues(presetIndex)
        _equalizerSettings.value = EqualizerSettings(
            bandLevels = presetValues.map { it.toFloat() },
            presetName = EqualizerPresets.effectTypes[presetIndex]
        )
        setEqualizerWithPreset()
    }

    private fun setEqualizerWithPreset() {
        if (!isEqualizerEnabled.value) return

        val action = MediaSessionCallback.SET_EQUALIZER_PRESET
        val bundle = Bundle().apply {
            putInt("value", equalizerCurrentPreset.value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    fun disableEqualizer() {
        if (isEqualizerEnabled.value) return
        val action = MediaSessionCallback.DISABLE_EQUALIZER
        val bundle = Bundle()
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    fun setEqualizerWithCustomValue(changedBand: Int) {
        if (!isEqualizerEnabled.value) return

        val action = MediaSessionCallback.SET_EQUALIZER_CUSTOM
        val bundle = Bundle().apply {
            putInt("band", changedBand)
            putInt("level", equalizerSettings.value.bandLevels[changedBand].toInt())
        }
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    fun updateEqualizerBandLevel(index: Int, newValue: Float) {
        _equalizerSettings.update { currentSettings ->
            currentSettings.withUpdatedBandLevel(index, newValue)
        }
        _equalizerCurrentPreset.value = EqualizerPresets.PRESET_DEFAULT
    }

    private val _isReverbEnabled = MutableStateFlow(false)
    val isReverbEnabled = _isReverbEnabled.asStateFlow()

    private val _reverbCurrentPreset = MutableStateFlow(ReverbPresets.PRESET_NONE)
    val reverbCurrentPreset = _reverbCurrentPreset.asStateFlow()

    private val _reverbValue = MutableStateFlow(0)
    val reverbValue = _reverbValue.asStateFlow()

    fun updateIsReverbEnabled() {
        if (isReverbEnabled.value) {
            _reverbCurrentPreset.value = ReverbPresets.PRESET_NONE
            initReverbValue()
        }
        _isReverbEnabled.value = !isReverbEnabled.value
    }

    fun updateReverbCurrentPreset(presetIndex: Int) {
        _reverbCurrentPreset.value = presetIndex
        setPresetReverb()
    }

    fun updateReverbValue(value: Int) {
        _reverbValue.value = value
    }

    fun initReverbValue() {
        _reverbValue.value = 0
        setPresetReverb()
    }

    fun setPresetReverb() {
        if (!isReverbEnabled.value) return

        val action = MediaSessionCallback.SET_REVERB
        val bundle = Bundle().apply {
            putInt("presetIndex", reverbCurrentPreset.value)
            putInt("sendLevel", reverbValue.value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    fun disablePreset() {
        if (isReverbEnabled.value) return
        val action = MediaSessionCallback.DISABLE_REVERB
        val bundle = Bundle()
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    private val _bassBoostValue = MutableStateFlow(0)
    val bassBoostValue = _bassBoostValue.asStateFlow()

    fun updateBassBoostValue(value: Int) {
        _bassBoostValue.value = value

    }

    fun initBassBoostValue() {
        _bassBoostValue.value = 0
        setBassBoost()
    }

    fun setBassBoost() {
        val action = MediaSessionCallback.SET_BASS_BOOST
        val bundle = Bundle().apply {
            putInt("value", bassBoostValue.value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    private val _loudnessEnhancerValue = MutableStateFlow(0)
    val loudnessEnhancerValue = _loudnessEnhancerValue.asStateFlow()

    fun updateLoudnessEnhancerValue(value: Int) {
        _loudnessEnhancerValue.value = value
    }

    fun initLoudnessEnhancerValue() {
        _loudnessEnhancerValue.value = 0
        setLoudnessEnhancer()
    }

    fun setLoudnessEnhancer() {
        val action = MediaSessionCallback.SET_LOUDNESS_ENHANCER
        val bundle = Bundle().apply {
            putInt("value", loudnessEnhancerValue.value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }


    private val _virtualizerValue = MutableStateFlow(0)
    val virtualizerValue = _virtualizerValue.asStateFlow()

    fun updateVirtualizerValue(value: Int) {
        _virtualizerValue.value = value
        setVirtualizer()
    }

    fun initVirtualizerValue() {
        _virtualizerValue.value = 0
        setVirtualizer()
    }

    fun setVirtualizer() {
        val action = MediaSessionCallback.SET_VIRTUALIZER
        val bundle = Bundle().apply {
            putInt("value", virtualizerValue.value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    private val _isHapticGeneratorEnabled = MutableStateFlow(false)
    val isHapticGeneratorEnabled = _isHapticGeneratorEnabled.asStateFlow()

    private fun setHapticGenerator() {
        val action = MediaSessionCallback.SET_HAPTIC_GENERATOR
        val bundle = Bundle().apply {
            putBoolean("isEnabled", isHapticGeneratorEnabled.value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    fun updateIsHapticGenerator() {
        if (isHapticGeneratorEnabled.value) {
            setHapticGenerator()
        }
        _isHapticGeneratorEnabled.value = !isHapticGeneratorEnabled.value
    }


    private val _isEnvironmentalReverbEnabled = MutableStateFlow(false)
    val isEnvironmentalReverbEnabled = _isEnvironmentalReverbEnabled.asStateFlow()

    private val _roomLevel = MutableStateFlow(0)
    val roomLevel = _roomLevel.asStateFlow()

    private val _roomHFLevel = MutableStateFlow(0)
    val roomHFLevel = _roomHFLevel.asStateFlow()

    private val _decayTime = MutableStateFlow(0)
    val decayTime = _decayTime.asStateFlow()

    private val _decayHFRatio = MutableStateFlow(0)
    val decayHFRatio = _decayHFRatio.asStateFlow()

    private val _reflectionsLevel = MutableStateFlow(0)
    val reflectionsLevel = _reflectionsLevel.asStateFlow()

    private val _reflectionsDelay = MutableStateFlow(0)
    val reflectionsDelay = _reflectionsDelay.asStateFlow()

    private val _reverbLevel = MutableStateFlow(0)
    val reverbLevel = _reverbLevel.asStateFlow()

    private val _reverbDelay = MutableStateFlow(0)
    val reverbDelay = _reverbDelay.asStateFlow()

    private val _diffusion = MutableStateFlow(0)
    val diffusion = _diffusion.asStateFlow()

    private val _density = MutableStateFlow(0)
    val density = _density.asStateFlow()

    fun updateIsEnvironmentalReverbEnabled(isEnabled: Boolean) {
        _isEnvironmentalReverbEnabled.value = isEnabled
        setEnvironmentalReverb()
    }

    fun updateRoomLevel(value: Int) {
        _roomLevel.value = value
    }

    fun updateRoomHFLevel(value: Int) {
        _roomHFLevel.value = value
    }

    fun updateDecayTime(value: Int) {
        _decayTime.value = value
    }

    fun updateDecayHFRatio(value: Int) {
        _decayHFRatio.value = value
    }

    fun updateReflectionsLevel(value: Int) {
        _reflectionsLevel.value = value
    }

    fun updateReflectionsDelay(value: Int) {
        _reflectionsDelay.value = value
    }

    fun updateReverbLevel(value: Int) {
        _reverbLevel.value = value
    }

    fun updateReverbDelay(value: Int) {
        _reverbDelay.value = value
    }

    fun updateDiffusion(value: Int) {
        _diffusion.value = value

    }

    fun updateDensity(value: Int) {
        _density.value = value
    }

    fun initEnvironmentalReverbValues() {
        _roomLevel.value = 0
        _roomHFLevel.value = 0
        _decayTime.value = 0
        _decayHFRatio.value = 0
        _reflectionsLevel.value = 0
        _reflectionsDelay.value = 0
        _reverbLevel.value = 0
        _reverbDelay.value = 0
        _diffusion.value = 0
        _density.value = 0
        setEnvironmentalReverb()
    }

    fun initRoomLevel() {
        _roomLevel.value = 0
        setEnvironmentalReverb()
    }

    fun initRoomHFLevel() {
        _roomHFLevel.value = 0
        setEnvironmentalReverb()
    }

    fun initDecayTime() {
        _decayTime.value = 0
        setEnvironmentalReverb()
    }

    fun initDecayHFRatio() {
        _decayHFRatio.value = 0
        setEnvironmentalReverb()
    }

    fun initReflectionsLevel() {
        _reflectionsLevel.value = 0
        setEnvironmentalReverb()
    }

    fun initReflectionsDelay() {
        _reflectionsDelay.value = 0
        setEnvironmentalReverb()
    }

    fun initReverbLevel() {
        _reverbLevel.value = 0
        setEnvironmentalReverb()
    }

    fun initReverbDelay() {
        _reverbDelay.value = 0
        setEnvironmentalReverb()
    }

    fun initDiffusion() {
        _diffusion.value = 0
        setEnvironmentalReverb()
    }

    fun initDensity() {
        _density.value = 0
        setEnvironmentalReverb()
    }

    fun setEnvironmentalReverb() {
        val action = MediaSessionCallback.SET_ENVIRONMENT_REVERB
        val bundle = Bundle().apply {
            putBoolean("isEnabled", isEnvironmentalReverbEnabled.value)
            putInt("roomLevel", roomLevel.value)
            putInt("roomHFLevel", roomHFLevel.value)
            putInt("decayTime", decayTime.value)
            putInt("decayHFRatio", decayHFRatio.value)
            putInt("reflectionsLevel", reflectionsLevel.value)
            putInt("reflectionsDelay", reflectionsDelay.value)
            putInt("reverbLevel", reverbLevel.value)
            putInt("reverbDelay", reverbDelay.value)
            putInt("diffusion", diffusion.value)
            putInt("density", density.value)
        }
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
    }

    fun disableEnvironmentalReverb() {
        _isEnvironmentalReverbEnabled.value = false
        setEnvironmentalReverb()
    }

    fun releaseMediaController() {
        viewModelScope.launch {
            mediaController.value?.let { controller ->
                controller.release()
                _mediaController.value = null
            }
            // MediaService 종료
            application.stopService(Intent(application, MediaService::class.java))
        }
    }


}