package com.example.convert.audio_edit

import androidx.lifecycle.ViewModel
import com.example.media.manager.AudioEffectsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConvertAudioEditViewModel @Inject constructor(
    private val audioEffectsManager: AudioEffectsManager
) : ViewModel() {
    init {
        // Preload model when edit screen/viewmodel is created to reduce first-enable hitch.
        audioEffectsManager.prewarmVocalRemovalModel()
    }

    // Pitch / Tempo
    val pitchValue = audioEffectsManager.pitchValue
    fun pitchPlusOne() = audioEffectsManager.pitchPlusOne()
    fun pitchMinusOne() = audioEffectsManager.pitchMinusOne()
    fun initPitchValue() = audioEffectsManager.initPitchValue()
    fun updatePitchValue(uiValue: Int) = audioEffectsManager.updatePitchValue(uiValue)
    fun setPitch() = audioEffectsManager.setPitch()

    val tempoValue = audioEffectsManager.tempoValue
    fun tempoPlusOne() = audioEffectsManager.tempoPlusOne()
    fun tempoMinusOne() = audioEffectsManager.tempoMinusOne()
    fun initTempoValue() = audioEffectsManager.initTempoValue()
    fun updateTempoValue(uiValue: Int) = audioEffectsManager.updateTempoValue(uiValue)
    fun setTempo() = audioEffectsManager.setTempo()

    // Signalsmith Chorus
    val isChorusEnabled = audioEffectsManager.isChorusEnabled
    val chorusMix = audioEffectsManager.chorusMix
    val chorusDepthMs = audioEffectsManager.chorusDepthMs
    val chorusDetune = audioEffectsManager.chorusDetune
    val chorusStereo = audioEffectsManager.chorusStereo
    fun updateIsChorusEnabled() = audioEffectsManager.updateIsChorusEnabled()
    fun updateChorusMix(value: Float) = audioEffectsManager.updateChorusMix(value)
    fun updateChorusDepthMs(value: Float) = audioEffectsManager.updateChorusDepthMs(value)
    fun updateChorusDetune(value: Float) = audioEffectsManager.updateChorusDetune(value)
    fun updateChorusStereo(value: Float) = audioEffectsManager.updateChorusStereo(value)
    fun setChorusParams() = audioEffectsManager.setChorusParams()
    fun initChorusValues() = audioEffectsManager.initChorusValues()
    fun applyChorusPreset(mix: Float, depthMs: Float, detune: Float, stereo: Float) {
        audioEffectsManager.updateChorusMix(mix)
        audioEffectsManager.updateChorusDepthMs(depthMs)
        audioEffectsManager.updateChorusDetune(detune)
        audioEffectsManager.updateChorusStereo(stereo)
        audioEffectsManager.setChorusParams()
    }

    // Reverb+
    val isReverbPlusEnabled = audioEffectsManager.isReverbPlusEnabled
    val reverbPlusPreset = audioEffectsManager.reverbPlusPreset
    val reverbPlusDry = audioEffectsManager.reverbPlusDry
    val reverbPlusWet = audioEffectsManager.reverbPlusWet
    val reverbPlusRoomSize = audioEffectsManager.reverbPlusRoomSize
    val reverbPlusDamping = audioEffectsManager.reverbPlusDamping
    fun updateIsReverbPlusEnabled() = audioEffectsManager.updateIsReverbPlusEnabled()
    fun updateReverbPlusPreset(presetIndex: Int) = audioEffectsManager.updateReverbPlusPreset(presetIndex)
    fun updateReverbPlusDry(value: Float) = audioEffectsManager.updateReverbPlusDry(value)
    fun updateReverbPlusWet(value: Float) = audioEffectsManager.updateReverbPlusWet(value)
    fun updateReverbPlusRoomSize(value: Float) = audioEffectsManager.updateReverbPlusRoomSize(value)
    fun updateReverbPlusDamping(value: Float) = audioEffectsManager.updateReverbPlusDamping(value)
    fun setReverbPlusParams() = audioEffectsManager.setReverbPlusParams()
    fun initReverbPlusValues() = audioEffectsManager.initReverbPlusValues()

    // Signalsmith Reverb
    val isSignalsmithReverbEnabled = audioEffectsManager.isSignalsmithReverbEnabled
    val signalsmithReverbPreset = audioEffectsManager.signalsmithReverbPreset
    val signalsmithReverbDry = audioEffectsManager.signalsmithReverbDry
    val signalsmithReverbWet = audioEffectsManager.signalsmithReverbWet
    val signalsmithReverbRoomMs = audioEffectsManager.signalsmithReverbRoomMs
    val signalsmithReverbDecaySec = audioEffectsManager.signalsmithReverbDecaySec
    val signalsmithReverbEarly = audioEffectsManager.signalsmithReverbEarly
    val signalsmithReverbDetune = audioEffectsManager.signalsmithReverbDetune
    val signalsmithReverbLowCutHz = audioEffectsManager.signalsmithReverbLowCutHz
    val signalsmithReverbHighCutHz = audioEffectsManager.signalsmithReverbHighCutHz
    val signalsmithReverbLowDampRate = audioEffectsManager.signalsmithReverbLowDampRate
    val signalsmithReverbHighDampRate = audioEffectsManager.signalsmithReverbHighDampRate
    fun updateIsSignalsmithReverbEnabled() = audioEffectsManager.updateIsSignalsmithReverbEnabled()
    fun updateSignalsmithReverbPreset(presetIndex: Int) = audioEffectsManager.updateSignalsmithReverbPreset(presetIndex)
    fun updateSignalsmithReverbDry(value: Float) = audioEffectsManager.updateSignalsmithReverbDry(value)
    fun updateSignalsmithReverbWet(value: Float) = audioEffectsManager.updateSignalsmithReverbWet(value)
    fun updateSignalsmithReverbRoomMs(value: Float) = audioEffectsManager.updateSignalsmithReverbRoomMs(value)
    fun updateSignalsmithReverbDecaySec(value: Float) = audioEffectsManager.updateSignalsmithReverbDecaySec(value)
    fun updateSignalsmithReverbEarly(value: Float) = audioEffectsManager.updateSignalsmithReverbEarly(value)
    fun updateSignalsmithReverbDetune(value: Float) = audioEffectsManager.updateSignalsmithReverbDetune(value)
    fun updateSignalsmithReverbLowCutHz(value: Float) = audioEffectsManager.updateSignalsmithReverbLowCutHz(value)
    fun updateSignalsmithReverbHighCutHz(value: Float) = audioEffectsManager.updateSignalsmithReverbHighCutHz(value)
    fun updateSignalsmithReverbLowDampRate(value: Float) = audioEffectsManager.updateSignalsmithReverbLowDampRate(value)
    fun updateSignalsmithReverbHighDampRate(value: Float) = audioEffectsManager.updateSignalsmithReverbHighDampRate(value)
    fun setSignalsmithReverbParams() = audioEffectsManager.setSignalsmithReverbParams()
    fun initSignalsmithReverbValues() = audioEffectsManager.initSignalsmithReverbValues()

    // Tone Filter
    val isToneFilterEnabled = audioEffectsManager.isToneFilterEnabled
    val toneFilterPreset = audioEffectsManager.toneFilterPreset
    val toneFilterLowCutHz = audioEffectsManager.toneFilterLowCutHz
    val toneFilterHighCutHz = audioEffectsManager.toneFilterHighCutHz
    val toneFilterLowShelfDb = audioEffectsManager.toneFilterLowShelfDb
    val toneFilterHighShelfDb = audioEffectsManager.toneFilterHighShelfDb
    fun updateIsToneFilterEnabled() = audioEffectsManager.updateIsToneFilterEnabled()
    fun updateToneFilterPreset(presetIndex: Int) = audioEffectsManager.updateToneFilterPreset(presetIndex)
    fun updateToneFilterLowCutHz(value: Float) = audioEffectsManager.updateToneFilterLowCutHz(value)
    fun updateToneFilterHighCutHz(value: Float) = audioEffectsManager.updateToneFilterHighCutHz(value)
    fun updateToneFilterLowShelfDb(value: Float) = audioEffectsManager.updateToneFilterLowShelfDb(value)
    fun updateToneFilterHighShelfDb(value: Float) = audioEffectsManager.updateToneFilterHighShelfDb(value)
    fun setToneFilterParams() = audioEffectsManager.setToneFilterParams()
    fun initToneFilterValues() = audioEffectsManager.initToneFilterValues()

    // Signalsmith EQ
    val isEqEnabled = audioEffectsManager.isEqEnabled
    val eqPreset = audioEffectsManager.eqPreset
    val eqBand1Gain = audioEffectsManager.eqBand1Gain
    val eqBand2Gain = audioEffectsManager.eqBand2Gain
    val eqBand3Gain = audioEffectsManager.eqBand3Gain
    val eqBand4Gain = audioEffectsManager.eqBand4Gain
    val eqBand5Gain = audioEffectsManager.eqBand5Gain
    fun updateIsEqEnabled() = audioEffectsManager.updateIsEqEnabled()
    fun updateEqPreset(presetIndex: Int) = audioEffectsManager.updateEqPreset(presetIndex)
    fun updateEqBandGain(band: Int, gain: Float) = audioEffectsManager.updateEqBandGain(band, gain)
    fun initEqValues() = audioEffectsManager.initEqValues()

    // Vocal Removal
    val isVocalRemovalEnabled = audioEffectsManager.isVocalRemovalEnabled
    val vocalRemovalMix = audioEffectsManager.vocalRemovalMix
    val isVocalOnlyMode = audioEffectsManager.isVocalOnlyMode
    fun updateIsVocalRemovalEnabled() = audioEffectsManager.updateIsVocalRemovalEnabled()
    fun updateIsVocalOnlyMode() = audioEffectsManager.updateIsVocalOnlyMode()
    fun updateVocalRemovalMix(value: Float) = audioEffectsManager.updateVocalRemovalMix(value)
    fun initVocalRemovalValues() = audioEffectsManager.initVocalRemovalValues()
}
