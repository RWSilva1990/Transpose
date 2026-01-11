package com.example.media.audio

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.util.UnstableApi
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

    @Volatile
    private var pitchSemitones: Float = 0f

    @Volatile
    private var tempoRate: Float = 1.0f

    private var chorusEnabled: Boolean = false
    private var chorusMix: Float = 0.5f
    private var chorusDepthMs: Float = 10.0f
    private var chorusDetune: Float = 10.0f
    private var chorusStereo: Float = 0.5f

    private var limiterEnabled: Boolean = false
    private var limiterInputGainDb: Float = 0.0f
    private var limiterLimitDb: Float = -3.0f
    private var limiterAttackMs: Float = 10.0f
    private var limiterReleaseMs: Float = 100.0f

    private var reverbEnabled: Boolean = false
    private var reverbDry: Float = 1.0f
    private var reverbWet: Float = 0.3f
    private var reverbRoomMs: Float = 50.0f
    private var reverbDecaySec: Float = 2.0f

    private var crunchEnabled: Boolean = false
    private var crunchDriveDb: Float = 0.0f
    private var crunchFuzz: Float = 0.0f
    private var crunchToneHz: Float = 5000.0f

    private var eqEnabled: Boolean = false
    private var eqBand1Freq: Float = 60.0f
    private var eqBand1Gain: Float = 0.0f
    private var eqBand2Freq: Float = 250.0f
    private var eqBand2Gain: Float = 0.0f
    private var eqBand3Freq: Float = 1000.0f
    private var eqBand3Gain: Float = 0.0f
    private var eqBand4Freq: Float = 4000.0f
    private var eqBand4Gain: Float = 0.0f
    private var eqBand5Freq: Float = 12000.0f
    private var eqBand5Gain: Float = 0.0f

    private var compressorEnabled: Boolean = false
    private var compThresholdDb: Float = -20.0f
    private var compRatio: Float = 4.0f
    private var compAttackMs: Float = 10.0f
    private var compReleaseMs: Float = 100.0f
    private var compMakeupGainDb: Float = 0.0f

    private var pitchDetectionEnabled: Boolean = false

    private var hrtfEnabled: Boolean = false
    private var hrtfIntensity: Float = 1.0f
    private var hrtfAzimuth: Int = 30

    private var phaserEnabled: Boolean = false
    private var phaserLfoFreq: Float = 0.5f
    private var phaserLfoDepth: Float = 0.5f
    private var phaserFeedback: Float = 0.7f
    private var phaserPoles: Int = 4

    private var flangerEnabled: Boolean = false
    private var flangerLfoFreq: Float = 0.2f
    private var flangerLfoDepth: Float = 0.5f
    private var flangerFeedback: Float = 0.5f
    private var flangerDelayMs: Float = 3.0f

    private var tremoloEnabled: Boolean = false
    private var tremoloFreq: Float = 5.0f
    private var tremoloDepth: Float = 0.5f
    private var tremoloWaveform: Int = 0

    private var autowahEnabled: Boolean = false
    private var autowahWah: Float = 0.5f
    private var autowahMix: Float = 50.0f
    private var autowahLevel: Float = 0.5f

    private var decimatorEnabled: Boolean = false
    private var decimatorBitcrush: Float = 0.5f
    private var decimatorDownsample: Float = 0.5f

    fun setPitchSemitones(semitones: Float) {
        pitchSemitones = semitones.coerceIn(-24f, 24f)
        if (nativeHandle != 0L) {
            nativeSetPitchSemitones(nativeHandle, pitchSemitones)
        }
        Log.d(TAG, "setPitchSemitones: $pitchSemitones")
    }

    fun setTempoRate(rate: Float) {
        tempoRate = rate.coerceIn(0.5f, 2.0f)
        if (nativeHandle != 0L) {
            nativeSetTempoRate(nativeHandle, tempoRate)
        }
        Log.d(TAG, "setTempoRate: $tempoRate")
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

    fun setLimiterEnabled(enabled: Boolean) {
        limiterEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetLimiterEnabled(nativeHandle, enabled)
        }
    }

    fun setLimiterParams(inputGainDb: Float, limitDb: Float, attackMs: Float, releaseMs: Float) {
        limiterInputGainDb = inputGainDb
        limiterLimitDb = limitDb
        limiterAttackMs = attackMs
        limiterReleaseMs = releaseMs
        if (nativeHandle != 0L) {
            nativeSetLimiterParams(nativeHandle, inputGainDb, limitDb, attackMs, releaseMs)
        }
    }

    fun setReverbEnabled(enabled: Boolean) {
        reverbEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetReverbEnabled(nativeHandle, enabled)
        }
    }

    fun setReverbParams(dry: Float, wet: Float, roomMs: Float, decaySec: Float) {
        reverbDry = dry
        reverbWet = wet
        reverbRoomMs = roomMs
        reverbDecaySec = decaySec
        if (nativeHandle != 0L) {
            nativeSetReverbParams(nativeHandle, dry, wet, roomMs, decaySec)
        }
    }

    fun setCrunchEnabled(enabled: Boolean) {
        crunchEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetCrunchEnabled(nativeHandle, enabled)
        }
    }

    fun setCrunchParams(driveDb: Float, fuzz: Float, toneHz: Float) {
        crunchDriveDb = driveDb
        crunchFuzz = fuzz
        crunchToneHz = toneHz
        if (nativeHandle != 0L) {
            nativeSetCrunchParams(nativeHandle, driveDb, fuzz, toneHz)
        }
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

    fun setCompressorEnabled(enabled: Boolean) {
        compressorEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetCompressorEnabled(nativeHandle, enabled)
        }
    }

    fun setCompressorParams(
        thresholdDb: Float,
        ratio: Float,
        attackMs: Float,
        releaseMs: Float,
        makeupGainDb: Float
    ) {
        compThresholdDb = thresholdDb
        compRatio = ratio
        compAttackMs = attackMs
        compReleaseMs = releaseMs
        compMakeupGainDb = makeupGainDb

        if (nativeHandle != 0L) {
            nativeSetCompressorParams(nativeHandle, thresholdDb, ratio, attackMs, releaseMs, makeupGainDb)
        }
    }

    fun setPitchDetectionEnabled(enabled: Boolean) {
        pitchDetectionEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetPitchDetectionEnabled(nativeHandle, enabled)
        }
    }

    fun getDetectedPitch(): Float {
        if (nativeHandle == 0L) return 0f
        return nativeGetDetectedPitch(nativeHandle)
    }

    fun setHrtfEnabled(enabled: Boolean) {
        hrtfEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetHrtfEnabled(nativeHandle, enabled)
        }
    }

    fun setHrtfParams(intensity: Float, azimuth: Int) {
        hrtfIntensity = intensity
        hrtfAzimuth = azimuth
        if (nativeHandle != 0L) {
            nativeSetHrtfParams(nativeHandle, intensity, azimuth)
        }
    }

    fun setPhaserEnabled(enabled: Boolean) {
        phaserEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetPhaserEnabled(nativeHandle, enabled)
        }
    }

    fun setPhaserParams(lfoFreq: Float, lfoDepth: Float, feedback: Float, poles: Int) {
        phaserLfoFreq = lfoFreq
        phaserLfoDepth = lfoDepth
        phaserFeedback = feedback
        phaserPoles = poles
        if (nativeHandle != 0L) {
            nativeSetPhaserParams(nativeHandle, lfoFreq, lfoDepth, feedback, poles)
        }
    }

    fun setFlangerEnabled(enabled: Boolean) {
        flangerEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetFlangerEnabled(nativeHandle, enabled)
        }
    }

    fun setFlangerParams(lfoFreq: Float, lfoDepth: Float, feedback: Float, delayMs: Float) {
        flangerLfoFreq = lfoFreq
        flangerLfoDepth = lfoDepth
        flangerFeedback = feedback
        flangerDelayMs = delayMs
        if (nativeHandle != 0L) {
            nativeSetFlangerParams(nativeHandle, lfoFreq, lfoDepth, feedback, delayMs)
        }
    }

    fun setTremoloEnabled(enabled: Boolean) {
        tremoloEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetTremoloEnabled(nativeHandle, enabled)
        }
    }

    fun setTremoloParams(freq: Float, depth: Float, waveform: Int) {
        tremoloFreq = freq
        tremoloDepth = depth
        tremoloWaveform = waveform
        if (nativeHandle != 0L) {
            nativeSetTremoloParams(nativeHandle, freq, depth, waveform)
        }
    }

    fun setAutowahEnabled(enabled: Boolean) {
        autowahEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetAutowahEnabled(nativeHandle, enabled)
        }
    }

    fun setAutowahParams(wah: Float, mix: Float, level: Float) {
        autowahWah = wah
        autowahMix = mix
        autowahLevel = level
        if (nativeHandle != 0L) {
            nativeSetAutowahParams(nativeHandle, wah, mix, level)
        }
    }

    fun setDecimatorEnabled(enabled: Boolean) {
        decimatorEnabled = enabled
        if (nativeHandle != 0L) {
            nativeSetDecimatorEnabled(nativeHandle, enabled)
        }
    }

    fun setDecimatorParams(bitcrush: Float, downsample: Float) {
        decimatorBitcrush = bitcrush
        decimatorDownsample = downsample
        if (nativeHandle != 0L) {
            nativeSetDecimatorParams(nativeHandle, bitcrush, downsample)
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
            nativeSetPitchSemitones(nativeHandle, pitchSemitones)
            nativeSetTempoRate(nativeHandle, tempoRate)

            nativeSetChorusEnabled(nativeHandle, chorusEnabled)
            nativeSetChorusParams(nativeHandle, chorusMix, chorusDepthMs, chorusDetune, chorusStereo)

            nativeSetLimiterEnabled(nativeHandle, limiterEnabled)
            nativeSetLimiterParams(nativeHandle, limiterInputGainDb, limiterLimitDb, limiterAttackMs, limiterReleaseMs)

            nativeSetReverbEnabled(nativeHandle, reverbEnabled)
            nativeSetReverbParams(nativeHandle, reverbDry, reverbWet, reverbRoomMs, reverbDecaySec)

            nativeSetCrunchEnabled(nativeHandle, crunchEnabled)
            nativeSetCrunchParams(nativeHandle, crunchDriveDb, crunchFuzz, crunchToneHz)

            nativeSetEqEnabled(nativeHandle, eqEnabled)
            nativeSetEqBand(nativeHandle, 0, eqBand1Freq, eqBand1Gain)
            nativeSetEqBand(nativeHandle, 1, eqBand2Freq, eqBand2Gain)
            nativeSetEqBand(nativeHandle, 2, eqBand3Freq, eqBand3Gain)
            nativeSetEqBand(nativeHandle, 3, eqBand4Freq, eqBand4Gain)
            nativeSetEqBand(nativeHandle, 4, eqBand5Freq, eqBand5Gain)

            nativeSetCompressorEnabled(nativeHandle, compressorEnabled)
            nativeSetCompressorParams(nativeHandle, compThresholdDb, compRatio, compAttackMs, compReleaseMs, compMakeupGainDb)

            nativeSetPitchDetectionEnabled(nativeHandle, pitchDetectionEnabled)

            nativeSetHrtfEnabled(nativeHandle, hrtfEnabled)
            nativeSetHrtfParams(nativeHandle, hrtfIntensity, hrtfAzimuth)

            nativeSetPhaserEnabled(nativeHandle, phaserEnabled)
            nativeSetPhaserParams(nativeHandle, phaserLfoFreq, phaserLfoDepth, phaserFeedback, phaserPoles)

            nativeSetFlangerEnabled(nativeHandle, flangerEnabled)
            nativeSetFlangerParams(nativeHandle, flangerLfoFreq, flangerLfoDepth, flangerFeedback, flangerDelayMs)

            nativeSetTremoloEnabled(nativeHandle, tremoloEnabled)
            nativeSetTremoloParams(nativeHandle, tremoloFreq, tremoloDepth, tremoloWaveform)

            nativeSetAutowahEnabled(nativeHandle, autowahEnabled)
            nativeSetAutowahParams(nativeHandle, autowahWah, autowahMix, autowahLevel)

            nativeSetDecimatorEnabled(nativeHandle, decimatorEnabled)
            nativeSetDecimatorParams(nativeHandle, decimatorBitcrush, decimatorDownsample)
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

    private external fun nativeSetLimiterEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetLimiterParams(
        handle: Long,
        inputGainDb: Float,
        limitDb: Float,
        attackMs: Float,
        releaseMs: Float
    )

    private external fun nativeSetReverbEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetReverbParams(
        handle: Long,
        dry: Float,
        wet: Float,
        roomMs: Float,
        decaySec: Float
    )

    private external fun nativeSetCrunchEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetCrunchParams(
        handle: Long,
        driveDb: Float,
        fuzz: Float,
        toneHz: Float
    )

    private external fun nativeSetEqEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetEqBand(
        handle: Long,
        band: Int,
        freq: Float,
        gainDb: Float
    )

    private external fun nativeSetCompressorEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetCompressorParams(
        handle: Long,
        thresholdDb: Float,
        ratio: Float,
        attackMs: Float,
        releaseMs: Float,
        makeupGainDb: Float
    )

    private external fun nativeSetPitchDetectionEnabled(handle: Long, enabled: Boolean)

    private external fun nativeGetDetectedPitch(handle: Long): Float

    private external fun nativeSetHrtfEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetHrtfParams(
        handle: Long,
        intensity: Float,
        azimuth: Int
    )

    private external fun nativeSetPhaserEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetPhaserParams(
        handle: Long,
        lfoFreq: Float,
        lfoDepth: Float,
        feedback: Float,
        poles: Int
    )

    private external fun nativeSetFlangerEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetFlangerParams(
        handle: Long,
        lfoFreq: Float,
        lfoDepth: Float,
        feedback: Float,
        delayMs: Float
    )

    private external fun nativeSetTremoloEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetTremoloParams(
        handle: Long,
        freq: Float,
        depth: Float,
        waveform: Int
    )

    private external fun nativeSetAutowahEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetAutowahParams(
        handle: Long,
        wah: Float,
        mix: Float,
        level: Float
    )

    private external fun nativeSetDecimatorEnabled(handle: Long, enabled: Boolean)

    private external fun nativeSetDecimatorParams(
        handle: Long,
        bitcrush: Float,
        downsample: Float
    )

    private external fun nativeFlush(handle: Long)
    
    private external fun nativeFlushAndGetRemaining(
        handle: Long,
        outputBuffer: ByteBuffer,
        maxOutputFrames: Int
    ): Int
    
    private external fun nativeRelease(handle: Long)
}
