package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.main.MainViewModel
import com.example.ui.components.items.CommonVideoItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailPanel(
    currentVideoData: Video?,
    currentVideoDetail: VideoDetail?,
    mainViewModel: MainViewModel,
    onNavigateToChannelScreen: (String) -> Unit,
    bottomSheetState: SheetState,
    modifier: Modifier,
) {


    val compositionTimestamp = remember { System.currentTimeMillis() }

    val currentVideoId = currentVideoData?.id ?: "unknown"
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    LazyColumn(
        modifier = modifier, state = listState
    ) {
        item(key = "header_${compositionTimestamp}") {
            VideoInfoHeader(currentVideoData, currentVideoDetail, mainViewModel, onNavigateToChannelScreen, bottomSheetState)
        }

        if (currentVideoDetail == null) {
            items(5, key = { "shimmer_${compositionTimestamp}_$it" }) {
                RelatedVideoShimmerItem()
            }
        } else {
            val items = currentVideoDetail.relatedVideos
            items.let { videoList ->
                items(
                    count = videoList.size,
                    key = { index -> "item_${compositionTimestamp}_${index}_${videoList[index].id}" }

                ) { index ->
                    val item = videoList[index]
                    CommonVideoItem(
                        item = item,
                        onClick = {
                            mainViewModel.onMediaItemClick(item)
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        dropDownMenuClick = {
                        })

                }
            }
        }


    }
}