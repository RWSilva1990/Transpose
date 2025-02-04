package com.example.data.newpipe.extractor

import com.example.data.extractor.Pager
import com.example.data.newpipe.exception.NewPipeException
import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.domain.model.youtube.playlist.PlaylistData
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem

class PlaylistPager(
    streamingService: StreamingService,
    extractor: ListExtractor<out InfoItem>
) : Pager<InfoItem, PlaylistData>(streamingService, extractor) {

    override fun extract(page: ListExtractor.InfoItemsPage<out InfoItem>): List<PlaylistData> {
        val result = ArrayList<PlaylistData>()

        for (infoItem in page.items) {
            if (infoItem is PlaylistInfoItem) {
                val id = getId(playlistLinkHandler, infoItem.url)
                val playlistDomain = InfoItemMapper.playlistInfoItemToPlaylistData(infoItem, id)
                result.add(playlistDomain)
            }
        }

        return result
    }

    private fun getId(handler: ListLinkHandlerFactory, url: String): String {
        return try {
            handler.getId(url)
        } catch (e: ParsingException) {
            throw NewPipeException.ParsingException("getId from ChannelPlaylistPager", e)
        }
    }
}
