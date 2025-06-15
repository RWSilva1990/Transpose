package com.example.ui.screen.playlist_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.model.youtube.video.Video
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.domain.repository.PlaylistRepository
import com.example.media.manager.MediaPlaybackManager
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.ui.common.PaginatedState
import com.example.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistInfoViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val mediaPlaybackManager: MediaPlaybackManager,
    private val nowPlayingStateHolder: NowPlayingStateHolder,
    private val myPlaylistDBRepository: MyPlaylistDBRepository
) : ViewModel() {

    private val _playlistItemsState =
        MutableStateFlow<PaginatedState<PlaylistItem>>(PaginatedState.Initial)
    val playlistItemsState = _playlistItemsState.asStateFlow()

    val currentPlaylistInfo = nowPlayingStateHolder.currentPlaylistInfo

    fun initializePlaylistPager(playlistId: String) = viewModelScope.launch(Dispatchers.IO) {
        if (playlistItemsState.value == PaginatedState.Initial) {
            _playlistItemsState.value = PaginatedState.Loading
            try {
                val result = playlistRepository.fetchPlaylistItemsResult(playlistId)
                if (result.isSuccess) {
                    val items = result.getOrElse { emptyList() }
                    _playlistItemsState.value = PaginatedState.Success(
                        items = items,
                        hasMore = playlistRepository.canLoadMorePlaylistItems(),
                        isLoadingMore = false
                    )
                } else {
                    val exception = result.exceptionOrNull()
                    _playlistItemsState.value = PaginatedState.Error(
                        exception?.message ?: "Unknown error occurred"
                    )
                }
            } catch (e: Exception) {
                _playlistItemsState.value = PaginatedState.Error(e.toString())
            }
        }
    }

    fun loadMorePlaylistItems() = viewModelScope.launch(Dispatchers.IO) {
        val currentState = _playlistItemsState.value
        if (currentState !is PaginatedState.Success || currentState.isLoadingMore) return@launch
        if (!currentState.hasMore) return@launch

        _playlistItemsState.value = currentState.copy(isLoadingMore = true)

        try {
            val result = playlistRepository.loadMorePlaylistItems()
            if (result.isSuccess) {
                val newItems = result.getOrElse { emptyList() }
                _playlistItemsState.value = currentState.copy(
                    items = currentState.items + newItems,
                    hasMore = playlistRepository.canLoadMorePlaylistItems(),
                    isLoadingMore = false
                )
            } else {
                val exception = result.exceptionOrNull()
                _playlistItemsState.value = PaginatedState.Error(
                    exception?.message ?: "Unknown error occurred"
                )
            }
        } catch (e: Exception) {
            Logger.e("Error loading more search results", e)
            _playlistItemsState.value = PaginatedState.Error(e.message ?: "Unknown error")
        }
    }

    private val _myPlaylists = MutableStateFlow<List<MyPlaylist>>(emptyList())
    val myPlaylists = _myPlaylists.asStateFlow()

    fun getAllMyPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        _myPlaylists.value = myPlaylistDBRepository.getAllPlaylists()
    }

    fun addVideoToPlaylist(video: Video, playlistId: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            myPlaylistDBRepository.addVideoToPlaylist(video, playlistId)
        }


    fun playPlaylist(
        playlistItems: List<Video>,
        clickedIndex: Int
    ) {
        mediaPlaybackManager.playPlaylist(playlistItems, clickedIndex)
    }

}