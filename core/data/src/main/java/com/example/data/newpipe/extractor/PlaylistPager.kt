package com.example.data.newpipe.extractor


import com.example.data.extractor.Pager
import com.example.data.newpipe.exception.NewPipeException
import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.model.youtube.search.SearchResult
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class PlaylistPager(
    streamingService: StreamingService,
    private val playlistExtractor: PlaylistExtractor
) : Pager<InfoItem, PlaylistItem>(streamingService, playlistExtractor) {

    fun getPlaylistData() = InfoItemMapper.playlistExtractorToDomain(playlistExtractor)

    override fun extract(page: ListExtractor.InfoItemsPage<out InfoItem>): List<PlaylistItem> {
        val result = ArrayList<PlaylistItem>(page.items.size)

        for (infoItem in page.items) {
            when (infoItem) {
                is StreamInfoItem -> {
                    val id = getId(streamLinkHandler, infoItem.url)
                    result.add(InfoItemMapper.streamInfoItemTo(infoItem, id))
                }
                is PlaylistInfoItem -> {
//                    result.add(InfoItemMapper.playlistInfoItemToDomain(infoItem))
                }
                is ChannelInfoItem -> {
//                    result.add(InfoItemMapper.channelInfoItemToDomain(infoItem))
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