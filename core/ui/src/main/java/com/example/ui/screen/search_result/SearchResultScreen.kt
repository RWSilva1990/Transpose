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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.domain.model.youtube.search.SearchResult
import com.example.transpose.core.ui.R
import com.example.ui.components.dialog.AddVideoToPlaylistDialog
import com.example.ui.components.items.ChannelItem
import com.example.ui.components.items.CommonVideoItem
import com.example.ui.components.items.LoadingIndicator
import com.example.ui.components.scrollbar.EndlessLazyColumn
import com.example.ui.common.PaginatedState
import com.example.ui.components.items.SearchResultPlaylistItem
import com.example.util.ToastUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    searchResultViewModel: SearchResultViewModel,
    query: String?,
    bottomSheetState: SheetState,
    navigateToBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val searchResultsState by searchResultViewModel.searchResultsState.collectAsState()
    val context = LocalContext.current
    var isShowingPlaylistDialog by remember {
        mutableStateOf(false)
    }
    var selectedVideo by remember {
        mutableStateOf(null as SearchResult.VideoResult?)
    }

    val myPlaylists by searchResultViewModel.myPlaylists.collectAsState()

    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }

    LaunchedEffect(key1 = query) {
        query?.let {
            searchResultViewModel.initializeSearchPager(it)
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
                        is SearchResult.VideoResult -> "video_${item.video.id}"
                        is SearchResult.ChannelResult -> "channel_${item.channel.id}"
                        is SearchResult.PlaylistResult -> "playlist_${item.playlist}"
                    }
                },
                itemContent = { index, item: SearchResult ->
                    when (item) {
                        is SearchResult.VideoResult -> {
                            CommonVideoItem(
                                item = item.video,
                                onClick = {
                                    coroutineScope.launch {
                                        bottomSheetState.expand()
                                    }
                                    searchResultViewModel.playSingleVideo(
                                        item.video
                                    )
                                },
                                dropDownMenuClick = {
                                    selectedVideo = item
                                    isShowingPlaylistDialog = true
                                }
                            )
                        }

                        is SearchResult.ChannelResult -> {
                            ChannelItem(
                                channel = item,
                                onClick = {
                                }
                            )
                        }

                        is SearchResult.PlaylistResult -> {
                            SearchResultPlaylistItem(playlist = item, onClick = {})
                        }
                    }

                },
                loading = state.isLoadingMore,
                loadMore = { searchResultViewModel.loadMoreSearchResults() },
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
                    searchResultViewModel.addVideoToPlaylist(
                        video = it.video,
                        playlistId = playlistId
                    )
                    ToastUtil.showShort(
                        context = context,
                        message = context.getString(R.string.notify_video_added_to_playlist)
                    )
                }
            }
        )
    }
}

@Composable
fun ErrorMessage(message: String) {
    // 에러 메시지 표시 구현
}