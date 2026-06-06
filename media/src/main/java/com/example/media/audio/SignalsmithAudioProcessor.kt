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
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Singleton
class SignalsmithAudioProcessor @Inject constructor() : AudioProcessor {

    companion object {
        private const val TAG = "SignalsmithProcessor"
        private const val PIPE_TAG = "AudioPipe"
        private const val EFFECT_TRANSITION_MS = 36
        private const val ENABLE_OUTPUT_TRANSITION = false
        private const val STARTUP_FADE_MS = 5
        private const val EOS_FADE_OUT_MS = 8
        private const val MIN_TEMPO_RATE = 0.5f
        private const val MAX_TEMPO_RATE = 2.0f
        private const val MAX_TEMPO_SEMITONES = 12f
        private const val MICROS_PER_SECOND = 1_000_000.0

        init {
            System.loadLibrary("signalsmith_audio")
        }
    }

    private data class TempoMappingSegment(
        val outputStartFrames: Long,
        val mediaStartFrames: Long,
        val tempoRate: Float
    )

    private var inputAudioFormat: AudioFormat = AudioFormat.NOT_SET
    private var outputAudioFormat: AudioFormat = AudioFormat.NOT_SET

    private var inputEnded = false

    private var inputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var processingBuffer: ByteBuffer? = null
    private var scratchInputBuffer: ByteBuffer? = null
    private var pendingOutputBuffer: ByteBuffer? = null
    private var eosOutputBuffer: ByteBuffer? = null
    @Volatile private var transitionRequested = false
    private var transitionActive = false
    private var transitionPositionBytes = 0
    private var transitionTotalBytes = 0
    private var transitionTail = ByteArray(0)
    private var transitionScratch = ByteArray(0)
    private var startupFadeTotalFrames = 0
    private var startupFadeRemainingFrames = 0

    @Volatile
    private var nativeHandle: Long = 0

    private val _pitchSemitones = MutableStateFlow(0f)
    val pitchSemitonesFlow: StateFlow<Float> = _pitchSemitones.asStateFlow()

    private val _tempoSemitones = MutableStateFlow(0f)
    val tempoSemitonesFlow: StateFlow<Float> = _tempoSemitones.asStateFlow()

