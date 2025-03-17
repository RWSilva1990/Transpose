package com.example.data.newpipe.mapper.playlist

import com.example.data.newpipe.mapper.base.BaseMapper
import com.example.data.newpipe.mapper.video.VideoMapper
import com.example.domain.model.youtube.channel.ChannelTabResult
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.model.youtube.search.SearchResult
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

object PlaylistMapper {

    fun playlistInfoItemToSearchResultPlaylist(
        item: PlaylistInfoItem,
        id: String
    ): SearchResult.PlaylistResult {
        return SearchResult.PlaylistResult(
            playlist = playlistInfoItemToPlaylistData(item, id)
        )
    }

    fun playlistInfoItemToChannelTabResultPlaylist(
        item: PlaylistInfoItem,
        id: String
    ): ChannelTabResult.PlaylistResult {
        return ChannelTabResult.PlaylistResult(
            playlist = playlistInfoItemToPlaylistData(item, id)
        )
    }

    fun playlistInfoItemToPlaylistData(item: PlaylistInfoItem, id: String): Playlist {
        return Playlist(
            id = id,
            title = item.name,
            description = item.description.content,
            publishTimestamp = null,
            thumbnailUrl = BaseMapper.getHighestResThumbnail(item.thumbnails.firstOrNull()?.url),
            infoType = InfoItem.InfoType.PLAYLIST,
            uploaderName = item.uploaderName,
            uploaderUrl = item.uploaderUrl,
            uploaderVerified = false,
            streamCount = item.streamCount,
            playlistType = item.playlistType
        )
    }

    fun playlistExtractorToPlaylistData(extractor: PlaylistExtractor): Playlist {
        return Playlist(
            id = extractor.id,
            title = extractor.name,
            description = extractor.description.content,
            publishTimestamp = null,
            thumbnailUrl = BaseMapper.getHighestResThumbnail(extractor.thumbnails.firstOrNull()?.url),
            infoType = InfoItem.InfoType.PLAYLIST,
            uploaderName = extractor.uploaderName,
            uploaderUrl = extractor.uploaderUrl,
            uploaderVerified = false,
            streamCount = extractor.streamCount,
            playlistType = null
        )
    }

    fun streamInfoItemToPlaylistItemData(item: StreamInfoItem, videoId: String, uploaderId: String): PlaylistItem {
        return PlaylistItem(
            video = VideoMapper.streamInfoItemToBasicVideoData(item, videoId, uploaderId)
        )
    }
}