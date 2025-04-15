package com.example.domain.repository

import com.example.domain.model.youtube.channel.ChannelDetail
import com.example.domain.model.youtube.channel.ChannelTabResult

interface ChannelRepository {
    suspend fun fetchChannelTabContent(channelId: String, contentType: String?): Result<List<ChannelTabResult>>
    suspend fun loadMoreChannelTabContent(contentType: String): Result<List<ChannelTabResult>>
    fun canLoadMoreChannelTabContent(contentType: String): Boolean
    suspend fun fetchChannelDetail(channelId: String): Result<ChannelDetail>
}