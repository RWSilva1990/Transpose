package com.example.domain.model.youtube.channel

data class ChannelDetail(
    val id: String,
    val name: String,
    val description: String,
    val bannerUrl: String?,
    val avatarUrl: String?,
    val subscriberCount: Long?,
    val verified: Boolean,
    val tabs: List<ChannelTab>
)

data class ChannelTab(
    val id: String,
    val contentType: String,
    val url: String // URL 추가
)