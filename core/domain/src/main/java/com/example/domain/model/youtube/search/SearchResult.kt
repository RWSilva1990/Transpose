package com.example.domain.model.youtube.search

import com.example.domain.model.youtube.video.BasicVideoData
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfo

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
        val id: String,
        val title: String,
        val description: String,
        val publishTimestamp: Long?,
        val thumbnailUrl: String?,
        val infoType: InfoItem.InfoType,
        val uploaderName: String,
        val uploaderUrl: String?,
        val uploaderVerified: Boolean,
        val streamCount: Long,
        val playlistType: PlaylistInfo.PlaylistType?,
    ) : SearchResult()
}
