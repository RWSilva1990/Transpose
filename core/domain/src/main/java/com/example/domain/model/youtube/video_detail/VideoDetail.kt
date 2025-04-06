package com.example.domain.model.youtube.video_detail

import com.example.domain.model.youtube.video.Video

data class VideoDetail(
    val id: String,
    val title: String,
    val videoOnlyStreams: List<String>?,
    val audioOnlyStreams: List<String>?,
    val videoStreamContent: String?,
    val description: String,
    val thumbnailUrl: String?,
    val uploaderName: String?,
    val uploaderId: String?,
    val uploaderAvatarUrl: String?,
    val uploaderSubscriberCount: Long?,
    val publishTimestamp: Long?,
    val publishedTimeText: String?,     // 상대적 시간 표현 ("3 hours ago")
    val viewCount: Long?,
    val likeCount: Long?,
    val dislikeCount: Long?,
    val relatedVideos: List<Video>
)


