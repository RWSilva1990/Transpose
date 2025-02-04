package com.example.data.newpipe.extractor

import com.example.data.extractor.Pager
import com.example.data.newpipe.exception.NewPipeException
import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.domain.model.youtube.playlist.PlaylistItemData
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem


class PlaylistItemPager(
    streamingService: StreamingService,
    extractor: ListExtractor<out InfoItem>
) : Pager<InfoItem, PlaylistItemData>(streamingService, extractor) {

    override fun extract(page: ListExtractor.InfoItemsPage<out InfoItem>): List<PlaylistItemData> {
        val result = mutableListOf<PlaylistItemData>()

        for (infoItem in page.items) {
            if (infoItem is StreamInfoItem) {
                val id = getId(streamLinkHandler, infoItem.url)
                val playlistItemDomain = InfoItemMapper.streamInfoItemToPlaylistItemData(infoItem, id)
                result.add(playlistItemDomain)
            }
        }

        return result
    }

    private fun getId(handler: LinkHandlerFactory, url: String): String {
        return try {
            handler.getId(url)
        } catch (e: ParsingException) {
            throw NewPipeException.ParsingException("getId from PlaylistItemPager", e)
        }
    }
}
