package com.example.library.my_playlist_item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.youtube.video.Video
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.media.manager.MediaPlaybackManager
import com.example.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryMyPlaylistItemViewModel @Inject constructor(
    private val myPlaylistDBRepository: MyPlaylistDBRepository,
    private val mediaPlaybackManager: MediaPlaybackManager
): ViewModel() {


    private val _myPlaylistItems = MutableStateFlow<List<Video>>(emptyList())
    val myPlaylistItems = _myPlaylistItems.asStateFlow()

    fun getVideosForPlaylist(playlistId: Long) = viewModelScope.launch {
        try {
            _myPlaylistItems.value = myPlaylistDBRepository.getVideosForPlaylist(playlistId)

        }catch (e: Exception){
            Logger.d("getVideosForPlaylist $e")
        }
    }

    fun deleteVideo(playlistId: Long, video: Video) = viewModelScope.launch {
        try {
            myPlaylistDBRepository.deleteVideoFromPlaylist(playlistId, video)
            getVideosForPlaylist(playlistId)
        }catch (e: Exception){
            Logger.d("deleteVideo")
        }
    }

    fun playPlaylist(clickedIndex: Int){
        mediaPlaybackManager.playPlaylist(playlistItems = myPlaylistItems.value, clickedIndex)
    }
}