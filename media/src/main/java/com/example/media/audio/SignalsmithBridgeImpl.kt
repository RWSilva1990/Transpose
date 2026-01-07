package com.example.media.audio

import android.util.Log
import com.example.audio.SignalsmithAudioEngine
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalsmithBridgeImpl @Inject constructor(
    private val engine: SignalsmithAudioEngine
) : SuperpoweredBridge {

    companion object {
        private const val TAG = "SignalsmithBridge"
        private const val LOG_INTERVAL = 100
        private const val POSITION_NOT_SET = Long.MIN_VALUE
    }

    private var pushCallCount = 0
    private var streamOffsetUs: Long = -1
    private var isFirstChunkAfterFlush = true

    private var firstPtsUs: Long = 0
    private var firstPtsSystemTimeNs: Long = 0
    private var positionLogCount = 0
    
    private var lastKnownPositionUs: Long = 0L
    @Volatile
    private var isSeeking: Boolean = false

    override fun init(sampleRate: Int, channelCount: Int, bufferSize: Int) {
        Log.d(TAG, "init: sampleRate=$sampleRate, channels=$channelCount, bufferSize=$bufferSize")
        streamOffsetUs = -1
        isFirstChunkAfterFlush = true
        pushCallCount = 0
        firstPtsSystemTimeNs = 0L
        positionLogCount = 0
        lastKnownPositionUs = 0L
        isSeeking = false
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
            Log.d(TAG, "Stream offset detected: ${streamOffsetUs / 1000000.0}sec")
        }

        if (isFirstChunkAfterFlush) {
            isFirstChunkAfterFlush = false
            val seekPositionUs = presentationTimeUs - streamOffsetUs
            Log.d(TAG, "Seek position: ${seekPositionUs / 1000}ms (pts=$presentationTimeUs, offset=$streamOffsetUs)")
            engine.setSeekPosition(seekPositionUs)

            firstPtsUs = presentationTimeUs
            firstPtsSystemTimeNs = System.nanoTime()
            lastKnownPositionUs = seekPositionUs
            isSeeking = false
        }

        pushCallCount++
        if (pushCallCount % LOG_INTERVAL == 0) {
            val normalizedPts = presentationTimeUs - streamOffsetUs
            Log.d(TAG, "pushPcm[$pushCallCount]: space=$spaceSamples, used=$usedSamples, pts=${normalizedPts / 1000}ms")
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
        
        if (firstPtsSystemTimeNs != 0L) {
            lastKnownPositionUs = getCurrentPositionUs()
        }
        isSeeking = true
        
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

    override fun getCurrentPositionUs(): Long {
        if (isSeeking) {
            return POSITION_NOT_SET
        }
        
        if (firstPtsSystemTimeNs == 0L) {
            return maxOf(0L, lastKnownPositionUs)
        }

        val elapsedNs = System.nanoTime() - firstPtsSystemTimeNs
        val elapsedUs = elapsedNs / 1000
        val expectedPositionUs = firstPtsUs + elapsedUs
        
        lastKnownPositionUs = expectedPositionUs

        positionLogCount++
        if (positionLogCount % 500 == 0) {
            val nativePos = engine.getCurrentPositionUs()
            Log.d(TAG, "getCurrentPositionUs: expected=${expectedPositionUs / 1000}ms, native=${nativePos / 1000}ms")
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
