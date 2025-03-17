package com.example.ui.components.dialog

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.domain.model.library.MyPlaylist
import com.example.transpose.core.ui.R
import com.example.ui.components.items.MyPlaylistItem

@Composable
fun AddVideoToPlaylistDialog(
    playlists: List<MyPlaylist>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.video_pop_up_menu_add_playlist_text)) },
        text = {
            LazyColumn {
                items(playlists.size) { index ->
                    val item = playlists[index]
                    MyPlaylistItem(
                        title = item.name,
                        onClick = { onPlaylistSelected(item.playlistId)
                                  onDismiss()},
                        dropDownMenuClick = {})

                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.dialog_cancel_text))
            }
        }
    )
}