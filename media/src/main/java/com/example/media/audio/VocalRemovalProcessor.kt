package com.example.media.audio

import android.content.Context
import android.os.Process
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.util.UnstableApi
import com.example.media.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
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
        private const val TAG = "VocalRT"
        private const val PIPE_TAG = "AudioPipe"
        // MDX-style dynamic-T vocal spectrogram model:
        // input/output [1,4,2048,T] = L_real, L_imag, R_real, R_imag.
        // The model output is vocals; instrumental is computed as dry - vocals.
        private const val USE_WAVEFORM_MODEL = false
        private const val USE_POLARFORMER_MASK_MODEL = false
        private const val WAVEFORM_MODEL_SAMPLES = 16384
        private const val N_FFT = 6144
        private const val HOP_LENGTH = 1024
        private const val DEFAULT_DIM_F = 2048
        private const val DEFAULT_TARGET_T = 32
        private const val DEFAULT_OUTPUT_T = 24
        private const val DEFAULT_MODEL_ASSET_FILE = "vocal_separation_core.bin"
        private const val MODEL_LOG_NAME = "vocal_separation_core"
        private const val MODEL_OUTPUTS_VOCAL = true
        private const val VOCAL_SUBTRACT_GAIN = 1.00f
        private const val VOCAL_CONFIDENCE_FILTER_ENABLED = true
        private const val VOCAL_CONFIDENCE_LOW = 0.36f
        private const val VOCAL_CONFIDENCE_HIGH = 0.72f
        private const val VOCAL_CONFIDENCE_MAX_REDUCTION = 0.28f
        private const val VOCAL_CONFIDENCE_MIN_FREQ_HZ = 150f
        private const val VOCAL_CONFIDENCE_MAX_FREQ_HZ = 6500f
        private const val PROCESSOR_NAME = "vocal_separator"
        private const val BACKEND_REQUEST = "XNNPACK"

        private const val MODEL_CHANNELS = 2

        private const val CROSSFADE_MS = 30
        private const val INFER_EMA_ALPHA = 0.1f
        private const val PERF_WARMUP_CHUNKS = 8
        private const val LAST_INFER_VERBOSE_CHUNKS = 32
        private const val LAST_INFER_LOG_EVERY_CHUNKS = 8
        private const val VERIFY_NATIVE_ISTFT_INTERVAL_CHUNKS = 0
        private const val PREBUFFER_DEFAULT_X100 = 180
        private const val PREBUFFER_HIGH_X100 = 240
        private const val PREBUFFER_MAX_X100 = 300
        private const val STARVED_RECOVERY_MIN_X100 = PREBUFFER_HIGH_X100
        private const val ACTIVE_SHORTFALL_GRACE_CHUNKS = 12
        private const val SHORTFALL_BOUNDARY_FADE_SAMPLES = 128
        private const val INPUT_RING_INITIAL_CHUNKS = 10
        private const val OUTPUT_RING_INITIAL_CHUNKS = 10
        private const val RING_MAX_CHUNKS = 48
        private const val SESSION_IDLE_TIMEOUT_SEC = 30L
        private const val INFERENCE_THREAD_PRIORITY = Process.THREAD_PRIORITY_DISPLAY
        private const val FORCE_UNSUPPORTED_FOR_UI_CHECK = false

        private val nativeLibraryAvailable: Boolean = runCatching {
            System.loadLibrary("vocal_removal_native")
        }.onFailure { t ->
            if (!BuildConfig.DEBUG) return@onFailure
            Log.w(TAG, "Vocal removal native library unavailable on this ABI", t)
        }.isSuccess
    }

    val isSupported: Boolean
        get() = nativeLibraryAvailable && !FORCE_UNSUPPORTED_FOR_UI_CHECK

    @Volatile
    var enabled: Boolean = false
        set(value) {
            if (field == value) return
            if (value && !isSupported) {
                field = false
                outputState = OutputState.BYPASS
                logPerfWarning("Vocal removal is unavailable on this device ABI")
                return
            }
            field = value
            processingGeneration.incrementAndGet()
            logVocal(
                "EVENT enabled=$value processor=$PROCESSOR_NAME modelReady=${isModelReady()} " +
                    "canProcess=$canProcessFormat backend=Native_ONNXRuntime requested=$BACKEND_REQUEST model=$MODEL_LOG_NAME"
            )
            if (value) {
                cancelSessionRelease()
                ensureModelLoadingAsync()
                outputState = OutputState.WARMUP
                resetAdaptiveRuntimeState()
                synchronized(inputLock) { if (::inputRing.isInitialized) inputRing.clear() }
                synchronized(outputLock) { if (::outputRing.isInitialized) outputRing.clear() }
            } else {
                shouldStopProcessing = true
                outputState = OutputState.BYPASS
                synchronized(inputLock) { if (::inputRing.isInitialized) inputRing.clear() }
                synchronized(outputLock) { if (::outputRing.isInitialized) outputRing.clear() }
                crossfadeActive = false
                crossfadePosition = 0
                resetAdaptiveRuntimeState()
                scheduleSessionRelease()
            }
        }

    @Volatile
    var mixRatio: Float = 1.0f

    @Volatile
    var vocalOnlyMode: Boolean = false

    fun prewarm() {
        if (!isSupported) return
        ensureModelLoadingAsync()
    }

    private var inputAudioFormat: AudioFormat = AudioFormat.NOT_SET
    private var outputAudioFormat: AudioFormat = AudioFormat.NOT_SET
    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var inputEnded = false

    private val modelLoadExecutor = Executors.newSingleThreadExecutor(
        namedThreadFactory("VocalRT-ModelLoad", Process.THREAD_PRIORITY_BACKGROUND)
    )
    private val modelProcessExecutor = Executors.newSingleThreadExecutor(
        namedThreadFactory("VocalRT-Inference", INFERENCE_THREAD_PRIORITY)
    )
    private val idleScheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
        namedThreadFactory("VocalRT-Idle", Process.THREAD_PRIORITY_BACKGROUND)
    )
    private var sessionReleaseTask: ScheduledFuture<*>? = null
    @Volatile private var modelLoading = false
    @Volatile private var mdxModelHandle: Long = 0L
    private val mdxThreads: Int = 4

    @Volatile private var nativeHandle: Long = 0L

    private fun logVocal(message: String) {
        if (BuildConfig.DEBUG) Log.i(TAG, message)
    }

    private fun logPipe(message: String) {
        if (BuildConfig.DEBUG) Log.i(PIPE_TAG, message)
    }

    private fun logPerfWarning(message: String) {
        if (BuildConfig.DEBUG) Log.w(TAG, message)
    }

    private fun namedThreadFactory(name: String, priority: Int): ThreadFactory {
        return ThreadFactory { runnable ->
            Thread({
                try {
                    Process.setThreadPriority(priority)
                    logVocal("THREAD_PRIORITY name=$name priority=$priority")
                } catch (t: Throwable) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "THREAD_PRIORITY failed name=$name priority=$priority", t)
                    }
                }
                runnable.run()
            }, name).apply {
                isDaemon = true
            }
        }
    }

    private val modelDimF: Int = DEFAULT_DIM_F
    private val modelTargetT: Int = DEFAULT_TARGET_T
    private val chunkSamples: Int =
        if (USE_WAVEFORM_MODEL) WAVEFORM_MODEL_SAMPLES else ((DEFAULT_TARGET_T - 1) * HOP_LENGTH) + N_FFT
    private val processIntervalSamples: Int =
        if (USE_WAVEFORM_MODEL) WAVEFORM_MODEL_SAMPLES else DEFAULT_OUTPUT_T * HOP_LENGTH
    private val extractOffsetSamples: Int =
        if (USE_WAVEFORM_MODEL) 0 else (chunkSamples - processIntervalSamples) / 2

    @Volatile private var canProcessFormat = false
    private var bytesPerFrame = 0
    private var chunkBytes = 0
    private var processIntervalBytes = 0
    private var extractOffsetBytes = 0

    private val inputLock = Any()
    private val outputLock = Any()
    private val processingDoneLock = java.lang.Object()

    private lateinit var inputRing: ByteRingBuffer
    private lateinit var outputRing: ByteRingBuffer

    private var outputState = OutputState.BYPASS
    private var prevOutputState = OutputState.BYPASS
    @Volatile private var preBufferBytes = 0

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
    private var stftSumMs = 0f
    private var stftMaxMs = 0f
    private var onnxSumMs = 0f
    private var onnxMaxMs = 0f
    private var istftSumMs = 0f
    private var istftMaxMs = 0f
    private var totalSumMs = 0f
    private var totalMaxMs = 0f
    private var avgStftMs = 0f
    private var avgOnnxMs = 0f
    private var avgIstftMs = 0f
    private var avgTotalMs = 0f
    private var lastStftMs = 0f
    private var lastOnnxMs = 0f
    private var lastIstftMs = 0f
    private var lastTotalMs = 0f
    private var lastCopyInMs = 0f
    private var lastPackMs = 0f
    private var lastNativeOnnxMs = 0f
    private var lastUnpackMs = 0f
    private var lastFilterMs = 0f
    private var lastNativeIstftMs = 0f
    private var lastPostMs = 0f
    private var inferCount = 0
    private var starvedTransitions = 0
    private var lastPerfLogChunk = 0
    private var processIntervalMs = 0f
    private var activeShortfallStreak = 0

    private var procStftInput: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procIstftOutput: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procStftRealBuffer: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procStftImagBuffer: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procIstftRealBuffer: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procIstftImagBuffer: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procModelInputBuffer: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procModelOutputBuffer: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procWaveformInputBuffer: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procWaveformOutputBuffer: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())
    private var procStftRealFloats: FloatBuffer = procStftRealBuffer.asFloatBuffer()
    private var procStftImagFloats: FloatBuffer = procStftImagBuffer.asFloatBuffer()
    private var procIstftRealFloats: FloatBuffer = procIstftRealBuffer.asFloatBuffer()
    private var procIstftImagFloats: FloatBuffer = procIstftImagBuffer.asFloatBuffer()
    private var procModelInputFloats: FloatBuffer = procModelInputBuffer.asFloatBuffer()
    private var procModelOutputFloats: FloatBuffer = procModelOutputBuffer.asFloatBuffer()
    private var procWaveformInputFloats: FloatBuffer = procWaveformInputBuffer.asFloatBuffer()
    private var procWaveformOutputFloats: FloatBuffer = procWaveformOutputBuffer.asFloatBuffer()

    private var scratchInput = ByteArray(0)
    private var scratchOut = ByteArray(0)
    private var outputBuf: ByteBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder())

    // Avoid per-chunk heap churn (GC spikes -> starvation -> dry/processed flip-flop).
    private var pcmChunkScratch = ByteArray(0)
    private var processedIntervalScratch = ByteArray(0)
    private var verifyIstftIntervalScratch = ByteArray(0)

    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        val sameConfiguredFormat = outputAudioFormat != AudioFormat.NOT_SET &&
            sameAudioFormat(this.inputAudioFormat, inputAudioFormat) &&
            ::outputRing.isInitialized &&
            nativeHandle != 0L

        if (sameConfiguredFormat) {
            if (enabled) {
                ensureModelLoadingAsync()
            }
            logPipe(
                "CONFIG_SKIP stage=vocal processor=$PROCESSOR_NAME sampleRate=${inputAudioFormat.sampleRate} " +
                    "channels=${inputAudioFormat.channelCount} encoding=${inputAudioFormat.encoding} " +
                    "enabled=$enabled state=$outputState nativeHandle=$nativeHandle modelReady=${isModelReady()} " +
                    "inputAvail=${synchronized(inputLock) { inputRing.availableBytes() }} " +
                    "outputAvail=${synchronized(outputLock) { outputRing.availableBytes() }} " +
                    "model=$MODEL_LOG_NAME reason=same_format"
            )
            return outputAudioFormat
        }

        processingGeneration.incrementAndGet()
        awaitProcessingStopped()
        outputBuffer = AudioProcessor.EMPTY_BUFFER

        // ExoPlayer may call reset()/configure() across track transitions and format changes.
        // If vocal removal is enabled, ensure the model is (re)loading here as well.
        if (enabled) {
            ensureModelLoadingAsync()
        }

        logVocal(
                "CONFIG processor=$PROCESSOR_NAME sampleRate=${inputAudioFormat.sampleRate} " +
                    "channels=${inputAudioFormat.channelCount} encoding=${inputAudioFormat.encoding} " +
                    "enabled=$enabled model=$MODEL_LOG_NAME backend=Native_ONNXRuntime requested=$BACKEND_REQUEST"
            )
        logPipe(
            "CONFIG_APPLY stage=vocal processor=$PROCESSOR_NAME sampleRate=${inputAudioFormat.sampleRate} " +
                "channels=${inputAudioFormat.channelCount} encoding=${inputAudioFormat.encoding} enabled=$enabled"
        )

        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            logPerfWarning("Unsupported encoding: ${inputAudioFormat.encoding} (need PCM_16BIT)")
            logPipe(
                "CONFIG_REJECT stage=vocal reason=encoding encoding=${inputAudioFormat.encoding}"
            )
            this.inputAudioFormat = AudioFormat.NOT_SET
            this.outputAudioFormat = AudioFormat.NOT_SET
            canProcessFormat = false
            return AudioFormat.NOT_SET
        }

        this.inputAudioFormat = inputAudioFormat
        this.outputAudioFormat = inputAudioFormat
        if (!isSupported) {
            canProcessFormat = false
            outputState = OutputState.BYPASS
            logPipe("CONFIG_REJECT stage=vocal reason=native_unavailable")
            return outputAudioFormat
        }
        setupRuntimeForFormat(inputAudioFormat)
        outputState = if (enabled) OutputState.WARMUP else OutputState.BYPASS
        return outputAudioFormat
    }

    private fun sameAudioFormat(a: AudioFormat, b: AudioFormat): Boolean {
        return a.sampleRate == b.sampleRate &&
            a.channelCount == b.channelCount &&
            a.encoding == b.encoding
    }

    override fun isActive(): Boolean = outputAudioFormat != AudioFormat.NOT_SET

    override fun queueInput(inputBuffer: ByteBuffer) {
        val inputBytes = inputBuffer.remaining()
        if (inputBytes <= 0) {
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

        if (!::outputRing.isInitialized || outputAudioFormat == AudioFormat.NOT_SET) {
            inputBuffer.position(inputBuffer.limit())
            outputBuffer = AudioProcessor.EMPTY_BUFFER
            return
        }

        ensureScratchCapacity(inputBytes)
        inputBuffer.get(scratchInput, 0, inputBytes)

        synchronized(inputLock) {
            var off = 0
            while (off < inputBytes) {
                val w = inputRing.writeBytes(scratchInput, off, inputBytes - off)
                if (w > 0) {
                    off += w
                    continue
                }
                // Last-resort backpressure handling. Prefer keeping stream continuity over hard failure.
                val dropped = inputRing.discard(maxOf(bytesPerFrame, processIntervalBytes / 4))
                if (dropped <= 0) break
            }
        }

        maybeScheduleProcessing()

        prevOutputState = outputState
        val processedAvail = synchronized(outputLock) { outputRing.availableBytes() }
        val warmupActivationBytes = warmupActivationPreBufferBytes()
        val starvedRecoveryBytes = starvedRecoveryPreBufferBytes()

        outputState = when (outputState) {
            OutputState.BYPASS -> OutputState.WARMUP
            OutputState.WARMUP -> {
                if (isModelReady() && processedAvail >= warmupActivationBytes) OutputState.ACTIVE
                else OutputState.WARMUP
            }
            OutputState.ACTIVE -> {
                if (processedAvail >= inputBytes) {
                    activeShortfallStreak = 0
                    OutputState.ACTIVE
                } else {
                    activeShortfallStreak += 1
                    if (activeShortfallStreak >= ACTIVE_SHORTFALL_GRACE_CHUNKS) OutputState.STARVED
                    else OutputState.ACTIVE
                }
            }
            OutputState.STARVED -> {
                if (processedAvail >= starvedRecoveryBytes) {
                    activeShortfallStreak = 0
                    OutputState.ACTIVE
                } else {
                    OutputState.STARVED
                }
            }
        }

        val wasActive = prevOutputState == OutputState.ACTIVE
        val isNowActive = outputState == OutputState.ACTIVE
        if (prevOutputState == OutputState.ACTIVE && outputState == OutputState.STARVED) {
            starvedTransitions += 1
            forceMaxPreBufferAfterStarve()
        }
        if (wasActive != isNowActive) {
            logVocal(
                "STATE from=$prevOutputState to=$outputState processor=$PROCESSOR_NAME processedAvail=$processedAvail " +
                    "inputBytes=$inputBytes preBufferBytes=$preBufferBytes warmupBytes=$warmupActivationBytes " +
                    "recoveryBytes=$starvedRecoveryBytes modelReady=${isModelReady()} " +
                    "channels=${inputAudioFormat.channelCount} avgInferMs=${avgInferMs.toInt()} " +
                    "maxInferMs=${inferMaxMs.toInt()} starvedTransitions=$starvedTransitions"
            )
        }
        if (wasActive != isNowActive) {
            crossfadeActive = true
            crossfadePosition = 0
        }

        if (isNowActive) {
            val read = synchronized(outputLock) { outputRing.readToArray(scratchOut, inputBytes) }
            if (read < inputBytes) {
                fillProcessedShortfallWithoutDry(read, inputBytes)
            }
        } else {
            synchronized(outputLock) {
                val maxBufferedWhileInactive = preBufferBytes + (processIntervalBytes * 4)
                val excess = outputRing.availableBytes() - maxBufferedWhileInactive
                if (excess > processIntervalBytes) {
                    // Keep enough processed backlog to recover from STARVED/WARMUP without exposing dry audio.
                    outputRing.discard(excess)
                }
            }
            // During WARMUP/STARVED, do not expose delayed dry/original audio.
            // Dry output sounds like vocal-removal failure to users; wait silently until processed audio is ready.
            fillShortfallWithFadeToSilence(0, inputBytes)
        }

        if (crossfadeActive) {
            applyCrossfade(scratchOut, inputBytes)
        }

        savePrevTail(scratchOut, inputBytes)

        val out = ensureOutputBuf(inputBytes)
        out.put(scratchOut, 0, inputBytes)
        out.flip()
        outputBuffer = out
    }

    private fun warmupActivationPreBufferBytes(): Int {
        if (processIntervalBytes <= 0) return preBufferBytes
        // Keep first activation fast on devices that can stay ahead of real time.
        // Larger preBuffer is still used for STARVED recovery and diagnostics.
        return minOf(preBufferBytes, processIntervalBytes)
    }

    private fun starvedRecoveryPreBufferBytes(): Int {
        if (processIntervalBytes <= 0) return preBufferBytes
        val minRecoveryBytes = (processIntervalBytes * STARVED_RECOVERY_MIN_X100 + 99) / 100
        val maxRecoveryBytes = processIntervalBytes * PREBUFFER_MAX_X100 / 100
        return minOf(maxOf(preBufferBytes, minRecoveryBytes), maxRecoveryBytes)
    }

    override fun queueEndOfStream() {
        if (inputEnded && !outputBuffer.hasRemaining()) {
            outputBuffer = AudioProcessor.EMPTY_BUFFER
            return
        }

        inputEnded = true
        val available = synchronized(outputLock) { outputRing.availableBytes() }
        logPipe(
            "EOS stage=vocal state=$outputState outputAvail=$available enabled=$enabled modelReady=${isModelReady()}"
        )
        // outputRing intentionally carries processed backlog for starvation protection.
        // At EOS, draining that backlog exposes delayed audio from the ending track
        // before the next track starts. Drop it instead of playing stale tail audio.
        synchronized(outputLock) { if (::outputRing.isInitialized) outputRing.clear() }
        outputBuffer = AudioProcessor.EMPTY_BUFFER
    }

    override fun getOutput(): ByteBuffer {
        val out = outputBuffer
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        return out
    }

    override fun isEnded(): Boolean {
        return inputEnded && !outputBuffer.hasRemaining() &&
            (!::outputRing.isInitialized || synchronized(outputLock) { outputRing.availableBytes() } == 0)
    }

    override fun flush() {
        val inputAvail = if (::inputRing.isInitialized) synchronized(inputLock) { inputRing.availableBytes() } else -1
        val outputAvail = if (::outputRing.isInitialized) synchronized(outputLock) { outputRing.availableBytes() } else -1
        logPipe(
            "FLUSH stage=vocal enabled=$enabled state=$outputState inputAvail=$inputAvail " +
                "outputAvail=$outputAvail scheduled=$processingScheduled generation=${processingGeneration.get()}"
        )
        processingGeneration.incrementAndGet()
        awaitProcessingStopped()
        inputEnded = false
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        synchronized(inputLock) { if (::inputRing.isInitialized) inputRing.clear() }
        synchronized(outputLock) { if (::outputRing.isInitialized) outputRing.clear() }
        crossfadeActive = false
        crossfadePosition = 0
        if (prevTail.isNotEmpty()) prevTail.fill(0)
        avgInferMs = 0f
        inferSumMs = 0f
        inferMaxMs = 0f
        stftSumMs = 0f
        stftMaxMs = 0f
        onnxSumMs = 0f
        onnxMaxMs = 0f
        istftSumMs = 0f
        istftMaxMs = 0f
        totalSumMs = 0f
        totalMaxMs = 0f
        avgStftMs = 0f
        avgOnnxMs = 0f
        avgIstftMs = 0f
        avgTotalMs = 0f
        lastStftMs = 0f
        lastOnnxMs = 0f
        lastIstftMs = 0f
        lastTotalMs = 0f
        resetDetailedStageMetrics()
        inferCount = 0
        lastPerfLogChunk = 0
        starvedTransitions = 0
        activeShortfallStreak = 0
        outputState = if (enabled) OutputState.WARMUP else OutputState.BYPASS
    }

    override fun reset() {
        logPipe(
            "RESET stage=vocal enabled=$enabled state=$outputState nativeHandle=$nativeHandle modelReady=${isModelReady()}"
        )
        flush()
        if (nativeHandle != 0L) {
            nativeReleaseStft(nativeHandle)
            nativeHandle = 0L
        }
        // Keep the native model session alive across track transitions to avoid reload gaps.
        // ExoPlayer can call reset() frequently; re-loading the model each time is expensive and brittle.
        inputAudioFormat = AudioFormat.NOT_SET
        outputAudioFormat = AudioFormat.NOT_SET
        canProcessFormat = false
    }

    private fun resetDetailedStageMetrics() {
        lastCopyInMs = 0f
        lastPackMs = 0f
        lastNativeOnnxMs = 0f
        lastUnpackMs = 0f
        lastFilterMs = 0f
        lastNativeIstftMs = 0f
        lastPostMs = 0f
    }

    private fun resetAdaptiveRuntimeState() {
        if (processIntervalBytes > 0) {
            preBufferBytes = (processIntervalBytes * PREBUFFER_DEFAULT_X100 + 99) / 100
        }
        avgInferMs = 0f
        inferSumMs = 0f
        inferMaxMs = 0f
        stftSumMs = 0f
        stftMaxMs = 0f
        onnxSumMs = 0f
        onnxMaxMs = 0f
        istftSumMs = 0f
        istftMaxMs = 0f
        totalSumMs = 0f
        totalMaxMs = 0f
        avgStftMs = 0f
        avgOnnxMs = 0f
        avgIstftMs = 0f
        avgTotalMs = 0f
        lastStftMs = 0f
        lastOnnxMs = 0f
        lastIstftMs = 0f
        lastTotalMs = 0f
        resetDetailedStageMetrics()
        inferCount = 0
        lastPerfLogChunk = 0
        starvedTransitions = 0
        activeShortfallStreak = 0
    }

    private fun setupRuntimeForFormat(format: AudioFormat) {
        bytesPerFrame = format.channelCount * 2
        chunkBytes = chunkSamples * bytesPerFrame
        processIntervalBytes = processIntervalSamples * bytesPerFrame
        extractOffsetBytes = extractOffsetSamples * bytesPerFrame

        // This implementation waits for a full chunk before producing the first processed interval.
        val latencySamples = (chunkSamples - extractOffsetSamples)
        val pipelineLatencyMs = latencySamples * 1000f / format.sampleRate
        preBufferBytes = (processIntervalBytes * PREBUFFER_DEFAULT_X100 + 99) / 100
        crossfadeTotalBytes = (format.sampleRate * CROSSFADE_MS / 1000) * bytesPerFrame
        crossfadeActive = false
        crossfadePosition = 0
        prevTail = ByteArray(crossfadeTotalBytes)
        processIntervalMs = processIntervalSamples * 1000f / format.sampleRate
        avgInferMs = 0f
        inferSumMs = 0f
        inferMaxMs = 0f
        stftSumMs = 0f
        stftMaxMs = 0f
        onnxSumMs = 0f
        onnxMaxMs = 0f
        istftSumMs = 0f
        istftMaxMs = 0f
        totalSumMs = 0f
        totalMaxMs = 0f
        avgStftMs = 0f
        avgOnnxMs = 0f
        avgIstftMs = 0f
        avgTotalMs = 0f
        lastStftMs = 0f
        lastOnnxMs = 0f
        lastIstftMs = 0f
        lastTotalMs = 0f
        resetDetailedStageMetrics()
        inferCount = 0
        starvedTransitions = 0
        lastPerfLogChunk = 0
        activeShortfallStreak = 0

        logVocal(
                "BACKEND_CONFIG processor=$PROCESSOR_NAME mode=LEGACY_MDX requested=$BACKEND_REQUEST " +
                "actualHardware=CPU runtime=Native_ONNXRuntime model=$MODEL_LOG_NAME " +
                "dimF=$modelDimF T=$modelTargetT outputT=$DEFAULT_OUTPUT_T intervalMs=${processIntervalMs.toInt()} " +
                "pipelineLatencyMs=${pipelineLatencyMs.toInt()} preBufferMs=${(preBufferBytes * 1000f / (format.sampleRate * bytesPerFrame)).toInt()} " +
                "vocalSubtractGain=$VOCAL_SUBTRACT_GAIN"
        )

        inputRing = ByteRingBuffer(
            INPUT_RING_INITIAL_CHUNKS * chunkBytes,
            RING_MAX_CHUNKS * chunkBytes
        )
        outputRing = ByteRingBuffer(
            OUTPUT_RING_INITIAL_CHUNKS * chunkBytes,
            RING_MAX_CHUNKS * chunkBytes
        )

        procStftInput = ByteBuffer.allocateDirect(chunkBytes).order(ByteOrder.nativeOrder())
        procIstftOutput = ByteBuffer.allocateDirect(chunkBytes).order(ByteOrder.nativeOrder())

        val frameTensorSize = modelDimF * modelTargetT
        val stereoTensorSize = MODEL_CHANNELS * frameTensorSize
        val modelTensorSize = 4 * frameTensorSize
        procStftRealBuffer = allocateFloatByteBuffer(stereoTensorSize)
        procStftImagBuffer = allocateFloatByteBuffer(stereoTensorSize)
        procIstftRealBuffer = allocateFloatByteBuffer(stereoTensorSize)
        procIstftImagBuffer = allocateFloatByteBuffer(stereoTensorSize)
        procModelInputBuffer = allocateFloatByteBuffer(modelTensorSize)
        procModelOutputBuffer = allocateFloatByteBuffer(modelTensorSize)
        procWaveformInputBuffer = allocateFloatByteBuffer(MODEL_CHANNELS * WAVEFORM_MODEL_SAMPLES)
        procWaveformOutputBuffer = allocateFloatByteBuffer(MODEL_CHANNELS * WAVEFORM_MODEL_SAMPLES)
        procStftRealFloats = procStftRealBuffer.asFloatBuffer()
        procStftImagFloats = procStftImagBuffer.asFloatBuffer()
        procIstftRealFloats = procIstftRealBuffer.asFloatBuffer()
        procIstftImagFloats = procIstftImagBuffer.asFloatBuffer()
        procModelInputFloats = procModelInputBuffer.asFloatBuffer()
        procModelOutputFloats = procModelOutputBuffer.asFloatBuffer()
        procWaveformInputFloats = procWaveformInputBuffer.asFloatBuffer()
        procWaveformOutputFloats = procWaveformOutputBuffer.asFloatBuffer()

        val initSize = processIntervalBytes
        scratchInput = ByteArray(initSize)
        scratchOut = ByteArray(initSize)
        outputBuf = ByteBuffer.allocateDirect(initSize).order(ByteOrder.nativeOrder())

        pcmChunkScratch = ByteArray(chunkBytes)
        processedIntervalScratch = ByteArray(processIntervalBytes)
        verifyIstftIntervalScratch = ByteArray(processIntervalBytes)

        if (nativeHandle != 0L) {
            nativeReleaseStft(nativeHandle)
            nativeHandle = 0L
        }
        nativeHandle = nativeInitStft(
            format.sampleRate, N_FFT, HOP_LENGTH, modelDimF
        )

        canProcessFormat = format.sampleRate == 44100 &&
            (format.channelCount == 1 || format.channelCount == 2) &&
            nativeHandle != 0L
        if (!canProcessFormat) {
            logPerfWarning(
                "Unsupported format for $PROCESSOR_NAME: sampleRate=${format.sampleRate} " +
                    "channels=${format.channelCount}; need 44100Hz mono/stereo"
            )
        }
    }

    private fun maybeUpdateAdaptivePreBuffer() {
        if (inferCount < PERF_WARMUP_CHUNKS || processIntervalMs <= 0f || processIntervalBytes <= 0) return
        val ratio = avgInferMs / processIntervalMs
        val multiplierX100 = when {
            starvedTransitions > 0 -> PREBUFFER_MAX_X100
            ratio <= 0.50f -> PREBUFFER_DEFAULT_X100
            ratio <= 0.65f -> 200
            ratio <= 0.80f -> PREBUFFER_HIGH_X100
            else -> PREBUFFER_MAX_X100
        }
        val newPreBuffer = (processIntervalBytes * multiplierX100 + 99) / 100
        if (newPreBuffer != preBufferBytes) {
            preBufferBytes = newPreBuffer
            val preBufferMs = preBufferBytes * 1000f / (inputAudioFormat.sampleRate * bytesPerFrame)
            logVocal(
                "Adaptive preBuffer updated: ${preBufferMs.toInt()}ms " +
                "(ratio=${String.format(Locale.US, "%.2f", ratio)}, " +
                    "avgInfer=${avgInferMs.toInt()}ms, budget=${processIntervalMs.toInt()}ms, " +
                    "starvedTransitions=$starvedTransitions)"
            )
        }
    }

    private fun forceMaxPreBufferAfterStarve() {
        if (processIntervalBytes <= 0 || inputAudioFormat == AudioFormat.NOT_SET) return
        val maxPreBuffer = processIntervalBytes * PREBUFFER_MAX_X100 / 100
        if (preBufferBytes >= maxPreBuffer) return

        preBufferBytes = maxPreBuffer
        val preBufferMs = preBufferBytes * 1000f / (inputAudioFormat.sampleRate * bytesPerFrame)
        logVocal(
            "Adaptive preBuffer forced after STARVED: ${preBufferMs.toInt()}ms " +
                "(avgInfer=${avgInferMs.toInt()}ms, budget=${processIntervalMs.toInt()}ms, " +
                "starvedTransitions=$starvedTransitions)"
        )
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
        val inputAvail = if (::inputRing.isInitialized) synchronized(inputLock) { inputRing.availableBytes() } else 0
        val outputAvail = if (::outputRing.isInitialized) synchronized(outputLock) { outputRing.availableBytes() } else 0
        val inputAvailMs = bytesToMs(inputAvail)
        val outputAvailMs = bytesToMs(outputAvail)
        val callsPerSec = if (processIntervalMs > 0f) 1000f / processIntervalMs else 0f
        logVocal(
            "INFER processor=$PROCESSOR_NAME chunks=$inferCount avgMs=${inferAvg.toInt()} " +
                "emaMs=${avgInferMs.toInt()} maxMs=${inferMaxMs.toInt()} " +
                "stageAvgMs=stft:${(stftSumMs / inferCount).toInt()},onnx:${(onnxSumMs / inferCount).toInt()},istft:${(istftSumMs / inferCount).toInt()},total:${(totalSumMs / inferCount).toInt()} " +
                "stageEmaMs=stft:${avgStftMs.toInt()},onnx:${avgOnnxMs.toInt()},istft:${avgIstftMs.toInt()},total:${avgTotalMs.toInt()} " +
                "stageMaxMs=stft:${stftMaxMs.toInt()},onnx:${onnxMaxMs.toInt()},istft:${istftMaxMs.toInt()},total:${totalMaxMs.toInt()} " +
                "budgetMs=${processIntervalMs.toInt()} preBufferMs=${preBufferMs.toInt()} " +
                "inputAvailMs=${inputAvailMs.toInt()} outputAvailMs=${outputAvailMs.toInt()} " +
                "callsPerSec=${String.format(Locale.US, "%.2f", callsPerSec)} " +
                "starvedTransitions=$starvedTransitions backend=Native_ONNXRuntime"
        )
    }

    private fun maybeLogLastInference() {
        if (inferCount <= 0) return
        val shouldLog = inferCount <= LAST_INFER_VERBOSE_CHUNKS ||
            inferCount % LAST_INFER_LOG_EVERY_CHUNKS == 0
        if (!shouldLog) return
        val rtf = if (processIntervalMs > 0f) lastOnnxMs / processIntervalMs else 0f
        val inputAvail = if (::inputRing.isInitialized) synchronized(inputLock) { inputRing.availableBytes() } else 0
        val outputAvail = if (::outputRing.isInitialized) synchronized(outputLock) { outputRing.availableBytes() } else 0
        logVocal(
            "INFER_LAST processor=$PROCESSOR_NAME chunk=$inferCount " +
                "onnxMs=${lastOnnxMs.toInt()} onnxSec=${String.format(Locale.US, "%.3f", lastOnnxMs / 1000f)} " +
                "totalMs=${lastTotalMs.toInt()} totalSec=${String.format(Locale.US, "%.3f", lastTotalMs / 1000f)} " +
                "stftMs=${lastStftMs.toInt()} istftMs=${lastIstftMs.toInt()} " +
                "detailMs=copyIn:${lastCopyInMs.toInt()},pack:${lastPackMs.toInt()}," +
                "native:${lastNativeOnnxMs.toInt()},unpack:${lastUnpackMs.toInt()}," +
                "filter:${lastFilterMs.toInt()},nativeIstft:${lastNativeIstftMs.toInt()}," +
                "post:${lastPostMs.toInt()} " +
                "budgetMs=${processIntervalMs.toInt()} rtf=${String.format(Locale.US, "%.2f", rtf)} " +
                "inputAvailMs=${bytesToMs(inputAvail).toInt()} outputAvailMs=${bytesToMs(outputAvail).toInt()}"
        )
    }

    // NOTE: We intentionally do not try to "seek-align" processed output to dry output via discards here.
    // Without an explicit timestamped queue, aggressive discards can prevent the processor from ever becoming ACTIVE.

    private fun allocateFloatByteBuffer(floatCount: Int): ByteBuffer {
        return ByteBuffer.allocateDirect(floatCount * Float.SIZE_BYTES).order(ByteOrder.nativeOrder())
    }

    private fun bytesToMs(bytes: Int): Float {
        if (bytes <= 0 || bytesPerFrame <= 0 || inputAudioFormat == AudioFormat.NOT_SET) return 0f
        return bytes * 1000f / (inputAudioFormat.sampleRate * bytesPerFrame)
    }

    private fun ensureScratchCapacity(bytes: Int) {
        if (scratchInput.size < bytes) {
            scratchInput = ByteArray(bytes)
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

    private fun fillProcessedShortfallWithoutDry(processedBytes: Int, totalBytes: Int) {
        if (processedBytes <= 0) {
            fillShortfallWithFadeToSilence(0, totalBytes)
            return
        }
        if (processedBytes >= totalBytes) return

        val missingBytes = totalBytes - processedBytes
        val fadeSamples = minOf(SHORTFALL_BOUNDARY_FADE_SAMPLES, missingBytes / 2)
        val fadeBytes = fadeSamples * 2

        if (fadeBytes <= 0 || processedBytes < 2) {
            java.util.Arrays.fill(scratchOut, processedBytes, totalBytes, 0.toByte())
            return
        }

        val lastProcessed =
            ((scratchOut[processedBytes - 1].toInt() shl 8) or (scratchOut[processedBytes - 2].toInt() and 0xFF)).toShort()

        for (i in 0 until fadeSamples) {
            val dstIndex = processedBytes + (i * 2)
            val alpha = (i + 1).toFloat() / fadeSamples.toFloat()
            val blended = (lastProcessed.toInt() * (1f - alpha)).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            scratchOut[dstIndex] = (blended and 0xFF).toByte()
            scratchOut[dstIndex + 1] = (blended shr 8).toByte()
        }

        val remainStart = processedBytes + fadeBytes
        if (remainStart < totalBytes) {
            java.util.Arrays.fill(scratchOut, remainStart, totalBytes, 0.toByte())
        }
    }

    private fun fillShortfallWithFadeToSilence(startByte: Int, totalBytes: Int) {
        if (startByte >= totalBytes) return
        val fadeSamples = minOf(SHORTFALL_BOUNDARY_FADE_SAMPLES, (totalBytes - startByte) / 2)
        val oldSample: Short = if (prevTail.size >= 2) {
            val idx = prevTail.size - 2
            ((prevTail[idx + 1].toInt() shl 8) or (prevTail[idx].toInt() and 0xFF)).toShort()
        } else {
            0
        }
        for (i in 0 until fadeSamples) {
            val dstIndex = startByte + (i * 2)
            val alpha = (i + 1).toFloat() / fadeSamples.toFloat()
            val blended = (oldSample.toInt() * (1f - alpha)).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            scratchOut[dstIndex] = (blended and 0xFF).toByte()
            scratchOut[dstIndex + 1] = (blended shr 8).toByte()
        }
        val remainStart = startByte + (fadeSamples * 2)
        if (remainStart < totalBytes) {
            java.util.Arrays.fill(scratchOut, remainStart, totalBytes, 0.toByte())
        }
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
        if (!isSupported) return
        if (mdxModelHandle != 0L || modelLoading) {
            logVocal("MODEL_LOAD skip processor=$PROCESSOR_NAME modelReady=${mdxModelHandle != 0L} loading=$modelLoading")
            return
        }
        modelLoading = true
        logVocal("MODEL_LOAD start processor=$PROCESSOR_NAME backend=Native_ONNXRuntime mode=LEGACY_MDX")
        modelLoadExecutor.execute {
            try {
                ensureModelLoaded()
            } catch (t: Throwable) {
                Log.e(TAG, "MODEL_LOAD failed processor=$PROCESSOR_NAME type=${t.javaClass.simpleName} message=${t.message}", t)
            } finally {
                modelLoading = false
            }
        }
    }

    private fun ensureModelLoaded() {
        if (!isSupported) return
        if (mdxModelHandle != 0L) return
        closeCurrentModelSession()

        val modelDir = File(context.filesDir, "mdx").apply { if (!exists()) mkdirs() }
        modelDir.listFiles()?.forEach { cachedFile ->
            if (cachedFile.isFile && cachedFile.name != DEFAULT_MODEL_ASSET_FILE) {
                cachedFile.delete()
            }
        }
        val modelFile = File(modelDir, DEFAULT_MODEL_ASSET_FILE)
        val assetSize = context.assets.open(DEFAULT_MODEL_ASSET_FILE).use { it.available().toLong() }
        if (!modelFile.exists() || modelFile.length() != assetSize) {
            logVocal("MODEL_COPY start asset=$MODEL_LOG_NAME expectedBytes=$assetSize")
            context.assets.open(DEFAULT_MODEL_ASSET_FILE).use { input ->
                modelFile.outputStream().use { output -> input.copyTo(output) }
            }
            logVocal("MODEL_COPY done asset=$MODEL_LOG_NAME bytes=${modelFile.length()}")
        } else {
            logVocal("MODEL_COPY skip asset=$MODEL_LOG_NAME bytes=${modelFile.length()}")
        }

        logVocal("BACKEND_ATTACH runtime=Native_ONNXRuntime requested=$BACKEND_REQUEST cpuFallback=disabled")
        val handle = nativeInitMdxModel(modelFile.absolutePath, mdxThreads, modelDimF, 4)
        if (handle == 0L) {
            throw IllegalStateException("nativeInitMdxModel returned 0 for $MODEL_LOG_NAME")
        }
        mdxModelHandle = handle
        val modelIo = if (MODEL_OUTPUTS_VOCAL) "spectrogram_vocals" else "spectrogram_instrumental"
        logVocal("BACKEND_READY processor=$PROCESSOR_NAME runtime=Native_ONNXRuntime requested=$BACKEND_REQUEST model=$MODEL_LOG_NAME dimF=$modelDimF T=$modelTargetT io=$modelIo")
    }

    private fun closeCurrentModelSession() {
        val handle = mdxModelHandle
        mdxModelHandle = 0L
        if (handle != 0L) {
            try { nativeReleaseMdxModel(handle) } catch (_: Throwable) {}
        }
    }

    private fun isModelReady(): Boolean =
        mdxModelHandle != 0L

    private fun scheduleSessionRelease() {
        sessionReleaseTask?.cancel(false)
        sessionReleaseTask = idleScheduler.schedule({
            if (!enabled && isModelReady()) {
                closeCurrentModelSession()
                logVocal("MDX session released after ${SESSION_IDLE_TIMEOUT_SEC}s idle timeout")
            }
        }, SESSION_IDLE_TIMEOUT_SEC, TimeUnit.SECONDS)
    }

    private fun cancelSessionRelease() {
        sessionReleaseTask?.cancel(false)
        sessionReleaseTask = null
    }

    private fun maybeScheduleProcessing() {
        if (processingScheduled) return
        val hasEnough = synchronized(inputLock) { inputRing.availableBytes() >= chunkBytes }
        if (!hasEnough) return
        processingScheduled = true
        shouldStopProcessing = false
        val gen = processingGeneration.get()
        modelProcessExecutor.execute { processChunksInBackground(gen) }
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
                        if (w > 0) {
                            off += w
                            continue
                        }
                        val dropped = outputRing.discard(maxOf(bytesPerFrame, processIntervalBytes / 4))
                        if (dropped <= 0) break
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
        if (USE_WAVEFORM_MODEL) {
            return processWaveformChunk(handle, pcmData, outInterval)
        }

        val t0 = System.nanoTime()

        procStftInput.clear()
        procStftInput.put(pcmData)
        procStftInput.flip()
        val tCopyIn = System.nanoTime()

        val frames = nativeComputeStft(
            handle, procStftInput, chunkSamples,
            procStftRealBuffer, procStftImagBuffer, inputAudioFormat.channelCount
        )
        if (frames <= 0) return false

        // Model expects stereo. If input is mono, duplicate channel 0 into channel 1.
        if (inputAudioFormat.channelCount == 1) {
            val channelStride = modelDimF * frames
            for (i in 0 until channelStride) {
                procStftRealFloats.put(channelStride + i, procStftRealFloats.get(i))
                procStftImagFloats.put(channelStride + i, procStftImagFloats.get(i))
            }
        }
        val t1 = System.nanoTime()

        packModelInput(frames)
        val tPack = System.nanoTime()
        lastNativeOnnxMs = 0f
        lastUnpackMs = 0f
        if (!runModel(frames)) return false
        val t2 = System.nanoTime()
        val modelOutputConvertedToInstrumental = MODEL_OUTPUTS_VOCAL &&
            VOCAL_CONFIDENCE_FILTER_ENABLED &&
            !vocalOnlyMode
        if (modelOutputConvertedToInstrumental) {
            if (!applyVocalConfidenceSpectralFilter(handle, frames)) return false
        }
        val tFilter = System.nanoTime()

        val outSamples = nativeComputeIstftInterval(
            handle,
            procIstftRealBuffer,
            procIstftImagBuffer,
            pcmData,
            outInterval,
            frames,
            inputAudioFormat.channelCount,
            extractOffsetSamples,
            processIntervalSamples,
            extractOffsetBytes,
            mixRatio.coerceIn(0f, 1f),
            VOCAL_SUBTRACT_GAIN,
            MODEL_OUTPUTS_VOCAL,
            modelOutputConvertedToInstrumental,
            vocalOnlyMode
        )
        val mixedInNative = outSamples == processIntervalSamples
        if (!mixedInNative) {
            logPerfWarning(
                "ISTFT_INTERVAL_FALLBACK processor=$PROCESSOR_NAME chunk=${inferCount + 1} " +
                    "outSamples=$outSamples expected=$processIntervalSamples frames=$frames " +
                    "channels=${inputAudioFormat.channelCount}"
            )
            procIstftOutput.clear()
            val fullOutSamples = nativeComputeIstft(
                handle, procIstftRealBuffer, procIstftImagBuffer,
                procIstftOutput, frames, inputAudioFormat.channelCount
            )
            if (fullOutSamples <= 0) return false

            val fullBytes = fullOutSamples * bytesPerFrame
            val extractEnd = extractOffsetBytes + processIntervalBytes
            if (extractEnd > fullBytes) return false

            procIstftOutput.position(extractOffsetBytes)
            procIstftOutput.get(outInterval, 0, processIntervalBytes)
        }
        val t3 = System.nanoTime()
        val verifyNs = verifyNativeIstftIntervalIfNeeded(handle, frames, outInterval)
        val tAfterVerify = System.nanoTime()

        if (!mixedInNative) {
            mixOutputIntervalFallback(pcmData, outInterval, modelOutputConvertedToInstrumental)
        }
        val t4 = System.nanoTime()

        val stftMs = (t1 - t0) / 1_000_000f
        val inferMs = (t2 - t1) / 1_000_000f
        val istftMs = (t3 - t2) / 1_000_000f
        val totalMs = ((t4 - t0) - verifyNs) / 1_000_000f
        inferCount += 1
        inferSumMs += inferMs
        if (inferMs > inferMaxMs) inferMaxMs = inferMs
        stftSumMs += stftMs
        onnxSumMs += inferMs
        istftSumMs += istftMs
        totalSumMs += totalMs
        if (stftMs > stftMaxMs) stftMaxMs = stftMs
        if (inferMs > onnxMaxMs) onnxMaxMs = inferMs
        if (istftMs > istftMaxMs) istftMaxMs = istftMs
        if (totalMs > totalMaxMs) totalMaxMs = totalMs
        lastStftMs = stftMs
        lastOnnxMs = inferMs
        lastIstftMs = istftMs
        lastTotalMs = totalMs
        lastCopyInMs = (tCopyIn - t0) / 1_000_000f
        lastPackMs = (tPack - t1) / 1_000_000f
        lastFilterMs = (tFilter - t2) / 1_000_000f
        lastNativeIstftMs = (t3 - tFilter) / 1_000_000f
        lastPostMs = (t4 - tAfterVerify) / 1_000_000f
        avgInferMs = avgInferMs * (1 - INFER_EMA_ALPHA) + inferMs * INFER_EMA_ALPHA
        avgStftMs = avgStftMs * (1 - INFER_EMA_ALPHA) + stftMs * INFER_EMA_ALPHA
        avgOnnxMs = avgOnnxMs * (1 - INFER_EMA_ALPHA) + inferMs * INFER_EMA_ALPHA
        avgIstftMs = avgIstftMs * (1 - INFER_EMA_ALPHA) + istftMs * INFER_EMA_ALPHA
        avgTotalMs = avgTotalMs * (1 - INFER_EMA_ALPHA) + totalMs * INFER_EMA_ALPHA
        maybeUpdateAdaptivePreBuffer()
        maybeLogLastInference()
        maybeLogPerfSnapshot()
        if (avgInferMs > processIntervalMs) {
            logPerfWarning("Inference too slow: avg=${avgInferMs.toInt()}ms, budget=${processIntervalMs.toInt()}ms")
        }
        if (BuildConfig.DEBUG && Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "T=$frames STFT=${(t1-t0)/1_000_000}ms ONNX=${(t2-t1)/1_000_000}ms ISTFT=${(t3-t2)/1_000_000}ms POST=${(t4-t3)/1_000_000}ms total=${(t4-t0)/1_000_000}ms avg=${avgInferMs.toInt()}ms")
        }

        return true
    }

    private fun mixOutputIntervalFallback(
        pcmData: ByteArray,
        outInterval: ByteArray,
        modelOutputConvertedToInstrumental: Boolean
    ) {
        // Fallback only: the normal path mixes in nativeComputeIstftInterval().
        val mix = mixRatio.coerceIn(0f, 1f)
        val outputVocalOnly = vocalOnlyMode
        val shortCount = processIntervalBytes / 2
        for (i in 0 until shortCount) {
            val ri = i * 2
            val origIndex = extractOffsetBytes + ri
            val orig = ((pcmData[origIndex + 1].toInt() shl 8) or
                (pcmData[origIndex].toInt() and 0xFF)).toShort()
            val modelOut = ((outInterval[ri + 1].toInt() shl 8) or
                (outInterval[ri].toInt() and 0xFF)).toShort()
            val instrumental = if (MODEL_OUTPUTS_VOCAL && !modelOutputConvertedToInstrumental) {
                (orig.toFloat() - (modelOut.toFloat() * VOCAL_SUBTRACT_GAIN)).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            } else {
                modelOut.toInt()
            }
            val vocal = if (MODEL_OUTPUTS_VOCAL && !modelOutputConvertedToInstrumental) {
                modelOut.toInt()
            } else {
                (orig.toInt() - instrumental)
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            }
            val target = if (outputVocalOnly) vocal else instrumental
            val mixed = ((orig.toInt() * (1f - mix)) + (target * mix)).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            outInterval[ri] = (mixed and 0xFF).toByte()
            outInterval[ri + 1] = (mixed shr 8).toByte()
        }
    }

    private fun verifyNativeIstftIntervalIfNeeded(
        handle: Long,
        frames: Int,
        outInterval: ByteArray
    ): Long {
        if (VERIFY_NATIVE_ISTFT_INTERVAL_CHUNKS <= 0) return 0L
        if (inferCount >= VERIFY_NATIVE_ISTFT_INTERVAL_CHUNKS) return 0L
        if (verifyIstftIntervalScratch.size < processIntervalBytes) return 0L

        val start = System.nanoTime()
        procIstftOutput.clear()
        val fullOutSamples = nativeComputeIstft(
            handle,
            procIstftRealBuffer,
            procIstftImagBuffer,
            procIstftOutput,
            frames,
            inputAudioFormat.channelCount
        )
        if (fullOutSamples <= 0) {
            val elapsed = System.nanoTime() - start
            logPerfWarning(
                "ISTFT_INTERVAL_VERIFY chunk=${inferCount + 1} status=full_istft_failed " +
                    "fullOutSamples=$fullOutSamples verifyMs=${elapsed / 1_000_000}"
            )
            return elapsed
        }

        val fullBytes = fullOutSamples * bytesPerFrame
        val extractEnd = extractOffsetBytes + processIntervalBytes
        if (extractEnd > fullBytes) {
            val elapsed = System.nanoTime() - start
            logPerfWarning(
                "ISTFT_INTERVAL_VERIFY chunk=${inferCount + 1} status=extract_oob " +
                    "extractEnd=$extractEnd fullBytes=$fullBytes verifyMs=${elapsed / 1_000_000}"
            )
            return elapsed
        }

        procIstftOutput.position(extractOffsetBytes)
        procIstftOutput.get(verifyIstftIntervalScratch, 0, processIntervalBytes)

        var maxDiff = 0
        var diffSum = 0L
        var mismatchedSamples = 0
        val shortCount = processIntervalBytes / 2
        for (i in 0 until shortCount) {
            val offset = i * 2
            val intervalSample = ((outInterval[offset + 1].toInt() shl 8) or
                (outInterval[offset].toInt() and 0xFF)).toShort().toInt()
            val fullSample = ((verifyIstftIntervalScratch[offset + 1].toInt() shl 8) or
                (verifyIstftIntervalScratch[offset].toInt() and 0xFF)).toShort().toInt()
            val diff = kotlin.math.abs(intervalSample - fullSample)
            if (diff > 0) {
                mismatchedSamples += 1
                diffSum += diff.toLong()
                if (diff > maxDiff) maxDiff = diff
            }
        }

        val elapsed = System.nanoTime() - start
        val avgDiff = if (mismatchedSamples > 0) diffSum.toDouble() / mismatchedSamples.toDouble() else 0.0
        logVocal(
            "ISTFT_INTERVAL_VERIFY chunk=${inferCount + 1} status=ok samples=$shortCount " +
                "mismatchedSamples=$mismatchedSamples maxPcmDiff=$maxDiff " +
                "avgPcmDiff=${String.format(Locale.US, "%.3f", avgDiff)} " +
                "verifyMs=${elapsed / 1_000_000}"
        )
        return elapsed
    }

    private fun applyVocalConfidenceSpectralFilter(handle: Long, frames: Int): Boolean {
        val channelCount = if (inputAudioFormat.channelCount == 1) 1 else MODEL_CHANNELS
        val nativeOk = nativeApplyVocalConfidenceFilter(
            handle,
            procStftRealBuffer,
            procStftImagBuffer,
            procIstftRealBuffer,
            procIstftImagBuffer,
            frames,
            channelCount,
            VOCAL_SUBTRACT_GAIN,
            VOCAL_CONFIDENCE_LOW,
            VOCAL_CONFIDENCE_HIGH,
            VOCAL_CONFIDENCE_MAX_REDUCTION,
            VOCAL_CONFIDENCE_MIN_FREQ_HZ,
            VOCAL_CONFIDENCE_MAX_FREQ_HZ
        )
        if (nativeOk) return true

        logPerfWarning("nativeApplyVocalConfidenceFilter failed; using Kotlin fallback")
        applyVocalConfidenceSpectralFilterFallback(frames, channelCount)
        return true
    }

    private fun applyVocalConfidenceSpectralFilterFallback(frames: Int, channelCount: Int) {
        val channelStride = modelDimF * frames
        val sampleRate = inputAudioFormat.sampleRate.toFloat()

        for (channel in 0 until channelCount) {
            val channelOffset = channel * channelStride
            for (f in 0 until modelDimF) {
                val freqHz = f * sampleRate / N_FFT.toFloat()
                val inVocalBand = freqHz >= VOCAL_CONFIDENCE_MIN_FREQ_HZ &&
                    freqHz <= VOCAL_CONFIDENCE_MAX_FREQ_HZ
                val bandOffset = channelOffset + (f * frames)

                for (t in 0 until frames) {
                    val index = bandOffset + t
                    val mixReal = procStftRealFloats.get(index)
                    val mixImag = procStftImagFloats.get(index)
                    val vocalReal = procIstftRealFloats.get(index)
                    val vocalImag = procIstftImagFloats.get(index)

                    val mixMag = kotlin.math.sqrt((mixReal * mixReal) + (mixImag * mixImag))
                    val vocalMag = kotlin.math.sqrt((vocalReal * vocalReal) + (vocalImag * vocalImag))
                    val confidence = if (mixMag > 1e-6f) (vocalMag / mixMag).coerceIn(0f, 1.5f) else 0f
                    val strength = if (inVocalBand) {
                        ((confidence - VOCAL_CONFIDENCE_LOW) /
                            (VOCAL_CONFIDENCE_HIGH - VOCAL_CONFIDENCE_LOW)).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    val attenuation = 1f - (VOCAL_CONFIDENCE_MAX_REDUCTION * strength)

                    val instrumentalReal = (mixReal - (vocalReal * VOCAL_SUBTRACT_GAIN)) * attenuation
                    val instrumentalImag = (mixImag - (vocalImag * VOCAL_SUBTRACT_GAIN)) * attenuation
                    procIstftRealFloats.put(index, instrumentalReal)
                    procIstftImagFloats.put(index, instrumentalImag)
                }
            }
        }
    }

    private fun processWaveformChunk(handle: Long, pcmData: ByteArray, outInterval: ByteArray): Boolean {
        val t0 = System.nanoTime()
        packWaveformInput(pcmData)
        val t1 = System.nanoTime()

        if (!runWaveformModel(handle)) return false
        val t2 = System.nanoTime()

        unpackWaveformOutputAndMix(pcmData, outInterval)
        val t3 = System.nanoTime()

        val stftMs = (t1 - t0) / 1_000_000f
        val inferMs = (t2 - t1) / 1_000_000f
        val istftMs = (t3 - t2) / 1_000_000f
        val totalMs = (t3 - t0) / 1_000_000f
        inferCount += 1
        inferSumMs += inferMs
        if (inferMs > inferMaxMs) inferMaxMs = inferMs
        stftSumMs += stftMs
        onnxSumMs += inferMs
        istftSumMs += istftMs
        totalSumMs += totalMs
        if (stftMs > stftMaxMs) stftMaxMs = stftMs
        if (inferMs > onnxMaxMs) onnxMaxMs = inferMs
        if (istftMs > istftMaxMs) istftMaxMs = istftMs
        if (totalMs > totalMaxMs) totalMaxMs = totalMs
        lastStftMs = stftMs
        lastOnnxMs = inferMs
        lastIstftMs = istftMs
        lastTotalMs = totalMs
        avgInferMs = avgInferMs * (1 - INFER_EMA_ALPHA) + inferMs * INFER_EMA_ALPHA
        avgStftMs = avgStftMs * (1 - INFER_EMA_ALPHA) + stftMs * INFER_EMA_ALPHA
        avgOnnxMs = avgOnnxMs * (1 - INFER_EMA_ALPHA) + inferMs * INFER_EMA_ALPHA
        avgIstftMs = avgIstftMs * (1 - INFER_EMA_ALPHA) + istftMs * INFER_EMA_ALPHA
        avgTotalMs = avgTotalMs * (1 - INFER_EMA_ALPHA) + totalMs * INFER_EMA_ALPHA
        maybeUpdateAdaptivePreBuffer()
        maybeLogLastInference()
        maybeLogPerfSnapshot()
        if (avgInferMs > processIntervalMs) {
            logPerfWarning("Inference too slow: avg=${avgInferMs.toInt()}ms, budget=${processIntervalMs.toInt()}ms")
        }

        return true
    }

    private fun packWaveformInput(pcmData: ByteArray) {
        val channels = inputAudioFormat.channelCount
        for (s in 0 until WAVEFORM_MODEL_SAMPLES) {
            val frameOffset = s * channels * 2
            val left = readPcm16(pcmData, frameOffset).toFloat() / 32768f
            val right = if (channels > 1) {
                readPcm16(pcmData, frameOffset + 2).toFloat() / 32768f
            } else {
                left
            }
            procWaveformInputFloats.put(s, left)
            procWaveformInputFloats.put(WAVEFORM_MODEL_SAMPLES + s, right)
        }
    }

    private fun unpackWaveformOutputAndMix(pcmData: ByteArray, outInterval: ByteArray) {
        val channels = inputAudioFormat.channelCount
        val mix = mixRatio.coerceIn(0f, 1f)
        for (s in 0 until WAVEFORM_MODEL_SAMPLES) {
            val modelLeft = procWaveformOutputFloats.get(s)
            val modelRight = procWaveformOutputFloats.get(WAVEFORM_MODEL_SAMPLES + s)
            val frameOffset = s * channels * 2
            for (ch in 0 until channels) {
                val byteOffset = frameOffset + (ch * 2)
                val orig = readPcm16(pcmData, byteOffset)
                val origFloat = orig.toFloat() / 32768f
                val instrumental = if (channels == 1) 0.5f * (modelLeft + modelRight) else if (ch == 0) modelLeft else modelRight
                val mixed = (origFloat * (1f - mix)) + (instrumental * mix)
                writePcm16(outInterval, byteOffset, floatToPcm16(mixed))
            }
        }
    }

    private fun readPcm16(data: ByteArray, offset: Int): Short {
        return ((data[offset + 1].toInt() shl 8) or (data[offset].toInt() and 0xFF)).toShort()
    }

    private fun writePcm16(data: ByteArray, offset: Int, value: Short) {
        val intValue = value.toInt()
        data[offset] = (intValue and 0xFF).toByte()
        data[offset + 1] = (intValue shr 8).toByte()
    }

    private fun floatToPcm16(value: Float): Short {
        val scaled = (value.coerceIn(-1f, 1f) * 32767f).toInt()
            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        return scaled.toShort()
    }

    private fun packModelInput(frames: Int) {
        if (USE_POLARFORMER_MASK_MODEL) {
            packPolarformerInput(frames)
            return
        }

        val nativeOk = nativePackMdxModelInput(
            nativeHandle,
            procStftRealBuffer,
            procStftImagBuffer,
            procModelInputBuffer,
            frames
        )
        if (nativeOk) return

        logPerfWarning("nativePackMdxModelInput failed; using Kotlin fallback")
        packMdxModelInputFallback(frames)
    }

    private fun packMdxModelInputFallback(frames: Int) {
        val channelStride = modelDimF * frames
        for (f in 0 until modelDimF) {
            val bandOffset = f * frames
            for (t in 0 until frames) {
                val tf = bandOffset + t
                val rBase = channelStride + tf
                procModelInputFloats.put(tf, procStftRealFloats.get(tf))
                procModelInputFloats.put(channelStride + tf, procStftImagFloats.get(tf))
                procModelInputFloats.put((2 * channelStride) + tf, procStftRealFloats.get(rBase))
                procModelInputFloats.put((3 * channelStride) + tf, procStftImagFloats.get(rBase))
            }
        }
    }

    private fun unpackModelOutput(modelOutput: FloatBuffer, frames: Int) {
        if (USE_POLARFORMER_MASK_MODEL) {
            unpackPolarformerMask(modelOutput, frames)
            return
        }

        val channelCount = if (inputAudioFormat.channelCount == 1) 1 else MODEL_CHANNELS
        val nativeOk = nativeUnpackMdxModelOutput(
            nativeHandle,
            procModelOutputBuffer,
            procIstftRealBuffer,
            procIstftImagBuffer,
            frames,
            channelCount
        )
        if (nativeOk) return

        logPerfWarning("nativeUnpackMdxModelOutput failed; using Kotlin fallback")
        unpackMdxModelOutputFallback(modelOutput, frames)
    }

    private fun unpackMdxModelOutputFallback(modelOutput: FloatBuffer, frames: Int) {
        val channelStride = modelDimF * frames
        if (inputAudioFormat.channelCount == 1) {
            // For mono output, average L/R model outputs into a single complex spectrum.
            for (i in 0 until channelStride) {
                val lR = modelOutput.get(i)
                val lI = modelOutput.get(channelStride + i)
                val rR = modelOutput.get((2 * channelStride) + i)
                val rI = modelOutput.get((3 * channelStride) + i)
                procIstftRealFloats.put(i, 0.5f * (lR + rR))
                procIstftImagFloats.put(i, 0.5f * (lI + rI))
            }
        } else {
            for (i in 0 until channelStride) {
                val rightIndex = channelStride + i
                procIstftRealFloats.put(i, modelOutput.get(i))
                procIstftImagFloats.put(i, modelOutput.get(channelStride + i))
                procIstftRealFloats.put(rightIndex, modelOutput.get((2 * channelStride) + i))
                procIstftImagFloats.put(rightIndex, modelOutput.get((3 * channelStride) + i))
            }
        }
    }

    private fun packPolarformerInput(frames: Int) {
        val channelStride = modelDimF * frames
        val featureCount = modelDimF * 4
        for (t in 0 until frames) {
            val frameOffset = t * featureCount
            for (f in 0 until modelDimF) {
                val leftIndex = f * frames + t
                val rightIndex = channelStride + leftIndex
                val featureOffset = frameOffset + (f * 4)
                procModelInputFloats.put(featureOffset, procStftRealFloats.get(leftIndex))
                procModelInputFloats.put(featureOffset + 1, procStftImagFloats.get(leftIndex))
                procModelInputFloats.put(featureOffset + 2, procStftRealFloats.get(rightIndex))
                procModelInputFloats.put(featureOffset + 3, procStftImagFloats.get(rightIndex))
            }
        }
    }

    private fun unpackPolarformerMask(modelOutput: FloatBuffer, frames: Int) {
        val channelStride = modelDimF * frames
        for (channel in 0 until MODEL_CHANNELS) {
            for (f in 0 until modelDimF) {
                for (t in 0 until frames) {
                    if (inputAudioFormat.channelCount == 1 && channel == 1) continue
                    val mixtureIndex = (channel * channelStride) + (f * frames) + t
                    val maskBase = (((f * MODEL_CHANNELS + channel) * frames + t) * 2)
                    val maskReal = if (f == 0) 0f else modelOutput.get(maskBase)
                    val maskImag = if (f == 0) 0f else modelOutput.get(maskBase + 1)
                    val inReal = procStftRealFloats.get(mixtureIndex)
                    val inImag = procStftImagFloats.get(mixtureIndex)
                    procIstftRealFloats.put(mixtureIndex, (inReal * maskReal) - (inImag * maskImag))
                    procIstftImagFloats.put(mixtureIndex, (inReal * maskImag) + (inImag * maskReal))
                }
            }
        }
    }

    private fun runModel(frames: Int): Boolean {
        val handle = mdxModelHandle
        if (handle == 0L) return false
        val frameTensorSize = modelDimF * frames
        val modelValueCount = 4 * frameTensorSize
        val byteCount = modelValueCount * 4
        if (procModelInputBuffer.capacity() < byteCount || procModelOutputBuffer.capacity() < byteCount) {
            Log.e(TAG, "Model buffer too small: input=${procModelInputBuffer.capacity()} output=${procModelOutputBuffer.capacity()} need=$byteCount")
            return false
        }
        return try {
            procModelInputBuffer.position(0)
            procModelInputBuffer.limit(byteCount)
            procModelOutputBuffer.position(0)
            procModelOutputBuffer.limit(byteCount)

            val tRunStart = System.nanoTime()
            val ok = if (USE_POLARFORMER_MASK_MODEL) {
                nativeRunPolarformerModel(
                    handle,
                    procModelInputBuffer,
                    procModelOutputBuffer,
                    frames,
                    modelDimF * 4
                )
            } else {
                nativeRunMdxModel(handle, procModelInputBuffer, procModelOutputBuffer, frames)
            }
            val tRunEnd = System.nanoTime()
            if (!ok) return false

            procModelOutputBuffer.position(0)
            procModelOutputFloats.position(0)
            procModelOutputFloats.limit(modelValueCount)
            unpackModelOutput(procModelOutputFloats, frames)
            val tUnpackEnd = System.nanoTime()
            lastNativeOnnxMs = (tRunEnd - tRunStart) / 1_000_000f
            lastUnpackMs = (tUnpackEnd - tRunEnd) / 1_000_000f
            true
        } catch (t: Throwable) {
            Log.e(TAG, "Native MDX inference failed", t)
            false
        }
    }

    private fun runWaveformModel(handle: Long): Boolean {
        if (handle == 0L) return false
        val modelValueCount = MODEL_CHANNELS * WAVEFORM_MODEL_SAMPLES
        val byteCount = modelValueCount * Float.SIZE_BYTES
        if (procWaveformInputBuffer.capacity() < byteCount || procWaveformOutputBuffer.capacity() < byteCount) {
            Log.e(
                TAG,
                "Waveform model buffer too small: input=${procWaveformInputBuffer.capacity()} " +
                    "output=${procWaveformOutputBuffer.capacity()} need=$byteCount"
            )
            return false
        }
        return try {
            procWaveformInputBuffer.position(0)
            procWaveformInputBuffer.limit(byteCount)
            procWaveformOutputBuffer.position(0)
            procWaveformOutputBuffer.limit(byteCount)

            val ok = nativeRunWaveformModel(
                handle,
                procWaveformInputBuffer,
                procWaveformOutputBuffer,
                WAVEFORM_MODEL_SAMPLES
            )
            if (!ok) return false

            procWaveformOutputBuffer.position(0)
            procWaveformOutputFloats.position(0)
            procWaveformOutputFloats.limit(modelValueCount)
            true
        } catch (t: Throwable) {
            Log.e(TAG, "Native waveform inference failed", t)
            false
        }
    }

    private external fun nativeInitStft(sampleRate: Int, nFft: Int, hopLength: Int, dimF: Int): Long

    private external fun nativeComputeStft(
        handle: Long, pcmInput: ByteBuffer, numSamples: Int,
        outputReal: ByteBuffer, outputImag: ByteBuffer, channelCount: Int
    ): Int

    private external fun nativeComputeIstft(
        handle: Long, inputReal: ByteBuffer, inputImag: ByteBuffer,
        pcmOutput: ByteBuffer, numFrames: Int, channelCount: Int
    ): Int

    private external fun nativeComputeIstftInterval(
        handle: Long,
        inputReal: ByteBuffer,
        inputImag: ByteBuffer,
        dryPcmInput: ByteArray,
        pcmOutput: ByteArray,
        numFrames: Int,
        channelCount: Int,
        extractOffsetSamples: Int,
        intervalSamples: Int,
        dryOffsetBytes: Int,
        mixRatio: Float,
        vocalSubtractGain: Float,
        modelOutputsVocal: Boolean,
        modelOutputConvertedToInstrumental: Boolean,
        vocalOnlyMode: Boolean
    ): Int

    private external fun nativePackMdxModelInput(
        handle: Long,
        stftReal: ByteBuffer,
        stftImag: ByteBuffer,
        modelInput: ByteBuffer,
        numFrames: Int
    ): Boolean

    private external fun nativeUnpackMdxModelOutput(
        handle: Long,
        modelOutput: ByteBuffer,
        istftReal: ByteBuffer,
        istftImag: ByteBuffer,
        numFrames: Int,
        channelCount: Int
    ): Boolean

    private external fun nativeApplyVocalConfidenceFilter(
        handle: Long,
        mixReal: ByteBuffer,
        mixImag: ByteBuffer,
        vocalReal: ByteBuffer,
        vocalImag: ByteBuffer,
        numFrames: Int,
        channelCount: Int,
        vocalSubtractGain: Float,
        confidenceLow: Float,
        confidenceHigh: Float,
        maxReduction: Float,
        minFreqHz: Float,
        maxFreqHz: Float
    ): Boolean

    private external fun nativeReleaseStft(handle: Long)

    private external fun nativeInitMdxModel(modelPath: String, threads: Int, dimF: Int, channels: Int): Long

    private external fun nativeRunMdxModel(
        handle: Long, inputBuffer: ByteBuffer, outputBuffer: ByteBuffer, frames: Int
    ): Boolean

    private external fun nativeRunPolarformerModel(
        handle: Long, inputBuffer: ByteBuffer, outputBuffer: ByteBuffer, frames: Int, features: Int
    ): Boolean

    private external fun nativeRunWaveformModel(
        handle: Long, inputBuffer: ByteBuffer, outputBuffer: ByteBuffer, samples: Int
    ): Boolean

    private external fun nativeReleaseMdxModel(handle: Long)

    private class ByteRingBuffer(initialCapacity: Int, private val maxCapacity: Int = initialCapacity) {
        private var buffer = ByteArray(initialCapacity.coerceAtLeast(1))
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
            if (length <= 0) return 0
            ensureWritable(length)
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

        private fun ensureWritable(requiredBytes: Int) {
            if (requiredBytes <= space()) return
            val currentCapacity = buffer.size
            val hardLimit = maxOf(currentCapacity, maxCapacity)
            var nextCapacity = currentCapacity
            val requiredCapacity = size + requiredBytes
            while (nextCapacity < requiredCapacity && nextCapacity < hardLimit) {
                val doubled = nextCapacity * 2
                nextCapacity = if (doubled <= 0) hardLimit else minOf(doubled, hardLimit)
            }
            if (nextCapacity > currentCapacity) {
                growTo(nextCapacity)
            }
        }

        private fun growTo(newCapacity: Int) {
            if (newCapacity <= buffer.size) return
            val newBuffer = ByteArray(newCapacity)
            if (size > 0) {
                if (readPos < writePos) {
                    System.arraycopy(buffer, readPos, newBuffer, 0, size)
                } else {
                    val first = buffer.size - readPos
                    System.arraycopy(buffer, readPos, newBuffer, 0, first)
                    if (writePos > 0) {
                        System.arraycopy(buffer, 0, newBuffer, first, writePos)
                    }
                }
            }
            buffer = newBuffer
            readPos = 0
            writePos = size
        }
    }
}
