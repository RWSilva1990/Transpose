package com.example.data.newpipe.repository.channel

import com.example.data.newpipe.extractor.channel.ChannelTabPager
import com.example.data.newpipe.repository.base.BaseNewPipeRepository
import com.example.domain.model.youtube.channel.ChannelDetail
import com.example.domain.model.youtube.channel.ChannelTab
import com.example.domain.model.youtube.channel.ChannelTabResult
import com.example.domain.repository.ChannelRepository
import com.example.util.Logger
import javax.inject.Inject

class ChannelRepositoryImpl @Inject constructor() : BaseNewPipeRepository(), ChannelRepository {

    private val channelTabPagers = mutableMapOf<String, ChannelTabPager>()

    override suspend fun fetchChannelDetail(channelId: String): Result<ChannelDetail> {
        return try {
            val channelLinkHandler = getChannelLinkHandler(channelId)
            val channelExtractor = getChannelExtractor(channelLinkHandler)

            channelExtractor.fetchPage()

            // 채널 탭 정보 추출
            val tabs = channelExtractor.tabs.map { tab ->
                ChannelTab(
                    id = tab.id,
                    contentType = determineTabContentType(tab.contentFilters),
                    url = tab.url
                )
            }
            Logger.d("Fetching channel detail - channelId: $channelId")
            channelExtractor.tabs.forEachIndexed { index, tab ->
                Logger.d("채널 탭 정보[$index]: ID=${tab.id}, URL=${tab.url}, ContentFilter=${tab.contentFilters}")
            }

            // 채널 상세 정보 구성
            val channelDetail = ChannelDetail(
                id = channelExtractor.id,
                name = channelExtractor.name,
                description = channelExtractor.description ?: "",
                bannerUrl = channelExtractor.banners.firstOrNull()?.url ?: "",
                avatarUrl = channelExtractor.avatars.firstOrNull()?.url ?: "",
                subscriberCount = channelExtractor.subscriberCount,
                verified = channelExtractor.isVerified,
                tabs = tabs
            )

            Result.success(channelDetail)
        } catch (e: Exception) {
            Logger.e("Error fetching channel detail", e)
            Result.failure(e)
        }
    }

    override suspend fun fetchChannelTabContent(
        channelId: String,
        contentType: String?
    ): Result<List<ChannelTabResult>> {
        if (contentType == null) {
            return Result.failure(IllegalArgumentException("tabId or contentType is null"))
        }
        return try {
            val channelLinkHandler = getChannelLinkHandler(channelId)
            val channelExtractor = getChannelExtractor(channelLinkHandler)
            channelExtractor.fetchPage()

            // 콘텐츠 유형으로 탭 찾기
            val targetTab = channelExtractor.tabs.find {
                determineTabContentType(it.contentFilters) == contentType
            } ?: throw IllegalArgumentException("Tab with content type $contentType not found")

            Logger.d("Fetching channel tab content - channelId: $channelId, tabContentType: $contentType")

            val tabExtractor = getChannelTabExtractor(targetTab)
            tabExtractor.fetchPage()

            val pager = ChannelTabPager(youtubeService, tabExtractor, contentType)
            channelTabPagers[contentType] = pager

            Result.success(pager.getNextPage())
        } catch (e: Exception) {
            Logger.e("Error fetching channel tab content", e)
            Result.failure(e)
        }
    }

    override suspend fun loadMoreChannelTabContent(contentType: String): Result<List<ChannelTabResult>> {
        val pager = channelTabPagers[contentType]
            ?: return Result.failure(IllegalStateException("No channel tab pager initiated for $contentType"))
        return try {
            Result.success(pager.getNextPage())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun canLoadMoreChannelTabContent(contentType: String): Boolean {
        val pager = channelTabPagers[contentType]
        val hasNextPage = pager?.isHasNextPage() == true
        Logger.d("Checking if can load more for $contentType: pager exists = ${pager != null}, hasNextPage = $hasNextPage")
        return channelTabPagers[contentType]?.isHasNextPage() == true
    }


}