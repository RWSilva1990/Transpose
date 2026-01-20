package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.domain.model.playable.PlayableItem
import com.example.main.MainViewModel
import com.example.main.components.bottomsheet.state.VideoDetailUiState
import com.example.ui.components.items.CommonVideoItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailPanel(
    mainViewModel: MainViewModel,
    onNavigateToChannelScreen: (String) -> Unit,
    bottomSheetState: SheetState,
    modifier: Modifier,
) {

    val videoDetailUiState by mainViewModel.videoDetailUiState.collectAsState()
    val currentItem by mainViewModel.currentItem.collectAsState()

    val compositionTimestamp = remember { System.currentTimeMillis() }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isLocalFile = currentItem is PlayableItem.Local

    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        item(key = "header_${compositionTimestamp}") {
            VideoInfoHeader(mainViewModel, onNavigateToChannelScreen, bottomSheetState)
        }

        if (!isLocalFile) {
            when (val state = videoDetailUiState) {
                is VideoDetailUiState.Loading -> {
                    items(5, key = { "shimmer_${compositionTimestamp}_$it" }) {
                        RelatedVideoShimmerItem()
                    }
                }

                is VideoDetailUiState.Success -> {
                    val items = state.videoDetail?.relatedVideos
                    items?.let { videoList ->
                        items(
                            count = videoList.size,
                            key = { index -> "item_${compositionTimestamp}_${index}_${videoList[index].id}" }

                        ) { index ->
                            val item = videoList[index]
                            CommonVideoItem(
                                item = item,
                                onClick = {
                                    mainViewModel.playVideo(item)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(0)
                                    }
                                },
                                dropDownMenuClick = {}
                            )
                        }
                    }
                }
                else -> {

                }
            }
        }
    }
}