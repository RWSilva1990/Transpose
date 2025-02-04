package com.example.home.playlist_item

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.youtube.playlist.PlaylistData
import com.example.domain.model.youtube.playlist.PlaylistItemData
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.repository.NewPipeRepository
import com.example.media.manager.MediaPlaybackManager
import com.example.ui.common.PaginatedState
import com.example.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomePlaylistItemViewModel @Inject constructor(
    private val newPipeRepository: NewPipeRepository,
    private val mediaPlaybackManager: MediaPlaybackManager
) : ViewModel() {

    private val _playlistItemsState =
        MutableStateFlow<PaginatedState<PlaylistItemData>>(PaginatedState.Initial)
    val playlistItemsState = _playlistItemsState.asStateFlow()

    private val _playlistInfo = MutableStateFlow<PlaylistData?>(null)
    val playlistInfo = _playlistInfo.asStateFlow()

    fun initializePlaylistPager(playlistId: String) = viewModelScope.launch(Dispatchers.IO) {
        _playlistItemsState.value = PaginatedState.Loading
        try {
            val result = newPipeRepository.fetchPlaylistItemsResult(playlistId)
            if (result.isSuccess) {
                val items = result.getOrElse { emptyList() }
                _playlistItemsState.value = PaginatedState.Success(
                    items = items,
                    hasMore = newPipeRepository.canLoadMorePlaylistItems(),
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

    fun loadMorePlaylistItems() = viewModelScope.launch(Dispatchers.IO) {
        val currentState = _playlistItemsState.value
        if (currentState !is PaginatedState.Success || currentState.isLoadingMore) return@launch
        if (!currentState.hasMore) return@launch

        _playlistItemsState.value = currentState.copy(isLoadingMore = true)

        try {
            val result = newPipeRepository.loadMorePlaylistItems()
            if (result.isSuccess) {
                val newItems = result.getOrElse { emptyList() }
                _playlistItemsState.value = currentState.copy(
                    items = currentState.items + newItems,
                    hasMore = newPipeRepository.canLoadMorePlaylistItems(),
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


    fun onMediaClicked(
        item: BasicVideoData,
        playlistItems: List<BasicVideoData>,
        clickedIndex: Int
    ) {
        mediaPlaybackManager.onMediaItemClick(item, playlistItems, clickedIndex)
    }

}