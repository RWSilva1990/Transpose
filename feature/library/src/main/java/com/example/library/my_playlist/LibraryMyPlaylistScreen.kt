package com.example.library.my_playlist

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.library.R
import com.example.library.my_playlist.items.PlaylistItem
import com.example.library.my_playlist.items.AddPlaylistItem
import com.example.library.my_playlist.items.AudioStorageItem
import com.example.library.my_playlist.items.VideoStorageItem
import com.example.util.PermissionUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryMyPlaylistScreen(
    bottomSheetState: SheetState,
    libraryMyPlaylistViewModel: LibraryMyPlaylistViewModel,
    navigateToMyPlaylistItemScreen: (Long) -> Unit,
    navigateToSearchResultScreen: (String) -> Unit,
    navigateToLocalFileScreen: (String) -> Unit,
) {

    val myPlaylists by libraryMyPlaylistViewModel.myPlaylists.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var pendingRoute by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            pendingRoute?.let { route ->
                navigateToLocalFileScreen(route)
                pendingRoute = null
            }
        } else {
            if (PermissionUtils.shouldShowRationale(context)) {
                showRationaleDialog = true
            } else {
                showSettingsDialog = true
            }
        }
    }

    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }

    fun handleItemClick(route: String) {
        if (PermissionUtils.checkPermissions(context)) {
            navigateToLocalFileScreen(route)
        } else {
            pendingRoute = route
            PermissionUtils.requestPermissions(permissionLauncher::launch)
        }
    }

    var showPlaylistDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }

    LazyColumn() {
        item {
            AddPlaylistItem(onClick = { showPlaylistDialog = true })
        }

        item {
            AudioStorageItem(onClick = {
                handleItemClick("audio")
            })
        }

        item {
            VideoStorageItem(onClick = {
                handleItemClick("video")
            })
        }

        items(
            count = myPlaylists.size,
            key = { index -> myPlaylists[index].playlistId }
        ) { index ->
            val item = myPlaylists[index]
            PlaylistItem(
                title = item.name,
                onClick = {
                    navigateToMyPlaylistItemScreen(item.playlistId)
                    libraryMyPlaylistViewModel.setCurrentPlaylistInfo(null)
                },
                dropDownMenuClick = { libraryMyPlaylistViewModel.deleteMyPlaylist(item) }
            )
        }
    }

    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text(stringResource(R.string.create_playlist_dialog_title)) },
            text = {
                TextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text(stringResource(R.string.playlist_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            libraryMyPlaylistViewModel.createMyPlaylist(playlistName)
                            showPlaylistDialog = false
                            playlistName = ""
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPlaylistDialog = false
                        playlistName = ""
                    }
                ) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text(stringResource(R.string.permission_required_title)) },
            text = { Text(stringResource(R.string.storage_permission_message)) },
            confirmButton = {
                Button(onClick = {
                    showRationaleDialog = false
                    PermissionUtils.requestPermissions(permissionLauncher::launch)
                }) {
                    Text(stringResource(R.string.request_permission_button))
                }
            },
            dismissButton = {
                Button(onClick = { showRationaleDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(stringResource(R.string.permission_required_title)) },
            text = { Text(stringResource(R.string.settings_permission_message)) },
            confirmButton = {
                Button(onClick = {
                    showSettingsDialog = false
                    PermissionUtils.openAppSettings(context)
                }) {
                    Text(stringResource(R.string.go_to_settings_button))
                }
            },
            dismissButton = {
                Button(onClick = { showSettingsDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}


