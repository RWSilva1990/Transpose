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

    private fun applyDefaultPitchAndTempo() {
        superpoweredBridge.setPitch(semitonesToRatio(DEFAULT_PITCH_SEMITONES))
        superpoweredBridge.setTempo(semitonesToRatio(DEFAULT_TEMPO_SEMITONES))
    }

    /**
     * 피치 설정 (현재 모드에 맞게 자동 적용)
     */
    fun setPitch(pitch: Float) {
        when (hybridAudioSink.mode) {
            PlaybackMode.VIDEO -> {
                // ExoPlayer는 PlaybackParameters로 설정해야 함
                // 이건 외부에서 player.setPlaybackParameters() 호출 필요
            }

            PlaybackMode.AUDIO -> {
                superpoweredBridge.setPitch(pitch)
            }
        }
    }

    /**
     * 템포 설정 (현재 모드에 맞게 자동 적용)
     */
    fun setTempo(tempo: Float) {
        when (hybridAudioSink.mode) {
            PlaybackMode.VIDEO -> {
                // ExoPlayer는 PlaybackParameters로 설정해야 함
            }

            PlaybackMode.AUDIO -> {
                superpoweredBridge.setTempo(tempo)
            }
        }
    }
}