package com.example.domain.model.youtube.video_detail

import com.example.domain.model.youtube.video.Video
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.VideoStream

data class VideoDetail(
    val id: String,
    val title: String,
    val videoStreams: List<VideoStream>?,
    val videoOnlyStreams: List<VideoStream>?,
    val audioOnlyStreams: List<AudioStream>?,
    val videoStreamContent: String?,
    val duration: Long,
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

