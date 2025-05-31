package com.example.domain.repository

import com.example.domain.model.youtube.video_detail.VideoDetail
import kotlinx.coroutines.flow.StateFlow

interface VideoRepository {
    val currentVideoDetail: StateFlow<VideoDetail?>
    suspend fun fetchVideoDetail(videoId: String)
}