package com.example.data.newpipe.extractor.channel

import com.example.data.newpipe.extractor.base.Pager
import com.example.data.newpipe.exception.NewPipeException
import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.domain.model.youtube.channel.ChannelTabResult
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class ChannelTabPager(
    streamingService: StreamingService,
    extractor: ListExtractor<out InfoItem>,
    private val tabType: String,
    initialPageFetched: Boolean = false
) : Pager<InfoItem, ChannelTabResult>(streamingService, extractor, initialPageFetched) {

    override fun extract(page: ListExtractor.InfoItemsPage<out InfoItem>): List<ChannelTabResult> {
        val result = ArrayList<ChannelTabResult>(page.items.size)

        when (tabType) {
            "videos" -> {
                page.items.filterIsInstance<StreamInfoItem>().forEach { item ->
                    val videoId = getId(streamLinkHandler, item.url)
                    val uploaderId = getId(channelLinkHandler, item.uploaderUrl)
                    result.add(InfoItemMapper.streamInfoItemToChannelTabResultVideo(item, videoId, uploaderId))
                }
            }

            "shorts" -> {
                page.items.filterIsInstance<StreamInfoItem>()
                    .filter { it.isShortFormContent }
                    .forEach { item ->
                        val videoId = getId(streamLinkHandler, item.url)
                        val uploaderId = getId(channelLinkHandler, item.uploaderUrl)
                        result.add(InfoItemMapper.streamInfoItemToChannelTabResultShorts(item, videoId, uploaderId))
                    }
            }

            "playlists" -> {
                page.items.filterIsInstance<PlaylistInfoItem>().forEach { item ->
                    val videoId = getId(playlistLinkHandler, item.url)
                    result.add(InfoItemMapper.playlistInfoItemToChannelTabResultPlaylist(item, videoId))
                }
            }

            "livestreams" -> {
//                page.items.filterIsInstance<StreamInfoItem>()
//                    .filter { it.streamType.name == "LIVE_STREAM" || it.streamType.name == "LIVE_STREAM_UPCOMING" }
//                    .forEach { item ->
//                        val videoId = getId(streamLinkHandler, item.url)
//                        val uploaderId = getId(channelLinkHandler, item.uploaderUrl)
//                        result.add(InfoItemMapper.streamInfoItemToChannelTabResultVideo(item, videoId, uploaderId))
//                    }
            }
            // 필요한 다른 탭 유형에 대한 처리 추가
        }

        return result
    }

    private fun getId(
        handler: org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory,
        url: String
    ): String {
        return try {
            handler.getId(url)
        } catch (e: ParsingException) {
            throw NewPipeException.ParsingException("getId from ChannelTabPager", e)
        }
    }
}
