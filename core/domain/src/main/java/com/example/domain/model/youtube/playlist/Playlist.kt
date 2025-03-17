package com.example.domain.model.youtube.playlist

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfo

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
)