package com.example.domain.repository

import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail

interface VideoRepository {
    suspend fun fetchVideoDetail(video: Video): Result<VideoDetail>
}