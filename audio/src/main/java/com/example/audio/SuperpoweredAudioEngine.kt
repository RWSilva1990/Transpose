package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.util.Log
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Superpowered Audio Engine - PCM 입력 방식
 * ExoPlayer에서 디코딩된 PCM을 받아서 Superpowered로 처리 후 출력
 */
@Singleton
class SuperpoweredAudioEngine @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "SuperpoweredEngine"

        init {
            System.loadLibrary("superpowered_audio")
        }
    }

    private var isInitialized = false
    private var sampleRate = 44100
    private var channelCount = 2

    /**
     * 오디오 엔진 초기화
     */
    fun initialize(sampleRate: Int, channelCount: Int, bufferSize: Int) {
        if (isInitialized) {
            Log.w(TAG, "Already initialized, reinitializing...")
            release()
        }

        this.sampleRate = sampleRate
        this.channelCount = channelCount

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val deviceSampleRate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
            ?.toIntOrNull() ?: 44100
        val deviceBufferSize = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
            ?.toIntOrNull() ?: 256

        Log.d(TAG, "Initializing: inputSampleRate=$sampleRate, inputChannels=$channelCount, " +
                "deviceSampleRate=$deviceSampleRate, deviceBufferSize=$deviceBufferSize")

        nativeInit(sampleRate, channelCount, deviceSampleRate, deviceBufferSize)
        isInitialized = true
        Log.d(TAG, "Initialized successfully")
    }

    /**
     * PCM 데이터 전달 (ExoPlayer에서 호출)
     */
    fun writePcm(buffer: ByteBuffer, sizeInBytes: Int, presentationTimeUs: Long) {
        if (!isInitialized) {
            Log.w(TAG, "Not initialized, ignoring PCM data")
            return
        }
        nativeWritePcm(buffer, sizeInBytes, presentationTimeUs)
    }

    /**
     * 재생 시작
     */
    fun play() {
        if (!isInitialized) return
        Log.d(TAG, "Play")
        nativePlay()
    }

    /**
     * 일시정지
     */
    fun pause() {
        if (!isInitialized) return
        Log.d(TAG, "Pause")
        nativePause()
    }

    /**
     * 버퍼 비우기 (seek 시 호출)
     */
    fun flush() {
        if (!isInitialized) return
        Log.d(TAG, "Flush")
        nativeFlush()
    }

    /**
     * 리셋
     */
    fun reset() {
        if (!isInitialized) return
        Log.d(TAG, "Reset")
        nativeReset()
    }

    /**
     * 리소스 해제
     */
    fun release() {
        if (!isInitialized) return
        Log.d(TAG, "Release")
        nativeRelease()
        isInitialized = false
    }

    /**
     * 피치 설정 (0.5 ~ 2.0)
     */
    fun setPitch(pitch: Float) {
        if (!isInitialized) return
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        Log.d(TAG, "SetPitch: $clampedPitch")
        nativeSetPitch(clampedPitch)
    }

    /**
     * 템포 설정 (0.5 ~ 2.0)
     */
    fun setTempo(tempo: Float) {
        if (!isInitialized) return
        val clampedTempo = tempo.coerceIn(0.5f, 2.0f)
        Log.d(TAG, "SetTempo: $clampedTempo")
        nativeSetTempo(clampedTempo)
    }

    /**
     * 현재 재생 위치 (마이크로초)
     */
    fun getCurrentPositionUs(): Long {
        if (!isInitialized) return 0L
        return nativeGetCurrentPositionUs()
    }

    /**
     * 재생 중 여부
     */
    fun isPlaying(): Boolean {
        if (!isInitialized) return false
        return nativeIsPlaying()
    }

    /**
     * 초기화 여부
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * 버퍼 사용 가능 공간 (샘플 단위)
     */
    fun getBufferAvailableSpace(): Int {
        if (!isInitialized) return 0
        return nativeGetBufferAvailableSpace()
    }

    fun getBufferUsedSamples(): Int {
        if (!isInitialized) return 0
        return nativeGetBufferUsedSamples()
    }

    fun getPushRejectedCount(): Int {
        if (!isInitialized) return 0
        return nativeGetPushRejectedCount()
    }

    fun getUnderrunCount(): Int {
        if (!isInitialized) return 0
        return nativeGetUnderrunCount()
    }

    fun getSilenceCount(): Int {
        if (!isInitialized) return 0
        return nativeGetSilenceCount()
    }

    fun resetStats() {
        if (!isInitialized) return
        nativeResetStats()
    }

    fun setSeekPosition(positionUs: Long) {
        if (!isInitialized) return
        nativeSetSeekPosition(positionUs)
    }

    // Native methods
    private external fun nativeInit(
        inputSampleRate: Int,
        inputChannelCount: Int,
        outputSampleRate: Int,
        outputBufferSize: Int
    )

    fun hasPendingData(): Boolean {
        if (!isInitialized) return false
        return nativeHasPendingData()
    }

    private external fun nativeHasPendingData(): Boolean
    private external fun nativeWritePcm(buffer: ByteBuffer, sizeInBytes: Int, presentationTimeUs: Long)
    private external fun nativePlay()
    private external fun nativePause()
    private external fun nativeFlush()
    private external fun nativeReset()
    private external fun nativeRelease()
    private external fun nativeSetPitch(pitch: Float)
    private external fun nativeSetTempo(tempo: Float)
    private external fun nativeGetCurrentPositionUs(): Long
    private external fun nativeIsPlaying(): Boolean
    private external fun nativeGetBufferAvailableSpace(): Int
    private external fun nativeGetBufferUsedSamples(): Int
    private external fun nativeGetPushRejectedCount(): Int
    private external fun nativeGetUnderrunCount(): Int
    private external fun nativeGetSilenceCount(): Int
    private external fun nativeResetStats()
    private external fun nativeSetSeekPosition(positionUs: Long)
}
