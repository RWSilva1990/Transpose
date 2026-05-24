package com.example.media.audio

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.util.UnstableApi
import com.example.media.BuildConfig
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
        private const val PIPE_TAG = "AudioPipe"
        private const val EFFECT_TRANSITION_MS = 36
        private const val ENABLE_OUTPUT_TRANSITION = false

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
    @Volatile private var transitionRequested = false
    private var transitionActive = false
    private var transitionPositionBytes = 0
    private var transitionTotalBytes = 0
    private var transitionTail = ByteArray(0)
    private var transitionScratch = ByteArray(0)

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

    private var reverbPlusEnabled: Boolean = false
    private var reverbPlusDry: Float = 1.0f
    private var reverbPlusWet: Float = 0.3f
    private var reverbPlusRoomSize: Float = 0.5f
    private var reverbPlusDamping: Float = 0.5f

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

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    private fun logPipeline(message: String) {
        if (BuildConfig.DEBUG) Log.i(PIPE_TAG, message)
    }

    private fun logWarning(message: String) {
        if (BuildConfig.DEBUG) Log.w(TAG, message)
    }

    private fun logPipelineWarning(message: String) {
        if (BuildConfig.DEBUG) Log.w(PIPE_TAG, message)
    }

    fun setPitchSemitones(semitones: Float) {
        _pitchSemitones.value = semitones.coerceIn(-24f, 24f)
        requestOutputTransition()
        if (nativeHandle != 0L) {
            nativeSetPitchSemitones(nativeHandle, _pitchSemitones.value)
        }
        logDebug("setPitchSemitones: ${_pitchSemitones.value}")
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
        requestOutputTransition()
        if (nativeHandle != 0L) {
            nativeSetTempoRate(nativeHandle, tempoRate)
        }
        logDebug("setTempoRate: $tempoRate")
    }

    fun setTempoSemitones(semitones: Float) {
        _tempoSemitones.value = semitones.coerceIn(-24f, 24f)
        tempoRate = Math.pow(2.0, semitones.toDouble() / 12.0).toFloat()
        requestOutputTransition()
        if (nativeHandle != 0L) {
            nativeSetTempoRate(nativeHandle, tempoRate)
        }
        logDebug("setTempoSemitones: $semitones, tempoRate: $tempoRate")
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
        requestOutputTransition()
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

    fun setReverbPlusEnabled(enabled: Boolean) {
        reverbPlusEnabled = enabled
        requestOutputTransition()
        if (nativeHandle != 0L) {
            nativeSetReverbPlusEnabled(nativeHandle, enabled)
        }
    }

    fun setReverbPlusParams(dry: Float, wet: Float, roomSize: Float, damping: Float) {
        reverbPlusDry = dry.coerceIn(0f, 1f)
        reverbPlusWet = wet.coerceIn(0f, 1f)
        reverbPlusRoomSize = roomSize.coerceIn(0f, 1f)
        reverbPlusDamping = damping.coerceIn(0f, 1f)
        if (nativeHandle != 0L) {
            nativeSetReverbPlusParams(
                nativeHandle,
                reverbPlusDry,
                reverbPlusWet,
                reverbPlusRoomSize,
                reverbPlusDamping
            )
        }
    }

    fun setReverbEnabled(enabled: Boolean) {
        reverbEnabled = enabled
        requestOutputTransition()
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
        requestOutputTransition()
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
        val sameConfiguredFormat = outputAudioFormat != AudioFormat.NOT_SET &&
            sameAudioFormat(this.inputAudioFormat, inputAudioFormat) &&
            nativeHandle != 0L

        if (sameConfiguredFormat) {
            logPipeline(
                "CONFIG_SKIP stage=signalsmith sampleRate=${inputAudioFormat.sampleRate} " +
                    "channels=${inputAudioFormat.channelCount} encoding=${inputAudioFormat.encoding} " +
                    "nativeHandle=$nativeHandle pending=${pendingOutputBuffer?.remaining() ?: 0} " +
                    "outputRemaining=${outputBuffer.remaining()} transitionActive=$transitionActive reason=same_format"
            )
            return outputAudioFormat
        }

        logPipeline(
            "CONFIG_APPLY stage=signalsmith sampleRate=${inputAudioFormat.sampleRate} " +
                "channels=${inputAudioFormat.channelCount} encoding=${inputAudioFormat.encoding} " +
                "oldNativeHandle=$nativeHandle"
        )
        logDebug("configure: sampleRate=${inputAudioFormat.sampleRate}, " +
                "channelCount=${inputAudioFormat.channelCount}, " +
                "encoding=${inputAudioFormat.encoding}")

        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            logWarning("Unsupported encoding: ${inputAudioFormat.encoding}")
            logPipeline(
                "CONFIG_REJECT stage=signalsmith reason=encoding encoding=${inputAudioFormat.encoding}"
            )
            this.inputAudioFormat = AudioFormat.NOT_SET
            this.outputAudioFormat = AudioFormat.NOT_SET
            return AudioFormat.NOT_SET
        }

        this.inputAudioFormat = inputAudioFormat
        this.outputAudioFormat = inputAudioFormat
        transitionTotalBytes =
            ((inputAudioFormat.sampleRate * inputAudioFormat.channelCount * 2 * EFFECT_TRANSITION_MS) / 1000)
                .coerceAtLeast(inputAudioFormat.channelCount * 2 * 8)
        transitionTail = ByteArray(transitionTotalBytes)
        transitionScratch = ByteArray(0)
        transitionActive = false
        transitionPositionBytes = 0

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

            nativeSetReverbPlusEnabled(nativeHandle, reverbPlusEnabled)
            nativeSetReverbPlusParams(
                nativeHandle,
                reverbPlusDry,
                reverbPlusWet,
                reverbPlusRoomSize,
                reverbPlusDamping
            )

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

        logDebug("configure: nativeHandle=$nativeHandle")
        return outputAudioFormat
    }

    private fun sameAudioFormat(a: AudioFormat, b: AudioFormat): Boolean {
        return a.sampleRate == b.sampleRate &&
            a.channelCount == b.channelCount &&
            a.encoding == b.encoding
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
            logPipelineWarning(
                "QUEUE_DROP stage=signalsmith reason=partial_frame inputBytes=$inputBytes bytesPerFrame=$bytesPerFrame"
            )
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
            if (actualOutputFrames != inputFrames) {
                logPipeline(
                    "QUEUE_RATE_CHANGE stage=signalsmith inputFrames=$inputFrames outputFrames=$actualOutputFrames " +
                        "inputBytes=$inputBytes outputBytes=$actualOutputBytes tempo=$tempoRate pitch=${_pitchSemitones.value}"
                )
            }
            processingBuffer!!.position(0)
            processingBuffer!!.limit(actualOutputBytes)
            maybeStartOutputTransition()
            applyOutputTransition(processingBuffer!!, actualOutputBytes)
            saveTransitionTail(processingBuffer!!, actualOutputBytes)
            processingBuffer!!.position(0)
            processingBuffer!!.limit(actualOutputBytes)

            outputBuffer = processingBuffer!!
        } else {
            logPipelineWarning(
                "QUEUE_NO_OUTPUT stage=signalsmith inputBytes=$inputBytes processBytes=$processBytes " +
                    "inputFrames=$inputFrames nativeHandle=$nativeHandle tempo=$tempoRate pitch=${_pitchSemitones.value}"
            )
            outputBuffer = AudioProcessor.EMPTY_BUFFER
        }

        this.inputBuffer = AudioProcessor.EMPTY_BUFFER
    }

    override fun queueEndOfStream() {
        if (inputEnded &&
            !outputBuffer.hasRemaining() &&
            (pendingOutputBuffer == null || !pendingOutputBuffer!!.hasRemaining())
        ) {
            outputBuffer = AudioProcessor.EMPTY_BUFFER
            pendingOutputBuffer = null
            return
        }

        inputEnded = true
        logPipeline(
            "EOS stage=signalsmith nativeHandle=$nativeHandle pending=${pendingOutputBuffer?.remaining() ?: 0} " +
                "outputRemaining=${outputBuffer.remaining()}"
        )

        if (!outputBuffer.hasRemaining()) {
            outputBuffer = AudioProcessor.EMPTY_BUFFER
        }

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
                logPipeline(
                    "EOS_DROP_REMAINING stage=signalsmith frames=$remainingFrames bytes=${remainingFrames * bytesPerFrame}"
                )
            }
            pendingOutputBuffer = null
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
               !outputBuffer.hasRemaining() &&
               (pendingOutputBuffer == null || !pendingOutputBuffer!!.hasRemaining())
    }

    override fun flush() {
        logPipeline(
            "FLUSH stage=signalsmith nativeHandle=$nativeHandle pending=${pendingOutputBuffer?.remaining() ?: 0} " +
                "outputRemaining=${outputBuffer.remaining()} transitionActive=$transitionActive"
        )
        logDebug("flush")
        inputBuffer = AudioProcessor.EMPTY_BUFFER
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        pendingOutputBuffer = null
        inputEnded = false
        transitionActive = false
        transitionPositionBytes = 0

        if (nativeHandle != 0L) {
            nativeFlush(nativeHandle)
        }
    }

    override fun reset() {
        logPipeline(
            "RESET stage=signalsmith nativeHandle=$nativeHandle pending=${pendingOutputBuffer?.remaining() ?: 0} " +
                "outputRemaining=${outputBuffer.remaining()}"
        )
        logDebug("reset")
        flush()

        if (nativeHandle != 0L) {
            nativeRelease(nativeHandle)
            nativeHandle = 0
        }

        inputAudioFormat = AudioFormat.NOT_SET
        outputAudioFormat = AudioFormat.NOT_SET
        processingBuffer = null
    }

    private fun requestOutputTransition() {
        if (!ENABLE_OUTPUT_TRANSITION) return
        transitionRequested = true
    }

    private fun maybeStartOutputTransition() {
        if (!transitionRequested || transitionTotalBytes <= 0) return
        transitionRequested = false
        transitionActive = true
        transitionPositionBytes = 0
    }

    private fun applyOutputTransition(buffer: ByteBuffer, outputBytes: Int) {
        if (!transitionActive || outputBytes <= 0 || transitionTotalBytes <= 0) return

        if (transitionScratch.size < outputBytes) {
            transitionScratch = ByteArray(outputBytes)
        }

        val readView = buffer.duplicate().order(ByteOrder.nativeOrder())
        readView.position(0)
        readView.limit(outputBytes)
        readView.get(transitionScratch, 0, outputBytes)

        val remaining = transitionTotalBytes - transitionPositionBytes
        if (remaining <= 0) {
            transitionActive = false
            return
        }

        val fadeBytes = minOf(remaining, outputBytes) and 0xFFFFFFFE.toInt()
        val fadeSamples = fadeBytes / 2
        for (i in 0 until fadeSamples) {
            val idx = i * 2
            val globalPos = transitionPositionBytes + idx
            val progress = globalPos.toFloat() / transitionTotalBytes.toFloat()
            val oldSample = if (globalPos + 1 < transitionTail.size) {
                ((transitionTail[globalPos + 1].toInt() shl 8) or (transitionTail[globalPos].toInt() and 0xFF)).toShort()
            } else {
                0
            }
            val newSample = ((transitionScratch[idx + 1].toInt() shl 8) or (transitionScratch[idx].toInt() and 0xFF)).toShort()
            val mixed = (oldSample.toInt() * (1f - progress) + newSample.toInt() * progress).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            transitionScratch[idx] = (mixed and 0xFF).toByte()
            transitionScratch[idx + 1] = (mixed shr 8).toByte()
        }

        val writeView = buffer.duplicate().order(ByteOrder.nativeOrder())
        writeView.position(0)
        writeView.limit(outputBytes)
        writeView.put(transitionScratch, 0, outputBytes)

        transitionPositionBytes += fadeBytes
        if (transitionPositionBytes >= transitionTotalBytes) {
            transitionActive = false
            transitionPositionBytes = 0
        }
    }

    private fun saveTransitionTail(buffer: ByteBuffer, outputBytes: Int) {
        if (transitionTotalBytes <= 0 || outputBytes <= 0) return
        if (transitionTail.size < transitionTotalBytes) {
            transitionTail = ByteArray(transitionTotalBytes)
        }
        val view = buffer.duplicate().order(ByteOrder.nativeOrder())
        view.position(0)
        view.limit(outputBytes)

        if (outputBytes >= transitionTotalBytes) {
            view.position(outputBytes - transitionTotalBytes)
            view.get(transitionTail, 0, transitionTotalBytes)
            return
        }

        val shift = transitionTotalBytes - outputBytes
        System.arraycopy(transitionTail, outputBytes, transitionTail, 0, shift)
        view.get(transitionTail, shift, outputBytes)
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

    private external fun nativeSetReverbPlusEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetReverbPlusParams(
        handle: Long,
        dry: Float,
        wet: Float,
        roomSize: Float,
        damping: Float
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
