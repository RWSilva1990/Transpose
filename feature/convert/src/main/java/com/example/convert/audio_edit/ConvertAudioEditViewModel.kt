package com.example.convert.audio_edit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.media.manager.AudioEffectsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConvertAudioEditViewModel @Inject constructor(
    private val audioEffectsManager: AudioEffectsManager
) : ViewModel() {

    // ---------------------------------------------------------
    // Pitch / Tempo
    // ---------------------------------------------------------
    val pitchValue = audioEffectsManager.pitchValue
    fun updatePitchValue(value: Int) = audioEffectsManager.updatePitchValue(value)
    fun setPitch() = audioEffectsManager.setPitch()
    fun pitchPlusOne() = audioEffectsManager.pitchPlusOne()
    fun pitchMinusOne() = audioEffectsManager.pitchMinusOne()
    fun initPitchValue() = audioEffectsManager.initPitchValue()

    val tempoValue = audioEffectsManager.tempoValue
    fun updateTempoValue(value: Int) = audioEffectsManager.updateTempoValue(value)
    fun setTempo() = audioEffectsManager.setTempo()
    fun tempoPlusOne() = audioEffectsManager.tempoPlusOne()
    fun tempoMinusOne() = audioEffectsManager.tempoMinusOne()
    fun initTempoValue() = audioEffectsManager.initTempoValue()


    // ---------------------------------------------------------
    // Equalizer
    // ---------------------------------------------------------
    val isEqualizerEnabled = audioEffectsManager.isEqualizerEnabled
    val equalizerCurrentPreset = audioEffectsManager.equalizerCurrentPreset
    val equalizerSettings = audioEffectsManager.equalizerSettings

    fun updateIsEqualizerEnabled() = audioEffectsManager.updateIsEqualizerEnabled()

    fun updateEqualizerWithPreset(presetIndex: Int) =
        audioEffectsManager.updateEqualizerWithPreset(presetIndex)

    fun setEqualizerWithCustomValue(bandIndex: Int) =
        audioEffectsManager.setEqualizerWithCustomValue(bandIndex)

    fun updateEqualizerBand(bandIndex: Int, value: Float) =
        audioEffectsManager.updateEqualizerBandLevel(bandIndex, value)

    fun initEqualizerValue() = audioEffectsManager.initEqualizerValue()

    fun disableEqualizer() = audioEffectsManager.disableEqualizer()


    // ---------------------------------------------------------
    // Reverb (Preset Reverb)
    // ---------------------------------------------------------
    val isReverbEnabled = audioEffectsManager.isReverbEnabled
    val reverbCurrentPreset = audioEffectsManager.reverbCurrentPreset
    val reverbValue = audioEffectsManager.reverbValue

    fun initReverbValue() = audioEffectsManager.initReverbValue()

    fun updateIsReverbEnabled() = audioEffectsManager.updateIsReverbEnabled()

    fun updateReverbValue(value: Int) = audioEffectsManager.updateReverbValue(value)

    fun setPresetReverb() = audioEffectsManager.setPresetReverb()

    fun updateReverbPreset(presetIndex: Int) = audioEffectsManager.updateReverbCurrentPreset(presetIndex)

    fun disableReverb() = audioEffectsManager.disableReverb()


    // ---------------------------------------------------------
    // Bass Boost
    // ---------------------------------------------------------
    val bassBoostValue = audioEffectsManager.bassBoostValue
    fun updateBassBoostValue(value: Int) = audioEffectsManager.updateBassBoostValue(value)
    fun setBassBoost() = audioEffectsManager.setBassBoost()
    fun initBassBoostValue() = audioEffectsManager.initBassBoostValue()


    // ---------------------------------------------------------
    // Loudness Enhancer
    // ---------------------------------------------------------
    val loudnessEnhancerValue = audioEffectsManager.loudnessEnhancerValue
    fun updateLoudnessEnhancerValue(value: Int) = audioEffectsManager.updateLoudnessEnhancerValue(value)
    fun setLoudnessEnhancer() = audioEffectsManager.setLoudnessEnhancer()
    fun initLoudnessEnhancerValue() = audioEffectsManager.initLoudnessEnhancerValue()


    // ---------------------------------------------------------
    // Virtualizer
    // ---------------------------------------------------------
    val virtualizerValue = audioEffectsManager.virtualizerValue
    fun updateVirtualizerValue(value: Int) = audioEffectsManager.updateVirtualizerValue(value)
    fun setVirtualizer() = audioEffectsManager.setVirtualizer()
    fun initVirtualizerValue() = audioEffectsManager.initVirtualizerValue()


    // ---------------------------------------------------------
    // Haptic Generator
    // ---------------------------------------------------------
    val isHapticGeneratorEnabled = audioEffectsManager.isHapticGeneratorEnabled
    fun updateHapticGeneratorValue() = audioEffectsManager.updateIsHapticGeneratorEnabled()


    // ---------------------------------------------------------
    // Environmental Reverb
    // ---------------------------------------------------------
    val isEnvironmentalReverbEnabled = audioEffectsManager.isEnvironmentalReverbEnabled
    val roomLevel = audioEffectsManager.roomLevel
    val roomHFLevel = audioEffectsManager.roomHFLevel
    val decayTime = audioEffectsManager.decayTime
    val decayHFRatio = audioEffectsManager.decayHFRatio
    val reflectionsLevel = audioEffectsManager.reflectionsLevel
    val reflectionsDelay = audioEffectsManager.reflectionsDelay
    val reverbLevel = audioEffectsManager.reverbLevel
    val reverbDelay = audioEffectsManager.reverbDelay
    val diffusion = audioEffectsManager.diffusion
    val density = audioEffectsManager.density


//    fun setEnvironmentalReverb(
//        isEnabled: Boolean,
//        roomLevel: Int,
//        roomHFLevel: Int,
//        decayTime: Int,
//        decayHFRatio: Int,
//        reflectionsLevel: Int,
//        reflectionsDelay: Int,
//        reverbLevel: Int,
//        reverbDelay: Int,
//        diffusion: Int,
//        density: Int
//    ) = audioEffectsManager.setEnvironmentalReverb(
//        isEnabled,
//        roomLevel,
//        roomHFLevel,
//        decayTime,
//        decayHFRatio,
//        reflectionsLevel,
//        reflectionsDelay,
//        reverbLevel,
//        reverbDelay,
//        diffusion,
//        density
//    )

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

    val isLimiterEnabled = audioEffectsManager.isLimiterEnabled
    val limiterInputGainDb = audioEffectsManager.limiterInputGainDb
    val limiterLimitDb = audioEffectsManager.limiterLimitDb
    val limiterAttackMs = audioEffectsManager.limiterAttackMs
    val limiterReleaseMs = audioEffectsManager.limiterReleaseMs
    fun updateIsLimiterEnabled() = audioEffectsManager.updateIsLimiterEnabled()
    fun updateLimiterInputGainDb(value: Float) = audioEffectsManager.updateLimiterInputGainDb(value)
    fun updateLimiterLimitDb(value: Float) = audioEffectsManager.updateLimiterLimitDb(value)
    fun updateLimiterAttackMs(value: Float) = audioEffectsManager.updateLimiterAttackMs(value)
    fun updateLimiterReleaseMs(value: Float) = audioEffectsManager.updateLimiterReleaseMs(value)
    fun setLimiterParams() = audioEffectsManager.setLimiterParams()
    fun initLimiterValues() = audioEffectsManager.initLimiterValues()
    fun applyLimiterPreset(inputGainDb: Float, limitDb: Float, attackMs: Float, releaseMs: Float) {
        audioEffectsManager.updateLimiterInputGainDb(inputGainDb)
        audioEffectsManager.updateLimiterLimitDb(limitDb)
        audioEffectsManager.updateLimiterAttackMs(attackMs)
        audioEffectsManager.updateLimiterReleaseMs(releaseMs)
        audioEffectsManager.setLimiterParams()
    }

    val isSignalsmithReverbEnabled = audioEffectsManager.isSignalsmithReverbEnabled
    val signalsmithReverbPreset = audioEffectsManager.signalsmithReverbPreset
    val signalsmithReverbDry = audioEffectsManager.signalsmithReverbDry
    val signalsmithReverbWet = audioEffectsManager.signalsmithReverbWet
    val signalsmithReverbRoomMs = audioEffectsManager.signalsmithReverbRoomMs
    val signalsmithReverbDecaySec = audioEffectsManager.signalsmithReverbDecaySec
    fun updateIsSignalsmithReverbEnabled() = audioEffectsManager.updateIsSignalsmithReverbEnabled()
    fun updateSignalsmithReverbPreset(presetIndex: Int) = audioEffectsManager.updateSignalsmithReverbPreset(presetIndex)
    fun updateSignalsmithReverbDry(value: Float) = audioEffectsManager.updateSignalsmithReverbDry(value)
    fun updateSignalsmithReverbWet(value: Float) = audioEffectsManager.updateSignalsmithReverbWet(value)
    fun updateSignalsmithReverbRoomMs(value: Float) = audioEffectsManager.updateSignalsmithReverbRoomMs(value)
    fun updateSignalsmithReverbDecaySec(value: Float) = audioEffectsManager.updateSignalsmithReverbDecaySec(value)
    fun setSignalsmithReverbParams() = audioEffectsManager.setSignalsmithReverbParams()
    fun initSignalsmithReverbValues() = audioEffectsManager.initSignalsmithReverbValues()

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

    val isCompressorEnabled = audioEffectsManager.isCompressorEnabled
    val compThresholdDb = audioEffectsManager.compThresholdDb
    val compRatio = audioEffectsManager.compRatio
    val compAttackMs = audioEffectsManager.compAttackMs
    val compReleaseMs = audioEffectsManager.compReleaseMs
    val compMakeupGainDb = audioEffectsManager.compMakeupGainDb
    fun updateIsCompressorEnabled() = audioEffectsManager.updateIsCompressorEnabled()
    fun updateCompThresholdDb(value: Float) = audioEffectsManager.updateCompThresholdDb(value)
    fun updateCompRatio(value: Float) = audioEffectsManager.updateCompRatio(value)
    fun updateCompAttackMs(value: Float) = audioEffectsManager.updateCompAttackMs(value)
    fun updateCompReleaseMs(value: Float) = audioEffectsManager.updateCompReleaseMs(value)
    fun updateCompMakeupGainDb(value: Float) = audioEffectsManager.updateCompMakeupGainDb(value)
    fun setCompressorParams() = audioEffectsManager.setCompressorParams()
    fun initCompressorValues() = audioEffectsManager.initCompressorValues()
    fun applyCompressorPreset(thresholdDb: Float, ratio: Float, attackMs: Float, releaseMs: Float, makeupGainDb: Float) {
        audioEffectsManager.updateCompThresholdDb(thresholdDb)
        audioEffectsManager.updateCompRatio(ratio)
        audioEffectsManager.updateCompAttackMs(attackMs)
        audioEffectsManager.updateCompReleaseMs(releaseMs)
        audioEffectsManager.updateCompMakeupGainDb(makeupGainDb)
        audioEffectsManager.setCompressorParams()
    }

    val isPitchDetectionEnabled = audioEffectsManager.isPitchDetectionEnabled
    fun updateIsPitchDetectionEnabled() = audioEffectsManager.updateIsPitchDetectionEnabled()
    fun getDetectedPitch(): Float = audioEffectsManager.getDetectedPitch()

    val isHrtfEnabled = audioEffectsManager.isHrtfEnabled
    val hrtfIntensity = audioEffectsManager.hrtfIntensity
    val hrtfAzimuth = audioEffectsManager.hrtfAzimuth
    fun updateIsHrtfEnabled() = audioEffectsManager.updateIsHrtfEnabled()
    fun updateHrtfIntensity(value: Float) = audioEffectsManager.updateHrtfIntensity(value)
    fun updateHrtfAzimuth(azimuth: Int) = audioEffectsManager.updateHrtfAzimuth(azimuth)
    fun initHrtfValues() = audioEffectsManager.initHrtfValues()

    // ---------------------------------------------------------
    // Stereo Widener
    // ---------------------------------------------------------
    val isStereoWidenerEnabled = audioEffectsManager.isStereoWidenerEnabled
    val stereoWidenerWidth = audioEffectsManager.stereoWidenerWidth
    fun updateIsStereoWidenerEnabled() = audioEffectsManager.updateIsStereoWidenerEnabled()
    fun updateStereoWidenerWidth(value: Float) = audioEffectsManager.updateStereoWidenerWidth(value)
    fun setStereoWidenerParams() = audioEffectsManager.setStereoWidenerParams()
    fun initStereoWidenerValues() = audioEffectsManager.initStereoWidenerValues()
    fun applyStereoWidenerPreset(width: Float) {
        audioEffectsManager.updateStereoWidenerWidth(width)
        audioEffectsManager.setStereoWidenerParams()
    }

    // ---------------------------------------------------------
    // DJ Filter
    // ---------------------------------------------------------
    val isDjFilterEnabled = audioEffectsManager.isDjFilterEnabled
    val djFilterPosition = audioEffectsManager.djFilterPosition
    val djFilterResonance = audioEffectsManager.djFilterResonance
    fun updateIsDjFilterEnabled() = audioEffectsManager.updateIsDjFilterEnabled()
    fun updateDjFilterPosition(value: Float) = audioEffectsManager.updateDjFilterPosition(value)
    fun updateDjFilterResonance(value: Float) = audioEffectsManager.updateDjFilterResonance(value)
    fun setDjFilterParams() = audioEffectsManager.setDjFilterParams()
    fun initDjFilterValues() = audioEffectsManager.initDjFilterValues()

}
