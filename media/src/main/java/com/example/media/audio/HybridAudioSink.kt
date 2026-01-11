package com.example.media.audio

import android.content.Context
import android.media.AudioDeviceInfo
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.AuxEffectInfo
import androidx.media3.common.Format
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import java.nio.ByteBuffer

@OptIn(UnstableApi::class)
class HybridAudioSink(
    context: Context,
    private val superpoweredBridge: SuperpoweredBridge
) : AudioSink {

    private val defaultAudioSink: DefaultAudioSink = DefaultAudioSink.Builder(context).build()

    private var isConfigured = false

    var mode: PlaybackMode = PlaybackMode.VIDEO
        set(value) {
            field = value
            if (isConfigured && (value == PlaybackMode.AUDIO || value == PlaybackMode.VIDEO_WITH_DSP)) {
                superpoweredBridge.init(currentSampleRate, currentChannelCount, DEFAULT_BUFFER_SIZE)
            }
        }
    
    private fun usesDsp(): Boolean = mode == PlaybackMode.AUDIO || mode == PlaybackMode.VIDEO_WITH_DSP

    private var currentSampleRate = 44100
    private var currentChannelCount = 2
    private var currentFormat: Format? = null

    companion object {
        private const val TAG = "HybridAudioSink"
        private const val DEFAULT_BUFFER_SIZE = 4096
        private const val MAX_BUFFER_SAMPLES = 44100 * 2 * 2
        private const val HANDLE_BUFFER_LOG_INTERVAL = 200
    }
    
    private var handleBufferCallCount = 0

    // ==================== Listener ====================

    override fun setListener(listener: AudioSink.Listener) {
        defaultAudioSink.setListener(listener)
    }

    // ==================== Format 지원 ====================

    override fun supportsFormat(format: Format): Boolean {
        return defaultAudioSink.supportsFormat(format)
    }

    override fun getFormatSupport(format: Format): Int {
        return defaultAudioSink.getFormatSupport(format)
    }

    // ==================== 초기화/설정 ====================

    override fun configure(
        inputFormat: Format,
        specifiedBufferSize: Int,
        outputChannels: IntArray?
    ) {
        // ★ 로그 추가
        android.util.Log.d(TAG, "configure: sampleRate=${inputFormat.sampleRate}, channels=${inputFormat.channelCount}, mode=$mode")

        currentFormat = inputFormat
        currentSampleRate = inputFormat.sampleRate.takeIf { it != Format.NO_VALUE } ?: 44100
        currentChannelCount = inputFormat.channelCount.takeIf { it != Format.NO_VALUE } ?: 2
        isConfigured = true

        defaultAudioSink.configure(inputFormat, specifiedBufferSize, outputChannels)

        if (usesDsp()) {
            superpoweredBridge.init(currentSampleRate, currentChannelCount, specifiedBufferSize)
        }
    }

    // ==================== 재생 제어 ====================

    override fun play() {
        if (usesDsp()) {
            superpoweredBridge.play()
        }
        if (mode != PlaybackMode.AUDIO) {
            defaultAudioSink.play()
        }
    }

    override fun pause() {
        android.util.Log.d(TAG, "pause() called, mode=$mode")
        when (mode) {
            PlaybackMode.VIDEO -> defaultAudioSink.pause()
            PlaybackMode.AUDIO -> superpoweredBridge.pause()
            PlaybackMode.VIDEO_WITH_DSP -> superpoweredBridge.pause()
        }
    }

    override fun flush() {
        if (usesDsp()) {
            superpoweredBridge.flush()
        }
        if (mode != PlaybackMode.AUDIO) {
            defaultAudioSink.flush()
        }
    }

    override fun reset() {
        if (usesDsp()) {
            superpoweredBridge.reset()
        }
        if (mode != PlaybackMode.AUDIO) {
            defaultAudioSink.reset()
        }
    }

    override fun release() {
        superpoweredBridge.release()
        defaultAudioSink.release()
    }

    // ==================== 핵심: 버퍼 처리 ====================

    override fun handleBuffer(buffer: ByteBuffer, presentationTimeUs: Long, encodedAccessUnitCount: Int): Boolean {
        return when (mode) {
            PlaybackMode.VIDEO -> defaultAudioSink.handleBuffer(buffer, presentationTimeUs, encodedAccessUnitCount)

            PlaybackMode.AUDIO, PlaybackMode.VIDEO_WITH_DSP -> {
                val size = buffer.remaining()
                handleBufferCallCount++
                
                if (handleBufferCallCount % HANDLE_BUFFER_LOG_INTERVAL == 0) {
                    android.util.Log.d(TAG, "handleBuffer[$handleBufferCallCount]: size=$size, pts=${presentationTimeUs/1000}ms, mode=$mode")
                }
                
                var result = false
                while (!result) {
                    val accepted = superpoweredBridge.pushPcm(buffer, size, presentationTimeUs)
                    
                    if (accepted) {
                        buffer.position(buffer.limit())
                        result = true
                    } else {
                        try {
                            Thread.sleep(2)
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                            break
                        }
                    }
                }
                result
            }
        }
    }


    override fun playToEndOfStream() {
        if (mode == PlaybackMode.VIDEO) {
            defaultAudioSink.playToEndOfStream()
        }
    }

    override fun handleDiscontinuity() {
        if (mode != PlaybackMode.AUDIO) {
            defaultAudioSink.handleDiscontinuity()
        }
        if (usesDsp()) {
            superpoweredBridge.flush()
        }
    }

    // ==================== 재생 파라미터 (피치/속도) ====================

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        when (mode) {
            PlaybackMode.VIDEO -> {
                defaultAudioSink.setPlaybackParameters(playbackParameters)
            }
            PlaybackMode.AUDIO, PlaybackMode.VIDEO_WITH_DSP -> {
            }
        }
    }

    override fun getPlaybackParameters(): PlaybackParameters {
        return defaultAudioSink.playbackParameters
    }

    // ==================== 볼륨 ====================

    override fun setVolume(volume: Float) {
        defaultAudioSink.setVolume(volume)
    }

    // ==================== 오디오 속성 ====================

    override fun setAudioAttributes(audioAttributes: AudioAttributes) {
        defaultAudioSink.setAudioAttributes(audioAttributes)
    }

    override fun getAudioAttributes(): AudioAttributes? {
        return defaultAudioSink.audioAttributes
    }

    override fun setAudioSessionId(audioSessionId: Int) {
        defaultAudioSink.setAudioSessionId(audioSessionId)
    }

    override fun setAuxEffectInfo(auxEffectInfo: AuxEffectInfo) {
        defaultAudioSink.setAuxEffectInfo(auxEffectInfo)
    }

    override fun setPreferredDevice(audioDeviceInfo: AudioDeviceInfo?) {
        defaultAudioSink.setPreferredDevice(audioDeviceInfo)
    }

    override fun setOutputStreamOffsetUs(outputStreamOffsetUs: Long) {
        defaultAudioSink.setOutputStreamOffsetUs(outputStreamOffsetUs)
    }

    override fun setClock(clock: Clock) {
        defaultAudioSink.setClock(clock)
    }

    // ==================== 상태 조회 ====================

    override fun getCurrentPositionUs(sourceEnded: Boolean): Long {
        val pos = when (mode) {
            PlaybackMode.VIDEO -> defaultAudioSink.getCurrentPositionUs(sourceEnded)
            PlaybackMode.AUDIO, PlaybackMode.VIDEO_WITH_DSP -> superpoweredBridge.getCurrentPositionUs()
        }
        if (pos == Long.MIN_VALUE) {
            return pos
        }
        return maxOf(0L, pos)
    }

    override fun isEnded(): Boolean {
        return when (mode) {
            PlaybackMode.VIDEO -> defaultAudioSink.isEnded
            PlaybackMode.AUDIO, PlaybackMode.VIDEO_WITH_DSP -> false
        }
    }


    override fun hasPendingData(): Boolean {
        return when (mode) {
            PlaybackMode.VIDEO -> defaultAudioSink.hasPendingData()
            PlaybackMode.AUDIO, PlaybackMode.VIDEO_WITH_DSP -> true
        }
    }


    // ==================== 터널링 ====================

    override fun enableTunnelingV21() {
        defaultAudioSink.enableTunnelingV21()
    }

    override fun disableTunneling() {
        defaultAudioSink.disableTunneling()
    }

    // ==================== Skip Silence ====================

    override fun setSkipSilenceEnabled(skipSilenceEnabled: Boolean) {
        defaultAudioSink.setSkipSilenceEnabled(skipSilenceEnabled)
    }

    override fun getSkipSilenceEnabled(): Boolean {
        return defaultAudioSink.skipSilenceEnabled
    }
}