package com.example.home.playlist_item

import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.model.youtube.video.Video
import com.example.transpose.feature.home.R
import com.example.ui.screen.playlist_info.items.PlaylistHeaderItem
import com.example.ui.components.scrollbar.EndlessLazyColumn
import com.example.ui.common.PaginatedState
import com.example.ui.components.dialog.AddVideoToPlaylistDialog
import com.example.ui.components.items.CommonVideoItem
import com.example.ui.components.items.LoadingIndicator
import com.example.util.Logger
import com.example.util.ToastUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePlaylistItemScreen(
    bottomSheetState: SheetState,
    homePlaylistItemViewModel: HomePlaylistItemViewModel,
    itemId: String?,
    navigateToBack: () -> Unit
) {
    val playlistInfo by homePlaylistItemViewModel.currentPlaylistInfo.collectAsState()
    val playlistItemsState by homePlaylistItemViewModel.playlistItemsState.collectAsState()
    val context = LocalContext.current
    var isShowingPlaylistDialog by remember {
        mutableStateOf(false)
    }
    var selectedVideo by remember {
        mutableStateOf(null as Video?)
    }

    val myPlaylists by homePlaylistItemViewModel.myPlaylists.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }

    LaunchedEffect(key1 = itemId) {
        itemId?.let { id ->
            homePlaylistItemViewModel.initializePlaylistPager(id)
        }
    }

    when (val state = playlistItemsState) {
        is PaginatedState.Initial -> {

        }

        is PaginatedState.Loading -> {
            LoadingIndicator()

        }

        is PaginatedState.Success -> {
            EndlessLazyColumn(
                items = state.items,
                headerData = playlistInfo,
                itemKey = { item: PlaylistItem -> item.video.id },
                itemContent = { index, item: PlaylistItem ->
                    CommonVideoItem(item = item.video,
                        onClick = {
                            homePlaylistItemViewModel.onMediaClicked(
                                item = item.video,
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
                            homePlaylistItemViewModel.getAllMyPlaylists()
                            selectedVideo = item.video
                            isShowingPlaylistDialog = true
                        })
                },
                headerContent = { playlistData ->
                    PlaylistHeaderItem(playlistData = playlistData)
                },
                loading = state.isLoadingMore,
                loadMore = { homePlaylistItemViewModel.loadMorePlaylistItems() },
                hasMoreItems = state.hasMore
            )
        }

        is PaginatedState.Error -> {
            ErrorMessage(message = state.message)
        }
    }
    if (isShowingPlaylistDialog) {
        AddVideoToPlaylistDialog(
            playlists = myPlaylists,
            onDismiss = { isShowingPlaylistDialog = false },
            onPlaylistSelected = { playlistId ->
                selectedVideo?.let {
                    homePlaylistItemViewModel.addVideoToPlaylist(it, playlistId)
                    ToastUtil.showShort(
                        context = context,
                        message = context.getString(R.string.notify_video_added_to_playlist)
                    )                }
            }
        )
    }
}



@Composable
fun ErrorMessage(message: String) {
    Logger.d("ErrorMessage $message")
}