package com.example.home.playlist_item

import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.domain.model.youtube.playlist.PlaylistItemData
import com.example.home.playlist_item.items.PlaylistHeaderItem
import com.example.ui.components.items.CommonVideoItem
import com.example.ui.components.items.LoadingIndicator
import com.example.transpose.ui.components.scrollbar.EndlessLazyColumn
import com.example.ui.common.PaginatedState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePlaylistItemScreen(
    bottomSheetState: SheetState,
    homePlaylistItemViewModel: HomePlaylistItemViewModel,
    itemId: String?,
    navigateToBack: () -> Unit
) {
    val playlistInfo by homePlaylistItemViewModel.playlistInfo.collectAsState()
    val playlistItemsState by homePlaylistItemViewModel.playlistItemsState.collectAsState()
//    val isShowingPlaylistDialog by mainViewModel.isShowAddVideoToPlaylistDialog.collectAsState()
//    val myPlaylists by mainViewModel.myPlaylists.collectAsState()
//    val selectedVideo by mainViewModel.selectedVideo.collectAsState()
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
                itemKey = { item: PlaylistItemData -> item.basicVideoData.id },
                itemContent = { index, item: PlaylistItemData ->
                    CommonVideoItem(item = item.basicVideoData,
                        currentIndex = index,
                        onClick = {
                            coroutineScope.launch {
                                bottomSheetState.partialExpand()
                            }
                            homePlaylistItemViewModel.onMediaClicked(
                                item = item.basicVideoData,
                                playlistItems = state.items.map {
                                    it.basicVideoData
                                },
                                clickedIndex = index
                            )
                        },
                        dropDownMenuClick = {
//                            mainViewModel.showAddToPlaylistDialog(item)
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
//    if (isShowingPlaylistDialog) {
//        AddVideoToPlaylistDialog(
//            playlists = myPlaylists,
//            onDismiss = { mainViewModel.dismissPlaylistDialog() },
//            onPlaylistSelected = { playlistId ->
//                selectedVideo?.let {
//                    mainViewModel.addVideoToPlaylist(it, playlistId)
//                }
//            }
//        )
//    }
}

@Composable
fun ErrorMessage(message: String) {
    // 에러 메시지 표시 구현
}