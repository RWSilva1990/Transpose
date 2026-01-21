package com.example.library.my_local_item

import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.domain.model.local_file.LocalFileData
import com.example.library.R
import com.example.library.my_local_item.item.LocalFileData as LocalFileDataItem
import com.example.ui.components.dialog.AddItemToPlaylistDialog
import com.example.util.ToastUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryMyLocalItemScreen(
    bottomSheetState: SheetState,
    libraryMyLocalItemViewModel: LibraryMyLocalItemViewModel,
    type: String?,
    navigateToBack: () -> Unit,
    localSearchQuery: String = "",
    isLocalSearchActive: Boolean = false,
    onCloseLocalSearch: () -> Unit = {}
) {
    val recoverableDeleteException by libraryMyLocalItemViewModel.recoverableDeleteEvent.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var isShowingPlaylistDialog by remember { mutableStateOf(false) }
    var selectedLocalFile by remember { mutableStateOf<LocalFileData?>(null) }
    val myPlaylists by libraryMyLocalItemViewModel.myPlaylists.collectAsState()

    // "권한 다이얼로그"를 띄우는 런처
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사용자 "허용" → 다시 삭제 재시도
            libraryMyLocalItemViewModel.retryDeleteAfterPermission()
        } else {
            // 거부 or 취소
            libraryMyLocalItemViewModel.clearRecoverableDeleteEvent()
        }
    }

    // RecoverableSecurityException이 새로 들어오면 다이얼로그 표시
    LaunchedEffect(recoverableDeleteException) {
        // check if we have an exception AND device is API >= 29
        val exception = recoverableDeleteException
        if (exception != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intentSender = exception.userAction.actionIntent.intentSender
            launcher.launch(
                IntentSenderRequest.Builder(intentSender).build()
            )
        }
    }
    val audioFiles by libraryMyLocalItemViewModel.audioFiles.collectAsState()
    val videoFiles by libraryMyLocalItemViewModel.videoFiles.collectAsState()

    val filteredAudioFiles = remember(audioFiles, localSearchQuery) {
        if (localSearchQuery.isBlank()) audioFiles
        else audioFiles.filter { it.title.contains(localSearchQuery, ignoreCase = true) }
    }
    val filteredVideoFiles = remember(videoFiles, localSearchQuery) {
        if (localSearchQuery.isBlank()) videoFiles
        else videoFiles.filter { it.title.contains(localSearchQuery, ignoreCase = true) }
    }

    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }

    // Local Search가 활성화된 경우 뒤로가기로 닫기
    BackHandler(
        enabled = isLocalSearchActive && bottomSheetState.currentValue != SheetValue.Expanded
    ) {
        onCloseLocalSearch()
    }

    LaunchedEffect(key1 = true) {
        type?.let { type ->
            when (type) {
                "audio" -> libraryMyLocalItemViewModel.loadAudioFiles()
                "video" -> libraryMyLocalItemViewModel.loadVideoFiles()
            }
        }
    }
    type?.let { type ->
        when (type) {
            "audio" -> {
                LazyColumn {
                    items(filteredAudioFiles.size) { index ->
                        val item = filteredAudioFiles[index]
                        LocalFileDataItem(
                            item = item,
                            onClick = {
                                libraryMyLocalItemViewModel.playLocalFiles(filteredAudioFiles, index)
                                coroutineScope.launch {
                                    bottomSheetState.expand()
                                }
                            },
                            onDeleteClick = {
                                libraryMyLocalItemViewModel.deleteFile(item)
                            },
                            onAddToPlaylistClick = {
                                selectedLocalFile = item
                                isShowingPlaylistDialog = true
                            },
                            searchQuery = localSearchQuery
                        )
                    }
                }
            }

            "video" -> {
                LazyColumn {
                    items(filteredVideoFiles.size) { index ->
                        val item = filteredVideoFiles[index]
                        LocalFileDataItem(
                            item = item,
                            onClick = {
                                libraryMyLocalItemViewModel.playLocalFiles(filteredVideoFiles, index)
                                coroutineScope.launch {
                                    bottomSheetState.expand()
                                }
                            },
                            onDeleteClick = {
                                libraryMyLocalItemViewModel.deleteFile(item)
                            },
                            onAddToPlaylistClick = {
                                selectedLocalFile = item
                                isShowingPlaylistDialog = true
                            },
                            searchQuery = localSearchQuery
                        )
                    }
                }
            }
        }
    }

    if (isShowingPlaylistDialog) {
        AddItemToPlaylistDialog(
            playlists = myPlaylists,
            onDismiss = { isShowingPlaylistDialog = false },
            onPlaylistSelected = { playlistId ->
                selectedLocalFile?.let { localFile ->
                    libraryMyLocalItemViewModel.addLocalFileToPlaylist(
                        localFile = localFile,
                        playlistId = playlistId
                    )
                    ToastUtil.showShort(
                        context = context,
                        message = context.getString(R.string.notify_item_added_to_playlist)
                    )
                }
            }
        )
    }
}