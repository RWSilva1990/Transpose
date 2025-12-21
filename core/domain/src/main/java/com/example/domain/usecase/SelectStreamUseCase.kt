package com.example.domain.usecase

import com.example.domain.model.preferences.AudioQuality
import com.example.domain.model.preferences.VideoQuality
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.VideoStream
import kotlin.math.abs

class SelectStreamUseCase {

    data class SelectedStreams(
        val videoStream: VideoStream?,
        val audioStream: AudioStream?,
        val useProgressiveSource: Boolean
    )

    fun selectStreams(
        videoStreams: List<VideoStream>?,
        audioStreams: List<AudioStream>?,
        videoQuality: VideoQuality,
        audioQuality: AudioQuality
    ): SelectedStreams {
        if (videoQuality.isAuto) {
            return SelectedStreams(
                videoStream = null,
                audioStream = null,
                useProgressiveSource = true
            )
        }

        val selectedVideo = selectVideoStream(videoStreams, videoQuality)
        val selectedAudio = selectAudioStream(audioStreams, audioQuality)

        return SelectedStreams(
            videoStream = selectedVideo,
            audioStream = selectedAudio,
            useProgressiveSource = false
        )
    }

    private fun selectVideoStream(
        streams: List<VideoStream>?,
        preferredQuality: VideoQuality
    ): VideoStream? {
        if (streams.isNullOrEmpty()) return null

        val targetItag = preferredQuality.itag ?: return null

        return streams.find { it.itag == targetItag }
            ?: findClosestQuality(streams, preferredQuality)
    }

    private fun findClosestQuality(
        streams: List<VideoStream>,
        preferredQuality: VideoQuality
    ): VideoStream? {
        val targetHeight = preferredQuality.height ?: return streams.firstOrNull()

        return streams
            .filter { it.isVideoOnly }
            .minByOrNull { stream ->
                val streamHeight = stream.height
                abs(streamHeight - targetHeight)
            }
    }

    private fun selectAudioStream(
        streams: List<AudioStream>?,
        preferredQuality: AudioQuality
    ): AudioStream? {
        if (streams.isNullOrEmpty()) return null

        return streams.find { it.itag == preferredQuality.itag }
            ?: streams.find { it.itag == AudioQuality.MEDIUM.itag }
            ?: streams.firstOrNull()
    }
}