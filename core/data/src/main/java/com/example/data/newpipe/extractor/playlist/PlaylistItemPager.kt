package com.example.data.newpipe.extractor.playlist

import com.example.data.newpipe.exception.NewPipeException
import com.example.data.newpipe.extractor.base.Pager
import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.util.Logger
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem


class PlaylistItemPager(
    streamingService: StreamingService,
    extractor: ListExtractor<out InfoItem>
) : Pager<InfoItem, PlaylistItem>(streamingService, extractor) {

    override fun extract(page: ListExtractor.InfoItemsPage<out InfoItem>): List<PlaylistItem> {
        val result = mutableListOf<PlaylistItem>()

        for (infoItem in page.items) {
            if (infoItem is StreamInfoItem) {
                Logger.d("PlaylistItemPager extract - videoId: $infoItem, uploaderId: ${infoItem.uploaderUrl}")

                val videoId = getId(streamLinkHandler, infoItem.url)
                val uploaderId = getId(channelLinkHandler, infoItem.uploaderUrl)

                val playlistItemDomain =
                    InfoItemMapper.streamInfoItemToPlaylistItemData(infoItem, videoId, uploaderId)
                result.add(playlistItemDomain)
            }
        }
        return result
    }

    private fun getId(handler: LinkHandlerFactory, url: String?): String {
        if (url == null) return ""

        return try {
            handler.getId(url)
        } catch (e: ParsingException) {
            throw NewPipeException.ParsingException("getId from PlaylistItemPager", e)
        }
    }

}
