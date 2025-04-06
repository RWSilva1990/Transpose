package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoInfoHeader(currentVideoData: Video?, currentVideoDetail: VideoDetail?, mainViewModel: MainViewModel, onNavigateToChannelScreen: (String) -> Unit, bottomSheetState: SheetState) {
    Column {
        VideoInfoSection(currentVideoData)
        ChannelSection(currentVideoData = currentVideoData, currentVideoDetail = currentVideoDetail, mainViewModel = mainViewModel, onNavigateToChannelScreen = onNavigateToChannelScreen, bottomSheetState = bottomSheetState)
        PitchControlItem(mainViewModel)
        TempoControlItem(mainViewModel)
    }
}