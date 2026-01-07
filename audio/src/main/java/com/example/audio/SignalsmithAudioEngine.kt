package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Process
import android.util.Log
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalsmithAudioEngine @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "SignalsmithEngine"

        init {
            System.loadLibrary("signalsmith_audio")
        }
    }

    private var isInitialized = false
    private var sampleRate = 44100
    private var channelCount = 2

    private var audioTrack: AudioTrack? = null
    private var renderThread: Thread? = null
    private val isRenderThreadRunning = AtomicBoolean(false)
    private var outputBufferSize = 512

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

        outputBufferSize = deviceBufferSize

        Log.d(TAG, "Initializing: inputSampleRate=$sampleRate, deviceSampleRate=$deviceSampleRate, deviceBufferSize=$deviceBufferSize")

        nativeInit(sampleRate, channelCount, bufferSize)
        initAudioTrack(sampleRate, channelCount)
        isInitialized = true
        
        startRenderThread()
        audioTrack?.play()

        Log.d(TAG, "Initialized successfully")
    }

    private fun initAudioTrack(sampleRate: Int, channelCount: Int) {
        val channelConfig = if (channelCount == 2) {
            AudioFormat.CHANNEL_OUT_STEREO
        } else {
            AudioFormat.CHANNEL_OUT_MONO
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val bufferSize = maxOf(minBufferSize, outputBufferSize * channelCount * 2 * 4)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()

        Log.d(TAG, "AudioTrack created: bufferSize=$bufferSize, minBufferSize=$minBufferSize")
    }

    private fun startRenderThread() {
        if (isRenderThreadRunning.get()) return

        isRenderThreadRunning.set(true)
        renderThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)

            val outputBuffer = ShortArray(outputBufferSize * channelCount)

            while (isRenderThreadRunning.get()) {
                val framesProcessed = nativeProcess(outputBuffer, outputBufferSize)

                if (framesProcessed > 0 && nativeIsPlaying()) {
                    audioTrack?.write(outputBuffer, 0, framesProcessed * channelCount)
                } else {
                    Thread.sleep(5)
                }
            }
        }, "SignalsmithRenderThread")

        renderThread?.start()
    }

    private fun stopRenderThread() {
        isRenderThreadRunning.set(false)
        renderThread?.join(1000)
        renderThread = null
    }

    fun writePcm(buffer: ByteBuffer, sizeInBytes: Int, presentationTimeUs: Long) {
        if (!isInitialized) {
            Log.w(TAG, "Not initialized, ignoring PCM data")
            return
        }
        nativeWritePcm(buffer, sizeInBytes, presentationTimeUs)
    }

    fun play() {
        if (!isInitialized) return
        Log.d(TAG, "Play")
        nativePlay()
        audioTrack?.play()
    }

    fun pause() {
        if (!isInitialized) return
        Log.d(TAG, "Pause")
        nativePause()
    }

    fun flush() {
        if (!isInitialized) return
        Log.d(TAG, "Flush")
        nativeFlush()
    }

    fun reset() {
        if (!isInitialized) return
        Log.d(TAG, "Reset")
        nativeReset()
    }

    fun release() {
        if (!isInitialized) return
        Log.d(TAG, "Release")
        stopRenderThread()
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        nativeRelease()
        isInitialized = false
    }

    fun setPitch(pitch: Float) {
        if (!isInitialized) return
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        Log.d(TAG, "SetPitch: $clampedPitch")
        nativeSetPitch(clampedPitch)
    }

    fun setTempo(tempo: Float) {
        if (!isInitialized) return
        val clampedTempo = tempo.coerceIn(0.5f, 2.0f)
        Log.d(TAG, "SetTempo: $clampedTempo")
        nativeSetTempo(clampedTempo)
    }

    fun getCurrentPositionUs(): Long {
        if (!isInitialized) return 0L
        return nativeGetCurrentPositionUs()
    }

    fun isPlaying(): Boolean {
        if (!isInitialized) return false
        return nativeIsPlaying()
    }

    fun isInitialized(): Boolean = isInitialized

    fun getBufferAvailableSpace(): Int {
        if (!isInitialized) return 0
        return nativeGetBufferAvailableSpace()
    }

    fun getBufferUsedSamples(): Int {
        if (!isInitialized) return 0
        return nativeGetBufferUsedSamples()
    }

    fun getUnderrunCount(): Int {
        if (!isInitialized) return 0
        return nativeGetUnderrunCount()
    }

    fun resetStats() {
        if (!isInitialized) return
        nativeResetStats()
    }

    fun setSeekPosition(positionUs: Long) {
        if (!isInitialized) return
        nativeSetSeekPosition(positionUs)
    }

    fun hasPendingData(): Boolean {
        if (!isInitialized) return false
        return nativeHasPendingData()
    }

    fun getInputLatency(): Int {
        if (!isInitialized) return 0
        return nativeGetInputLatency()
    }

    fun getOutputLatency(): Int {
        if (!isInitialized) return 0
        return nativeGetOutputLatency()
    }

    private external fun nativeInit(inputSampleRate: Int, inputChannelCount: Int, bufferSize: Int)
    private external fun nativeWritePcm(buffer: ByteBuffer, sizeInBytes: Int, presentationTimeUs: Long)
    private external fun nativeProcess(outputArray: ShortArray, outputFrames: Int): Int
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
    private external fun nativeGetUnderrunCount(): Int
    private external fun nativeResetStats()
    private external fun nativeSetSeekPosition(positionUs: Long)
    private external fun nativeHasPendingData(): Boolean
    private external fun nativeGetInputLatency(): Int
    private external fun nativeGetOutputLatency(): Int
}
