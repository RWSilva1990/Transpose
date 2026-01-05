package com.example.media.audio

import android.util.Log
import com.example.audio.SuperpoweredAudioEngine
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SuperpoweredBridge 실제 구현
 * SuperpoweredAudioEngine을 래핑하여 ExoPlayer AudioSink와 연동
 */
@Singleton
class SuperpoweredBridgeImpl @Inject constructor(
    private val engine: SuperpoweredAudioEngine
) : SuperpoweredBridge {

    companion object {
        private const val TAG = "SuperpoweredBridge"
        private const val LOG_INTERVAL = 100
    }
    
    private var pushCallCount = 0
    private var streamOffsetUs: Long = -1
    private var isFirstChunkAfterFlush = true
    
    private var firstPtsUs: Long = 0
    private var firstPtsSystemTimeNs: Long = 0

    override fun init(sampleRate: Int, channelCount: Int, bufferSize: Int) {
        Log.d(TAG, "init: sampleRate=$sampleRate, channels=$channelCount, bufferSize=$bufferSize")
        streamOffsetUs = -1
        isFirstChunkAfterFlush = true
        pushCallCount = 0
        firstPtsSystemTimeNs = 0L
        positionLogCount = 0
        engine.initialize(sampleRate, channelCount, bufferSize)
    }
    override fun hasPendingData(): Boolean = engine.hasPendingData()


    override fun pushPcm(buffer: ByteBuffer, sizeInBytes: Int, presentationTimeUs: Long): Boolean {
        val bytesPerSample = 2
        val neededSamples = sizeInBytes / bytesPerSample

        val spaceSamples = engine.getBufferAvailableSpace()
        val usedSamples = engine.getBufferUsedSamples()
        
        if (streamOffsetUs < 0) {
            streamOffsetUs = presentationTimeUs
            Log.d(TAG, "Stream offset detected: ${streamOffsetUs/1000000.0}sec")
        }
        
        if (isFirstChunkAfterFlush) {
            isFirstChunkAfterFlush = false
            val seekPositionUs = presentationTimeUs - streamOffsetUs
            Log.d(TAG, "Seek position: ${seekPositionUs/1000}ms (pts=${presentationTimeUs}, offset=${streamOffsetUs})")
            engine.setSeekPosition(seekPositionUs)
            
            firstPtsUs = presentationTimeUs
            firstPtsSystemTimeNs = System.nanoTime()
        }
        
        pushCallCount++
        if (pushCallCount % LOG_INTERVAL == 0) {
            val normalizedPts = presentationTimeUs - streamOffsetUs
            Log.d(TAG, "pushPcm[$pushCallCount]: space=$spaceSamples, used=$usedSamples, pts=${normalizedPts/1000}ms")
        }
        
        if (spaceSamples < neededSamples) {
            Log.w(TAG, "Buffer FULL: space=$spaceSamples, used=$usedSamples")
            return false
        }

        if (!buffer.isDirect) {
            val directBuffer = ByteBuffer.allocateDirect(sizeInBytes)
            directBuffer.order(buffer.order())
            val oldPosition = buffer.position()
            directBuffer.put(buffer)
            buffer.position(oldPosition)
            directBuffer.flip()
            engine.writePcm(directBuffer, sizeInBytes, presentationTimeUs)
        } else {
            engine.writePcm(buffer, sizeInBytes, presentationTimeUs)
        }
        return true
    }

    override fun play() {
        Log.d(TAG, "play")
        engine.play()
    }

    override fun pause() {
        Log.d(TAG, "pause")
        engine.pause()
    }

    override fun flush() {
        Log.d(TAG, "flush - resetting stream offset for new seek position")
        streamOffsetUs = -1
        isFirstChunkAfterFlush = true
        pushCallCount = 0
        firstPtsSystemTimeNs = 0L
        firstPtsUs = 0L
        positionLogCount = 0
        engine.flush()
    }

    override fun reset() {
        Log.d(TAG, "reset")
        engine.reset()
    }

    override fun release() {
        Log.d(TAG, "release")
        engine.release()
    }

    override fun setPitch(pitch: Float) {
        engine.setPitch(pitch)
    }

    override fun setTempo(tempo: Float) {
        engine.setTempo(tempo)
    }

    private var positionLogCount = 0
    
    override fun getCurrentPositionUs(): Long {
        if (firstPtsSystemTimeNs == 0L) {
            return 0L
        }
        
        val elapsedNs = System.nanoTime() - firstPtsSystemTimeNs
        val elapsedUs = elapsedNs / 1000
        val expectedPositionUs = firstPtsUs + elapsedUs
        
        positionLogCount++
        if (positionLogCount % 500 == 0) {
            val nativePos = engine.getCurrentPositionUs()
            Log.d(TAG, "getCurrentPositionUs: expected=${expectedPositionUs/1000}ms, native=${nativePos/1000}ms")
        }
        
        return expectedPositionUs
    }

    override fun isPlaying(): Boolean {
        return engine.isPlaying()
    }

    override fun getBufferAvailableSpace(): Int {
        return engine.getBufferAvailableSpace()
    }

    override fun setSeekPosition(positionUs: Long) {
        Log.d(TAG, "setSeekPosition: ${positionUs / 1000}ms")
        engine.setSeekPosition(positionUs)
    }
}
