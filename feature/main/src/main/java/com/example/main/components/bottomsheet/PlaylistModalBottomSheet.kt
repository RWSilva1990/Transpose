package com.example.main.components.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.preferences.RepeatMode
import com.example.main.MainViewModel
import com.example.main.components.bottomsheet.item.PlaylistBottomSheetItem
import com.example.transpose.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistModalBottomSheet(
    showPlaylist: Boolean,
    onDismiss: () -> Unit,
    mainViewModel: MainViewModel,
    playerViewHeight: Int,
) {
    val currentPlaylistItems by mainViewModel.currentPlaylistItems.collectAsState()
    val currentPlaylistIndex by mainViewModel.currentPlaylistIndex.collectAsState()
    val currentPlaylistInfo by mainViewModel.currentPlaylistInfo.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val repeatMode by mainViewModel.repeatMode.collectAsState()
    val shuffleMode by mainViewModel.shuffleMode.collectAsState()

    // 화면 크기 정보 가져오기
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val startHeightFraction = 1f - (playerViewHeight / screenHeightPx)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    // 최초 표시 시 부분 확장 상태로 시작
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(showPlaylist, currentPlaylistIndex) {
        if (showPlaylist && currentPlaylistIndex >= 0) {
            val targetIndex = maxOf(0, currentPlaylistIndex - 1)
            listState.animateScrollToItem(
                index = targetIndex,
                scrollOffset = 0
            )
        }
    }

    if (showPlaylist) {
        LaunchedEffect(sheetState) {
            if (!isInitialized) {
                isInitialized = true
            }
        }

        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = sheetState,
            containerColor = Color.White,
            dragHandle = null

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(startHeightFraction)
                    .padding(bottom = 24.dp) // 바닥에 추가 패딩
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPlaylistInfo?.title ?: stringResource(id = R.string.main_playlist_title),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )


                    IconButton(onClick = { onDismiss() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_close_24),
                            contentDescription = "Close"
                        )
                    }
                }

                // 플레이 모드 및 재생목록 정보
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { mainViewModel.toggleRepeatMode() }) {
                        Icon(
                            painter = painterResource(
                                id = when (repeatMode) {
                                    RepeatMode.NONE -> R.drawable.baseline_repeat_none_24
                                    RepeatMode.ALL -> R.drawable.baseline_repeat_24
                                    RepeatMode.ONE -> R.drawable.baseline_repeat_one_24
                                }
                            ),
                            contentDescription = "Repeat",
                            modifier = Modifier.size(36.dp),
                            // 활성화 상태에 따라 색상 변경
                            tint = if (repeatMode == RepeatMode.NONE) Color.Gray else Color(0xFF3F51B5)
                        )
                    }
                    IconButton(onClick = { mainViewModel.toggleShuffleMode() }, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.baseline_shuffle_24
                            ),
                            contentDescription = "Shuffle",
                            modifier = Modifier.size(36.dp),
                            // 활성화 상태에 따라 색상 변경
                            tint = if (shuffleMode) Color(0xFF3F51B5) else Color.Gray
                        )
                    }
                    Text(
                        text = stringResource(R.string.playlist_items_count, currentPlaylistItems.size),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        color = Color.Gray
                    )
                }

                Divider(color = Color.LightGray, thickness = 1.dp)

                // 재생목록 항목
                if (currentPlaylistItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("재생목록이 비어 있습니다", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(
                            count = currentPlaylistItems.size,
                            key = { index -> currentPlaylistItems[index].id }
                        ) { index ->
                            val item = currentPlaylistItems[index]
                            PlaylistBottomSheetItem(
                                item = item,
                                onClick = {
                                    mainViewModel.onMediaItemClick(
                                        item,
                                        currentPlaylistItems,
                                        index
                                    )
                                },
                                isCurrentlyPlaying = index == currentPlaylistIndex
                            )

                        }
                    }
                }
            }
        }
    }
}
