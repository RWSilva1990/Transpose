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
import androidx.compose.runtime.rememberCoroutineScope

import com.example.library.my_local_item.item.LocalFileData
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryMyLocalItemScreen(
    bottomSheetState: SheetState,
    libraryMyLocalItemViewModel: LibraryMyLocalItemViewModel,
    type: String?,
    navigateToBack: () -> Unit
) {
    val recoverableDeleteException by libraryMyLocalItemViewModel.recoverableDeleteEvent.collectAsState()
    val coroutineScope = rememberCoroutineScope()
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

    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
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
                            libraryMyLocalItemViewModel.playLocalFiles(audioFiles, index)
                            coroutineScope.launch {
                                bottomSheetState.expand()
                            }
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
                            libraryMyLocalItemViewModel.playLocalFiles(videoFiles, index)
                            coroutineScope.launch {
                                bottomSheetState.expand()
                            }
                        }, {
                            libraryMyLocalItemViewModel.deleteFile(item)
                        })
                    }
                }
            }
        }
    }


}