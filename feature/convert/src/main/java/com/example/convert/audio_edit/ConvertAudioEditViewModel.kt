package com.example.convert.audio_edit

import androidx.lifecycle.ViewModel
import com.example.media.manager.AudioEffectsManager
import dagger.hilt.android.lifecycle.HiltViewModel
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

}
