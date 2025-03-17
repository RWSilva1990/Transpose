package com.example.media.manager

import android.os.Bundle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import com.example.media.MediaSessionCallback
import com.example.media.audio_effect.data.equalizer.EqualizerPresets
import com.example.media.audio_effect.data.equalizer.EqualizerSettings
import com.example.media.audio_effect.data.reverb.ReverbPresets
import com.example.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEffectsManager @Inject constructor(
    private val controllerProvider: MediaControllerProvider
) {

    private val mediaController: MediaController?
        get() = controllerProvider.mediaController.value

    // =========================
    // Pitch / Tempo
    // =========================

    private val _pitchValue = MutableStateFlow(100)
    val pitchValue: StateFlow<Int> = _pitchValue.asStateFlow()

    private val _tempoValue = MutableStateFlow(100)
    val tempoValue: StateFlow<Int> = _tempoValue.asStateFlow()

    fun updatePitchValue(value: Int) {
        _pitchValue.value = value
    }

    fun setPitch() {
        sendSessionCommand(MediaSessionCallback.SET_PITCH, Bundle().apply {
            putInt("value", pitchValue.value)
        })
    }

    fun initPitchValue() {
        sendSessionAction(MediaSessionCallback.INIT_PITCH_VALUE)
        _pitchValue.value = 100
    }

    fun pitchPlusOne() {
        sendSessionAction(MediaSessionCallback.PITCH_PLUS)
        _pitchValue.update { it + 10 }

    }

    fun pitchMinusOne() {
        sendSessionAction(MediaSessionCallback.PITCH_MINUS)
        _pitchValue.update { it - 10 }

    }

    fun setTempo() {
        sendSessionCommand(MediaSessionCallback.SET_TEMPO, Bundle().apply {
            putInt("value", tempoValue.value)
        })
    }

    fun updateTempoValue(value: Int) {
        _tempoValue.value = value
    }

    fun initTempoValue() {
        sendSessionAction(MediaSessionCallback.INIT_TEMPO_VALUE)
        _tempoValue.update { 100 }
    }

    fun tempoPlusOne() {
        sendSessionAction(MediaSessionCallback.TEMPO_PLUS)
        _tempoValue.update { it + 10 }
    }

    fun tempoMinusOne() {
        sendSessionAction(MediaSessionCallback.TEMPO_MINUS)
        _tempoValue.update { it - 10 }
    }


    // =========================
    // Equalizer
    // =========================

    private val _isEqualizerEnabled = MutableStateFlow(false)
    val isEqualizerEnabled: StateFlow<Boolean> = _isEqualizerEnabled.asStateFlow()

    private val _equalizerCurrentPreset = MutableStateFlow(EqualizerPresets.PRESET_DEFAULT)
    val equalizerCurrentPreset: StateFlow<Int> = _equalizerCurrentPreset.asStateFlow()

    private val _equalizerSettings = MutableStateFlow(EqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    fun updateIsEqualizerEnabled() {
        if (_isEqualizerEnabled.value) {
            // EQ를 끄기 전에 init (원래 로직 맞춤)
            initEqualizerValue()
        }
        _isEqualizerEnabled.value = !_isEqualizerEnabled.value
    }

    fun initEqualizerValue() {
        // Preset DEFAULT로 세팅
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
        sendSessionCommand(
            MediaSessionCallback.SET_EQUALIZER_PRESET,
            Bundle().apply { putInt("value", _equalizerCurrentPreset.value) }
        )
    }

    fun disableEqualizer() {
        if (!_isEqualizerEnabled.value) return
        sendSessionAction(MediaSessionCallback.DISABLE_EQUALIZER)
        _isEqualizerEnabled.value = false
    }

    fun setEqualizerWithCustomValue(changedBand: Int) {
        if (!_isEqualizerEnabled.value) return
        val bandLevel = equalizerSettings.value.bandLevels[changedBand].toInt()
        sendSessionCommand(
            MediaSessionCallback.SET_EQUALIZER_CUSTOM,
            Bundle().apply {
                putInt("band", changedBand)
                putInt("level", bandLevel)
            }
        )
    }

    fun updateEqualizerBandLevel(index: Int, newValue: Float) {
        _equalizerSettings.update { current ->
            current.withUpdatedBandLevel(index, newValue)
        }
        // preset을 DEFAULT로 되돌리는 로직
        _equalizerCurrentPreset.value = EqualizerPresets.PRESET_DEFAULT
    }


    // =========================
    // Reverb
    // =========================

    private val _isReverbEnabled = MutableStateFlow(false)
    val isReverbEnabled: StateFlow<Boolean> = _isReverbEnabled.asStateFlow()

    private val _reverbCurrentPreset = MutableStateFlow(ReverbPresets.PRESET_NONE)
    val reverbCurrentPreset: StateFlow<Int> = _reverbCurrentPreset.asStateFlow()

    private val _reverbValue = MutableStateFlow(0)
    val reverbValue: StateFlow<Int> = _reverbValue.asStateFlow()

    fun updateIsReverbEnabled() {
        // 토글
        if (_isReverbEnabled.value) {
            // 꺼질 때 => disable
            disableReverb()
        } else {
            _isReverbEnabled.value = true
            setPresetReverb()
        }
    }

    fun updateReverbCurrentPreset(presetIndex: Int) {
        _reverbCurrentPreset.value = presetIndex
        setPresetReverb()
    }

    fun initReverbValue() {
        _reverbValue.value = 0
        setPresetReverb()
    }

    fun updateReverbValue(value: Int) {
        _reverbValue.value = value
    }


    fun setPresetReverb() {
        if (!_isReverbEnabled.value) return
        val action = MediaSessionCallback.SET_REVERB
        val bundle = Bundle().apply {
            putInt("presetIndex", _reverbCurrentPreset.value)
            putInt("sendLevel", _reverbValue.value)
        }
        sendSessionCommand(action, bundle)
    }

    fun disableReverb() {
        if (!_isReverbEnabled.value) return
        sendSessionAction(MediaSessionCallback.DISABLE_REVERB)
        _isReverbEnabled.value = false
        _reverbCurrentPreset.value = ReverbPresets.PRESET_NONE
        _reverbValue.value = 0
    }


    // =========================
    // BassBoost
    // =========================

    private val _bassBoostValue = MutableStateFlow(0)
    val bassBoostValue: StateFlow<Int> = _bassBoostValue.asStateFlow()

    fun updateBassBoostValue(value: Int) {
        _bassBoostValue.value = value
    }

    fun setBassBoost() {
        val action = MediaSessionCallback.SET_BASS_BOOST
        val bundle = Bundle().apply {
            putInt("value", bassBoostValue.value)
        }
        sendSessionCommand(action, bundle)
    }

    fun initBassBoostValue() {
        _bassBoostValue.value = 0
        setBassBoost()
    }


    // =========================
    // LoudnessEnhancer
    // =========================

    private val _loudnessEnhancerValue = MutableStateFlow(0)
    val loudnessEnhancerValue: StateFlow<Int> = _loudnessEnhancerValue.asStateFlow()

    fun updateLoudnessEnhancerValue(value: Int) {
        _loudnessEnhancerValue.value = value
    }

    fun setLoudnessEnhancer() {
        val action = MediaSessionCallback.SET_LOUDNESS_ENHANCER
        val bundle = Bundle().apply {
            putInt("value", loudnessEnhancerValue.value)
        }
        sendSessionCommand(action, bundle)
    }

    fun initLoudnessEnhancerValue() {
        _loudnessEnhancerValue.value = 0
        setLoudnessEnhancer()
    }


    // =========================
    // Virtualizer
    // =========================

    private val _virtualizerValue = MutableStateFlow(0)
    val virtualizerValue: StateFlow<Int> = _virtualizerValue.asStateFlow()

    fun updateVirtualizerValue(value: Int) {
        _virtualizerValue.value = value
    }

    fun setVirtualizer() {
        val action = MediaSessionCallback.SET_VIRTUALIZER
        val bundle = Bundle().apply {
            putInt("value", virtualizerValue.value)
        }
        sendSessionCommand(action, bundle)
    }

    fun initVirtualizerValue() {
        _virtualizerValue.value = 0
        setVirtualizer()
    }


    // =========================
    // HapticGenerator
    // =========================

    private val _isHapticGeneratorEnabled = MutableStateFlow(false)
    val isHapticGeneratorEnabled: StateFlow<Boolean> = _isHapticGeneratorEnabled.asStateFlow()

    fun updateIsHapticGeneratorEnabled() {
        // 토글
        if (_isHapticGeneratorEnabled.value) {
            setHapticGenerator(false)
        } else {
            setHapticGenerator(true)
        }
        _isHapticGeneratorEnabled.value = !_isHapticGeneratorEnabled.value
    }

    private fun setHapticGenerator(isEnabled: Boolean) {
        val action = MediaSessionCallback.SET_HAPTIC_GENERATOR
        val bundle = Bundle().apply {
            putBoolean("isEnabled", isEnabled)
        }
        sendSessionCommand(action, bundle)
    }


    // =========================
    // EnvironmentalReverb
    // =========================

    private val _isEnvironmentalReverbEnabled = MutableStateFlow(false)
    val isEnvironmentalReverbEnabled: StateFlow<Boolean> = _isEnvironmentalReverbEnabled.asStateFlow()

    private val _roomLevel = MutableStateFlow(0)
    val roomLevel: StateFlow<Int> = _roomLevel.asStateFlow()

    private val _roomHFLevel = MutableStateFlow(0)
    val roomHFLevel: StateFlow<Int> = _roomHFLevel.asStateFlow()

    private val _decayTime = MutableStateFlow(0)
    val decayTime: StateFlow<Int> = _decayTime.asStateFlow()

    private val _decayHFRatio = MutableStateFlow(0)
    val decayHFRatio: StateFlow<Int> = _decayHFRatio.asStateFlow()

    private val _reflectionsLevel = MutableStateFlow(0)
    val reflectionsLevel: StateFlow<Int> = _reflectionsLevel.asStateFlow()

    private val _reflectionsDelay = MutableStateFlow(0)
    val reflectionsDelay: StateFlow<Int> = _reflectionsDelay.asStateFlow()

    private val _reverbLevel = MutableStateFlow(0)
    val reverbLevel: StateFlow<Int> = _reverbLevel.asStateFlow()

    private val _reverbDelay = MutableStateFlow(0)
    val reverbDelay: StateFlow<Int> = _reverbDelay.asStateFlow()

    private val _diffusion = MutableStateFlow(0)
    val diffusion: StateFlow<Int> = _diffusion.asStateFlow()

    private val _density = MutableStateFlow(0)
    val density: StateFlow<Int> = _density.asStateFlow()

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
            putBoolean("isEnabled", _isEnvironmentalReverbEnabled.value)
            putInt("roomLevel", _roomLevel.value)
            putInt("roomHFLevel", _roomHFLevel.value)
            putInt("decayTime", _decayTime.value)
            putInt("decayHFRatio", _decayHFRatio.value)
            putInt("reflectionsLevel", _reflectionsLevel.value)
            putInt("reflectionsDelay", _reflectionsDelay.value)
            putInt("reverbLevel", _reverbLevel.value)
            putInt("reverbDelay", _reverbDelay.value)
            putInt("diffusion", _diffusion.value)
            putInt("density", _density.value)
        }
        sendSessionCommand(action, bundle)
    }

    fun disableEnvironmentalReverb() {
        _isEnvironmentalReverbEnabled.value = false
        setEnvironmentalReverb()
    }


    // =========================
    // Helpers
    // =========================
    private fun sendSessionAction(action: String) {
        val ctrl = mediaController ?: return
        Logger.d("sendSessionAction $action")
        val sessionCommand = SessionCommand(action, Bundle())
        ctrl.sendCustomCommand(sessionCommand, Bundle())
    }

    private fun sendSessionCommand(action: String, bundle: Bundle) {
        val ctrl = mediaController ?: return
        val sessionCommand = SessionCommand(action, bundle)
        ctrl.sendCustomCommand(sessionCommand, bundle)
    }

    // 필요하다면 release() 등 추가
    fun release() {
        // 예: Flow 초기화 or cleanup
    }
}
