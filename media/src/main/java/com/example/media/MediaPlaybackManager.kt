package com.example.media

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.example.domain.model.youtube.PlayableVideo
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.media.audio_effect.data.equalizer.EqualizerPresets
import com.example.media.audio_effect.data.equalizer.EqualizerSettings
import com.example.media.audio_effect.data.reverb.ReverbPresets
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt


class MediaPlaybackManager @Inject constructor(
    private val context: Context,
) {

    private val _mediaController = MutableStateFlow<MediaController?>(null)
    val mediaController: StateFlow<MediaController?> = _mediaController.asStateFlow()

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

    // 초기화 블록
    init {
        initializeMediaController()
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(context, ComponentName(context, MediaService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken)
            .buildAsync()
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            _mediaController.value = controller
            controller.addListener(playerListener)
            updatePlaybackState(controller)
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            super.onPlaybackParametersChanged(playbackParameters)
            updatePitchTempo(playbackParameters)
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
            val controller = _mediaController.value ?: return
            updatePlaybackState(controller)

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
            val controller = _mediaController.value ?: return@launch
            val nextIndex = controller.currentMediaItemIndex + 1
            if (nextIndex < controller.mediaItemCount) {
                controller.seekToNext()
                controller.play()

                _currentPlaylistIndex.value = nextIndex

            } else {
                controller.seekTo(0, 0)
                controller.pause()
            }
        }
    }

    private fun handleMediaItemTransition(mediaItem: MediaItem?) {
        var metadata: BasicVideoData? = null
        mediaItem?.localConfiguration?.let { localConfiguration ->
            metadata = localConfiguration.tag as? BasicVideoData
        }
        updateUiForPlayingMediaItem(metadata)
    }

    private fun updateUiForPlayingMediaItem(metadata: BasicVideoData?){
        _currentVideoData.value = metadata

    }


    fun onMediaItemClick(
        clickedItem: PlayableVideo,
        playlistItems: List<PlayableVideo>? = null,
        clickedIndex: Int = 0
    ) {
        // UI에서 호출 -> 도메인 매니저가 컨트롤러를 통해 아이템을 세팅
        CoroutineScope(Dispatchers.Main).launch {
            val controller = _mediaController.value ?: return@launch
            val isSameItem = controller.currentMediaItem?.mediaId == clickedItem.id
            if (isSameItem) {
                // 같은 아이템이면 play/pause 토글
                if (controller.isPlaying) controller.pause() else controller.play()
                return@launch
            }

            clearCurrentPlayback(controller)

            // playlist or single item
            if (playlistItems != null) {
                val mediaItems = createMediaItems(playlistItems)
                controller.setMediaItems(mediaItems, clickedIndex, 0L)

                _currentPlaylist.value = playlistItems
                _currentPlaylistIndex.value = clickedIndex

            } else {
                val mediaItem = createMediaItem(clickedItem)
                controller.setMediaItem(mediaItem)

                _currentPlaylist.value = null
                _currentPlaylistIndex.value = -1
            }
            controller.prepare()
            controller.play()

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

    private fun createMediaItems(playableVideos: List<PlayableVideo>): List<MediaItem> {
        val uri = Uri.parse("asset:///15-seconds-of-silence.mp3")
        return playableVideos.map {
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
        val ctrl = _mediaController.value ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }


    private val _pitchValue = MutableStateFlow(100)
    val pitchValue: StateFlow<Int> = _pitchValue.asStateFlow()

    private val _tempoValue = MutableStateFlow(100)
    val tempoValue: StateFlow<Int> = _tempoValue.asStateFlow()

    private fun updatePitchTempo(playbackParameters: PlaybackParameters) {
        val currentPitch = playbackParameters.pitch
        val currentTempo = playbackParameters.speed
        // 예: pitch/tempo 계산 후 Flow에 반영
        val pitch = calculateSemitoneValue(currentPitch)    // 0~200
        val tempo = calculateSemitoneValue(currentTempo)    // 0~200
        _pitchValue.value = pitch
        _tempoValue.value = tempo
    }

    private fun calculateSemitoneValue(rate: Float): Int {
        val semitones = 12 * kotlin.math.log2(rate.toDouble())
        return ((semitones * 10) + 100).roundToInt().coerceIn(0, 200)
    }

    fun setPitch(value: Int) {
        _pitchValue.value = value
        sendSessionCommand(MediaSessionCallback.SET_PITCH, Bundle().apply {
            putInt("value", value)
        })
    }

    fun initPitchValue() {
        val action = MediaSessionCallback.INIT_PITCH_VALUE
        val bundle = Bundle()
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
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

    fun initTempoValue() {
        val action = MediaSessionCallback.INIT_TEMPO_VALUE
        val bundle = Bundle()
        val sessionCommand = SessionCommand(action, bundle)
        mediaController.value?.sendCustomCommand(sessionCommand, bundle)
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

    private val _isEqualizerEnabled = MutableStateFlow(false)
    val isEqualizerEnabled: StateFlow<Boolean> = _isEqualizerEnabled.asStateFlow()

    private val _equalizerCurrentPreset = MutableStateFlow(EqualizerPresets.PRESET_DEFAULT)
    val equalizerCurrentPreset: StateFlow<Int> = _equalizerCurrentPreset.asStateFlow()

    private val _equalizerSettings = MutableStateFlow(EqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings

    fun updateIsEqualizerEnabled() {
        if (_isEqualizerEnabled.value) initEqualizerValue()
        _isEqualizerEnabled.value = !_isEqualizerEnabled.value
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
        if (!_isEqualizerEnabled.value) return
        sendSessionCommand(MediaSessionCallback.SET_EQUALIZER_PRESET, Bundle().apply {
            putInt("value", _equalizerCurrentPreset.value)
        })
    }

    fun disableEqualizer() {
        if (!_isEqualizerEnabled.value) return
        sendSessionCommand(MediaSessionCallback.DISABLE_EQUALIZER, Bundle())
    }

    fun setEqualizerWithCustomValue(changedBand: Int) {
        if (!_isEqualizerEnabled.value) return
        val bandLevel = equalizerSettings.value.bandLevels[changedBand].toInt()
        sendSessionCommand(MediaSessionCallback.SET_EQUALIZER_CUSTOM, Bundle().apply {
            putInt("band", changedBand)
            putInt("level", bandLevel)
        })
    }

    fun updateEqualizerBandLevel(index: Int, newValue: Float) {
        _equalizerSettings.update { current ->
            current.withUpdatedBandLevel(index, newValue)
        }
        // preset을 DEFAULT로 되돌린다거나...
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


    private fun sendSessionCommand(action: String, bundle: Bundle) {
        val ctrl = _mediaController.value ?: return
        val sessionCommand = SessionCommand(action, bundle)
        ctrl.sendCustomCommand(sessionCommand, bundle)
    }


    fun release() {
        CoroutineScope(Dispatchers.Main).launch {
            _mediaController.value?.let { c ->
                c.removeListener(playerListener)
                c.release()
            }
            _mediaController.value = null
        }
    }
}
