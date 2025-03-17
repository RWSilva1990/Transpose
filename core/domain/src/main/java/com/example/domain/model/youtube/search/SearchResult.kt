package com.example.domain.model.youtube.search


import com.example.domain.model.youtube.channel.Channel
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.video.Video
import org.schabi.newpipe.extractor.InfoItem

sealed class SearchResult {
    data class VideoResult(
        val video: Video
    ): SearchResult()

    data class ChannelResult(
        val channel: Channel
    ) : SearchResult()

    data class PlaylistResult(
        val playlist: Playlist
    ) : SearchResult()
}