    @Volatile
    private var tempoRate: Float = 1.0f
    private val tempoMappingLock = Any()
    private val tempoMappingSegments = mutableListOf<TempoMappingSegment>()
    private var processedMediaFrames = 0L
    private var processedOutputFrames = 0L

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
        tempoRate = rate.coerceIn(MIN_TEMPO_RATE, MAX_TEMPO_RATE)
        _tempoSemitones.value = rateToTempoSemitones(tempoRate)
        requestOutputTransition()
        if (nativeHandle != 0L) {
            nativeSetTempoRate(nativeHandle, tempoRate)
        }
        logDebug("setTempoRate: $tempoRate")
    }

    fun setTempoSemitones(semitones: Float) {
        _tempoSemitones.value = semitones.coerceIn(-MAX_TEMPO_SEMITONES, MAX_TEMPO_SEMITONES)
        tempoRate = tempoSemitonesToRate(_tempoSemitones.value)
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

    fun getMediaDurationUs(playoutDurationUs: Long): Long {
        if (playoutDurationUs <= 0L || inputAudioFormat == AudioFormat.NOT_SET) {
            return playoutDurationUs
        }

        val sampleRate = inputAudioFormat.sampleRate
        if (sampleRate <= 0) return playoutDurationUs

        val outputFramesAtPosition = playoutDurationUs * sampleRate / MICROS_PER_SECOND
        val mediaFrames = synchronized(tempoMappingLock) {
            if (tempoMappingSegments.isEmpty()) {
                outputFramesAtPosition
            } else {
                val index = tempoMappingSegments.indexOfLast {
                    it.outputStartFrames <= outputFramesAtPosition
                }.coerceAtLeast(0)
                val segment = tempoMappingSegments[index]
                val outputDelta = outputFramesAtPosition - segment.outputStartFrames
                segment.mediaStartFrames + outputDelta * segment.tempoRate
            }
        }
        return (mediaFrames * MICROS_PER_SECOND / sampleRate).toLong()
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
        startupFadeTotalFrames = ((inputAudioFormat.sampleRate * STARTUP_FADE_MS) / 1000)
            .coerceAtLeast(1)
        startupFadeRemainingFrames = startupFadeTotalFrames
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

        resetTempoMapping()
        logDebug("configure: nativeHandle=$nativeHandle")
        return outputAudioFormat
    }

    private fun sameAudioFormat(a: AudioFormat, b: AudioFormat): Boolean {
        return a.sampleRate == b.sampleRate &&
            a.channelCount == b.channelCount &&
            a.encoding == b.encoding
    }

    private fun tempoSemitonesToRate(semitones: Float): Float {
        return 2.0.pow(semitones.toDouble() / 12.0)
            .toFloat()
            .coerceIn(MIN_TEMPO_RATE, MAX_TEMPO_RATE)
    }

    private fun rateToTempoSemitones(rate: Float): Float {
        val safeRate = rate.coerceIn(MIN_TEMPO_RATE, MAX_TEMPO_RATE).toDouble()
        return (12.0 * ln(safeRate) / ln(2.0))
            .toFloat()
            .coerceIn(-MAX_TEMPO_SEMITONES, MAX_TEMPO_SEMITONES)
    }

    private fun resetTempoMapping() {
        synchronized(tempoMappingLock) {
            processedMediaFrames = 0L
            processedOutputFrames = 0L
            tempoMappingSegments.clear()
            tempoMappingSegments.add(
                TempoMappingSegment(
                    outputStartFrames = 0L,
                    mediaStartFrames = 0L,
                    tempoRate = tempoRate
                )
            )
        }
    }

    private fun recordProcessedFrames(inputFrames: Int, outputFrames: Int, rate: Float) {
        if (inputFrames <= 0 || outputFrames <= 0) return

        val safeRate = rate.coerceIn(MIN_TEMPO_RATE, MAX_TEMPO_RATE)
        synchronized(tempoMappingLock) {
            val lastSegment = tempoMappingSegments.lastOrNull()
            if (lastSegment == null || abs(lastSegment.tempoRate - safeRate) > 0.0001f) {
                tempoMappingSegments.add(
                    TempoMappingSegment(
                        outputStartFrames = processedOutputFrames,
                        mediaStartFrames = processedMediaFrames,
                        tempoRate = safeRate
                    )
                )
            }
            processedMediaFrames += inputFrames.toLong()
            processedOutputFrames += outputFrames.toLong()
        }
    }

    private fun estimateMaxOutputFrames(inputFrames: Int): Int {
        return ((inputFrames / MIN_TEMPO_RATE).roundToInt() + 1024).coerceAtLeast(inputFrames)
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
        val tempoRateForProcess = tempoRate
        val maxOutputFrames = estimateMaxOutputFrames(inputFrames)
        val maxOutputBytes = maxOutputFrames * bytesPerFrame
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

        if (processingBuffer == null || processingBuffer!!.capacity() < maxOutputBytes) {
            processingBuffer = ByteBuffer.allocateDirect(maxOutputBytes)
                .order(ByteOrder.nativeOrder())
        }
        processingBuffer!!.clear()
        processingBuffer!!.limit(maxOutputBytes)

        val actualOutputFrames = nativeProcess(
            nativeHandle,
            nativeInputBuffer,
            processBytes,
            processingBuffer!!,
            maxOutputFrames
        )

        if (actualOutputFrames > 0) {
            val actualOutputBytes = actualOutputFrames * bytesPerFrame
            recordProcessedFrames(
                inputFrames = inputFrames,
                outputFrames = actualOutputFrames,
                rate = inputFrames.toFloat() / actualOutputFrames.toFloat()
            )
            if (actualOutputFrames != inputFrames) {
                logPipeline(
                    "QUEUE_RATE_CHANGE stage=signalsmith inputFrames=$inputFrames outputFrames=$actualOutputFrames " +
                        "inputBytes=$inputBytes outputBytes=$actualOutputBytes tempo=$tempoRateForProcess pitch=${_pitchSemitones.value}"
                )
            }
            processingBuffer!!.position(0)
            processingBuffer!!.limit(actualOutputBytes)
            maybeStartOutputTransition()
            applyStartupFade(processingBuffer!!, actualOutputBytes)
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

        var producedTail = false
        if (nativeHandle != 0L) {
            val bytesPerFrame = inputAudioFormat.channelCount * 2
            val maxRemainingFrames = 4096
            val maxRemainingBytes = maxRemainingFrames * bytesPerFrame

            if (eosOutputBuffer == null || eosOutputBuffer!!.capacity() < maxRemainingBytes) {
                eosOutputBuffer = ByteBuffer.allocateDirect(maxRemainingBytes)
                    .order(ByteOrder.nativeOrder())
            }
            eosOutputBuffer!!.clear()

            val remainingFrames = nativeFlushAndGetRemaining(nativeHandle, eosOutputBuffer!!, maxRemainingFrames)
            if (remainingFrames > 0) {
                val remainingBytes = remainingFrames * bytesPerFrame
                eosOutputBuffer!!.position(0)
                eosOutputBuffer!!.limit(remainingBytes)
                applyEndFadeOut(eosOutputBuffer!!)
                pendingOutputBuffer = eosOutputBuffer!!
                producedTail = true
                logPipeline(
                    "EOS_DRAIN_REMAINING stage=signalsmith frames=$remainingFrames bytes=$remainingBytes"
                )
            }
        }

        if (!producedTail) {
            if (outputBuffer.hasRemaining()) {
                applyEndFadeOut(outputBuffer)
            } else {
                outputBuffer = AudioProcessor.EMPTY_BUFFER
            }
            pendingOutputBuffer?.takeIf { it.hasRemaining() }?.let(::applyEndFadeOut)
        }
    }

    override fun getOutput(): ByteBuffer {
        if (outputBuffer.hasRemaining()) {
            val output = outputBuffer
            outputBuffer = AudioProcessor.EMPTY_BUFFER
            return output
        }

        pendingOutputBuffer?.takeIf { it.hasRemaining() }?.let { output ->
            pendingOutputBuffer = null
            return output
        }

        return AudioProcessor.EMPTY_BUFFER
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
        startupFadeRemainingFrames = startupFadeTotalFrames

        if (nativeHandle != 0L) {
            nativeFlush(nativeHandle)
        }
        resetTempoMapping()
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
        eosOutputBuffer = null
    }

    private fun requestOutputTransition() {
        if (!ENABLE_OUTPUT_TRANSITION) return
        transitionRequested = true
    }

    private fun applyStartupFade(buffer: ByteBuffer, outputBytes: Int) {
        if (startupFadeRemainingFrames <= 0 || outputBytes <= 0) return

        val channelCount = inputAudioFormat.channelCount
        if (channelCount <= 0) return

        val bytesPerFrame = channelCount * 2
        val outputFrames = outputBytes / bytesPerFrame
        val fadeFrames = minOf(startupFadeRemainingFrames, outputFrames)
        if (fadeFrames <= 0) return

        val fadeTotal = startupFadeTotalFrames.coerceAtLeast(1)
        val completedFrames = fadeTotal - startupFadeRemainingFrames
        val view = buffer.duplicate().order(ByteOrder.nativeOrder())

        for (frame in 0 until fadeFrames) {
            val gain = (completedFrames + frame + 1).toFloat() / fadeTotal.toFloat()
            val frameOffsetBytes = frame * bytesPerFrame
            for (channel in 0 until channelCount) {
                val sampleOffset = frameOffsetBytes + channel * 2
                val sample = view.getShort(sampleOffset)
                val faded = (sample * gain)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()
                view.putShort(sampleOffset, faded)
            }
        }

        startupFadeRemainingFrames -= fadeFrames
    }

    private fun applyEndFadeOut(buffer: ByteBuffer) {
        if (inputAudioFormat == AudioFormat.NOT_SET || !buffer.hasRemaining()) return

        val channelCount = inputAudioFormat.channelCount
        if (channelCount <= 0) return

        val bytesPerFrame = channelCount * 2
        val outputFrames = buffer.remaining() / bytesPerFrame
        if (outputFrames <= 0) return

        val fadeFrames = minOf(
            outputFrames,
            ((inputAudioFormat.sampleRate * EOS_FADE_OUT_MS) / 1000).coerceAtLeast(1)
        )
        val basePosition = buffer.position()
        val fadeStartFrame = outputFrames - fadeFrames
        val view = buffer.duplicate().order(ByteOrder.nativeOrder())

        for (frame in 0 until fadeFrames) {
            val gain = 1f - ((frame + 1).toFloat() / fadeFrames.toFloat())
            val frameOffsetBytes = basePosition + (fadeStartFrame + frame) * bytesPerFrame
            for (channel in 0 until channelCount) {
                val sampleOffset = frameOffsetBytes + channel * 2
                val sample = view.getShort(sampleOffset)
                val faded = (sample * gain)
                    .roundToInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()
                view.putShort(sampleOffset, faded)
            }
        }
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
