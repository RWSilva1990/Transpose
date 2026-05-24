package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.playable.PlayableItem
import com.example.domain.model.youtube.video.Video
import com.example.main.components.bottomsheet.state.VideoDetailUiState
import com.example.ui.components.items.CommonVideoItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailPanel(
    videoDetailUiState: VideoDetailUiState,
    currentItem: PlayableItem?,
    myPlaylists: List<MyPlaylist>,
    pitchValue: Int,
    tempoValue: Int,
    onPlayVideo: (Video) -> Unit,
    onPitchPlusOne: () -> Unit,
    onPitchMinusOne: () -> Unit,
    onPitchInit: () -> Unit,
    onTempoPlusOne: () -> Unit,
    onTempoMinusOne: () -> Unit,
    onTempoInit: () -> Unit,
    onAddItemToPlaylist: (PlayableItem, Long) -> Unit,
    onNavigateToChannelScreen: (String) -> Unit,
    bottomSheetState: SheetState,
    reservePlaylistButtonSpace: Boolean,
    modifier: Modifier,
) {

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val isLocalFile = currentItem is PlayableItem.Local

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(
            bottom = if (reservePlaylistButtonSpace) 96.dp else 0.dp
        )
    ) {
        item(key = "header") {
            VideoInfoHeader(
                currentItem = currentItem,
                videoDetailUiState = videoDetailUiState,
                myPlaylists = myPlaylists,
                pitchValue = pitchValue,
                tempoValue = tempoValue,
                onPitchPlusOne = onPitchPlusOne,
                onPitchMinusOne = onPitchMinusOne,
                onPitchInit = onPitchInit,
                onTempoPlusOne = onTempoPlusOne,
                onTempoMinusOne = onTempoMinusOne,
                onTempoInit = onTempoInit,
                onAddItemToPlaylist = onAddItemToPlaylist,
                onNavigateToChannelScreen = onNavigateToChannelScreen,
                bottomSheetState = bottomSheetState
            )
        }

        if (!isLocalFile) {
            when (val state = videoDetailUiState) {
                is VideoDetailUiState.Loading -> {
                    items(5, key = { "shimmer_$it" }) {
                        RelatedVideoShimmerItem()
                    }
                }

                is VideoDetailUiState.Success -> {
                    val items = state.videoDetail?.relatedVideos
                    items?.let { videoList ->
                        items(
                            count = videoList.size,
                            key = { index -> "${videoList[index].id}-$index" },
                            contentType = { "related_video" }
                        ) { index ->
                            val item = videoList[index]
                            CommonVideoItem(
                                item = item,
                                onClick = {
                                    onPlayVideo(item)
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
