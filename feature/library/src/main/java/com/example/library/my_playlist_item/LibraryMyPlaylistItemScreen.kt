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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.library.R
import com.example.library.my_playlist_item.items.PlaylistItem
import com.example.ui.common.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryMyPlaylistItemScreen(
    bottomSheetState: SheetState,
    libraryMyPlaylistItemViewModel: LibraryMyPlaylistItemViewModel,
    navigateToBack: () -> Unit
) {

    val uiState by libraryMyPlaylistItemViewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }
    when (val state = uiState) {
        is UiState.Initial -> {}
        is UiState.Loading -> {}
        is UiState.Success -> {
            val items = state.data
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = stringResource(id = R.string.playlist_item_empty_text),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items.size) { index ->
                        val item = items[index]
                        PlaylistItem(
                            item = item,
                            onClick = {
                                libraryMyPlaylistItemViewModel.playPlaylist(items, index)
                                coroutineScope.launch {
                                    bottomSheetState.expand()
                                }
                            },
                            dropDownMenuClick = {
                                libraryMyPlaylistItemViewModel.deleteItem(item)
                            }
                        )
                    }
                }
            }
        }
        is UiState.Error -> {}
    }
}