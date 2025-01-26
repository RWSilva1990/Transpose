package com.example.domain.model.youtube.video_detail

import com.example.domain.model.youtube.video.BasicVideoData
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.stream.VideoStream

data class VideoDetailData(
    val id: String,
    val title: String,
    val videoStream: VideoStream?,
    val description: String,
    val thumbnailUrl: String?,
    val uploaderName: String?,
    val uploaderUrl: String?,
    val uploaderAvatars: List<Image>?,
    val uploaderSubscriberCount: Long?,
    val publishTimestamp: Long?,
    val publishedTimeText: String?,     // 상대적 시간 표현 ("3 hours ago")
    val viewCount: Long?,
    val likeCount: Long?,
    val dislikeCount: Long?,
    val relatedVideos: List<BasicVideoData>
)


