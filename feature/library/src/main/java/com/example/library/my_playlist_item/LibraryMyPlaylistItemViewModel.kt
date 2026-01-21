package com.example.library.my_playlist_item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.playable.PlayableItem
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.media.manager.MediaPlaybackManager
import com.example.ui.common.UiState
import com.example.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryMyPlaylistItemViewModel @Inject constructor(
    private val myPlaylistDBRepository: MyPlaylistDBRepository,
    private val mediaPlaybackManager: MediaPlaybackManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playlistId = savedStateHandle.get<Long>("playlistId") ?: 0L

    val uiState: StateFlow<UiState<List<PlayableItem>>> =
        myPlaylistDBRepository.getPlayableItemsForPlaylist(playlistId)
            .map<List<PlayableItem>, UiState<List<PlayableItem>>> { items ->
                UiState.Success(items)
            }
            .catch { exception ->
                emit(UiState.Error(exception.message ?: "Unknown error"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )

    fun deleteItem(item: PlayableItem) = viewModelScope.launch {
        try {
            myPlaylistDBRepository.deletePlayableItemFromPlaylist(playlistId, item.id)
        } catch (e: Exception) {
            Logger.d("deleteItem failed: ${e.message}")
        }
    }

    fun playPlaylist(items: List<PlayableItem>, clickedIndex: Int) {
        mediaPlaybackManager.playPlaylistItems(playlistItems = items, clickedIndex)
    }
}