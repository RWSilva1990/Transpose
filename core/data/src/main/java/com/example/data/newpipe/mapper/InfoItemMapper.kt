package com.example.data.newpipe.mapper

import com.example.data.newpipe.mapper.channel.ChannelMapper
import com.example.data.newpipe.mapper.playlist.PlaylistMapper
import com.example.data.newpipe.mapper.video.VideoMapper
import com.example.domain.model.youtube.channel.ChannelDetail
import com.example.domain.model.youtube.channel.ChannelTab
import com.example.domain.model.youtube.channel.ChannelTabResult
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.model.youtube.search.SearchResult
import com.example.domain.model.youtube.video_detail.VideoDetail
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem

object InfoItemMapper {

    // 비디오 매핑 메서드
    fun streamInfoItemToSearchResultVideo(item: StreamInfoItem, videoId: String, uploaderId: String): SearchResult.VideoResult {
        return VideoMapper.streamInfoItemToSearchResultVideo(item, videoId, uploaderId)
    }

    // 채널 매핑 메서드
    fun channelInfoItemToSearchResultChannel(item: ChannelInfoItem, id: String): SearchResult.ChannelResult {
        return ChannelMapper.channelInfoItemToSearchResultChannel(item, id)
    }

    // 플레이리스트 매핑 메서드
    fun playlistInfoItemToSearchResultPlaylist(item: PlaylistInfoItem, id: String): SearchResult.PlaylistResult {
        return PlaylistMapper.playlistInfoItemToSearchResultPlaylist(item, id)
    }

    fun streamExtractorToVideoDetail(extractor: StreamExtractor, uploaderId: String): VideoDetail {
        return VideoMapper.streamExtractorToVideoDetail(extractor, uploaderId)
    }

    fun playlistInfoItemToPlaylistData(item: PlaylistInfoItem, id: String): Playlist {
        return PlaylistMapper.playlistInfoItemToPlaylistData(item, id)
    }

    fun playlistExtractorToPlaylistData(extractor: PlaylistExtractor): Playlist {
        return PlaylistMapper.playlistExtractorToPlaylistData(extractor)
    }

    fun streamInfoItemToPlaylistItemData(item: StreamInfoItem, videoId: String, uploaderId: String): PlaylistItem {
        return PlaylistMapper.streamInfoItemToPlaylistItemData(item, videoId, uploaderId)
    }

    fun channelExtractorToChannelDetail(extractor: ChannelExtractor, tabs: List<ChannelTab>): ChannelDetail {
        return ChannelMapper.channelExtractorToChannelDetail(extractor, tabs)
    }

    fun streamInfoItemToChannelTabResultVideo(item: StreamInfoItem, videoId: String, uploaderId: String): ChannelTabResult.VideoResult {
        return VideoMapper.streamInfoItemToChannelTabResultVideo(item, videoId, uploaderId)
    }

    fun streamInfoItemToChannelTabResultShorts(item: StreamInfoItem, videoId: String, uploaderId: String): ChannelTabResult.ShortsResult {
        return VideoMapper.streamInfoItemToChannelTabResultShorts(item, videoId, uploaderId)
    }

    fun playlistInfoItemToChannelTabResultPlaylist(item: PlaylistInfoItem, videoId: String): ChannelTabResult.PlaylistResult {
        return PlaylistMapper.playlistInfoItemToChannelTabResultPlaylist(item, videoId)
    }


}