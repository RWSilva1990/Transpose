package com.example.media.manager

import com.example.media.audio.SignalsmithAudioProcessor
import com.example.media.audio.VocalRemovalProcessor
import com.example.media.audio_effect.data.eq.SignalsmithEqPresets
import com.example.media.audio_effect.data.reverb.SignalsmithReverbPresets
import dagger.Lazy
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
    private val vocalRemovalProcessor: VocalRemovalProcessor,
    private val mediaPlaybackManager: Lazy<MediaPlaybackManager>
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
    private var lastLimiterInputGainDb = Float.NaN
    private var lastLimiterLimitDb = Float.NaN
    private var lastLimiterAttackMs = Float.NaN
    private var lastLimiterReleaseMs = Float.NaN
    private var lastReverbDry = Float.NaN
    private var lastReverbWet = Float.NaN
    private var lastReverbRoomMs = Float.NaN
    private var lastReverbDecaySec = Float.NaN

    // =========================
    // Pitch / Tempo
    // =========================

    private fun semitonesToUiValue(semitones: Float): Int = ((semitones * 10) + 100).toInt()

    val pitchValue: StateFlow<Int> = signalsmithAudioProcessor.pitchSemitonesFlow
        .map { semitonesToUiValue(it) }
        .stateIn(scope, SharingStarted.Eagerly, 100)

    private fun uiValueToSemitones(uiValue: Int): Float = (uiValue - 100) / 10f
    private fun semitonesToRate(semitones: Float): Float = Math.pow(2.0, semitones.toDouble() / 12.0).toFloat()

    fun pitchPlusOne() {
        signalsmithAudioProcessor.addPitchSemitone()
    }

    fun pitchMinusOne() {
        signalsmithAudioProcessor.subtractPitchSemitone()
    }

    fun initPitchValue() {
        signalsmithAudioProcessor.resetPitch()
    }

    fun updatePitchValue(uiValue: Int) {
        signalsmithAudioProcessor.setPitchSemitones(uiValueToSemitones(uiValue))
    }

    fun setPitch() {
    }

    private val _tempoSemitones = MutableStateFlow(0f)

    val tempoValue: StateFlow<Int> = _tempoSemitones
        .map { semitonesToUiValue(it) }
        .stateIn(scope, SharingStarted.Eagerly, 100)

    private fun applyTempoToPlayer() {
        val tempoRate = semitonesToRate(_tempoSemitones.value)
        mediaPlaybackManager.get().setPlaybackSpeed(tempoRate)
    }

    fun tempoPlusOne() {
        _tempoSemitones.value = (_tempoSemitones.value + 1f).coerceIn(-24f, 24f)
        applyTempoToPlayer()
    }

    fun tempoMinusOne() {
        _tempoSemitones.value = (_tempoSemitones.value - 1f).coerceIn(-24f, 24f)
        applyTempoToPlayer()
    }

    fun initTempoValue() {
        _tempoSemitones.value = 0f
        applyTempoToPlayer()
    }

    fun updateTempoValue(uiValue: Int) {
        _tempoSemitones.value = uiValueToSemitones(uiValue).coerceIn(-24f, 24f)
        applyTempoToPlayer()
    }

    fun setTempo() {
    }

    private val _isVocalRemovalEnabled = MutableStateFlow(false)
    val isVocalRemovalEnabled: StateFlow<Boolean> = _isVocalRemovalEnabled.asStateFlow()

    private val _vocalRemovalMix = MutableStateFlow(1.0f)
    val vocalRemovalMix: StateFlow<Float> = _vocalRemovalMix.asStateFlow()

    fun updateIsVocalRemovalEnabled() {
        _isVocalRemovalEnabled.value = !_isVocalRemovalEnabled.value
        vocalRemovalProcessor.enabled = _isVocalRemovalEnabled.value
    }

    fun updateVocalRemovalMix(value: Float) {
        _vocalRemovalMix.value = value.coerceIn(0f, 1f)
        vocalRemovalProcessor.mixRatio = _vocalRemovalMix.value
    }

    fun prewarmVocalRemovalModel() {
        vocalRemovalProcessor.prewarm()
    }

    fun initVocalRemovalValues() {
        _vocalRemovalMix.value = 1.0f
        vocalRemovalProcessor.mixRatio = 1.0f
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
        if (
            isNearlySame(_limiterInputGainDb.value, lastLimiterInputGainDb) &&
            isNearlySame(_limiterLimitDb.value, lastLimiterLimitDb) &&
            isNearlySame(_limiterAttackMs.value, lastLimiterAttackMs) &&
            isNearlySame(_limiterReleaseMs.value, lastLimiterReleaseMs)
        ) return

        lastLimiterInputGainDb = _limiterInputGainDb.value
        lastLimiterLimitDb = _limiterLimitDb.value
        lastLimiterAttackMs = _limiterAttackMs.value
        lastLimiterReleaseMs = _limiterReleaseMs.value
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

    private val _signalsmithReverbPreset = MutableStateFlow(SignalsmithReverbPresets.PRESET_DEFAULT)
    val signalsmithReverbPreset: StateFlow<Int> = _signalsmithReverbPreset.asStateFlow()

    private val _signalsmithReverbDry = MutableStateFlow(0.95f)
    val signalsmithReverbDry: StateFlow<Float> = _signalsmithReverbDry.asStateFlow()

    private val _signalsmithReverbWet = MutableStateFlow(0.2f)
    val signalsmithReverbWet: StateFlow<Float> = _signalsmithReverbWet.asStateFlow()

    private val _signalsmithReverbRoomMs = MutableStateFlow(25f)
    val signalsmithReverbRoomMs: StateFlow<Float> = _signalsmithReverbRoomMs.asStateFlow()

    private val _signalsmithReverbDecaySec = MutableStateFlow(0.5f)
    val signalsmithReverbDecaySec: StateFlow<Float> = _signalsmithReverbDecaySec.asStateFlow()

    fun updateIsSignalsmithReverbEnabled() {
        _isSignalsmithReverbEnabled.value = !_isSignalsmithReverbEnabled.value
        signalsmithAudioProcessor.setReverbEnabled(_isSignalsmithReverbEnabled.value)
    }

    fun updateSignalsmithReverbPreset(presetIndex: Int) {
        _signalsmithReverbPreset.value = presetIndex
        val preset = SignalsmithReverbPresets.getPreset(presetIndex)
        _signalsmithReverbDry.value = preset.dry
        _signalsmithReverbWet.value = preset.wet
        _signalsmithReverbRoomMs.value = preset.roomMs
        _signalsmithReverbDecaySec.value = preset.decaySec
        setSignalsmithReverbParams()
    }

    fun updateSignalsmithReverbDry(value: Float) {
        _signalsmithReverbDry.value = value
        _signalsmithReverbPreset.value = -1
    }
    fun updateSignalsmithReverbWet(value: Float) {
        _signalsmithReverbWet.value = value
        _signalsmithReverbPreset.value = -1
    }
    fun updateSignalsmithReverbRoomMs(value: Float) {
        _signalsmithReverbRoomMs.value = value
        _signalsmithReverbPreset.value = -1
    }
    fun updateSignalsmithReverbDecaySec(value: Float) {
        _signalsmithReverbDecaySec.value = value
        _signalsmithReverbPreset.value = -1
    }

    fun setSignalsmithReverbParams() {
        if (
            isNearlySame(_signalsmithReverbDry.value, lastReverbDry) &&
            isNearlySame(_signalsmithReverbWet.value, lastReverbWet) &&
            isNearlySame(_signalsmithReverbRoomMs.value, lastReverbRoomMs) &&
            isNearlySame(_signalsmithReverbDecaySec.value, lastReverbDecaySec)
        ) return

        lastReverbDry = _signalsmithReverbDry.value
        lastReverbWet = _signalsmithReverbWet.value
        lastReverbRoomMs = _signalsmithReverbRoomMs.value
        lastReverbDecaySec = _signalsmithReverbDecaySec.value
        signalsmithAudioProcessor.setReverbParams(
            _signalsmithReverbDry.value, _signalsmithReverbWet.value,
            _signalsmithReverbRoomMs.value, _signalsmithReverbDecaySec.value
        )
    }

    fun initSignalsmithReverbValues() {
        _signalsmithReverbPreset.value = SignalsmithReverbPresets.PRESET_DEFAULT
        val preset = SignalsmithReverbPresets.getPreset(SignalsmithReverbPresets.PRESET_DEFAULT)
        _signalsmithReverbDry.value = preset.dry
        _signalsmithReverbWet.value = preset.wet
        _signalsmithReverbRoomMs.value = preset.roomMs
        _signalsmithReverbDecaySec.value = preset.decaySec
        setSignalsmithReverbParams()
    }


    // =========================
    // Signalsmith EQ
    // =========================

    private val _isEqEnabled = MutableStateFlow(false)
    val isEqEnabled: StateFlow<Boolean> = _isEqEnabled.asStateFlow()

    private val _eqPreset = MutableStateFlow(SignalsmithEqPresets.PRESET_FLAT)
    val eqPreset: StateFlow<Int> = _eqPreset.asStateFlow()

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
        if (!_isEqEnabled.value) {
            clearPendingEqUpdates()
        }
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
        enqueueEqUpdate(band)
    }

    fun updateEqBandGain(band: Int, gain: Float) {
        _eqPreset.value = -1
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
        _eqPreset.value = SignalsmithEqPresets.PRESET_FLAT
        clearPendingEqUpdates()
        _eqBand1Gain.value = 0f; _eqBand2Gain.value = 0f; _eqBand3Gain.value = 0f
        _eqBand4Gain.value = 0f; _eqBand5Gain.value = 0f
        for (i in 0..4) {
            val freq = when (i) { 0 -> 60f; 1 -> 250f; 2 -> 1000f; 3 -> 4000f; else -> 12000f }
            signalsmithAudioProcessor.setEqBand(i, freq, 0f)
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

    private fun isNearlySame(a: Float, b: Float, epsilon: Float = 0.0001f): Boolean {
        return !a.isNaN() && !b.isNaN() && abs(a - b) <= epsilon
    }

    // =========================
    // Signalsmith Pitch Detection
    // =========================

    private val _isPitchDetectionEnabled = MutableStateFlow(false)
    val isPitchDetectionEnabled: StateFlow<Boolean> = _isPitchDetectionEnabled.asStateFlow()

    fun updateIsPitchDetectionEnabled() {
        _isPitchDetectionEnabled.value = !_isPitchDetectionEnabled.value
        signalsmithAudioProcessor.setPitchDetectionEnabled(_isPitchDetectionEnabled.value)
    }

    fun getDetectedPitch(): Float = signalsmithAudioProcessor.getDetectedPitch()

    fun release() {
        eqDispatchJob?.cancel()
    }
}
