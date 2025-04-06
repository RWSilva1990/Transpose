package com.example.home.home_playlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.youtube.playlist.Playlist
import com.example.home.home_playlist.items.NationalPlaylistItem
import com.example.home.home_playlist.items.RegularPlaylistItem
import com.example.transpose.feature.home.R
import com.example.ui.common.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePlaylistScreen(
    bottomSheetState: SheetState,
    homePlaylistViewModel: HomePlaylistViewModel,
    navigateToPlaylistItemScreen: (String) -> Unit,
    canGoBack: () -> Boolean,
    modifier: Modifier = Modifier,
    navigateToBack: () -> Unit
) {
    val nationalPlaylistState by homePlaylistViewModel.nationalPlaylistDataState.collectAsState()
    val recommendedPlaylistState by homePlaylistViewModel.recommendedPlaylistDataState.collectAsState()
    val typedPlaylistState by homePlaylistViewModel.typedPlaylistDataState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            PlaylistSection(
                title = stringResource(id = R.string.Global_popular_Top100_playlist_text),
                playlistState = nationalPlaylistState
            ) { playlist ->
                NationalPlaylistItem(
                    playlistData = playlist,
                    onClick = {
                        navigateToPlaylistItemScreen(it)
                        homePlaylistViewModel.setCurrentPlaylistInfo(playlist)
                    }
                )
            }
        }
        item {
            PlaylistSection(
                title = stringResource(id = R.string.Recommended_playlist_text),
                playlistState = recommendedPlaylistState
            ) { playlist ->
                RegularPlaylistItem(
                    playlistData = playlist,
                    onClick = {
                        navigateToPlaylistItemScreen(it)
                        homePlaylistViewModel.setCurrentPlaylistInfo(playlist)
                    }
                )
            }
        }
        item {
            PlaylistSection(
                title = stringResource(id = R.string.Type_based_playlist_text),
                playlistState = typedPlaylistState
            ) { playlist ->
                RegularPlaylistItem(
                    playlistData = playlist,
                    onClick = {
                        navigateToPlaylistItemScreen(it)
                        homePlaylistViewModel.setCurrentPlaylistInfo(playlist)
                    }
                )
            }
        }
    }
}

@Composable
fun PlaylistSection(
    title: String,
    playlistState: UiState<List<Playlist>>,
    itemContent: @Composable (Playlist) -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        when (playlistState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                ErrorMessage(
                    isVisible = true,
                    message = (playlistState).message,
                    onRefresh = {}
                )
            }

            is UiState.Success -> {
                if (playlistState.data.isEmpty()) {
                    Text(
                        text = "No playlists available",
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            count = playlistState.data.size,
                            key = { index -> playlistState.data[index].id }
                        ) { index ->
                            itemContent(playlistState.data[index])
                        }
                    }
                }
            }

            is UiState.Initial -> {
                Text(
                    text = "Ready to load playlists",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


@Composable
fun ErrorMessage(
    isVisible: Boolean,
    message: String,
    onRefresh: () -> Unit
) {
    if (isVisible) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRefresh) {
                Text(text = "다시로딩")
            }
        }
    }
}
