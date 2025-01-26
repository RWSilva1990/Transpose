package com.example.transpose.components.bottomsheet.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.example.transpose.MainViewModel
import com.example.transpose.utils.constants.AppColors

@Composable
fun PlayerThumbnailView(
    currentVideoData: BasicVideoData?,
    currentVideoDetailData: VideoDetailData?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {



    if (currentVideoDetailData == null) {
        Box(modifier = modifier.background(AppColors.LightGray)) {
            AsyncImage(
                model = currentVideoData!!.thumbnailUrl,
                contentDescription = "Video Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
    else { }

}