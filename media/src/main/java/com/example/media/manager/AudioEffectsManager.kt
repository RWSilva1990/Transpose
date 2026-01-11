package com.example.media.manager

import android.os.Bundle
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import com.example.media.audio.SignalsmithAudioProcessor
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
    private val controllerProvider: MediaControllerProvider,
    private val signalsmithAudioProcessor: SignalsmithAudioProcessor
) {

    private val mediaController: MediaController?
        get() = controllerProvider.mediaController.value

    // =========================
    // Pitch / Tempo
    // =========================

    private val DEFAULT_PITCH_VALUE = 100

    private val _pitchValue = MutableStateFlow(DEFAULT_PITCH_VALUE)
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
        _pitchValue.value = DEFAULT_PITCH_VALUE
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
    // Signalsmith Chorus
    // =========================

    private val _isChorusEnabled = MutableStateFlow(false)
    val isChorusEnabled: StateFlow<Boolean> = _isChorusEnabled.asStateFlow()

    private val _chorusMix = MutableStateFlow(0.5f)
    val chorusMix: StateFlow<Float> = _chorusMix.asStateFlow()

    private val _chorusDepthMs = MutableStateFlow(10f)
    val chorusDepthMs: StateFlow<Float> = _chorusDepthMs.asStateFlow()

    private val _chorusDetune = MutableStateFlow(10f)
    val chorusDetune: StateFlow<Float> = _chorusDetune.asStateFlow()

    private val _chorusStereo = MutableStateFlow(0.5f)
    val chorusStereo: StateFlow<Float> = _chorusStereo.asStateFlow()

    fun updateIsChorusEnabled() {
        _isChorusEnabled.value = !_isChorusEnabled.value
        signalsmithAudioProcessor.setChorusEnabled(_isChorusEnabled.value)
    }

    fun updateChorusMix(value: Float) { _chorusMix.value = value }
    fun updateChorusDepthMs(value: Float) { _chorusDepthMs.value = value }
    fun updateChorusDetune(value: Float) { _chorusDetune.value = value }
    fun updateChorusStereo(value: Float) { _chorusStereo.value = value }

    fun setChorusParams() {
        signalsmithAudioProcessor.setChorusParams(
            _chorusMix.value, _chorusDepthMs.value, _chorusDetune.value, _chorusStereo.value
        )
    }

    fun initChorusValues() {
        _chorusMix.value = 0.5f
        _chorusDepthMs.value = 10f
        _chorusDetune.value = 10f
        _chorusStereo.value = 0.5f
        setChorusParams()
    }


    // =========================
    // Signalsmith Limiter
    // =========================

    private val _isLimiterEnabled = MutableStateFlow(false)
    val isLimiterEnabled: StateFlow<Boolean> = _isLimiterEnabled.asStateFlow()

    private val _limiterInputGainDb = MutableStateFlow(0f)
    val limiterInputGainDb: StateFlow<Float> = _limiterInputGainDb.asStateFlow()

    private val _limiterLimitDb = MutableStateFlow(-3f)
    val limiterLimitDb: StateFlow<Float> = _limiterLimitDb.asStateFlow()

    private val _limiterAttackMs = MutableStateFlow(10f)
    val limiterAttackMs: StateFlow<Float> = _limiterAttackMs.asStateFlow()

    private val _limiterReleaseMs = MutableStateFlow(100f)
    val limiterReleaseMs: StateFlow<Float> = _limiterReleaseMs.asStateFlow()

    fun updateIsLimiterEnabled() {
        _isLimiterEnabled.value = !_isLimiterEnabled.value
        signalsmithAudioProcessor.setLimiterEnabled(_isLimiterEnabled.value)
    }

    fun updateLimiterInputGainDb(value: Float) { _limiterInputGainDb.value = value }
    fun updateLimiterLimitDb(value: Float) { _limiterLimitDb.value = value }
    fun updateLimiterAttackMs(value: Float) { _limiterAttackMs.value = value }
    fun updateLimiterReleaseMs(value: Float) { _limiterReleaseMs.value = value }

    fun setLimiterParams() {
        signalsmithAudioProcessor.setLimiterParams(
            _limiterInputGainDb.value, _limiterLimitDb.value, _limiterAttackMs.value, _limiterReleaseMs.value
        )
    }

    fun initLimiterValues() {
        _limiterInputGainDb.value = 0f
        _limiterLimitDb.value = -3f
        _limiterAttackMs.value = 10f
        _limiterReleaseMs.value = 100f
        setLimiterParams()
    }


    // =========================
    // Signalsmith Reverb
    // =========================

    private val _isSignalsmithReverbEnabled = MutableStateFlow(false)
    val isSignalsmithReverbEnabled: StateFlow<Boolean> = _isSignalsmithReverbEnabled.asStateFlow()

    private val _signalsmithReverbDry = MutableStateFlow(1f)
    val signalsmithReverbDry: StateFlow<Float> = _signalsmithReverbDry.asStateFlow()

    private val _signalsmithReverbWet = MutableStateFlow(0.3f)
    val signalsmithReverbWet: StateFlow<Float> = _signalsmithReverbWet.asStateFlow()

    private val _signalsmithReverbRoomMs = MutableStateFlow(50f)
    val signalsmithReverbRoomMs: StateFlow<Float> = _signalsmithReverbRoomMs.asStateFlow()

    private val _signalsmithReverbDecaySec = MutableStateFlow(2f)
    val signalsmithReverbDecaySec: StateFlow<Float> = _signalsmithReverbDecaySec.asStateFlow()

    fun updateIsSignalsmithReverbEnabled() {
        _isSignalsmithReverbEnabled.value = !_isSignalsmithReverbEnabled.value
        signalsmithAudioProcessor.setReverbEnabled(_isSignalsmithReverbEnabled.value)
    }

    fun updateSignalsmithReverbDry(value: Float) { _signalsmithReverbDry.value = value }
    fun updateSignalsmithReverbWet(value: Float) { _signalsmithReverbWet.value = value }
    fun updateSignalsmithReverbRoomMs(value: Float) { _signalsmithReverbRoomMs.value = value }
    fun updateSignalsmithReverbDecaySec(value: Float) { _signalsmithReverbDecaySec.value = value }

    fun setSignalsmithReverbParams() {
        signalsmithAudioProcessor.setReverbParams(
            _signalsmithReverbDry.value, _signalsmithReverbWet.value,
            _signalsmithReverbRoomMs.value, _signalsmithReverbDecaySec.value
        )
    }

    fun initSignalsmithReverbValues() {
        _signalsmithReverbDry.value = 1f
        _signalsmithReverbWet.value = 0.3f
        _signalsmithReverbRoomMs.value = 50f
        _signalsmithReverbDecaySec.value = 2f
        setSignalsmithReverbParams()
    }


    // =========================
    // Signalsmith Crunch
    // =========================

    private val _isCrunchEnabled = MutableStateFlow(false)
    val isCrunchEnabled: StateFlow<Boolean> = _isCrunchEnabled.asStateFlow()

    private val _crunchDriveDb = MutableStateFlow(0f)
    val crunchDriveDb: StateFlow<Float> = _crunchDriveDb.asStateFlow()

    private val _crunchFuzz = MutableStateFlow(0f)
    val crunchFuzz: StateFlow<Float> = _crunchFuzz.asStateFlow()

    private val _crunchToneHz = MutableStateFlow(5000f)
    val crunchToneHz: StateFlow<Float> = _crunchToneHz.asStateFlow()

    fun updateIsCrunchEnabled() {
        _isCrunchEnabled.value = !_isCrunchEnabled.value
        signalsmithAudioProcessor.setCrunchEnabled(_isCrunchEnabled.value)
    }

    fun updateCrunchDriveDb(value: Float) { _crunchDriveDb.value = value }
    fun updateCrunchFuzz(value: Float) { _crunchFuzz.value = value }
    fun updateCrunchToneHz(value: Float) { _crunchToneHz.value = value }

    fun setCrunchParams() {
        signalsmithAudioProcessor.setCrunchParams(_crunchDriveDb.value, _crunchFuzz.value, _crunchToneHz.value)
    }

    fun initCrunchValues() {
        _crunchDriveDb.value = 0f
        _crunchFuzz.value = 0f
        _crunchToneHz.value = 5000f
        setCrunchParams()
    }


    private val _isEqEnabled = MutableStateFlow(false)
    val isEqEnabled: StateFlow<Boolean> = _isEqEnabled.asStateFlow()

    private val _eqBand1Freq = MutableStateFlow(60f)
    val eqBand1Freq: StateFlow<Float> = _eqBand1Freq.asStateFlow()
    private val _eqBand1Gain = MutableStateFlow(0f)
    val eqBand1Gain: StateFlow<Float> = _eqBand1Gain.asStateFlow()

    private val _eqBand2Freq = MutableStateFlow(250f)
    val eqBand2Freq: StateFlow<Float> = _eqBand2Freq.asStateFlow()
    private val _eqBand2Gain = MutableStateFlow(0f)
    val eqBand2Gain: StateFlow<Float> = _eqBand2Gain.asStateFlow()

    private val _eqBand3Freq = MutableStateFlow(1000f)
    val eqBand3Freq: StateFlow<Float> = _eqBand3Freq.asStateFlow()
    private val _eqBand3Gain = MutableStateFlow(0f)
    val eqBand3Gain: StateFlow<Float> = _eqBand3Gain.asStateFlow()

    private val _eqBand4Freq = MutableStateFlow(4000f)
    val eqBand4Freq: StateFlow<Float> = _eqBand4Freq.asStateFlow()
    private val _eqBand4Gain = MutableStateFlow(0f)
    val eqBand4Gain: StateFlow<Float> = _eqBand4Gain.asStateFlow()

    private val _eqBand5Freq = MutableStateFlow(12000f)
    val eqBand5Freq: StateFlow<Float> = _eqBand5Freq.asStateFlow()
    private val _eqBand5Gain = MutableStateFlow(0f)
    val eqBand5Gain: StateFlow<Float> = _eqBand5Gain.asStateFlow()

    fun updateIsEqEnabled() {
        _isEqEnabled.value = !_isEqEnabled.value
        signalsmithAudioProcessor.setEqEnabled(_isEqEnabled.value)
    }

    fun updateEqBand(band: Int, freq: Float, gain: Float) {
        when (band) {
            0 -> { _eqBand1Freq.value = freq; _eqBand1Gain.value = gain }
            1 -> { _eqBand2Freq.value = freq; _eqBand2Gain.value = gain }
            2 -> { _eqBand3Freq.value = freq; _eqBand3Gain.value = gain }
            3 -> { _eqBand4Freq.value = freq; _eqBand4Gain.value = gain }
            4 -> { _eqBand5Freq.value = freq; _eqBand5Gain.value = gain }
        }
        signalsmithAudioProcessor.setEqBand(band, freq, gain)
    }

    fun updateEqBandGain(band: Int, gain: Float) {
        val freq = when (band) {
            0 -> _eqBand1Freq.value
            1 -> _eqBand2Freq.value
            2 -> _eqBand3Freq.value
            3 -> _eqBand4Freq.value
            4 -> _eqBand5Freq.value
            else -> 1000f
        }
        updateEqBand(band, freq, gain)
    }

    fun initEqValues() {
        _eqBand1Gain.value = 0f; _eqBand2Gain.value = 0f; _eqBand3Gain.value = 0f
        _eqBand4Gain.value = 0f; _eqBand5Gain.value = 0f
        for (i in 0..4) {
            val freq = when (i) { 0 -> 60f; 1 -> 250f; 2 -> 1000f; 3 -> 4000f; else -> 12000f }
            signalsmithAudioProcessor.setEqBand(i, freq, 0f)
        }
    }


    private val _isCompressorEnabled = MutableStateFlow(false)
    val isCompressorEnabled: StateFlow<Boolean> = _isCompressorEnabled.asStateFlow()

    private val _compThresholdDb = MutableStateFlow(-20f)
    val compThresholdDb: StateFlow<Float> = _compThresholdDb.asStateFlow()

    private val _compRatio = MutableStateFlow(4f)
    val compRatio: StateFlow<Float> = _compRatio.asStateFlow()

    private val _compAttackMs = MutableStateFlow(10f)
    val compAttackMs: StateFlow<Float> = _compAttackMs.asStateFlow()

    private val _compReleaseMs = MutableStateFlow(100f)
    val compReleaseMs: StateFlow<Float> = _compReleaseMs.asStateFlow()

    private val _compMakeupGainDb = MutableStateFlow(0f)
    val compMakeupGainDb: StateFlow<Float> = _compMakeupGainDb.asStateFlow()

    fun updateIsCompressorEnabled() {
        _isCompressorEnabled.value = !_isCompressorEnabled.value
        signalsmithAudioProcessor.setCompressorEnabled(_isCompressorEnabled.value)
    }

    fun updateCompThresholdDb(value: Float) { _compThresholdDb.value = value }
    fun updateCompRatio(value: Float) { _compRatio.value = value }
    fun updateCompAttackMs(value: Float) { _compAttackMs.value = value }
    fun updateCompReleaseMs(value: Float) { _compReleaseMs.value = value }
    fun updateCompMakeupGainDb(value: Float) { _compMakeupGainDb.value = value }

    fun setCompressorParams() {
        signalsmithAudioProcessor.setCompressorParams(
            _compThresholdDb.value, _compRatio.value, _compAttackMs.value,
            _compReleaseMs.value, _compMakeupGainDb.value
        )
    }

    fun initCompressorValues() {
        _compThresholdDb.value = -20f
        _compRatio.value = 4f
        _compAttackMs.value = 10f
        _compReleaseMs.value = 100f
        _compMakeupGainDb.value = 0f
        setCompressorParams()
    }


    private val _isPitchDetectionEnabled = MutableStateFlow(false)
    val isPitchDetectionEnabled: StateFlow<Boolean> = _isPitchDetectionEnabled.asStateFlow()

    fun updateIsPitchDetectionEnabled() {
        _isPitchDetectionEnabled.value = !_isPitchDetectionEnabled.value
        signalsmithAudioProcessor.setPitchDetectionEnabled(_isPitchDetectionEnabled.value)
    }

    fun getDetectedPitch(): Float = signalsmithAudioProcessor.getDetectedPitch()


    private val _isHrtfEnabled = MutableStateFlow(false)
    val isHrtfEnabled: StateFlow<Boolean> = _isHrtfEnabled.asStateFlow()

    private val _hrtfIntensity = MutableStateFlow(1.0f)
    val hrtfIntensity: StateFlow<Float> = _hrtfIntensity.asStateFlow()

    private val _hrtfAzimuth = MutableStateFlow(30)
    val hrtfAzimuth: StateFlow<Int> = _hrtfAzimuth.asStateFlow()

    fun updateIsHrtfEnabled() {
        _isHrtfEnabled.value = !_isHrtfEnabled.value
        signalsmithAudioProcessor.setHrtfEnabled(_isHrtfEnabled.value)
    }

    fun updateHrtfIntensity(value: Float) {
        _hrtfIntensity.value = value
        setHrtfParams()
    }

    fun updateHrtfAzimuth(azimuth: Int) {
        _hrtfAzimuth.value = azimuth
        setHrtfParams()
    }

    fun setHrtfParams() {
        signalsmithAudioProcessor.setHrtfParams(_hrtfIntensity.value, _hrtfAzimuth.value)
    }

    fun initHrtfValues() {
        _hrtfIntensity.value = 1.0f
        _hrtfAzimuth.value = 30
        setHrtfParams()
    }


    private val _isPhaserEnabled = MutableStateFlow(false)
    val isPhaserEnabled: StateFlow<Boolean> = _isPhaserEnabled.asStateFlow()

    private val _phaserLfoFreq = MutableStateFlow(0.5f)
    val phaserLfoFreq: StateFlow<Float> = _phaserLfoFreq.asStateFlow()

    private val _phaserLfoDepth = MutableStateFlow(0.5f)
    val phaserLfoDepth: StateFlow<Float> = _phaserLfoDepth.asStateFlow()

    private val _phaserFeedback = MutableStateFlow(0.7f)
    val phaserFeedback: StateFlow<Float> = _phaserFeedback.asStateFlow()

    private val _phaserPoles = MutableStateFlow(4)
    val phaserPoles: StateFlow<Int> = _phaserPoles.asStateFlow()

    fun updateIsPhaserEnabled() {
        _isPhaserEnabled.value = !_isPhaserEnabled.value
        signalsmithAudioProcessor.setPhaserEnabled(_isPhaserEnabled.value)
    }

    fun updatePhaserLfoFreq(value: Float) { _phaserLfoFreq.value = value }
    fun updatePhaserLfoDepth(value: Float) { _phaserLfoDepth.value = value }
    fun updatePhaserFeedback(value: Float) { _phaserFeedback.value = value }
    fun updatePhaserPoles(value: Int) { _phaserPoles.value = value }

    fun setPhaserParams() {
        signalsmithAudioProcessor.setPhaserParams(
            _phaserLfoFreq.value, _phaserLfoDepth.value, _phaserFeedback.value, _phaserPoles.value
        )
    }

    fun initPhaserValues() {
        _phaserLfoFreq.value = 0.5f
        _phaserLfoDepth.value = 0.5f
        _phaserFeedback.value = 0.7f
        _phaserPoles.value = 4
        setPhaserParams()
    }


    private val _isFlangerEnabled = MutableStateFlow(false)
    val isFlangerEnabled: StateFlow<Boolean> = _isFlangerEnabled.asStateFlow()

    private val _flangerLfoFreq = MutableStateFlow(0.2f)
    val flangerLfoFreq: StateFlow<Float> = _flangerLfoFreq.asStateFlow()

    private val _flangerLfoDepth = MutableStateFlow(0.5f)
    val flangerLfoDepth: StateFlow<Float> = _flangerLfoDepth.asStateFlow()

    private val _flangerFeedback = MutableStateFlow(0.5f)
    val flangerFeedback: StateFlow<Float> = _flangerFeedback.asStateFlow()

    private val _flangerDelayMs = MutableStateFlow(3.0f)
    val flangerDelayMs: StateFlow<Float> = _flangerDelayMs.asStateFlow()

    fun updateIsFlangerEnabled() {
        _isFlangerEnabled.value = !_isFlangerEnabled.value
        signalsmithAudioProcessor.setFlangerEnabled(_isFlangerEnabled.value)
    }

    fun updateFlangerLfoFreq(value: Float) { _flangerLfoFreq.value = value }
    fun updateFlangerLfoDepth(value: Float) { _flangerLfoDepth.value = value }
    fun updateFlangerFeedback(value: Float) { _flangerFeedback.value = value }
    fun updateFlangerDelayMs(value: Float) { _flangerDelayMs.value = value }

    fun setFlangerParams() {
        signalsmithAudioProcessor.setFlangerParams(
            _flangerLfoFreq.value, _flangerLfoDepth.value, _flangerFeedback.value, _flangerDelayMs.value
        )
    }

    fun initFlangerValues() {
        _flangerLfoFreq.value = 0.2f
        _flangerLfoDepth.value = 0.5f
        _flangerFeedback.value = 0.5f
        _flangerDelayMs.value = 3.0f
        setFlangerParams()
    }


    private val _isTremoloEnabled = MutableStateFlow(false)
    val isTremoloEnabled: StateFlow<Boolean> = _isTremoloEnabled.asStateFlow()

    private val _tremoloFreq = MutableStateFlow(5.0f)
    val tremoloFreq: StateFlow<Float> = _tremoloFreq.asStateFlow()

    private val _tremoloDepth = MutableStateFlow(0.5f)
    val tremoloDepth: StateFlow<Float> = _tremoloDepth.asStateFlow()

    private val _tremoloWaveform = MutableStateFlow(0)
    val tremoloWaveform: StateFlow<Int> = _tremoloWaveform.asStateFlow()

    fun updateIsTremoloEnabled() {
        _isTremoloEnabled.value = !_isTremoloEnabled.value
        signalsmithAudioProcessor.setTremoloEnabled(_isTremoloEnabled.value)
    }

    fun updateTremoloFreq(value: Float) { _tremoloFreq.value = value }
    fun updateTremoloDepth(value: Float) { _tremoloDepth.value = value }
    fun updateTremoloWaveform(value: Int) { _tremoloWaveform.value = value }

    fun setTremoloParams() {
        signalsmithAudioProcessor.setTremoloParams(_tremoloFreq.value, _tremoloDepth.value, _tremoloWaveform.value)
    }

    fun initTremoloValues() {
        _tremoloFreq.value = 5.0f
        _tremoloDepth.value = 0.5f
        _tremoloWaveform.value = 0
        setTremoloParams()
    }


    private val _isAutowahEnabled = MutableStateFlow(false)
    val isAutowahEnabled: StateFlow<Boolean> = _isAutowahEnabled.asStateFlow()

    private val _autowahWah = MutableStateFlow(0.5f)
    val autowahWah: StateFlow<Float> = _autowahWah.asStateFlow()

    private val _autowahMix = MutableStateFlow(50.0f)
    val autowahMix: StateFlow<Float> = _autowahMix.asStateFlow()

    private val _autowahLevel = MutableStateFlow(0.5f)
    val autowahLevel: StateFlow<Float> = _autowahLevel.asStateFlow()

    fun updateIsAutowahEnabled() {
        _isAutowahEnabled.value = !_isAutowahEnabled.value
        signalsmithAudioProcessor.setAutowahEnabled(_isAutowahEnabled.value)
    }

    fun updateAutowahWah(value: Float) { _autowahWah.value = value }
    fun updateAutowahMix(value: Float) { _autowahMix.value = value }
    fun updateAutowahLevel(value: Float) { _autowahLevel.value = value }

    fun setAutowahParams() {
        signalsmithAudioProcessor.setAutowahParams(_autowahWah.value, _autowahMix.value, _autowahLevel.value)
    }

    fun initAutowahValues() {
        _autowahWah.value = 0.5f
        _autowahMix.value = 50.0f
        _autowahLevel.value = 0.5f
        setAutowahParams()
    }


    private val _isDecimatorEnabled = MutableStateFlow(false)
    val isDecimatorEnabled: StateFlow<Boolean> = _isDecimatorEnabled.asStateFlow()

    private val _decimatorBitcrush = MutableStateFlow(0.5f)
    val decimatorBitcrush: StateFlow<Float> = _decimatorBitcrush.asStateFlow()

    private val _decimatorDownsample = MutableStateFlow(0.5f)
    val decimatorDownsample: StateFlow<Float> = _decimatorDownsample.asStateFlow()

    fun updateIsDecimatorEnabled() {
        _isDecimatorEnabled.value = !_isDecimatorEnabled.value
        signalsmithAudioProcessor.setDecimatorEnabled(_isDecimatorEnabled.value)
    }

    fun updateDecimatorBitcrush(value: Float) { _decimatorBitcrush.value = value }
    fun updateDecimatorDownsample(value: Float) { _decimatorDownsample.value = value }

    fun setDecimatorParams() {
        signalsmithAudioProcessor.setDecimatorParams(_decimatorBitcrush.value, _decimatorDownsample.value)
    }

    fun initDecimatorValues() {
        _decimatorBitcrush.value = 0.5f
        _decimatorDownsample.value = 0.5f
        setDecimatorParams()
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
