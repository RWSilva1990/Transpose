package com.example.library.my_playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryMyPlaylistViewModel @Inject constructor(
    private val myPlaylistDBRepository: MyPlaylistDBRepository,
    private val nowPlayingStateHolder: NowPlayingStateHolder
) : ViewModel() {

    val myPlaylists = myPlaylistDBRepository.getAllPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setCurrentPlaylistInfo(playlist: Playlist?) {
        nowPlayingStateHolder.setCurrentPlaylistInfo(playlist)
    }

    fun createMyPlaylist(name: String) = viewModelScope.launch {
        try {
            myPlaylistDBRepository.createPlaylist(name)
        } catch (e: Exception) {
            Logger.d("createMyPlaylist $e")
        }
    }

    fun deleteMyPlaylist(playlist: MyPlaylist) = viewModelScope.launch {
        try {
            myPlaylistDBRepository.deletePlaylist(playlist.playlistId)
        } catch (e: Exception) {
            Logger.d("deleteMyPlaylist $e")
        }
    }

}