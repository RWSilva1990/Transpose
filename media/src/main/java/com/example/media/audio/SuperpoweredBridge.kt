package com.example.media.audio

import java.nio.ByteBuffer

/**
 * Superpowered 오디오 엔진과의 브릿지 인터페이스
 * ExoPlayer에서 디코딩된 PCM을 받아 Superpowered로 전달
 */
interface SuperpoweredBridge {
    fun init(sampleRate: Int, channelCount: Int, bufferSize: Int)
    fun pushPcm(buffer: ByteBuffer, sizeInBytes: Int, presentationTimeUs: Long): Boolean
    fun play()
    fun pause()
    fun flush()
    fun reset()
    fun release()
    fun hasPendingData(): Boolean

    fun setPitch(pitch: Float)
    fun setTempo(tempo: Float)
    fun getCurrentPositionUs(): Long
    fun isPlaying(): Boolean
    fun getBufferAvailableSpace(): Int
    fun setSeekPosition(positionUs: Long)
}
