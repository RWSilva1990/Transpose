package com.example.domain.model.youtube.video

import org.schabi.newpipe.extractor.InfoItem

data class Video(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val description: String,
    val publishTimestamp: Long?,
    val infoType: InfoItem.InfoType,
    val uploaderName: String?,
    val uploaderUrl: String?,
    val uploaderAvatarUrl: String?,
    val uploaderVerified: Boolean?,
    val duration: Long,
    val viewCount: Long,
    val textualUploadDate: String?,
    val streamType: String?,
    val shortFormContent: Boolean
)
