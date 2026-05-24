package com.example.ui.screen.playlist_info

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.model.youtube.video.Video
import com.example.ui.components.scrollbar.EndlessLazyColumn
import com.example.ui.common.PaginatedState
import com.example.ui.components.dialog.AddItemToPlaylistDialog
import com.example.ui.components.items.CommonVideoItem
import com.example.ui.components.items.LoadingIndicator
import com.example.util.constants.AppColors
import com.example.util.Logger
import com.example.util.ToastUtil
import com.example.transpose.core.ui.R
import com.example.ui.screen.playlist_info.items.PlaylistHeaderItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistInfoScreen(
    bottomSheetState: SheetState,
    playlistInfoViewModel: PlaylistInfoViewModel,
    playlistId: String?,
) {

    val playlistInfo by playlistInfoViewModel.currentPlaylistInfo.collectAsStateWithLifecycle()
    val playlistItemsState by playlistInfoViewModel.playlistItemsState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var isShowingPlaylistDialog by remember {
        mutableStateOf(false)
    }
    var selectedVideo by remember {
        mutableStateOf(null as Video?)
    }

    val myPlaylists by playlistInfoViewModel.myPlaylists.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }

    LaunchedEffect(key1 = playlistId) {
        playlistId?.let { id ->
            playlistInfoViewModel.initializePlaylistPager(id)
        }
    }

    when (val state = playlistItemsState) {
        is PaginatedState.Initial -> {

        }

        is PaginatedState.Loading -> {
            LoadingIndicator()

        }

        is PaginatedState.Success -> {
            if (state.items.isEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState
                ) {
                    playlistInfo?.let { playlistData ->
                        item {
                            PlaylistHeaderItem(playlistData = playlistData)
                        }
                    }
                    item {
                        PlaylistItemsUnavailableMessage(
                            onRetry = {
                                playlistId?.let { playlistInfoViewModel.retryPlaylistItems(it) }
                            }
                        )
                    }
                }
            } else {
                EndlessLazyColumn(
                    listState = listState,
                    items = state.items,
                    headerData = playlistInfo,
                    itemKey = { item: PlaylistItem -> item.video.id },
                    itemContent = { index, item: PlaylistItem ->
                        CommonVideoItem(item = item.video,
                            onClick = {
                                Logger.d("onClick ${item}")
                                playlistInfoViewModel.playPlaylist(
                                    playlistItems = state.items.map {
                                        it.video
                                    },
                                    clickedIndex = index
                                )
                                coroutineScope.launch {
                                    bottomSheetState.expand()
                                }

                            },
                            dropDownMenuClick = {
                                selectedVideo = item.video
                                isShowingPlaylistDialog = true
                            })
                    },
                    headerContent = { playlistData ->
                        PlaylistHeaderItem(playlistData = playlistData)
                    },
                    loading = state.isLoadingMore,
                    loadMore = { playlistInfoViewModel.loadMorePlaylistItems() },
                    hasMoreItems = state.hasMore
                )
            }
        }

        is PaginatedState.Error -> {
            PlaylistLoadErrorMessage(
                message = state.message,
                onRetry = {
                    playlistId?.let { playlistInfoViewModel.retryPlaylistItems(it) }
                }
            )
        }
    }
    if (isShowingPlaylistDialog) {
        AddItemToPlaylistDialog(
            playlists = myPlaylists,
            onDismiss = { isShowingPlaylistDialog = false },
            onPlaylistSelected = { selectedPlaylistId ->
                selectedVideo?.let {
                    playlistInfoViewModel.addVideoToPlaylist(it, selectedPlaylistId)
                    ToastUtil.showShort(
                        context = context,
                        message = context.getString(R.string.notify_video_added_to_playlist)
                    )                }
            }
        )
    }
}



@Composable
private fun PlaylistItemsUnavailableMessage(
    onRetry: () -> Unit
) {
    PlaylistLoadMessage(
        title = stringResource(R.string.playlist_items_unavailable_title),
        message = stringResource(R.string.playlist_items_unavailable_message),
        onRetry = onRetry
    )
}

@Composable
private fun PlaylistLoadErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Logger.d("ErrorMessage $message")
    PlaylistLoadMessage(
        title = stringResource(R.string.load_fail_message),
        message = message,
        onRetry = onRetry
    )
}

@Composable
private fun PlaylistLoadMessage(
    title: String,
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = AppColors.BlueBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = AppColors.DescriptionColor,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.playlist_items_retry_text))
        }
    }
}
