package com.example.domain.repository

import com.example.domain.model.youtube.video_detail.VideoDetail
import kotlinx.coroutines.flow.StateFlow

interface VideoRepository {
    suspend fun fetchVideoDetail(videoId: String): Result<VideoDetail>
}