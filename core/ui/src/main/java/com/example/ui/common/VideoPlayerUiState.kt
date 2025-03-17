package com.example.ui.common

import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail

sealed class VideoPlayerUiState {
    data object Initial : VideoPlayerUiState()
    data class BasicDataLoaded(val video: Video) : VideoPlayerUiState()
    data class DetailedDataLoaded(val videoDetail: VideoDetail) : VideoPlayerUiState()
    data class LoadError(val message: String?) : VideoPlayerUiState()
}
