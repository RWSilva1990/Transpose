package com.example.transpose.ui.screen.library.my_local_item

import android.app.RecoverableSecurityException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transpose.data.model.local_file.LocalFileData
import com.example.transpose.data.repository.local_file.LocalFileRepositoryImpl
import com.example.transpose.utils.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryMyLocalItemViewModel @Inject constructor(
    private val localFileRepositoryImpl: LocalFileRepositoryImpl
) : ViewModel() {

    private val _audioFiles = MutableStateFlow<List<LocalFileData>>(emptyList())
    val audioFiles: StateFlow<List<LocalFileData>> = _audioFiles

    private val _videoFiles = MutableStateFlow<List<LocalFileData>>(emptyList())
    val videoFiles: StateFlow<List<LocalFileData>> = _videoFiles

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _recoverableDeleteEvent = MutableStateFlow<RecoverableSecurityException?>(null)
    val recoverableDeleteEvent: StateFlow<RecoverableSecurityException?> = _recoverableDeleteEvent


    private val _pendingDeleteFile = MutableStateFlow<LocalFileData?>(null)

    fun loadAudioFiles() = viewModelScope.launch(Dispatchers.IO) {
        localFileRepositoryImpl.getAudioFiles()
            .onSuccess { files ->
                _audioFiles.value = files
            }
            .onFailure { error ->
                _errorMessage.value = "오디오 파일 로딩 실패: ${error.message}"
            }
    }

    fun loadVideoFiles() = viewModelScope.launch(Dispatchers.IO) {
        localFileRepositoryImpl.getVideoFiles()
            .onSuccess { files ->
                Logger.d("${files}")
                _videoFiles.value = files
            }
            .onFailure { error ->
                Logger.d("${error}")
                _errorMessage.value = "비디오 파일 로딩 실패: ${error.message}"
            }

    }

    /**
     * 실제 파일 삭제 시도
     */
    fun deleteFile(file: LocalFileData) = viewModelScope.launch {
        // 어떤 파일을 삭제하려 하는지 기록
        _pendingDeleteFile.value = file

        localFileRepositoryImpl.deleteFile(file)
            .onSuccess { isDeleted ->
                if (isDeleted) {
                    // 삭제 성공 -> 목록 갱신
                    _audioFiles.value = _audioFiles.value.filter { it.id != file.id }
                    _videoFiles.value = _videoFiles.value.filter { it.id != file.id }
                } else {
                    _errorMessage.value = "파일을 삭제하지 못했습니다."
                }
            }
            .onFailure { e ->
                // 안드로이드 10+ 에서 외부 파일 삭제시 보안 예외 가능
                if (e is RecoverableSecurityException) {
                    _recoverableDeleteEvent.value = e
                } else {
                    _errorMessage.value = e.message
                }
            }
    }

    /**
     * 사용자가 시스템 다이얼로그에서 '허용'을 누른 뒤 재시도
     */
    fun retryDeleteAfterPermission() = viewModelScope.launch {
        // 이미 이벤트 사용 -> null로 초기화
        _recoverableDeleteEvent.value = null

        val file = _pendingDeleteFile.value ?: return@launch
        // 다시 삭제 시도
        deleteFile(file)
    }

    /**
     * 사용자가 거부했거나, 이벤트를 클리어해야 하는 경우
     */
    fun clearRecoverableDeleteEvent() {
        _recoverableDeleteEvent.value = null
    }

}
