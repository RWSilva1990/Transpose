package com.example.library.my_playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryMyPlaylistViewModel @Inject constructor(
    private val myPlaylistDBRepository: MyPlaylistDBRepository
): ViewModel() {

    private val _myPlaylists = MutableStateFlow<List<MyPlaylist>>(emptyList())
    val myPlaylists = _myPlaylists.asStateFlow()

    init {
        getAllMyPlaylist()
    }

    fun createMyPlaylist(name: String) = viewModelScope.launch {
        try {
            myPlaylistDBRepository.createPlaylist(name)

        }catch (e: Exception){
            Logger.d("createMyPlaylist $e")

        }finally {
            getAllMyPlaylist()
        }
    }

    private fun getAllMyPlaylist() = viewModelScope.launch {
        try {
            _myPlaylists.value = myPlaylistDBRepository.getAllPlaylists()

        }catch (e: Exception){
            Logger.d("getAllMyPlaylist $e")
        }
    }

    fun deleteMyPlaylist(playlist: MyPlaylist) = viewModelScope.launch {
        try {
            myPlaylistDBRepository.deletePlaylist(playlist.playlistId)
            getAllMyPlaylist()
        }catch (e: Exception){
            Logger.d("deleteMyPlaylist $e")
        }
    }

}