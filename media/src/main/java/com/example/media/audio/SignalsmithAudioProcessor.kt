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
    private var isProcessorActive = false
    private var inputEnded = false
    
    private var inputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var processingBuffer: ByteBuffer? = null
    private var pendingOutputBuffer: ByteBuffer? = null
    
    private var nativeHandle: Long = 0
    private var pitchSemitones: Float = 0f
    private var tempoRate: Float = 1.0f

    fun setPitchSemitones(semitones: Float) {
        pitchSemitones = semitones.coerceIn(-24f, 24f)
        if (nativeHandle != 0L) {
            nativeSetPitchSemitones(nativeHandle, pitchSemitones)
        }
        updateActiveState()
        Log.d(TAG, "setPitchSemitones: $pitchSemitones")
    }

    fun setTempoRate(rate: Float) {
        tempoRate = rate.coerceIn(0.5f, 2.0f)
        if (nativeHandle != 0L) {
            nativeSetTempoRate(nativeHandle, tempoRate)
        }
        updateActiveState()
        Log.d(TAG, "setTempoRate: $tempoRate")
    }

    private fun updateActiveState() {
        isProcessorActive = pitchSemitones != 0f || tempoRate != 1.0f
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
        }

        Log.d(TAG, "configure: nativeHandle=$nativeHandle")
        return outputAudioFormat
    }

    override fun isActive(): Boolean {
        return isProcessorActive && outputAudioFormat != AudioFormat.NOT_SET
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!isActive() || nativeHandle == 0L) {
            this.outputBuffer = inputBuffer
            this.inputBuffer = AudioProcessor.EMPTY_BUFFER
            return
        }

        val inputBytes = inputBuffer.remaining()
        if (inputBytes == 0) {
            this.inputBuffer = AudioProcessor.EMPTY_BUFFER
            return
        }

        val bytesPerFrame = inputAudioFormat.channelCount * 2
        val inputFrames = inputBytes / bytesPerFrame
        val outputFrames = (inputFrames / tempoRate).toInt() + 1
        val outputBytes = outputFrames * bytesPerFrame

        if (processingBuffer == null || processingBuffer!!.capacity() < outputBytes) {
            processingBuffer = ByteBuffer.allocateDirect(outputBytes * 2)
                .order(ByteOrder.nativeOrder())
        }
        processingBuffer!!.clear()
        processingBuffer!!.limit(outputBytes)

        val actualOutputFrames = nativeProcess(
            nativeHandle,
            inputBuffer,
            inputBytes,
            processingBuffer!!,
            outputFrames
        )

        if (actualOutputFrames > 0) {
            val actualOutputBytes = actualOutputFrames * bytesPerFrame
            processingBuffer!!.position(0)
            processingBuffer!!.limit(actualOutputBytes)
            this.outputBuffer = processingBuffer!!
        } else {
            this.outputBuffer = AudioProcessor.EMPTY_BUFFER
        }

        inputBuffer.position(inputBuffer.limit())
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
    
    private external fun nativeFlush(handle: Long)
    
    private external fun nativeFlushAndGetRemaining(
        handle: Long,
        outputBuffer: ByteBuffer,
        maxOutputFrames: Int
    ): Int
    
    private external fun nativeRelease(handle: Long)
}
