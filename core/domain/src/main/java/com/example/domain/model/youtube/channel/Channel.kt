package com.example.domain.model.youtube.channel

import org.schabi.newpipe.extractor.InfoItem

data class Channel(
    val id: String,
    val title: String,
    val description: String,
    val publishTimestamp: Long?,
    val thumbnailUrl: String?,
    val infoType: InfoItem.InfoType,
    val subscriberCount: Long,
    val streamCount: Long,
    val verified: Boolean
)