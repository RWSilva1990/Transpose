package com.example.media.audio

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.util.UnstableApi
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class VocalRemovalProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioProcessor {

    private enum class OutputState { BYPASS, WARMUP, ACTIVE, STARVED }

    companion object {
        private const val TAG = "VocalRemoval"
        private const val N_FFT = 6144
        private const val HOP_LENGTH = 1024
        private const val DEFAULT_DIM_F = 2048
        private const val DEFAULT_TARGET_T = 32
        private const val MODEL_CHANNELS = 2

        private const val CROSSFADE_MS = 30
        private const val WARMUP_BLEND_MS = 70
        private const val INFER_EMA_ALPHA = 0.1f
        private const val PERF_WARMUP_CHUNKS = 8
        private const val PREBUFFER_DEFAULT_X100 = 125
        private const val PREBUFFER_MAX_X100 = 140

        init {
            System.loadLibrary("signalsmith_audio")
        }
    }

    @Volatile
    var enabled: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            processingGeneration.incrementAndGet()
            if (value) {
                ensureModelLoadingAsync()
                outputState = OutputState.WARMUP
                enabledOutputBytesEmitted = 0L
                starvedTransitions = 0
                synchronized(inputLock) { if (::inputRing.isInitialized) inputRing.clear() }
                synchronized(outputLock) { if (::outputRing.isInitialized) outputRing.clear() }
                synchronized(dryLock) {
                    if (::dryDelayRing.isInitialized) {
                        dryDelayRing.clear()
                        prefillDryDelay()
                    }
                }
            } else {
                shouldStopProcessing = true
                outputState = OutputState.BYPASS
                synchronized(inputLock) { if (::inputRing.isInitialized) inputRing.clear() }
                synchronized(outputLock) { if (::outputRing.isInitialized) outputRing.clear() }
                synchronized(dryLock) {
                    if (::dryDelayRing.isInitialized) {
                        dryDelayRing.clear()
                        prefillDryDelay()
                    }
                }
                crossfadeActive = false
                crossfadePosition = 0
                enabledOutputBytesEmitted = 0L
                starvedTransitions = 0
            }
        }

    @Volatile
    var mixRatio: Float = 1.0f

    fun getVocalRemovalModelProfiles(): List<VocalRemovalModelProfile> = VocalRemovalModelProfile.values().toList()

    fun getSelectedVocalRemovalModelProfile(): VocalRemovalModelProfile = selectedModelProfile

    fun updateVocalRemovalModelProfile(profile: VocalRemovalModelProfile) {
        val previousProfile = selectedModelProfile
        if (previousProfile == profile) return
        selectedModelProfile = profile
        processingGeneration.incrementAndGet()
        shouldStopProcessing = true
        canProcessFormat = false
        modelExecutor.execute {
            try {
                applySelectedModelRuntimeConfig()
                closeCurrentOrtSession()
                synchronized(inputLock) { if (::inputRing.isInitialized) inputRing.clear() }
                synchronized(outputLock) { if (::outputRing.isInitialized) outputRing.clear() }
                synchronized(dryLock) {
                    if (::dryDelayRing.isInitialized) {
                        dryDelayRing.clear()
                        prefillDryDelay()
                    }
                }
                outputState = if (enabled) OutputState.WARMUP else OutputState.BYPASS
                enabledOutputBytesEmitted = 0L
                if (inputAudioFormat != AudioFormat.NOT_SET) {
                    setupRuntimeForFormat(inputAudioFormat)
                }
                if (enabled) ensureModelLoaded()
                Log.i(TAG, "Vocal model switched to ${profile.uiLabel} (${profile.assetFileName})")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to switch vocal model to ${profile.uiLabel}", t)
                selectedModelProfile = previousProfile
                try {
                    applySelectedModelRuntimeConfig()
                    if (inputAudioFormat != AudioFormat.NOT_SET) {
                        setupRuntimeForFormat(inputAudioFormat)
                    }
                    ensureModelLoaded()
                    Log.i(TAG, "Reverted vocal model to ${previousProfile.uiLabel}")
                } catch (fallbackError: Throwable) {
                    Log.e(TAG, "Failed to restore previous vocal model ${previousProfile.uiLabel}", fallbackError)
                }
            }
        }
    }

    private fun applySelectedModelRuntimeConfig() {
        val profile = selectedModelProfile
        modelDimF = profile.dimF
        modelTargetT = profile.targetT
        chunkSamples = ((modelTargetT - 1) * HOP_LENGTH) + N_FFT
        processIntervalSamples = modelTargetT * HOP_LENGTH
        extractOffsetSamples = (chunkSamples - processIntervalSamples) / 2
    }

    private var inputAudioFormat: AudioFormat = AudioFormat.NOT_SET
    private var outputAudioFormat: AudioFormat = AudioFormat.NOT_SET
    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var inputEnded = false

    private val modelExecutor = Executors.newSingleThreadExecutor()
    @Volatile private var modelLoading = false
    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var modelInputName: String = "input"
    private var activeModelProfile: VocalRemovalModelProfile? = null
    @Volatile private var selectedModelProfile: VocalRemovalModelProfile = VocalRemovalModelProfile.MDX_MAIN

    @Volatile private var nativeHandle: Long = 0L

    private var modelDimF: Int = DEFAULT_DIM_F
    private var modelTargetT: Int = DEFAULT_TARGET_T
    private var chunkSamples: Int = ((DEFAULT_TARGET_T - 1) * HOP_LENGTH) + N_FFT
    private var processIntervalSamples: Int = DEFAULT_TARGET_T * HOP_LENGTH
    private var extractOffsetSamples: Int = (chunkSamples - processIntervalSamples) / 2

    @Volatile private var canProcessFormat = false
    private var bytesPerFrame = 0
    private var chunkBytes = 0
    private var processIntervalBytes = 0
    private var extractOffsetBytes = 0

    private val inputLock = Any()
    private val outputLock = Any()
    private val dryLock = Any()
    private val processingDoneLock = java.lang.Object()

    private lateinit var inputRing: ByteRingBuffer
    private lateinit var outputRing: ByteRingBuffer
    private lateinit var dryDelayRing: ByteRingBuffer

    private var outputState = OutputState.BYPASS
    private var prevOutputState = OutputState.BYPASS
    @Volatile private var preBufferBytes = 0
    private var warmupBlendBytes = 0

    private var dryDelayBytes = 0
    private var enabledOutputBytesEmitted: Long = 0L

    private var crossfadeTotalBytes = 0
    private var crossfadeActive = false
    private var crossfadePosition = 0
    private var prevTail = ByteArray(0)

    @Volatile private var processingScheduled = false
    @Volatile private var shouldStopProcessing = false
    private val processingGeneration = AtomicLong(0L)

    private var avgInferMs = 0f
    private var inferSumMs = 0f
    private var inferMaxMs = 0f
    private var inferCount = 0
    private var starvedTransitions = 0
    private var lastPerfLogChunk = 0
    private var processIntervalMs = 0f

    private var procStftInput: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procIstftOutput: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procStftReal = FloatArray(0)
    private var procStftImag = FloatArray(0)
    private var procModelInput = FloatArray(0)
    private var procModelOutput = FloatArray(0)
    private var procIstftReal = FloatArray(0)
    private var procIstftImag = FloatArray(0)

    private var scratchInput = ByteArray(0)
    private var scratchDry = ByteArray(0)
    private var scratchOut = ByteArray(0)
    private var outputBuf: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())

    // Avoid per-chunk heap churn (GC spikes -> starvation -> dry/processed flip-flop).
    private var pcmChunkScratch = ByteArray(0)
    private var processedIntervalScratch = ByteArray(0)

    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        awaitProcessingStopped()

        // ExoPlayer may call reset()/configure() across track transitions and format changes.
        // If vocal removal is enabled, ensure the model is (re)loading here as well.
        if (enabled) {
            ensureModelLoadingAsync()
        }

        Log.i(
            TAG,
            "configure: sampleRate=${inputAudioFormat.sampleRate}, " +
                "channels=${inputAudioFormat.channelCount}, encoding=${inputAudioFormat.encoding}, " +
                "enabled=$enabled, model=${selectedModelProfile.uiLabel}"
        )

        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            Log.w(TAG, "Unsupported encoding: ${inputAudioFormat.encoding} (need PCM_16BIT)")
            this.inputAudioFormat = AudioFormat.NOT_SET
            this.outputAudioFormat = AudioFormat.NOT_SET
            canProcessFormat = false
            return AudioFormat.NOT_SET
        }

        this.inputAudioFormat = inputAudioFormat
        this.outputAudioFormat = inputAudioFormat
        applySelectedModelRuntimeConfig()
        setupRuntimeForFormat(inputAudioFormat)
        outputState = if (enabled) OutputState.WARMUP else OutputState.BYPASS
        return outputAudioFormat
    }

    override fun isActive(): Boolean = outputAudioFormat != AudioFormat.NOT_SET

    override fun queueInput(inputBuffer: ByteBuffer) {
        val inputBytes = inputBuffer.remaining()
        if (inputBytes <= 0) {
            outputBuffer = AudioProcessor.EMPTY_BUFFER
            return
        }

        if (!::outputRing.isInitialized || outputAudioFormat == AudioFormat.NOT_SET) {
            inputBuffer.position(inputBuffer.limit())
            outputBuffer = AudioProcessor.EMPTY_BUFFER
            return
        }

        if (!enabled || !canProcessFormat) {
            outputState = OutputState.BYPASS
            val out = ensureOutputBuf(inputBytes)
            out.put(inputBuffer)
            out.flip()
            outputBuffer = out
            return
        }

        ensureScratchCapacity(inputBytes)
        inputBuffer.get(scratchInput, 0, inputBytes)

        val outputStartBytes = enabledOutputBytesEmitted

        synchronized(inputLock) {
            var off = 0
            while (off < inputBytes) {
                val w = inputRing.writeBytes(scratchInput, off, inputBytes - off)
                if (w == 0) inputRing.discard(processIntervalBytes)
                else off += w
            }
        }

        synchronized(dryLock) {
            var off = 0
            while (off < inputBytes) {
                val w = dryDelayRing.writeBytes(scratchInput, off, inputBytes - off)
                if (w == 0) dryDelayRing.discard(processIntervalBytes)
                else off += w
            }
        }

        maybeScheduleProcessing()

        val dryRead: Int
        synchronized(dryLock) {
            val excess = dryDelayRing.availableBytes() - dryDelayBytes
            dryRead = if (excess >= inputBytes) inputBytes else maxOf(0, excess)
            if (dryRead > 0) {
                dryDelayRing.readToArray(scratchDry, dryRead)
            }
        }
        if (dryRead < inputBytes) {
            for (j in dryRead until inputBytes) scratchDry[j] = 0
        }

        prevOutputState = outputState
        val processedAvail = synchronized(outputLock) { outputRing.availableBytes() }

        outputState = when (outputState) {
            OutputState.BYPASS -> OutputState.WARMUP
            OutputState.WARMUP -> {
                val warmupDelayReady = enabledOutputBytesEmitted >= (dryDelayBytes + warmupBlendBytes).toLong()
                if (warmupDelayReady && isModelReady() && processedAvail >= preBufferBytes) OutputState.ACTIVE
                else OutputState.WARMUP
            }
            OutputState.ACTIVE -> {
                if (processedAvail >= inputBytes) OutputState.ACTIVE
                else OutputState.STARVED
            }
            OutputState.STARVED -> {
                val warmupDelayReady = enabledOutputBytesEmitted >= (dryDelayBytes + warmupBlendBytes).toLong()
                if (warmupDelayReady && processedAvail >= preBufferBytes) OutputState.ACTIVE
                else OutputState.STARVED
            }
        }

        val wasActive = prevOutputState == OutputState.ACTIVE
        val isNowActive = outputState == OutputState.ACTIVE
        if (prevOutputState == OutputState.ACTIVE && outputState == OutputState.STARVED) {
            starvedTransitions += 1
        }
        if (wasActive != isNowActive) {
            Log.i(
                TAG,
                "state: $prevOutputState -> $outputState (processedAvail=$processedAvail, inputBytes=$inputBytes, " +
                    "preBufferBytes=$preBufferBytes, modelReady=${isModelReady()}, channels=${inputAudioFormat.channelCount})"
            )
        }
        if (wasActive != isNowActive) {
            crossfadeActive = true
            crossfadePosition = 0
        }

        if (isNowActive) {
            synchronized(outputLock) {
                val read = outputRing.readToArray(scratchOut, inputBytes)
                if (read < inputBytes) {
                    // Safety: shouldn't happen due to state checks, but avoid stale bytes.
                    System.arraycopy(scratchDry, 0, scratchOut, 0, inputBytes)
                }
            }
        } else {
            // During WARMUP/STARVED, keep output continuous: start with original audio, then gently crossfade
            // to the delayed dry path (so we can later switch to processed without a hard jump).
            val emitted = enabledOutputBytesEmitted
            when {
                emitted < dryDelayBytes.toLong() -> {
                    System.arraycopy(scratchInput, 0, scratchOut, 0, inputBytes)
                }
                warmupBlendBytes <= 0 -> {
                    System.arraycopy(scratchDry, 0, scratchOut, 0, inputBytes)
                }
                emitted < (dryDelayBytes + warmupBlendBytes).toLong() -> {
                    val a = ((emitted - dryDelayBytes).toFloat() / warmupBlendBytes.toFloat()).coerceIn(0f, 1f)
                    val invA = 1f - a
                    val sampleCount = inputBytes / 2
                    for (i in 0 until sampleCount) {
                        val idx = i * 2
                        val sNow = ((scratchInput[idx + 1].toInt() shl 8) or (scratchInput[idx].toInt() and 0xFF)).toShort()
                        val sDel = ((scratchDry[idx + 1].toInt() shl 8) or (scratchDry[idx].toInt() and 0xFF)).toShort()
                        val mixed = (sNow.toInt() * invA + sDel.toInt() * a).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        scratchOut[idx] = (mixed and 0xFF).toByte()
                        scratchOut[idx + 1] = (mixed shr 8).toByte()
                    }
                }
                else -> {
                    System.arraycopy(scratchDry, 0, scratchOut, 0, inputBytes)
                }
            }
        }

        if (crossfadeActive) {
            applyCrossfade(scratchOut, inputBytes)
        }

        savePrevTail(scratchOut, inputBytes)

        val out = ensureOutputBuf(inputBytes)
        out.put(scratchOut, 0, inputBytes)
        out.flip()
        outputBuffer = out

        enabledOutputBytesEmitted = outputStartBytes + inputBytes.toLong()
    }

    override fun queueEndOfStream() {
        inputEnded = true
        val available = synchronized(outputLock) { outputRing.availableBytes() }
        if (available > 0 && outputState == OutputState.ACTIVE) {
            ensureScratchCapacity(available)
            val read = synchronized(outputLock) { outputRing.readToArray(scratchOut, available) }
            if (read > 0) {
                val out = ensureOutputBuf(read)
                out.put(scratchOut, 0, read)
                out.flip()
                outputBuffer = out
            } else {
                outputBuffer = AudioProcessor.EMPTY_BUFFER
            }
        } else {
            outputBuffer = AudioProcessor.EMPTY_BUFFER
        }
    }

    override fun getOutput(): ByteBuffer {
        val out = outputBuffer
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        return out
    }

    override fun isEnded(): Boolean {
        return inputEnded && outputBuffer === AudioProcessor.EMPTY_BUFFER &&
            (!::outputRing.isInitialized || synchronized(outputLock) { outputRing.availableBytes() } == 0)
    }

    override fun flush() {
        awaitProcessingStopped()
        inputEnded = false
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        synchronized(inputLock) { if (::inputRing.isInitialized) inputRing.clear() }
        synchronized(outputLock) { if (::outputRing.isInitialized) outputRing.clear() }
        synchronized(dryLock) {
            if (::dryDelayRing.isInitialized) {
                dryDelayRing.clear()
                prefillDryDelay()
            }
        }
        crossfadeActive = false
        crossfadePosition = 0
        if (prevTail.isNotEmpty()) prevTail.fill(0)
        avgInferMs = 0f
        inferSumMs = 0f
        inferMaxMs = 0f
        inferCount = 0
        lastPerfLogChunk = 0
        starvedTransitions = 0
        enabledOutputBytesEmitted = 0L
        outputState = if (enabled) OutputState.WARMUP else OutputState.BYPASS
    }

    override fun reset() {
        flush()
        if (nativeHandle != 0L) {
            nativeReleaseStft(nativeHandle)
            nativeHandle = 0L
        }
        // Keep ORT env/session alive across track transitions to avoid "effect disabled" after next song.
        // ExoPlayer can call reset() frequently; re-loading the model each time is expensive and brittle.
        inputAudioFormat = AudioFormat.NOT_SET
        outputAudioFormat = AudioFormat.NOT_SET
        canProcessFormat = false
    }

    private fun prefillDryDelay() {
        if (dryDelayBytes > 0 && ::dryDelayRing.isInitialized) {
            val silence = ByteArray(dryDelayBytes)
            dryDelayRing.writeBytes(silence, 0, silence.size)
        }
    }

    private fun setupRuntimeForFormat(format: AudioFormat) {
        bytesPerFrame = format.channelCount * 2
        chunkBytes = chunkSamples * bytesPerFrame
        processIntervalBytes = processIntervalSamples * bytesPerFrame
        extractOffsetBytes = extractOffsetSamples * bytesPerFrame

        // This implementation waits for a full chunk before producing the first processed interval.
        val latencySamples = (chunkSamples - extractOffsetSamples)
        dryDelayBytes = latencySamples * bytesPerFrame
        preBufferBytes = (processIntervalBytes * PREBUFFER_DEFAULT_X100 + 99) / 100
        warmupBlendBytes = (format.sampleRate * bytesPerFrame * WARMUP_BLEND_MS) / 1000
        crossfadeTotalBytes = (format.sampleRate * CROSSFADE_MS / 1000) * bytesPerFrame
        crossfadeActive = false
        crossfadePosition = 0
        prevTail = ByteArray(crossfadeTotalBytes)
        processIntervalMs = processIntervalSamples * 1000f / format.sampleRate
        avgInferMs = 0f
        inferSumMs = 0f
        inferMaxMs = 0f
        inferCount = 0
        starvedTransitions = 0
        lastPerfLogChunk = 0
        enabledOutputBytesEmitted = 0L

        val dryDelayMs = dryDelayBytes.toFloat() * 1000f / (format.sampleRate * bytesPerFrame)
        Log.i(
            TAG,
            "latency config: profile=${selectedModelProfile.uiLabel}, dimF=$modelDimF, T=$modelTargetT, " +
                "interval=${processIntervalMs.toInt()}ms, dryDelay=${dryDelayMs.toInt()}ms, " +
                "preBuffer=${(preBufferBytes * 1000f / (format.sampleRate * bytesPerFrame)).toInt()}ms, blend=${WARMUP_BLEND_MS}ms"
        )

        inputRing = ByteRingBuffer(3 * chunkBytes)
        outputRing = ByteRingBuffer(3 * chunkBytes)
        dryDelayRing = ByteRingBuffer(dryDelayBytes + 2 * chunkBytes)
        prefillDryDelay()

        procStftInput = ByteBuffer.allocateDirect(chunkBytes).order(ByteOrder.nativeOrder())
        procIstftOutput = ByteBuffer.allocateDirect(chunkBytes).order(ByteOrder.nativeOrder())

        val frameTensorSize = modelDimF * modelTargetT
        procStftReal = FloatArray(MODEL_CHANNELS * frameTensorSize)
        procStftImag = FloatArray(MODEL_CHANNELS * frameTensorSize)
        procModelInput = FloatArray(4 * frameTensorSize)
        procModelOutput = FloatArray(4 * frameTensorSize)
        procIstftReal = FloatArray(MODEL_CHANNELS * frameTensorSize)
        procIstftImag = FloatArray(MODEL_CHANNELS * frameTensorSize)

        val initSize = processIntervalBytes
        scratchInput = ByteArray(initSize)
        scratchDry = ByteArray(initSize)
        scratchOut = ByteArray(initSize)
        outputBuf = ByteBuffer.allocateDirect(initSize).order(ByteOrder.nativeOrder())

        pcmChunkScratch = ByteArray(chunkBytes)
        processedIntervalScratch = ByteArray(processIntervalBytes)

        if (nativeHandle != 0L) {
            nativeReleaseStft(nativeHandle)
            nativeHandle = 0L
        }
        nativeHandle = nativeInitStft(
            format.sampleRate, N_FFT, HOP_LENGTH, modelDimF
        )

        canProcessFormat = (format.channelCount == 1 || format.channelCount == 2) && nativeHandle != 0L
        if (!canProcessFormat) {
            Log.w(TAG, "Unsupported channelCount=${format.channelCount} (need mono/stereo) or native init failed")
        }
    }

    private fun maybeUpdateAdaptivePreBuffer() {
        if (inferCount < PERF_WARMUP_CHUNKS || processIntervalMs <= 0f || processIntervalBytes <= 0) return
        val ratio = avgInferMs / processIntervalMs
        val multiplierX100 = when {
            starvedTransitions > 0 -> PREBUFFER_MAX_X100
            ratio <= 0.50f -> 110
            ratio <= 0.65f -> 120
            ratio <= 0.80f -> PREBUFFER_DEFAULT_X100
            else -> PREBUFFER_MAX_X100
        }
        val newPreBuffer = (processIntervalBytes * multiplierX100 + 99) / 100
        if (newPreBuffer != preBufferBytes) {
            preBufferBytes = newPreBuffer
            val preBufferMs = preBufferBytes * 1000f / (inputAudioFormat.sampleRate * bytesPerFrame)
            Log.i(
                TAG,
                "Adaptive preBuffer updated: ${preBufferMs.toInt()}ms " +
                "(ratio=${String.format(Locale.US, "%.2f", ratio)}, " +
                    "avgInfer=${avgInferMs.toInt()}ms, budget=${processIntervalMs.toInt()}ms, " +
                    "starvedTransitions=$starvedTransitions)"
            )
        }
    }

    private fun maybeLogPerfSnapshot() {
        if (inferCount <= 0) return
        val shouldLog = inferCount == PERF_WARMUP_CHUNKS || inferCount == 24 || (inferCount - lastPerfLogChunk) >= 64
        if (!shouldLog) return
        lastPerfLogChunk = inferCount
        val inferAvg = inferSumMs / inferCount.toFloat()
        val preBufferMs = if (bytesPerFrame > 0) {
            preBufferBytes * 1000f / (inputAudioFormat.sampleRate * bytesPerFrame)
        } else {
            0f
        }
        Log.i(
            TAG,
            "perf snapshot: chunks=$inferCount avgInfer=${inferAvg.toInt()}ms emaInfer=${avgInferMs.toInt()}ms " +
                "maxInfer=${inferMaxMs.toInt()}ms budget=${processIntervalMs.toInt()}ms " +
                "preBuffer=${preBufferMs.toInt()}ms starvedTransitions=$starvedTransitions"
        )
    }

    // NOTE: We intentionally do not try to "seek-align" processed output to dry output via discards here.
    // Without an explicit timestamped queue, aggressive discards can prevent the processor from ever becoming ACTIVE.

    private fun ensureScratchCapacity(bytes: Int) {
        if (scratchInput.size < bytes) {
            scratchInput = ByteArray(bytes)
            scratchDry = ByteArray(bytes)
            scratchOut = ByteArray(bytes)
        }
    }

    private fun ensureOutputBuf(bytes: Int): ByteBuffer {
        if (outputBuf.capacity() < bytes) {
            outputBuf = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder())
        }
        outputBuf.clear()
        return outputBuf
    }

    private fun applyCrossfade(data: ByteArray, length: Int) {
        val remainingFade = crossfadeTotalBytes - crossfadePosition
        if (remainingFade <= 0) {
            crossfadeActive = false
            return
        }
        val fadeBytes = minOf(remainingFade, length)
        val sampleCount = fadeBytes / 2

        for (i in 0 until sampleCount) {
            val globalPos = crossfadePosition + i * 2
            val progress = globalPos.toFloat() / crossfadeTotalBytes.toFloat()
            val idx = i * 2

            val newSample = ((data[idx + 1].toInt() shl 8) or (data[idx].toInt() and 0xFF)).toShort()

            val oldSample: Short = if (globalPos + 1 < prevTail.size) {
                ((prevTail[globalPos + 1].toInt() shl 8) or (prevTail[globalPos].toInt() and 0xFF)).toShort()
            } else {
                0
            }

            val mixed = (oldSample.toInt() * (1f - progress) + newSample.toInt() * progress).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

            data[idx] = (mixed and 0xFF).toByte()
            data[idx + 1] = (mixed shr 8).toByte()
        }

        crossfadePosition += fadeBytes
        if (crossfadePosition >= crossfadeTotalBytes) {
            crossfadeActive = false
            crossfadePosition = 0
        }
    }

    private fun savePrevTail(data: ByteArray, length: Int) {
        if (crossfadeTotalBytes <= 0 || prevTail.size < crossfadeTotalBytes) return
        if (length >= crossfadeTotalBytes) {
            System.arraycopy(data, length - crossfadeTotalBytes, prevTail, 0, crossfadeTotalBytes)
        } else {
            val shift = crossfadeTotalBytes - length
            System.arraycopy(prevTail, length, prevTail, 0, shift)
            System.arraycopy(data, 0, prevTail, shift, length)
        }
    }

    private fun ensureModelLoadingAsync() {
        val alreadyLoadedForSelected = ortSession != null && activeModelProfile == selectedModelProfile
        if (alreadyLoadedForSelected || modelLoading) return
        modelLoading = true
        modelExecutor.execute {
            try {
                ensureModelLoaded()
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to load ONNX model; vocal removal will stay bypassed.", t)
            } finally {
                modelLoading = false
            }
        }
    }

    private fun ensureModelLoaded() {
        if (ortSession != null && activeModelProfile == selectedModelProfile) return
        closeCurrentOrtSession()

        val profile = selectedModelProfile
        val env = ortEnv ?: OrtEnvironment.getEnvironment().also { ortEnv = it }
        val modelBytes = context.assets.open(profile.assetFileName).use { it.readBytes() }

        val nnapiOptions = OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            addNnapi()
        }
        try {
            val newSession = env.createSession(modelBytes, nnapiOptions)
            val inputName = newSession.inputNames.firstOrNull()
                ?: throw IllegalStateException("Model ${profile.assetFileName} has no input")
            val inputNodeInfo = newSession.inputInfo[inputName]
                ?: throw IllegalStateException("Model ${profile.assetFileName} input info missing")
            val inputTensorInfo = inputNodeInfo.info as? TensorInfo
                ?: throw IllegalStateException("Model ${profile.assetFileName} input is not tensor")

            val outputName = newSession.outputNames.firstOrNull()
                ?: throw IllegalStateException("Model ${profile.assetFileName} has no output")
            val outputNodeInfo = newSession.outputInfo[outputName]
                ?: throw IllegalStateException("Model ${profile.assetFileName} output info missing")
            val outputTensorInfo = outputNodeInfo.info as? TensorInfo
                ?: throw IllegalStateException("Model ${profile.assetFileName} output is not tensor")

            if (!isSupportedModelShape(inputTensorInfo.shape, profile) || !isSupportedModelShape(outputTensorInfo.shape, profile)) {
                throw IllegalStateException(
                    "Unsupported model I/O shape for ${profile.assetFileName}. " +
                        "input=${inputTensorInfo.shape.contentToString()}, " +
                        "output=${outputTensorInfo.shape.contentToString()}, " +
                        "expected rank4 [1,4,${profile.dimF},${profile.targetT}|dynamic]"
                )
            }

            ortSession = newSession
            modelInputName = inputName
            activeModelProfile = profile
            Log.i(
                TAG,
                "ONNX session created with NNAPI for ${profile.uiLabel} " +
                    "(input=$inputName, inputShape=${inputTensorInfo.shape.contentToString()})"
            )
        } finally {
            try { nnapiOptions.close() } catch (_: Throwable) {}
        }
    }

    private fun isSupportedModelShape(shape: LongArray, profile: VocalRemovalModelProfile): Boolean {
        if (shape.size != 4) return false
        val channel = shape[1]
        val freq = shape[2]
        val t = shape[3]
        if (channel > 0 && channel != 4L) return false
        if (freq > 0 && freq != profile.dimF.toLong()) return false
        if (t > 0 && t != profile.targetT.toLong()) return false
        return true
    }

    private fun closeCurrentOrtSession() {
        val current = ortSession ?: return
        try {
            current.close()
        } catch (_: Throwable) {
        } finally {
            ortSession = null
            activeModelProfile = null
            modelInputName = "input"
        }
    }

    private fun isModelReady(): Boolean = ortSession != null

    private fun maybeScheduleProcessing() {
        if (processingScheduled) return
        val hasEnough = synchronized(inputLock) { inputRing.availableBytes() >= chunkBytes }
        if (!hasEnough) return
        processingScheduled = true
        shouldStopProcessing = false
        val gen = processingGeneration.get()
        modelExecutor.execute { processChunksInBackground(gen) }
    }

    private fun processChunksInBackground(gen: Long) {
        try {
            val handle = nativeHandle
            if (handle == 0L || !isModelReady()) return

            while (!shouldStopProcessing) {
                if (!enabled || gen != processingGeneration.get()) return
                val pcmData = pcmChunkScratch
                synchronized(inputLock) {
                    if (inputRing.availableBytes() < chunkBytes) return
                    inputRing.peekToArray(pcmData, chunkBytes)
                }

                val ok = processChunk(handle, pcmData, processedIntervalScratch)
                if (!ok) break
                if (!enabled || gen != processingGeneration.get()) return

                synchronized(outputLock) {
                    var off = 0
                    while (off < processIntervalBytes) {
                        val w = outputRing.writeBytes(processedIntervalScratch, off, processIntervalBytes - off)
                        if (w == 0) outputRing.discard(processIntervalBytes)
                        else off += w
                    }
                }
                synchronized(inputLock) {
                    inputRing.discard(processIntervalBytes)
                }
            }
        } finally {
            finalizeProcessingLoop()
        }
    }

    private fun finalizeProcessingLoop() {
        processingScheduled = false
        synchronized(processingDoneLock) {
            processingDoneLock.notifyAll()
        }
        if (!shouldStopProcessing) {
            maybeScheduleProcessing()
        }
    }

    private fun awaitProcessingStopped(timeoutMs: Long = 2000) {
        if (!processingScheduled) return
        shouldStopProcessing = true
        synchronized(processingDoneLock) {
            val deadline = System.currentTimeMillis() + timeoutMs
            while (processingScheduled) {
                val wait = deadline - System.currentTimeMillis()
                if (wait <= 0) break
                try {
                    processingDoneLock.wait(wait)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
    }

    private fun processChunk(handle: Long, pcmData: ByteArray, outInterval: ByteArray): Boolean {
        if (outInterval.size < processIntervalBytes) return false
        val t0 = System.nanoTime()

        procStftInput.clear()
        procStftInput.put(pcmData)
        procStftInput.flip()

        val frames = nativeComputeStft(
            handle, procStftInput, chunkSamples,
            procStftReal, procStftImag, inputAudioFormat.channelCount
        )
        if (frames <= 0) return false

        // Model expects stereo. If input is mono, duplicate channel 0 into channel 1.
        if (inputAudioFormat.channelCount == 1) {
            val channelStride = modelDimF * frames
            System.arraycopy(procStftReal, 0, procStftReal, channelStride, channelStride)
            System.arraycopy(procStftImag, 0, procStftImag, channelStride, channelStride)
        }
        val t1 = System.nanoTime()

        packModelInput(frames)
        if (!runModel(frames)) return false
        val t2 = System.nanoTime()

        unpackModelOutput(frames)

        procIstftOutput.clear()
        val outSamples = nativeComputeIstft(
            handle, procIstftReal, procIstftImag,
            procIstftOutput, frames, inputAudioFormat.channelCount
        )
        if (outSamples <= 0) return false
        val t3 = System.nanoTime()

        val inferMs = (t2 - t1) / 1_000_000f
        inferCount += 1
        inferSumMs += inferMs
        if (inferMs > inferMaxMs) inferMaxMs = inferMs
        avgInferMs = avgInferMs * (1 - INFER_EMA_ALPHA) + inferMs * INFER_EMA_ALPHA
        maybeUpdateAdaptivePreBuffer()
        maybeLogPerfSnapshot()
        if (avgInferMs > 0.8f * processIntervalMs) {
            Log.w(TAG, "Inference too slow: avg=${avgInferMs.toInt()}ms, budget=${processIntervalMs.toInt()}ms")
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "T=$frames STFT=${(t1-t0)/1_000_000}ms ONNX=${(t2-t1)/1_000_000}ms ISTFT=${(t3-t2)/1_000_000}ms total=${(t3-t0)/1_000_000}ms avg=${avgInferMs.toInt()}ms")
        }

        val fullBytes = outSamples * bytesPerFrame
        val extractEnd = extractOffsetBytes + processIntervalBytes
        if (extractEnd > fullBytes) return false

        procIstftOutput.position(extractOffsetBytes)
        procIstftOutput.get(outInterval, 0, processIntervalBytes)

        val mix = mixRatio.coerceIn(0f, 1f)
        if (mix < 1f) {
            val origOffset = extractOffsetBytes
            val shortCount = processIntervalBytes / 2
            for (i in 0 until shortCount) {
                val ri = i * 2
                val orig = ((pcmData[origOffset + ri + 1].toInt() shl 8) or (pcmData[origOffset + ri].toInt() and 0xFF)).toShort()
                val inst = ((outInterval[ri + 1].toInt() shl 8) or (outInterval[ri].toInt() and 0xFF)).toShort()
                val mixed = ((orig.toInt() * (1f - mix)) + (inst.toInt() * mix)).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                outInterval[ri] = (mixed and 0xFF).toByte()
                outInterval[ri + 1] = (mixed shr 8).toByte()
            }
        }

        return true
    }

    private fun packModelInput(frames: Int) {
        val channelStride = modelDimF * frames
        for (f in 0 until modelDimF) {
            val bandOffset = f * frames
            for (t in 0 until frames) {
                val tf = bandOffset + t
                val rBase = channelStride + tf
                procModelInput[tf] = procStftReal[tf]
                procModelInput[channelStride + tf] = procStftImag[tf]
                procModelInput[(2 * channelStride) + tf] = procStftReal[rBase]
                procModelInput[(3 * channelStride) + tf] = procStftImag[rBase]
            }
        }
    }

    private fun unpackModelOutput(frames: Int) {
        val channelStride = modelDimF * frames
        if (inputAudioFormat.channelCount == 1) {
            // For mono output, average L/R model outputs into a single complex spectrum.
            for (i in 0 until channelStride) {
                val lR = procModelOutput[i]
                val lI = procModelOutput[channelStride + i]
                val rR = procModelOutput[(2 * channelStride) + i]
                val rI = procModelOutput[(3 * channelStride) + i]
                procIstftReal[i] = 0.5f * (lR + rR)
                procIstftImag[i] = 0.5f * (lI + rI)
            }
        } else {
            for (f in 0 until modelDimF) {
                val bandOffset = f * frames
                for (t in 0 until frames) {
                    val tf = bandOffset + t
                    val rBase = channelStride + tf
                    procIstftReal[tf] = procModelOutput[tf]
                    procIstftImag[tf] = procModelOutput[channelStride + tf]
                    procIstftReal[rBase] = procModelOutput[(2 * channelStride) + tf]
                    procIstftImag[rBase] = procModelOutput[(3 * channelStride) + tf]
                }
            }
        }
    }

    private fun runModel(frames: Int): Boolean {
        val env = ortEnv ?: return false
        val session = ortSession ?: return false
        val shape = longArrayOf(1L, 4L, modelDimF.toLong(), frames.toLong())
        return try {
            OnnxTensor.createTensor(env, FloatBuffer.wrap(procModelInput), shape).use { inputTensor ->
                session.run(mapOf(modelInputName to inputTensor)).use { result ->
                    val outputTensor = result[0] as? OnnxTensor ?: return false
                    val buf = outputTensor.floatBuffer
                    buf.rewind()
                    buf.get(procModelOutput, 0, procModelOutput.size)
                }
            }
            true
        } catch (t: Throwable) {
            if (t is OrtException) Log.e(TAG, "ONNX inference failed", t)
            else Log.e(TAG, "Unexpected inference failure", t)
            false
        }
    }

    private external fun nativeInitStft(sampleRate: Int, nFft: Int, hopLength: Int, dimF: Int): Long

    private external fun nativeComputeStft(
        handle: Long, pcmInput: ByteBuffer, numSamples: Int,
        outputReal: FloatArray, outputImag: FloatArray, channelCount: Int
    ): Int

    private external fun nativeComputeIstft(
        handle: Long, inputReal: FloatArray, inputImag: FloatArray,
        pcmOutput: ByteBuffer, numFrames: Int, channelCount: Int
    ): Int

    private external fun nativeReleaseStft(handle: Long)

    private class ByteRingBuffer(capacity: Int) {
        private val buffer = ByteArray(capacity)
        private var readPos = 0
        private var writePos = 0
        private var size = 0

        fun availableBytes(): Int = size
        fun space(): Int = buffer.size - size

        fun clear() {
            readPos = 0
            writePos = 0
            size = 0
        }

        fun writeBytes(src: ByteArray, offset: Int, length: Int): Int {
            val writable = minOf(space(), length)
            if (writable <= 0) return 0
            var written = 0
            while (written < writable) {
                val chunk = minOf(writable - written, buffer.size - writePos)
                System.arraycopy(src, offset + written, buffer, writePos, chunk)
                writePos = (writePos + chunk) % buffer.size
                written += chunk
            }
            size += written
            return written
        }

        fun peekToArray(dst: ByteArray, length: Int): Int {
            val readable = minOf(length, size, dst.size)
            if (readable <= 0) return 0
            var copied = 0
            var cursor = readPos
            while (copied < readable) {
                val chunk = minOf(readable - copied, buffer.size - cursor)
                System.arraycopy(buffer, cursor, dst, copied, chunk)
                cursor = (cursor + chunk) % buffer.size
                copied += chunk
            }
            return copied
        }

        fun readToArray(dst: ByteArray, length: Int): Int {
            val readable = minOf(length, size, dst.size)
            if (readable <= 0) return 0
            var copied = 0
            while (copied < readable) {
                val chunk = minOf(readable - copied, buffer.size - readPos)
                System.arraycopy(buffer, readPos, dst, copied, chunk)
                readPos = (readPos + chunk) % buffer.size
                copied += chunk
            }
            size -= copied
            return copied
        }

        fun discard(length: Int): Int {
            val discarded = minOf(length, size)
            if (discarded <= 0) return 0
            readPos = (readPos + discarded) % buffer.size
            size -= discarded
            return discarded
        }
    }
}
