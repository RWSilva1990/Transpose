package com.example.data.newpipe.repository.channel

import com.example.data.di.IoDispatcher
import com.example.data.newpipe.extractor.channel.ChannelTabPager
import com.example.data.newpipe.repository.base.NewPipeManager
import com.example.domain.model.youtube.channel.ChannelDetail
import com.example.domain.model.youtube.channel.ChannelTab
import com.example.domain.model.youtube.channel.ChannelTabResult
import com.example.domain.repository.ChannelRepository
import com.example.util.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChannelRepositoryImpl @Inject constructor(
    private val newPipeManager: NewPipeManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ChannelRepository {

    private val channelTabPagers = mutableMapOf<String, ChannelTabPager>()

    override suspend fun fetchChannelDetail(channelId: String): Result<ChannelDetail> {
        return withContext(ioDispatcher) {
            try {
                val channelLinkHandler = newPipeManager.getChannelLinkHandler(channelId)
                val channelExtractor = newPipeManager.getChannelExtractor(channelLinkHandler)

                channelExtractor.fetchPage()

                // 채널 탭 정보 추출
                val tabs = channelExtractor.tabs.map { tab ->
                    ChannelTab(
                        id = tab.id,
                        contentType = newPipeManager.determineTabContentType(tab.contentFilters),
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
                Logger.e("Error fetching channel detail for $channelId", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun fetchChannelTabContent(
        channelId: String,
        contentType: String?
    ): Result<List<ChannelTabResult>> {
        return withContext(ioDispatcher) {
            try {
                if (contentType == null) throw IllegalArgumentException("tabId or contentType is null")

                val channelLinkHandler = newPipeManager.getChannelLinkHandler(channelId)
                val channelExtractor = newPipeManager.getChannelExtractor(channelLinkHandler)
                channelExtractor.fetchPage()

                // 콘텐츠 유형으로 탭 찾기
                val targetTab = channelExtractor.tabs.find {
                    newPipeManager.determineTabContentType(it.contentFilters) == contentType
                } ?: throw IllegalArgumentException("Tab with content type $contentType not found")

                Logger.d("Fetching channel tab content - channelId: $channelId, tabContentType: $contentType")

                val tabExtractor = newPipeManager.getChannelTabExtractor(targetTab)
                tabExtractor.fetchPage()

                val pager = ChannelTabPager(
                    newPipeManager.youtubeService,
                    tabExtractor,
                    contentType,
                    initialPageFetched = true
                )
                channelTabPagers[contentType] = pager

                val result = pager.getNextPage()
                Result.success(result)
            } catch (e: Exception) {
                Logger.e("Error fetching channel tab content for $channelId, contentType: $contentType", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun loadMoreChannelTabContent(contentType: String): Result<List<ChannelTabResult>> {
        return withContext(ioDispatcher) {
            try {
                val pager = channelTabPagers[contentType]
                    ?: throw IllegalStateException("No channel tab pager initiated for $contentType")

                val result = pager.getNextPage()
                Result.success(result)
            } catch (e: Exception) {
                Logger.e("Error loading more channel tab content for contentType: $contentType", e)
                Result.failure(e)
            }
        }
    }

    override fun canLoadMoreChannelTabContent(contentType: String): Boolean {
        val pager = channelTabPagers[contentType]
        val hasNextPage = pager?.isHasNextPage() == true
        Logger.d("Checking if can load more for $contentType: pager exists = ${pager != null}, hasNextPage = $hasNextPage")
        return channelTabPagers[contentType]?.isHasNextPage() == true
    }
}
