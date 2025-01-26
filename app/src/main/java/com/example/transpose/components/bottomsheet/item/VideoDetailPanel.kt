package com.example.transpose.components.bottomsheet.item

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.example.transpose.MainViewModel
import com.example.transpose.ui.components.bottomsheet.item.RelatedVideoShimmerItem
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.InfoItem

@Composable
fun VideoDetailPanel(
    currentVideoData: BasicVideoData?,
    currentVideoDetailData: VideoDetailData?,
    mainViewModel: MainViewModel,
    modifier: Modifier,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        item {
            VideoInfoHeader(currentVideoData, currentVideoDetailData, mainViewModel)
        }

        if (currentVideoDetailData == null) {
            items(5){
                RelatedVideoShimmerItem()
            }
        }
        else{
            val items = currentVideoDetailData.relatedVideos
            items.let { videoList ->
                items(videoList.size) { index ->
                    val item = videoList[index]
                    if (item.infoType == InfoItem.InfoType.STREAM){
                        RelatedVideoItem(
                            relatedVideo = item,
                            onClick = { mainViewModel.onMediaItemClick(item)
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            }
                        )
                    }
                }
            }
        }

        when (val state = currentVideoItemState) {

            is PlayableItemUiState.BasicInfoLoaded -> {
                if (state.basicInfo.type == MediaItemType.YOUTUBE)
                    items(5){
                        RelatedVideoShimmerItem()
                    }
            }
            is PlayableItemUiState.Error -> {
                val data = state.message
                item{
                    Text(data ?: "")
                }
            }
            is PlayableItemUiState.FullInfoLoaded -> {
                val items = state.fullInfo.relatedItems
                items?.let { videoList ->
                    items(videoList.size) { index ->
                        val item = videoList[index]
                        if (item.infoType == InfoItem.InfoType.STREAM){
                            RelatedVideoItem(
                                infoItem = item,
                                onClick = { mediaViewModel.onMediaItemClick(item)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                }
                            )
                        }
                    }
                }

            }
            PlayableItemUiState.Initial -> {


            }
        }


    }
}