package com.example.transpose.ui.screen.library.my_local_item

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.transpose.MainViewModel
import com.example.transpose.MediaViewModel
import com.example.transpose.navigation.viewmodel.NavigationViewModel
import com.example.transpose.ui.screen.library.my_local_item.item.LocalFileData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryMyLocalItemScreen(
    mainViewModel: MainViewModel,
    mediaViewModel: MediaViewModel,
    navigationViewModel: NavigationViewModel,
    libraryMyLocalItemViewModel: LibraryMyLocalItemViewModel,
    type: String?
) {
    val bottomSheetState by mainViewModel.bottomSheetState.collectAsState()

    val recoverableDeleteException by libraryMyLocalItemViewModel.recoverableDeleteEvent.collectAsState()

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
        val exception = recoverableDeleteException
        if (exception != null) {
            val intentSender = exception.userAction.actionIntent.intentSender
            launcher.launch(androidx.activity.result.IntentSenderRequest.Builder(intentSender).build())
        }
    }
    val audioFiles by libraryMyLocalItemViewModel.audioFiles.collectAsState()
    val videoFiles by libraryMyLocalItemViewModel.videoFiles.collectAsState()

    BackHandler(
        enabled = bottomSheetState == SheetValue.Expanded
    ) {
        mainViewModel.partialExpandBottomSheet()
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
                    items(audioFiles.size) { index ->
                        val item = audioFiles[index]
                        LocalFileData(item = item, onClick = {
                            mediaViewModel.onMediaItemClick(item)
                            mainViewModel.expandBottomSheet()
                        }, {
                            libraryMyLocalItemViewModel.deleteFile(item)
                        })

                    }
                }
            }

            "video" -> {
                LazyColumn {
                    items(videoFiles.size) { index ->
                        val item = videoFiles[index]
                        LocalFileData(item = item, onClick = {
                            mediaViewModel.onMediaItemClick(item)
                            mainViewModel.expandBottomSheet()
                        }, {
                            libraryMyLocalItemViewModel.deleteFile(item)
                        })

                    }
                }
            }
        }
    }


}