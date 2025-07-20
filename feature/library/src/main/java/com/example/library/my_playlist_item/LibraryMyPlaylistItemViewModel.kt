package com.example.library.my_playlist_item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.youtube.video.Video
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

    val uiState: StateFlow<UiState<List<Video>>> =
        myPlaylistDBRepository.getVideosForPlaylist(playlistId)
            .map<List<Video>, UiState<List<Video>>> { videos ->
                UiState.Success(videos)
            }
            .catch { exception ->
                emit(UiState.Error(exception.message ?: "Unknown error"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )

    fun deleteVideo(video: Video) = viewModelScope.launch {
        try {
            myPlaylistDBRepository.deleteVideoFromPlaylist(playlistId, video)
        } catch (e: Exception) {
            Logger.d("deleteVideo")
        }
    }

    fun playPlaylist(myPlaylistItems: List<Video>, clickedIndex: Int) {
        mediaPlaybackManager.playPlaylist(playlistItems = myPlaylistItems, clickedIndex)
    }
}