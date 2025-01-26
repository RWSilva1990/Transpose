package com.example.ui.screen.search_result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.domain.model.youtube.search.SearchResult
import com.example.transpose.ui.components.items.ChannelItem
import com.example.transpose.ui.components.items.CommonVideoItem
import com.example.transpose.ui.components.items.LoadingIndicator
import com.example.transpose.ui.components.items.PlaylistItem
import com.example.transpose.ui.components.scrollbar.EndlessLazyColumn
import com.example.ui.common.PaginatedState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchResultScreen(
    homeSearchResultViewModel: HomeSearchResultViewModel,
    query: String?,
    bottomSheetState: SheetState
) {
    val coroutineScope = rememberCoroutineScope()
    val searchResultsState by homeSearchResultViewModel.searchResultsState.collectAsState()
    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }

    LaunchedEffect(key1 = query) {
        query?.let {
            homeSearchResultViewModel.initializeSearchPager(it)
        }
    }

    when (val state = searchResultsState) {
        is PaginatedState.Initial -> {
        }

        is PaginatedState.Loading -> {
            LoadingIndicator()
        }

        is PaginatedState.Success -> {
            EndlessLazyColumn(
                modifier = Modifier.fillMaxSize(),
                items = state.items,
                headerData = null,
                itemKey = { item: SearchResult ->
                    when (item) {
                        is SearchResult.Video -> "video_${item.basicVideoData.id}"
                        is SearchResult.Channel -> "channel_${item.id}"
                        is SearchResult.Playlist -> "playlist_${item.id}"
                    }
                },
                itemContent = { index, item: SearchResult ->
                    when (item) {
                        is SearchResult.Video -> {
                            CommonVideoItem(
                                item = item.basicVideoData,
                                currentIndex = index,
                                onClick = {
                                    coroutineScope.launch {
                                        bottomSheetState.expand()
                                    }
//                                    mediaViewModel.onMediaItemClick(newPipeVideoData)
                                },
                                dropDownMenuClick = {
//                                    mainViewModel.showAddToPlaylistDialog(
//                                        newPipeVideoData
//                                    )
                                }
                            )
                        }

                        is SearchResult.Channel -> {
                            ChannelItem(
                                channel = item,
                                onClick = {
                                }
                            )
                        }

                        is SearchResult.Playlist -> {
                            PlaylistItem(playlist = item, onClick = {})
                        }
                    }

                },
                loading = state.isLoadingMore,
                loadMore = { homeSearchResultViewModel.loadMoreSearchResults() },
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