package com.example.domain.model.youtube.search

import com.example.domain.model.youtube.playlist.PlaylistData
import com.example.domain.model.youtube.video.BasicVideoData
import org.schabi.newpipe.extractor.InfoItem

sealed class SearchResult {
    data class Video(
        val basicVideoData: BasicVideoData
    ): SearchResult()

    data class Channel(
        val id: String,
        val title: String,
        val description: String,
        val publishTimestamp: Long?,
        val thumbnailUrl: String?,
        val infoType: InfoItem.InfoType,
        val subscriberCount: Long,
        val streamCount: Long,
        val verified: Boolean
    ) : SearchResult()

    data class Playlist(
        val playlist: PlaylistData
    ) : SearchResult()
}
