package com.example.main.components.bottomsheet.item

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.util.constants.AppColors

@Composable
fun PlayerLoadingIndicator(
    videoDetail: VideoDetail?, isPlaying: Boolean, modifier: Modifier = Modifier
) {
    if (videoDetail == null){
        CircularProgressIndicator(
            modifier = modifier,
            color = AppColors.BlueBackground
        )
    }

}