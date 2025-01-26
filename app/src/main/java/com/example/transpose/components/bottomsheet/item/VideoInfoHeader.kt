package com.example.transpose.components.bottomsheet.item

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.example.transpose.MainViewModel

@Composable
fun VideoInfoHeader(currentVideoData: BasicVideoData?, currentVideoDetailData: VideoDetailData?, mainViewModel: MainViewModel){
    Column {
        VideoInfoSection(currentVideoData)
        ChannelSection(currentVideoData = currentVideoData, currentVideoDetailData = currentVideoDetailData)
        PitchControlItem(mainViewModel)
        TempoControlItem(mainViewModel)
    }
}