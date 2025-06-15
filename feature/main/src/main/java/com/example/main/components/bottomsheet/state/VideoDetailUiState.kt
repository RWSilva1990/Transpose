package com.example.main.components.bottomsheet.state

import com.example.domain.model.youtube.video_detail.VideoDetail

sealed class VideoDetailUiState {
    data object Loading : VideoDetailUiState()
    data class Success(
        val videoDetail: VideoDetail?
    ): VideoDetailUiState()
    data class Error(
        val message: String
    ): VideoDetailUiState()

}