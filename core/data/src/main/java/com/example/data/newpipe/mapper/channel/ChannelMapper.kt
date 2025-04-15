package com.example.data.newpipe.mapper.channel

import com.example.data.newpipe.mapper.base.BaseMapper
import com.example.domain.model.youtube.channel.Channel
import com.example.domain.model.youtube.channel.ChannelDetail
import com.example.domain.model.youtube.channel.ChannelTab
import com.example.domain.model.youtube.search.SearchResult
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.ChannelInfoItem

object ChannelMapper {

    fun channelInfoItemToSearchResultChannel(
        item: ChannelInfoItem,
        id: String
    ): SearchResult.ChannelResult {
        return SearchResult.ChannelResult(
            channel = Channel(
                id = id,
                title = item.name,
                description = item.description ?: "",
                publishTimestamp = null,
                thumbnailUrl = BaseMapper.getHighestResThumbnail(item.thumbnails.firstOrNull()?.url),
                infoType = item.infoType,
                subscriberCount = item.subscriberCount,
                streamCount = item.streamCount,
                verified = item.isVerified
            )
        )
    }

    fun channelExtractorToChannelDetail(
        extractor: ChannelExtractor,
        tabs: List<ChannelTab>
    ): ChannelDetail {
        return ChannelDetail(
            id = extractor.id,
            name = extractor.name,
            description = extractor.description ?: "",
            bannerUrl = extractor.banners.firstOrNull()?.url ?: "",
            avatarUrl = extractor.avatars.firstOrNull()?.url ?: "",
            subscriberCount = extractor.subscriberCount,
            verified = extractor.isVerified,
            tabs = tabs
        )
    }
}