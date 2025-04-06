package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.util.constants.AppColors

@Composable
fun PlayerThumbnailView(
    currentVideoData: Video?,
    currentVideoDetail: VideoDetail?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {

    if (currentVideoDetail == null) {
        Box(modifier = modifier.background(AppColors.LightGray)) {
            AsyncImage(
                model = currentVideoData?.thumbnailUrl,
                contentDescription = "Video Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                placeholder = ColorPainter(AppColors.LightGray),
                error = ColorPainter(AppColors.LightGray)
            )
        }
    }

}