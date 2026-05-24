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
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.playable.PlayableItem
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.model.youtube.playlist.Playlist
import com.example.main.R
import com.example.main.components.bottomsheet.item.PlaylistBottomSheetItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistModalBottomSheet(
    onDismiss: () -> Unit,
    currentPlaylist: List<PlayableItem>,
    currentPlaylistIndex: Int,
    currentPlaylistInfo: Playlist?,
    repeatMode: RepeatMode,
    shuffleMode: Boolean,
    onPlayPlaylistItems: (List<PlayableItem>, Int) -> Unit,
    onToggleRepeatMode: () -> Unit,
    onToggleShuffleMode: () -> Unit,
    playerViewHeight: Int,
) {

    var allowSheetDismiss by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { targetValue ->
            targetValue != androidx.compose.material3.SheetValue.Hidden || allowSheetDismiss
        }
    )
    val coroutineScope = rememberCoroutineScope()
    val initialFirstVisibleItemIndex = remember(currentPlaylistIndex, currentPlaylist.size) {
        if (currentPlaylistIndex >= 0) maxOf(0, currentPlaylistIndex - 1) else 0
    }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialFirstVisibleItemIndex
    )


    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val startHeightFraction = 1f - (playerViewHeight / screenHeightPx)


    ModalBottomSheet(
        onDismissRequest = {
            allowSheetDismiss = true
            coroutineScope.launch {
                sheetState.hide()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(startHeightFraction)
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .dismissSheetOnHeaderDrag(
                        onDismiss = {
                            coroutineScope.launch {
                                allowSheetDismiss = true
                                sheetState.hide()
                                onDismiss()
                            }
                        }
                    )
                    .padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPlaylistInfo?.title
                            ?: stringResource(id = R.string.main_playlist_title),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    IconButton(onClick = {
                        allowSheetDismiss = true
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_close_24),
                            contentDescription = "Close"
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onToggleRepeatMode) {
                        Icon(
                            painter = painterResource(
                                id = when (repeatMode) {
                                    RepeatMode.OFF -> R.drawable.baseline_repeat_none_24
                                    RepeatMode.ALL -> R.drawable.baseline_repeat_24
                                    RepeatMode.ONE -> R.drawable.baseline_repeat_one_24
                                }
                            ),
                            contentDescription = "Repeat",
                            modifier = Modifier.size(36.dp),
                            tint = if (repeatMode == RepeatMode.OFF) Color.Gray else Color(0xFF3F51B5)
                        )
                    }
                    IconButton(
                        onClick = onToggleShuffleMode,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.baseline_shuffle_24
                            ),
                            contentDescription = "Shuffle",
                            modifier = Modifier.size(36.dp),
                            tint = if (shuffleMode) Color(0xFF3F51B5) else Color.Gray
                        )
                    }
                    Text(
                        text = stringResource(R.string.playlist_items_count, currentPlaylist.size),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        color = Color.Gray
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
            }

            if (currentPlaylist.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.empty_playlist), color = Color.Gray)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(
                        count = currentPlaylist.size,
                        key = { index -> "${currentPlaylist[index].id}-$index" },
                        contentType = { "playlist_item" }
                    ) { index ->
                        val item = currentPlaylist[index]
                        PlaylistBottomSheetItem(
                            item = item,
                            onClick = {
                                onPlayPlaylistItems(currentPlaylist, index)
                            },
                            isCurrentlyPlaying = index == currentPlaylistIndex
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.dismissSheetOnHeaderDrag(onDismiss: () -> Unit): Modifier = pointerInput(onDismiss) {
    var dragDistance = 0f
    detectVerticalDragGestures(
        onDragStart = { dragDistance = 0f },
        onVerticalDrag = { _, dragAmount ->
            if (dragAmount > 0f) {
                dragDistance += dragAmount
            }
        },
        onDragEnd = {
            if (dragDistance > 64.dp.toPx()) {
                onDismiss()
            }
        },
        onDragCancel = {
            dragDistance = 0f
        }
    )
}
