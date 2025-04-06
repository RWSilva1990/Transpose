package com.example.domain.model.youtube.channel

import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.video.Video

sealed class ChannelTabResult {
    data class VideoResult(
        val video: Video
    ): ChannelTabResult()

    data class ShortsResult(
        val video: Video
    ): ChannelTabResult()

    data class PlaylistResult(
        val playlist: Playlist
    ): ChannelTabResult()
}