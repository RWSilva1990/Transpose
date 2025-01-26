package com.example.domain.model.youtube.video

import com.example.domain.model.youtube.PlayableVideo
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamType

data class BasicVideoData(
    override val id: String,
    override val title: String,
    override val thumbnailUrl: String?,
    val description: String,
    val publishTimestamp: Long?,
    val infoType: InfoItem.InfoType,
    val uploaderName: String?,
    val uploaderUrl: String?,
    val uploaderAvatars: List<Image>?,
    val uploaderVerified: Boolean?,
    val duration: Long,
    val viewCount: Long,
    val textualUploadDate: String?,
    val streamType: StreamType?,
    val shortFormContent: Boolean
): PlayableVideo
