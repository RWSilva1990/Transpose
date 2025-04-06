package com.example.data.newpipe.extractor.search


import com.example.data.newpipe.extractor.base.Pager
import com.example.data.newpipe.exception.NewPipeException
import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.domain.model.youtube.search.SearchResult
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

open class ContentPager(
    streamingService: StreamingService,
    extractor: ListExtractor<out InfoItem>
) : Pager<InfoItem, SearchResult>(streamingService, extractor) {

    private val seenVideos = HashSet<String>()

    override fun extract(page: ListExtractor.InfoItemsPage<out InfoItem>): List<SearchResult> {
        val result = ArrayList<SearchResult>(page.items.size)

        for (infoItem in page.items) {
            when (infoItem) {
                is StreamInfoItem -> {
                    val videoId = getId(streamLinkHandler, infoItem.url)
                    if (!seenVideos.contains(videoId)) {
                        seenVideos.add(videoId)
                        val uploaderId = getId(channelLinkHandler, infoItem.uploaderUrl)
                        result.add(InfoItemMapper.streamInfoItemToSearchResultVideo(infoItem, videoId, uploaderId))
                    }
                }
                is PlaylistInfoItem -> {
//                    val id = getId(playlistLinkHandler, infoItem.url)
//                    result.add(InfoItemMapper.playlistInfoItemToSearchResultPlaylist(infoItem, id))
                }
                is ChannelInfoItem -> {
                    val id = getId(channelLinkHandler, infoItem.url)
                    result.add(InfoItemMapper.channelInfoItemToSearchResultChannel(infoItem, id))
                }
            }
        }

        return result
    }

    private fun getId(handler: LinkHandlerFactory, url: String): String {
        return try {
            handler.getId(url)
        } catch (e: ParsingException) {
            throw NewPipeException.ParsingException("getId from VideoPager", e)
        }
    }
}
