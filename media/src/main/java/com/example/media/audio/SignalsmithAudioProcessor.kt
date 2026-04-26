package com.example.media.audio

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class SignalsmithAudioProcessor @Inject constructor() : AudioProcessor {

    companion object {
        private const val TAG = "SignalsmithProcessor"

        init {
            System.loadLibrary("signalsmith_audio")
        }
    }

    private var inputAudioFormat: AudioFormat = AudioFormat.NOT_SET
    private var outputAudioFormat: AudioFormat = AudioFormat.NOT_SET

    private var inputEnded = false

    private var inputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var processingBuffer: ByteBuffer? = null
    private var scratchInputBuffer: ByteBuffer? = null
    private var pendingOutputBuffer: ByteBuffer? = null

    @Volatile
    private var nativeHandle: Long = 0

    private val _pitchSemitones = MutableStateFlow(0f)
    val pitchSemitonesFlow: StateFlow<Float> = _pitchSemitones.asStateFlow()

    private val _tempoSemitones = MutableStateFlow(0f)
    val tempoSemitonesFlow: StateFlow<Float> = _tempoSemitones.asStateFlow()

    @Volatile
    private var tempoRate: Float = 1.0f

    private var chorusEnabled: Boolean = false
    private var chorusMix: Float = 0.5f
    private var chorusDepthMs: Float = 10.0f
    private var chorusDetune: Float = 10.0f
    private var chorusStereo: Float = 0.5f

    private var reverbEnabled: Boolean = false
    private var reverbDry: Float = 1.0f
    private var reverbWet: Float = 0.35f
    private var reverbRoomMs: Float = 80.0f
    private var reverbDecaySec: Float = 1.0f
    private var reverbEarly: Float = 1.0f
    private var reverbDetune: Float = 2.0f
    private var reverbLowCutHz: Float = 80f
    private var reverbHighCutHz: Float = 12000f
    private var reverbLowDampRate: Float = 1.6f
    private var reverbHighDampRate: Float = 2.5f

    private var eqEnabled: Boolean = false
    private var eqBand1Freq: Float = 60.0f
    private var eqBand1Gain: Float = 6.0f
    private var eqBand2Freq: Float = 250.0f
    private var eqBand2Gain: Float = 4.0f
    private var eqBand3Freq: Float = 1000.0f
    private var eqBand3Gain: Float = 0.0f
    private var eqBand4Freq: Float = 4000.0f
    private var eqBand4Gain: Float = 0.0f
    private var eqBand5Freq: Float = 12000.0f
    private var eqBand5Gain: Float = 0.0f

    private var isToneFilterActive: Boolean = false
    private var toneFilterLowCutHz: Float = 700f
    private var toneFilterHighCutHz: Float = 12000f
    private var toneFilterLowShelfDb: Float = 2.5f
    private var toneFilterHighShelfDb: Float = -2.5f

    fun setPitchSemitones(semitones: Float) {
        _pitchSemitones.value = semitones.coerceIn(-24f, 24f)
        if (nativeHandle != 0L) {
            nativeSetPitchSemitones(nativeHandle, _pitchSemitones.value)
        }
        Log.d(TAG, "setPitchSemitones: ${_pitchSemitones.value}")
    }

    fun addPitchSemitone() {
        setPitchSemitones(_pitchSemitones.value + 1f)
    }

    fun subtractPitchSemitone() {
        setPitchSemitones(_pitchSemitones.value - 1f)
    }

    fun resetPitch() {
        setPitchSemitones(0f)
    }

    fun setTempoRate(rate: Float) {
        tempoRate = rate.coerceIn(0.5f, 2.0f)
        if (nativeHandle != 0L) {
            nativeSetTempoRate(nativeHandle, tempoRate)
        }
        Log.d(TAG, "setTempoRate: $tempoRate")
    }

    fun setTempoSemitones(semitones: Float) {
        _tempoSemitones.value = semitones.coerceIn(-24f, 24f)
        tempoRate = Math.pow(2.0, semitones.toDouble() / 12.0).toFloat()
        if (nativeHandle != 0L) {
            nativeSetTempoRate(nativeHandle, tempoRate)
        }
        Log.d(TAG, "setTempoSemitones: $semitones, tempoRate: $tempoRate")
    }

    fun addTempoSemitone() {
        setTempoSemitones(_tempoSemitones.value + 1f)
    }

    fun subtractTempoSemitone() {
        setTempoSemitones(_tempoSemitones.value - 1f)
    }

    fun resetTempo() {
        setTempoSemitones(0f)
    }

    fun setChorusEnabled(enabled: Boolean) {
        chorusEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetChorusEnabled(nativeHandle, enabled)
        }
    }

    fun setChorusParams(mix: Float, depthMs: Float, detune: Float, stereo: Float) {
        chorusMix = mix
        chorusDepthMs = depthMs
        chorusDetune = detune
        chorusStereo = stereo
        if (nativeHandle != 0L) {
            nativeSetChorusParams(nativeHandle, mix, depthMs, detune, stereo)
        }
    }

    fun setReverbEnabled(enabled: Boolean) {
        reverbEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetReverbEnabled(nativeHandle, enabled)
        }
    }

    fun setReverbParams(
        dry: Float, wet: Float, roomMs: Float, decaySec: Float,
        early: Float, detune: Float,
        lowCutHz: Float, highCutHz: Float,
        lowDampRate: Float, highDampRate: Float
    ) {
        if (nativeHandle != 0L) {
            nativeSetReverbParams(
                nativeHandle,
                dry, wet, roomMs, decaySec,
                early, detune, lowCutHz, highCutHz, lowDampRate, highDampRate
            )
        }
        reverbDry = dry
        reverbWet = wet
        reverbRoomMs = roomMs
        reverbDecaySec = decaySec
        reverbEarly = early
        reverbDetune = detune
        reverbLowCutHz = lowCutHz
        reverbHighCutHz = highCutHz
        reverbLowDampRate = lowDampRate
        reverbHighDampRate = highDampRate
    }

    fun setEqEnabled(enabled: Boolean) {
        eqEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetEqEnabled(nativeHandle, enabled)
        }
    }

    fun setEqBand(band: Int, freq: Float, gainDb: Float) {
        when (band) {
            0 -> {
                eqBand1Freq = freq
                eqBand1Gain = gainDb
            }
            1 -> {
                eqBand2Freq = freq
                eqBand2Gain = gainDb
            }
            2 -> {
                eqBand3Freq = freq
                eqBand3Gain = gainDb
            }
            3 -> {
                eqBand4Freq = freq
                eqBand4Gain = gainDb
            }
            4 -> {
                eqBand5Freq = freq
                eqBand5Gain = gainDb
            }
        }

        if (nativeHandle != 0L) {
            nativeSetEqBand(nativeHandle, band, freq, gainDb)
        }
    }

    fun setToneFilterEnabled(enabled: Boolean) {
        isToneFilterActive = enabled
        if (nativeHandle != 0L) {
            nativeSetToneFilterEnabled(nativeHandle, enabled)
        }
    }

    fun setToneFilterParams(lowCutHz: Float, highCutHz: Float, lowShelfDb: Float, highShelfDb: Float) {
        toneFilterLowCutHz = lowCutHz
        toneFilterHighCutHz = highCutHz
        toneFilterLowShelfDb = lowShelfDb
        toneFilterHighShelfDb = highShelfDb
        if (nativeHandle != 0L) {
            nativeSetToneFilterParams(nativeHandle, lowCutHz, highCutHz, lowShelfDb, highShelfDb)
        }
    }

    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        Log.d(TAG, "configure: sampleRate=${inputAudioFormat.sampleRate}, " +
                "channelCount=${inputAudioFormat.channelCount}, " +
                "encoding=${inputAudioFormat.encoding}")

        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            Log.w(TAG, "Unsupported encoding: ${inputAudioFormat.encoding}")
            this.inputAudioFormat = AudioFormat.NOT_SET
            this.outputAudioFormat = AudioFormat.NOT_SET
            return AudioFormat.NOT_SET
        }

        this.inputAudioFormat = inputAudioFormat
        this.outputAudioFormat = inputAudioFormat

        if (nativeHandle != 0L) {
            nativeRelease(nativeHandle)
        }
        nativeHandle = nativeInit(
            inputAudioFormat.sampleRate,
            inputAudioFormat.channelCount
        )

        if (nativeHandle != 0L) {
            nativeSetPitchSemitones(nativeHandle, _pitchSemitones.value)
            nativeSetTempoRate(nativeHandle, tempoRate)

            nativeSetChorusEnabled(nativeHandle, chorusEnabled)
            nativeSetChorusParams(nativeHandle, chorusMix, chorusDepthMs, chorusDetune, chorusStereo)

            nativeSetReverbEnabled(nativeHandle, reverbEnabled)
            nativeSetReverbParams(
                nativeHandle,
                reverbDry, reverbWet, reverbRoomMs, reverbDecaySec,
                reverbEarly, reverbDetune, reverbLowCutHz, reverbHighCutHz,
                reverbLowDampRate, reverbHighDampRate
            )

            nativeSetEqEnabled(nativeHandle, eqEnabled)
            nativeSetEqBand(nativeHandle, 0, eqBand1Freq, eqBand1Gain)
            nativeSetEqBand(nativeHandle, 1, eqBand2Freq, eqBand2Gain)
            nativeSetEqBand(nativeHandle, 2, eqBand3Freq, eqBand3Gain)
            nativeSetEqBand(nativeHandle, 3, eqBand4Freq, eqBand4Gain)
            nativeSetEqBand(nativeHandle, 4, eqBand5Freq, eqBand5Gain)

            if (isToneFilterActive) nativeSetToneFilterEnabled(nativeHandle, true)
            nativeSetToneFilterParams(nativeHandle, toneFilterLowCutHz, toneFilterHighCutHz, toneFilterLowShelfDb, toneFilterHighShelfDb)
        }

        Log.d(TAG, "configure: nativeHandle=$nativeHandle")
        return outputAudioFormat
    }

    override fun isActive(): Boolean {
        return outputAudioFormat != AudioFormat.NOT_SET
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val inputBytes = inputBuffer.remaining()
        if (inputBytes == 0) {
            outputBuffer = AudioProcessor.EMPTY_BUFFER
            return
        }

        if (outputAudioFormat == AudioFormat.NOT_SET ||
            inputAudioFormat == AudioFormat.NOT_SET ||
            nativeHandle == 0L
        ) {
            if (processingBuffer == null || processingBuffer!!.capacity() < inputBytes) {
                processingBuffer = ByteBuffer.allocateDirect(inputBytes)
                    .order(ByteOrder.nativeOrder())
            }
            processingBuffer!!.clear()
            processingBuffer!!.put(inputBuffer)
            processingBuffer!!.flip()
            outputBuffer = processingBuffer!!
            this.inputBuffer = AudioProcessor.EMPTY_BUFFER
            return
        }

        val bytesPerFrame = inputAudioFormat.channelCount * 2
        val inputFrames = inputBytes / bytesPerFrame
        val processBytes = inputFrames * bytesPerFrame
        if (processBytes == 0) {
            inputBuffer.position(inputBuffer.limit())
            outputBuffer = AudioProcessor.EMPTY_BUFFER
            this.inputBuffer = AudioProcessor.EMPTY_BUFFER
            return
        }

        val nativeInputBuffer = if (inputBuffer.isDirect) {
            inputBuffer.slice().order(ByteOrder.nativeOrder()).apply { limit(processBytes) }
        } else {
            if (scratchInputBuffer == null || scratchInputBuffer!!.capacity() < processBytes) {
                scratchInputBuffer = ByteBuffer.allocateDirect(processBytes)
                    .order(ByteOrder.nativeOrder())
            }
            scratchInputBuffer!!.clear()

            val originalLimit = inputBuffer.limit()
            val originalPosition = inputBuffer.position()
            inputBuffer.limit(originalPosition + processBytes)
            scratchInputBuffer!!.put(inputBuffer)
            inputBuffer.limit(originalLimit)

            scratchInputBuffer!!.flip()
            scratchInputBuffer!!
        }

        inputBuffer.position(inputBuffer.limit())

        if (processingBuffer == null || processingBuffer!!.capacity() < processBytes) {
            processingBuffer = ByteBuffer.allocateDirect(processBytes)
                .order(ByteOrder.nativeOrder())
        }
        processingBuffer!!.clear()
        processingBuffer!!.limit(processBytes)

        val actualOutputFrames = nativeProcess(
            nativeHandle,
            nativeInputBuffer,
            processBytes,
            processingBuffer!!,
            inputFrames
        )

        if (actualOutputFrames > 0) {
            val actualOutputBytes = actualOutputFrames * bytesPerFrame
            processingBuffer!!.position(0)
            processingBuffer!!.limit(actualOutputBytes)

            outputBuffer = processingBuffer!!
        } else {
            outputBuffer = AudioProcessor.EMPTY_BUFFER
        }

        this.inputBuffer = AudioProcessor.EMPTY_BUFFER
    }

    override fun queueEndOfStream() {
        inputEnded = true

        if (nativeHandle != 0L) {
            val bytesPerFrame = inputAudioFormat.channelCount * 2
            val maxRemainingFrames = 4096
            val maxRemainingBytes = maxRemainingFrames * bytesPerFrame

            if (processingBuffer == null || processingBuffer!!.capacity() < maxRemainingBytes) {
                processingBuffer = ByteBuffer.allocateDirect(maxRemainingBytes)
                    .order(ByteOrder.nativeOrder())
            }
            processingBuffer!!.clear()

            val remainingFrames = nativeFlushAndGetRemaining(nativeHandle, processingBuffer!!, maxRemainingFrames)

            if (remainingFrames > 0) {
                val remainingBytes = remainingFrames * bytesPerFrame
                processingBuffer!!.position(0)
                processingBuffer!!.limit(remainingBytes)
                pendingOutputBuffer = processingBuffer
            }
        }
    }

    override fun getOutput(): ByteBuffer {
        if (pendingOutputBuffer != null && pendingOutputBuffer!!.hasRemaining()) {
            val output = pendingOutputBuffer!!
            pendingOutputBuffer = null
            return output
        }

        val output = outputBuffer
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        return output
    }

    override fun isEnded(): Boolean {
        return inputEnded &&
               outputBuffer === AudioProcessor.EMPTY_BUFFER &&
               (pendingOutputBuffer == null || !pendingOutputBuffer!!.hasRemaining())
    }

    override fun flush() {
        Log.d(TAG, "flush")
        inputBuffer = AudioProcessor.EMPTY_BUFFER
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        pendingOutputBuffer = null
        inputEnded = false

        if (nativeHandle != 0L) {
            nativeFlush(nativeHandle)
        }
    }

    override fun reset() {
        Log.d(TAG, "reset")
        flush()

        if (nativeHandle != 0L) {
            nativeRelease(nativeHandle)
            nativeHandle = 0
        }

        inputAudioFormat = AudioFormat.NOT_SET
        outputAudioFormat = AudioFormat.NOT_SET
        processingBuffer = null
    }

    private external fun nativeInit(sampleRate: Int, channelCount: Int): Long

    private external fun nativeProcess(
        handle: Long,
        inputBuffer: ByteBuffer,
        inputBytes: Int,
        outputBuffer: ByteBuffer,
        maxOutputFrames: Int
    ): Int

    private external fun nativeSetPitchSemitones(handle: Long, semitones: Float)

    private external fun nativeSetTempoRate(handle: Long, rate: Float)

    private external fun nativeSetChorusEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetChorusParams(
        handle: Long,
        mix: Float,
        depthMs: Float,
        detune: Float,
        stereo: Float
    )

    private external fun nativeSetReverbEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetReverbParams(
        handle: Long,
        dry: Float, wet: Float, roomMs: Float, decaySec: Float,
        early: Float, detune: Float,
        lowCutHz: Float, highCutHz: Float,
        lowDampRate: Float, highDampRate: Float
    )

    private external fun nativeSetEqEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetEqBand(
        handle: Long,
        band: Int,
        freq: Float,
        gainDb: Float
    )

    private external fun nativeSetToneFilterEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetToneFilterParams(handle: Long, lowCutHz: Float, highCutHz: Float, lowShelfDb: Float, highShelfDb: Float)

    private external fun nativeFlush(handle: Long)

    private external fun nativeFlushAndGetRemaining(
        handle: Long,
        outputBuffer: ByteBuffer,
        maxOutputFrames: Int
    ): Int

    private external fun nativeRelease(handle: Long)
}
