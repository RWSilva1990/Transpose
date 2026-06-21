package com.example.media.manager

import com.example.media.audio.SignalsmithAudioProcessor
import com.example.media.audio.VocalRemovalProcessor
import com.example.media.audio_effect.data.eq.SignalsmithEqPresets
import com.example.media.audio_effect.data.filter.ToneFilterPresets
import com.example.media.audio_effect.data.reverb.ReverbPlusPresets
import com.example.media.audio_effect.data.reverb.SignalsmithReverbPresets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEffectsManager @Inject constructor(
    private val signalsmithAudioProcessor: SignalsmithAudioProcessor,
    private val vocalRemovalProcessor: VocalRemovalProcessor
) {
    companion object {
        private const val EQ_CONTROL_INTERVAL_MS = 40L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val eqUpdateLock = Any()
    private val pendingEqBands = LinkedHashSet<Int>()
    private var eqDispatchJob: Job? = null
    private var lastChorusMix = Float.NaN
    private var lastChorusDepthMs = Float.NaN
    private var lastChorusDetune = Float.NaN
    private var lastChorusStereo = Float.NaN

    // =========================
    // Pitch / Tempo
    // =========================

    private fun semitonesToUiValue(semitones: Float): Int = ((semitones * 10) + 100).toInt()

    private val _pitchSemitones = MutableStateFlow(0f)
    private val _tempoSemitones = MutableStateFlow(0f)

    val pitchValue: StateFlow<Int> = _pitchSemitones
        .map { semitonesToUiValue(it) }
        .stateIn(scope, SharingStarted.Eagerly, 100)

    private fun uiValueToSemitones(uiValue: Int): Float = (uiValue - 100) / 10f

    fun pitchPlusOne() {
        setPitchSemitones(_pitchSemitones.value + 1f)
    }

    fun pitchMinusOne() {
        setPitchSemitones(_pitchSemitones.value - 1f)
    }

    fun initPitchValue() {
        setPitchSemitones(0f)
    }

    fun updatePitchValue(uiValue: Int) {
        setPitchSemitones(uiValueToSemitones(uiValue))
    }

    fun setPitch() {
    }

    val tempoValue: StateFlow<Int> = _tempoSemitones
        .map { semitonesToUiValue(it) }
        .stateIn(scope, SharingStarted.Eagerly, 100)

    fun tempoPlusOne() {
        setTempoSemitones(_tempoSemitones.value + 1f)
    }

    fun tempoMinusOne() {
        setTempoSemitones(_tempoSemitones.value - 1f)
    }

    fun initTempoValue() {
        setTempoSemitones(0f)
    }

    fun updateTempoValue(uiValue: Int) {
        setTempoSemitones(uiValueToSemitones(uiValue))
    }

    fun setTempo() {
    }

    private fun setPitchSemitones(semitones: Float) {
        val safeValue = semitones.coerceIn(-24f, 24f)
        _pitchSemitones.value = safeValue
        signalsmithAudioProcessor.setPitchSemitones(safeValue)
    }

    private fun setTempoSemitones(semitones: Float) {
        val safeValue = semitones.coerceIn(-12f, 12f)
        _tempoSemitones.value = safeValue
        signalsmithAudioProcessor.setTempoSemitones(safeValue)
    }

    private val _isVocalRemovalEnabled = MutableStateFlow(false)
    val isVocalRemovalEnabled: StateFlow<Boolean> = _isVocalRemovalEnabled.asStateFlow()

    private val _isVocalRemovalSupported = MutableStateFlow(vocalRemovalProcessor.isSupported)
    val isVocalRemovalSupported: StateFlow<Boolean> = _isVocalRemovalSupported.asStateFlow()

    private val _vocalRemovalMix = MutableStateFlow(1.0f)
    val vocalRemovalMix: StateFlow<Float> = _vocalRemovalMix.asStateFlow()

    private val _isVocalOnlyMode = MutableStateFlow(false)
    val isVocalOnlyMode: StateFlow<Boolean> = _isVocalOnlyMode.asStateFlow()

    fun updateIsVocalRemovalEnabled() {
        val nextEnabled = !_isVocalRemovalEnabled.value
        if (nextEnabled && !_isVocalRemovalSupported.value) {
            _isVocalRemovalEnabled.value = false
            vocalRemovalProcessor.enabled = false
            return
        }
        _isVocalRemovalEnabled.value = nextEnabled
        vocalRemovalProcessor.enabled = _isVocalRemovalEnabled.value
    }

    fun disableVocalRemovalForBackground() {
        if (!_isVocalRemovalEnabled.value) return

        _isVocalRemovalEnabled.value = false
        vocalRemovalProcessor.enabled = false
        vocalRemovalProcessor.flush()
    }

    fun updateIsVocalOnlyMode() {
        _isVocalOnlyMode.value = !_isVocalOnlyMode.value
        vocalRemovalProcessor.vocalOnlyMode = _isVocalOnlyMode.value
    }

    fun updateVocalRemovalMix(value: Float) {
        _vocalRemovalMix.value = value.coerceIn(0f, 1f)
        vocalRemovalProcessor.mixRatio = _vocalRemovalMix.value
    }

    fun prewarmVocalRemovalModel() {
        if (!_isVocalRemovalSupported.value) return
        vocalRemovalProcessor.prewarm()
    }

    fun initVocalRemovalValues() {
        _vocalRemovalMix.value = 1.0f
        vocalRemovalProcessor.mixRatio = 1.0f
        _isVocalOnlyMode.value = false
        vocalRemovalProcessor.vocalOnlyMode = false
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
        if (
            isNearlySame(_chorusMix.value, lastChorusMix) &&
            isNearlySame(_chorusDepthMs.value, lastChorusDepthMs) &&
            isNearlySame(_chorusDetune.value, lastChorusDetune) &&
            isNearlySame(_chorusStereo.value, lastChorusStereo)
        ) return

        lastChorusMix = _chorusMix.value
        lastChorusDepthMs = _chorusDepthMs.value
        lastChorusDetune = _chorusDetune.value
        lastChorusStereo = _chorusStereo.value
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
    // Reverb+
    // =========================

    private val _isReverbPlusEnabled = MutableStateFlow(false)
    val isReverbPlusEnabled: StateFlow<Boolean> = _isReverbPlusEnabled.asStateFlow()

    private val _reverbPlusPreset = MutableStateFlow(ReverbPlusPresets.PRESET_BALANCED)
    val reverbPlusPreset: StateFlow<Int> = _reverbPlusPreset.asStateFlow()

    private val _reverbPlusDry = MutableStateFlow(1.0f)
    val reverbPlusDry: StateFlow<Float> = _reverbPlusDry.asStateFlow()

    private val _reverbPlusWet = MutableStateFlow(0.30f)
    val reverbPlusWet: StateFlow<Float> = _reverbPlusWet.asStateFlow()

    private val _reverbPlusRoomSize = MutableStateFlow(0.50f)
    val reverbPlusRoomSize: StateFlow<Float> = _reverbPlusRoomSize.asStateFlow()

    private val _reverbPlusDamping = MutableStateFlow(0.50f)
    val reverbPlusDamping: StateFlow<Float> = _reverbPlusDamping.asStateFlow()

    private var lastReverbPlusDry: Float = Float.NaN
    private var lastReverbPlusWet: Float = Float.NaN
    private var lastReverbPlusRoomSize: Float = Float.NaN
    private var lastReverbPlusDamping: Float = Float.NaN

    fun updateIsReverbPlusEnabled() {
        val newEnabled = !_isReverbPlusEnabled.value
        _isReverbPlusEnabled.value = newEnabled
        if (newEnabled) {
            setReverbPlusParams()
        }
        signalsmithAudioProcessor.setReverbPlusEnabled(newEnabled)
    }

    fun updateReverbPlusPreset(presetIndex: Int) {
        _reverbPlusPreset.value = presetIndex
        val preset = ReverbPlusPresets.getPreset(presetIndex)
        _reverbPlusDry.value = preset.dry
        _reverbPlusWet.value = preset.wet
        _reverbPlusRoomSize.value = preset.roomSize
        _reverbPlusDamping.value = preset.damping
        setReverbPlusParams()
    }

    fun updateReverbPlusDry(value: Float) { _reverbPlusDry.value = value.coerceIn(0f, 1f) }
    fun updateReverbPlusWet(value: Float) { _reverbPlusWet.value = value.coerceIn(0f, 1f) }
    fun updateReverbPlusRoomSize(value: Float) { _reverbPlusRoomSize.value = value.coerceIn(0f, 1f) }
    fun updateReverbPlusDamping(value: Float) { _reverbPlusDamping.value = value.coerceIn(0f, 1f) }

    fun setReverbPlusParams() {
        val dry = _reverbPlusDry.value
        val wet = _reverbPlusWet.value
        val roomSize = _reverbPlusRoomSize.value
        val damping = _reverbPlusDamping.value

        if (isNearlySame(dry, lastReverbPlusDry) &&
            isNearlySame(wet, lastReverbPlusWet) &&
            isNearlySame(roomSize, lastReverbPlusRoomSize) &&
            isNearlySame(damping, lastReverbPlusDamping)
        ) {
            return
        }

        signalsmithAudioProcessor.setReverbPlusParams(dry, wet, roomSize, damping)
        lastReverbPlusDry = dry
        lastReverbPlusWet = wet
        lastReverbPlusRoomSize = roomSize
        lastReverbPlusDamping = damping
    }

    fun initReverbPlusValues() {
        updateReverbPlusPreset(ReverbPlusPresets.PRESET_BALANCED)
    }


    // =========================
    // Signalsmith Reverb
    // =========================

    private val _isSignalsmithReverbEnabled = MutableStateFlow(false)
    val isSignalsmithReverbEnabled: StateFlow<Boolean> = _isSignalsmithReverbEnabled.asStateFlow()

    private val _signalsmithReverbPreset = MutableStateFlow(SignalsmithReverbPresets.PRESET_DEFAULT)
    val signalsmithReverbPreset: StateFlow<Int> = _signalsmithReverbPreset.asStateFlow()

    private val _signalsmithReverbDry = MutableStateFlow(1.0f)
    val signalsmithReverbDry: StateFlow<Float> = _signalsmithReverbDry.asStateFlow()

    private val _signalsmithReverbWet = MutableStateFlow(0.35f)
    val signalsmithReverbWet: StateFlow<Float> = _signalsmithReverbWet.asStateFlow()

    private val _signalsmithReverbRoomMs = MutableStateFlow(80f)
    val signalsmithReverbRoomMs: StateFlow<Float> = _signalsmithReverbRoomMs.asStateFlow()

    private val _signalsmithReverbDecaySec = MutableStateFlow(1.0f)
    val signalsmithReverbDecaySec: StateFlow<Float> = _signalsmithReverbDecaySec.asStateFlow()

    private val _signalsmithReverbEarly = MutableStateFlow(1.0f)
    val signalsmithReverbEarly: StateFlow<Float> = _signalsmithReverbEarly.asStateFlow()

    private val _signalsmithReverbDetune = MutableStateFlow(2.0f)
    val signalsmithReverbDetune: StateFlow<Float> = _signalsmithReverbDetune.asStateFlow()

    private val _signalsmithReverbLowCutHz = MutableStateFlow(80f)
    val signalsmithReverbLowCutHz: StateFlow<Float> = _signalsmithReverbLowCutHz.asStateFlow()

    private val _signalsmithReverbHighCutHz = MutableStateFlow(12000f)
    val signalsmithReverbHighCutHz: StateFlow<Float> = _signalsmithReverbHighCutHz.asStateFlow()

    private val _signalsmithReverbLowDampRate = MutableStateFlow(1.6f)
    val signalsmithReverbLowDampRate: StateFlow<Float> = _signalsmithReverbLowDampRate.asStateFlow()

    private val _signalsmithReverbHighDampRate = MutableStateFlow(2.5f)
    val signalsmithReverbHighDampRate: StateFlow<Float> = _signalsmithReverbHighDampRate.asStateFlow()

    private var lastReverbDry: Float = Float.NaN
    private var lastReverbWet: Float = Float.NaN
    private var lastReverbRoomMs: Float = Float.NaN
    private var lastReverbDecaySec: Float = Float.NaN
    private var lastReverbEarly: Float = Float.NaN
    private var lastReverbDetune: Float = Float.NaN
    private var lastReverbLowCutHz: Float = Float.NaN
    private var lastReverbHighCutHz: Float = Float.NaN
    private var lastReverbLowDampRate: Float = Float.NaN
    private var lastReverbHighDampRate: Float = Float.NaN

    private fun isNearlySame(a: Float, b: Float, epsilon: Float = 0.0001f): Boolean {
        return kotlin.math.abs(a - b) < epsilon
    }

    fun updateIsSignalsmithReverbEnabled() {
        val newEnabled = !_isSignalsmithReverbEnabled.value
        _isSignalsmithReverbEnabled.value = newEnabled
        if (newEnabled) {
            setSignalsmithReverbParams()
        }
        signalsmithAudioProcessor.setReverbEnabled(newEnabled)
    }

    fun updateSignalsmithReverbPreset(presetIndex: Int) {
        _signalsmithReverbPreset.value = presetIndex
        val preset = SignalsmithReverbPresets.getPreset(presetIndex)
        _signalsmithReverbDry.value = preset.dry
        _signalsmithReverbWet.value = preset.wet
        _signalsmithReverbRoomMs.value = preset.roomMs
        _signalsmithReverbDecaySec.value = preset.decaySec
        _signalsmithReverbEarly.value = preset.early
        _signalsmithReverbDetune.value = preset.detune
        _signalsmithReverbLowCutHz.value = preset.lowCutHz
        _signalsmithReverbHighCutHz.value = preset.highCutHz
        _signalsmithReverbLowDampRate.value = preset.lowDampRate
        _signalsmithReverbHighDampRate.value = preset.highDampRate
        setSignalsmithReverbParams()
    }

    fun updateSignalsmithReverbDry(value: Float) { _signalsmithReverbDry.value = value }
    fun updateSignalsmithReverbWet(value: Float) { _signalsmithReverbWet.value = value }
    fun updateSignalsmithReverbRoomMs(value: Float) { _signalsmithReverbRoomMs.value = value }
    fun updateSignalsmithReverbDecaySec(value: Float) { _signalsmithReverbDecaySec.value = value }
    fun updateSignalsmithReverbEarly(value: Float) { _signalsmithReverbEarly.value = value }
    fun updateSignalsmithReverbDetune(value: Float) { _signalsmithReverbDetune.value = value }
    fun updateSignalsmithReverbLowCutHz(value: Float) { _signalsmithReverbLowCutHz.value = value }
    fun updateSignalsmithReverbHighCutHz(value: Float) { _signalsmithReverbHighCutHz.value = value }
    fun updateSignalsmithReverbLowDampRate(value: Float) { _signalsmithReverbLowDampRate.value = value }
    fun updateSignalsmithReverbHighDampRate(value: Float) { _signalsmithReverbHighDampRate.value = value }

    fun setSignalsmithReverbParams() {
        val dry = _signalsmithReverbDry.value
        val wet = _signalsmithReverbWet.value
        val roomMs = _signalsmithReverbRoomMs.value
        val decaySec = _signalsmithReverbDecaySec.value
        val early = _signalsmithReverbEarly.value
        val detune = _signalsmithReverbDetune.value
        val lowCutHz = _signalsmithReverbLowCutHz.value
        val highCutHz = _signalsmithReverbHighCutHz.value
        val lowDampRate = _signalsmithReverbLowDampRate.value
        val highDampRate = _signalsmithReverbHighDampRate.value

        if (isNearlySame(dry, lastReverbDry) &&
            isNearlySame(wet, lastReverbWet) &&
            isNearlySame(roomMs, lastReverbRoomMs) &&
            isNearlySame(decaySec, lastReverbDecaySec) &&
            isNearlySame(early, lastReverbEarly) &&
            isNearlySame(detune, lastReverbDetune) &&
            isNearlySame(lowCutHz, lastReverbLowCutHz) &&
            isNearlySame(highCutHz, lastReverbHighCutHz) &&
            isNearlySame(lowDampRate, lastReverbLowDampRate) &&
            isNearlySame(highDampRate, lastReverbHighDampRate)
        ) {
            return
        }

        signalsmithAudioProcessor.setReverbParams(
            dry, wet, roomMs, decaySec,
            early, detune, lowCutHz, highCutHz, lowDampRate, highDampRate
        )

        lastReverbDry = dry
        lastReverbWet = wet
        lastReverbRoomMs = roomMs
        lastReverbDecaySec = decaySec
        lastReverbEarly = early
        lastReverbDetune = detune
        lastReverbLowCutHz = lowCutHz
        lastReverbHighCutHz = highCutHz
        lastReverbLowDampRate = lowDampRate
        lastReverbHighDampRate = highDampRate
    }

    fun initSignalsmithReverbValues() {
        _signalsmithReverbPreset.value = SignalsmithReverbPresets.PRESET_DEFAULT
        val preset = SignalsmithReverbPresets.getPreset(SignalsmithReverbPresets.PRESET_DEFAULT)
        _signalsmithReverbDry.value = preset.dry
        _signalsmithReverbWet.value = preset.wet
        _signalsmithReverbRoomMs.value = preset.roomMs
        _signalsmithReverbDecaySec.value = preset.decaySec
        _signalsmithReverbEarly.value = preset.early
        _signalsmithReverbDetune.value = preset.detune
        _signalsmithReverbLowCutHz.value = preset.lowCutHz
        _signalsmithReverbHighCutHz.value = preset.highCutHz
        _signalsmithReverbLowDampRate.value = preset.lowDampRate
        _signalsmithReverbHighDampRate.value = preset.highDampRate
        setSignalsmithReverbParams()
    }


    // =========================
    // Tone Filter
    // =========================

    private val _isToneFilterEnabled = MutableStateFlow(false)
    val isToneFilterEnabled: StateFlow<Boolean> = _isToneFilterEnabled.asStateFlow()

    private val _toneFilterPreset = MutableStateFlow(ToneFilterPresets.PRESET_VOCAL_FOCUS)
    val toneFilterPreset: StateFlow<Int> = _toneFilterPreset.asStateFlow()

    private val _toneFilterLowCutHz = MutableStateFlow(700f)
    val toneFilterLowCutHz: StateFlow<Float> = _toneFilterLowCutHz.asStateFlow()

    private val _toneFilterHighCutHz = MutableStateFlow(12000f)
    val toneFilterHighCutHz: StateFlow<Float> = _toneFilterHighCutHz.asStateFlow()

    private val _toneFilterLowShelfDb = MutableStateFlow(2.5f)
    val toneFilterLowShelfDb: StateFlow<Float> = _toneFilterLowShelfDb.asStateFlow()

    private val _toneFilterHighShelfDb = MutableStateFlow(-2.5f)
    val toneFilterHighShelfDb: StateFlow<Float> = _toneFilterHighShelfDb.asStateFlow()

    fun updateIsToneFilterEnabled() {
        val newEnabled = !_isToneFilterEnabled.value
        _isToneFilterEnabled.value = newEnabled
        if (newEnabled) {
            setToneFilterParams()
        }
        signalsmithAudioProcessor.setToneFilterEnabled(newEnabled)
    }

    fun updateToneFilterPreset(presetIndex: Int) {
        _toneFilterPreset.value = presetIndex
        val preset = ToneFilterPresets.getPreset(presetIndex)
        _toneFilterLowCutHz.value = preset.lowCutHz
        _toneFilterHighCutHz.value = preset.highCutHz
        _toneFilterLowShelfDb.value = preset.lowShelfDb
        _toneFilterHighShelfDb.value = preset.highShelfDb
        setToneFilterParams()
    }

    fun updateToneFilterLowCutHz(value: Float) { _toneFilterLowCutHz.value = value }
    fun updateToneFilterHighCutHz(value: Float) { _toneFilterHighCutHz.value = value }
    fun updateToneFilterLowShelfDb(value: Float) { _toneFilterLowShelfDb.value = value }
    fun updateToneFilterHighShelfDb(value: Float) { _toneFilterHighShelfDb.value = value }

    fun setToneFilterParams() {
        signalsmithAudioProcessor.setToneFilterParams(
            _toneFilterLowCutHz.value,
            _toneFilterHighCutHz.value,
            _toneFilterLowShelfDb.value,
            _toneFilterHighShelfDb.value
        )
    }

    fun initToneFilterValues() {
        _toneFilterPreset.value = ToneFilterPresets.PRESET_VOCAL_FOCUS
        val preset = ToneFilterPresets.getPreset(ToneFilterPresets.PRESET_VOCAL_FOCUS)
        _toneFilterLowCutHz.value = preset.lowCutHz
        _toneFilterHighCutHz.value = preset.highCutHz
        _toneFilterLowShelfDb.value = preset.lowShelfDb
        _toneFilterHighShelfDb.value = preset.highShelfDb
        setToneFilterParams()
    }


    // =========================
    // Signalsmith EQ
    // =========================

    private val _isEqEnabled = MutableStateFlow(false)
    val isEqEnabled: StateFlow<Boolean> = _isEqEnabled.asStateFlow()

    private val _eqPreset = MutableStateFlow(SignalsmithEqPresets.PRESET_BASS_BOOST)
    val eqPreset: StateFlow<Int> = _eqPreset.asStateFlow()

    private val _eqBand1Freq = MutableStateFlow(60f)
    val eqBand1Freq: StateFlow<Float> = _eqBand1Freq.asStateFlow()
    private val _eqBand1Gain = MutableStateFlow(6f)
    val eqBand1Gain: StateFlow<Float> = _eqBand1Gain.asStateFlow()

    private val _eqBand2Freq = MutableStateFlow(250f)
    val eqBand2Freq: StateFlow<Float> = _eqBand2Freq.asStateFlow()
    private val _eqBand2Gain = MutableStateFlow(4f)
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
        val newEnabled = !_isEqEnabled.value
        _isEqEnabled.value = newEnabled
        if (newEnabled) {
            val freqs = floatArrayOf(
                _eqBand1Freq.value, _eqBand2Freq.value, _eqBand3Freq.value,
                _eqBand4Freq.value, _eqBand5Freq.value
            )
            val gains = floatArrayOf(
                _eqBand1Gain.value, _eqBand2Gain.value, _eqBand3Gain.value,
                _eqBand4Gain.value, _eqBand5Gain.value
            )
            for (i in 0..4) {
                signalsmithAudioProcessor.setEqBand(i, freqs[i], gains[i])
            }
        } else {
            clearPendingEqUpdates()
        }
        signalsmithAudioProcessor.setEqEnabled(newEnabled)
    }

    fun updateEqBand(band: Int, freq: Float, gain: Float) {
        when (band) {
            0 -> { _eqBand1Freq.value = freq; _eqBand1Gain.value = gain }
            1 -> { _eqBand2Freq.value = freq; _eqBand2Gain.value = gain }
            2 -> { _eqBand3Freq.value = freq; _eqBand3Gain.value = gain }
            3 -> { _eqBand4Freq.value = freq; _eqBand4Gain.value = gain }
            4 -> { _eqBand5Freq.value = freq; _eqBand5Gain.value = gain }
        }
        enqueueEqUpdate(band)
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

    fun updateEqPreset(presetIndex: Int) {
        _eqPreset.value = presetIndex
        clearPendingEqUpdates()
        val preset = SignalsmithEqPresets.getPreset(presetIndex)
        val gains = preset.gains
        _eqBand1Gain.value = gains[0]
        _eqBand2Gain.value = gains[1]
        _eqBand3Gain.value = gains[2]
        _eqBand4Gain.value = gains[3]
        _eqBand5Gain.value = gains[4]
        val freqs = listOf(60f, 250f, 1000f, 4000f, 12000f)
        for (i in 0..4) {
            signalsmithAudioProcessor.setEqBand(i, freqs[i], gains[i])
        }
    }

    fun initEqValues() {
        _eqPreset.value = SignalsmithEqPresets.PRESET_BASS_BOOST
        clearPendingEqUpdates()
        val preset = SignalsmithEqPresets.getPreset(SignalsmithEqPresets.PRESET_BASS_BOOST)
        val gains = preset.gains
        _eqBand1Gain.value = gains[0]
        _eqBand2Gain.value = gains[1]
        _eqBand3Gain.value = gains[2]
        _eqBand4Gain.value = gains[3]
        _eqBand5Gain.value = gains[4]
        val freqs = listOf(60f, 250f, 1000f, 4000f, 12000f)
        for (i in 0..4) {
            signalsmithAudioProcessor.setEqBand(i, freqs[i], gains[i])
        }
    }

    private fun enqueueEqUpdate(band: Int) {
        if (band !in 0..4) return
        synchronized(eqUpdateLock) {
            pendingEqBands.add(band)
        }
        scheduleEqDispatch()
    }

    private fun clearPendingEqUpdates() {
        synchronized(eqUpdateLock) {
            pendingEqBands.clear()
        }
    }

    private fun scheduleEqDispatch() {
        if (eqDispatchJob?.isActive == true) return
        eqDispatchJob = scope.launch {
            while (true) {
                delay(EQ_CONTROL_INTERVAL_MS)
                val bands = synchronized(eqUpdateLock) {
                    if (pendingEqBands.isEmpty()) {
                        eqDispatchJob = null
                        return@launch
                    }
                    val copy = pendingEqBands.toList()
                    pendingEqBands.clear()
                    copy
                }
                for (band in bands) {
                    signalsmithAudioProcessor.setEqBand(band, getEqFreqForBand(band), getEqGainForBand(band))
                }
            }
        }
    }

    private fun getEqFreqForBand(band: Int): Float {
        return when (band) {
            0 -> _eqBand1Freq.value
            1 -> _eqBand2Freq.value
            2 -> _eqBand3Freq.value
            3 -> _eqBand4Freq.value
            4 -> _eqBand5Freq.value
            else -> 1000f
        }
    }

    private fun getEqGainForBand(band: Int): Float {
        return when (band) {
            0 -> _eqBand1Gain.value
            1 -> _eqBand2Gain.value
            2 -> _eqBand3Gain.value
            3 -> _eqBand4Gain.value
            4 -> _eqBand5Gain.value
            else -> 0f
        }
    }

    fun release() {
        eqDispatchJob?.cancel()
    }
}
