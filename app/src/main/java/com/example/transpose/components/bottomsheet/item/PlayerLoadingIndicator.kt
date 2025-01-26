package com.example.transpose.components.bottomsheet.item

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.example.transpose.MainViewModel
import com.example.transpose.MediaViewModel
import com.example.transpose.utils.constants.AppColors

@Composable
fun PlayerLoadingIndicator(
    videoDetailData: VideoDetailData?, isPlaying: Boolean, modifier: Modifier = Modifier
) {
    if (videoDetailData == null){
        CircularProgressIndicator(
            modifier = modifier,
            color = AppColors.BlueBackground
        )
    }
    else{}

}