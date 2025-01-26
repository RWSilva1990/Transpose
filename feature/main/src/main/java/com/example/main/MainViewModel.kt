package com.example.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.repository.SuggestionKeywordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val suggestionKeywordRepository: SuggestionKeywordRepository,
) : ViewModel() {

//    private val _permissionGranted = MutableStateFlow(false)
//    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()
//
//    init {
//        checkPermissions()
//    }
//
//    fun checkPermissions() {
//        _permissionGranted.value = PermissionUtils.checkPermissions(context)
//    }
//
//    fun setPermissionGranted(granted: Boolean) {
//        _permissionGranted.value = granted
//    }
//
//    fun requestPermissions(launcher: (Array<String>) -> Unit) {
//        PermissionUtils.requestPermissions(launcher)
//    }

    private val _suggestionKeywords: MutableStateFlow<List<String>> = MutableStateFlow(
        listOf()
    )
    val suggestionKeywords = _suggestionKeywords.asStateFlow()

    fun clearSuggestionKeywords() {
        _suggestionKeywords.value = arrayListOf()
    }

    fun getSuggestionKeyword(query: String) = viewModelScope.launch {
        suggestionKeywordRepository.getSuggestionKeywords(query)
            .onSuccess { list ->
                _suggestionKeywords.value = list
            }
            .onFailure {
                // 에러 처리
            }
    }

    private val _isShowingAddVideoToPlaylistDialog = MutableStateFlow(false)
    val isShowAddVideoToPlaylistDialog = _isShowingAddVideoToPlaylistDialog.asStateFlow()

    private val _myPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val myPlaylists = _myPlaylists.asStateFlow()

//    private val _selectedVideo = MutableStateFlow<NewPipeVideoData?>(null)
//    val selectedVideo = _selectedVideo.asStateFlow()

//    fun showAddToPlaylistDialog(video: NewPipeVideoData) {
//        getAllMyPlaylist()
//        _selectedVideo.value = video
//        _isShowingAddVideoToPlaylistDialog.value = true
//
//    }
//
//    fun dismissPlaylistDialog() {
//        _isShowingAddVideoToPlaylistDialog.value = false
//        _myPlaylists.value = emptyList()
//        _selectedVideo.value = null
//    }

//    private fun getAllMyPlaylist() = viewModelScope.launch {
//        try {
//            _myPlaylists.value = playlistDBRepository.getAllPlaylists()
//
//        } catch (e: Exception) {
//            Logger.d("getAllMyPlaylist $e")
//        }
//    }

//    fun addVideoToPlaylist(video: NewPipeVideoData, playlistId: Long) =
//        viewModelScope.launch(Dispatchers.IO) {
//            playlistDBRepository.addVideoToPlaylist(video, playlistId)
//        }

}