package com.example.library.my_playlist_item

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.library.my_playlist_item.items.PlaylistVideoItem
import kotlinx.coroutines.launch
import com.example.transpose.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryMyPlaylistItemScreen(
    bottomSheetState: SheetState,
    libraryMyPlaylistItemViewModel: LibraryMyPlaylistItemViewModel,
    itemId: Long?,
    navigateToBack: () -> Unit
) {

    val myPlaylistItems by libraryMyPlaylistItemViewModel.myPlaylistItems.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(itemId) {
        itemId?.let {
            libraryMyPlaylistItemViewModel.getVideosForPlaylist(itemId.toLong())
        }
    }

    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }
    if (myPlaylistItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(id = R.string.playlist_item_empty_text),
                modifier = Modifier.align(
                    Alignment.Center
                )
            )

        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        items(myPlaylistItems.size) { index ->
            val item = myPlaylistItems[index]
            PlaylistVideoItem(item = item, onClick = {
                libraryMyPlaylistItemViewModel.onMediaItemClicked(item, index)
                coroutineScope.launch {
                    bottomSheetState.expand()
                }

            }, dropDownMenuClick = {
                itemId?.let {
                    libraryMyPlaylistItemViewModel.deleteVideo(itemId.toLong(), item)
                }
            })
        }
    }


}