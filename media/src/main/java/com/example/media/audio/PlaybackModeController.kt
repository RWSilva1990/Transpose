package com.example.media.audio

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class PlaybackModeController @Inject constructor(
    private val hybridAudioSink: HybridAudioSink,
    private val superpoweredBridge: SuperpoweredBridge
) {
    companion object {
        const val DEFAULT_PITCH_SEMITONES = -2.0
        const val DEFAULT_TEMPO_SEMITONES = 0.0
    }

    private fun semitonesToRatio(semitones: Double): Float = 2.0.pow(semitones / 12.0).toFloat()

    var currentMode: PlaybackMode
        get() = hybridAudioSink.mode
        set(value) {
            hybridAudioSink.mode = value
        }

    fun switchToVideoMode() {
        hybridAudioSink.mode = PlaybackMode.VIDEO
    }

    fun switchToAudioMode() {
        hybridAudioSink.mode = PlaybackMode.AUDIO
        applyDefaultPitchAndTempo()
    }
    
    fun switchToVideoWithDspMode() {
        hybridAudioSink.mode = PlaybackMode.VIDEO_WITH_DSP
        applyDefaultPitchAndTempo()
    }

    private fun applyDefaultPitchAndTempo() {
        superpoweredBridge.setPitch(semitonesToRatio(DEFAULT_PITCH_SEMITONES))
        superpoweredBridge.setTempo(semitonesToRatio(DEFAULT_TEMPO_SEMITONES))
    }

    fun setPitch(pitch: Float) {
        when (hybridAudioSink.mode) {
            PlaybackMode.VIDEO -> {
            }
            PlaybackMode.AUDIO, PlaybackMode.VIDEO_WITH_DSP -> {
                superpoweredBridge.setPitch(pitch)
            }
        }
    }

    fun setTempo(tempo: Float) {
        when (hybridAudioSink.mode) {
            PlaybackMode.VIDEO -> {
            }
            PlaybackMode.AUDIO, PlaybackMode.VIDEO_WITH_DSP -> {
                superpoweredBridge.setTempo(tempo)
            }
        }
    }
}