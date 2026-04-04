package com.example.main.components.bottomsheet

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.main.MainViewModel

@Composable
fun PlaylistModalBottomSheetContainer(
    onDismiss: () -> Unit,
    mainViewModel: MainViewModel,
    playerViewHeight: Int,
) {
    val currentPlaylist by mainViewModel.currentPlaylist.collectAsStateWithLifecycle()
    val currentPlaylistIndex by mainViewModel.currentPlaylistIndex.collectAsStateWithLifecycle()
    val currentPlaylistInfo by mainViewModel.currentPlaylistInfo.collectAsStateWithLifecycle()
    val repeatMode by mainViewModel.repeatMode.collectAsStateWithLifecycle()
    val shuffleMode by mainViewModel.shuffleMode.collectAsStateWithLifecycle()

    PlaylistModalBottomSheet(
        onDismiss = onDismiss,
        currentPlaylist = currentPlaylist,
        currentPlaylistIndex = currentPlaylistIndex,
        currentPlaylistInfo = currentPlaylistInfo,
        repeatMode = repeatMode,
        shuffleMode = shuffleMode,
        onPlayPlaylistItems = mainViewModel::playPlaylistItems,
        onToggleRepeatMode = mainViewModel::toggleRepeatMode,
        onToggleShuffleMode = mainViewModel::toggleShuffleMode,
        playerViewHeight = playerViewHeight
    )
}
