package com.example.ui.common

import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData

sealed class VideoPlayerUiState {
    data object Initial : VideoPlayerUiState()
    data class BasicDataLoaded(val basicVideoData: BasicVideoData) : VideoPlayerUiState()
    data class DetailedDataLoaded(val videoDetailData: VideoDetailData) : VideoPlayerUiState()
    data class LoadError(val message: String?) : VideoPlayerUiState()
}
