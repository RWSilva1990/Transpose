package com.example.home.home_playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.constants.MusicCategoryConstants
import com.example.domain.model.youtube.playlist.PlaylistData
import com.example.domain.repository.NewPipeRepository
import com.example.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomePlaylistViewModel @Inject constructor(
    private val newPipeRepository: NewPipeRepository
) : ViewModel(){


    private val _nationalPlaylistDataState = MutableStateFlow<UiState<List<PlaylistData>>>(UiState.Initial)
    val nationalPlaylistDataState: StateFlow<UiState<List<PlaylistData>>> = _nationalPlaylistDataState.asStateFlow()

    private val _recommendedPlaylistDataState = MutableStateFlow<UiState<List<PlaylistData>>>(UiState.Initial)
    val recommendedPlaylistDataState: StateFlow<UiState<List<PlaylistData>>> = _recommendedPlaylistDataState.asStateFlow()

    private val _typedPlaylistDataState = MutableStateFlow<UiState<List<PlaylistData>>>(UiState.Initial)
    val typedPlaylistDataState: StateFlow<UiState<List<PlaylistData>>> = _typedPlaylistDataState.asStateFlow()


    fun fetchNationalPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        _nationalPlaylistDataState.value = UiState.Loading
        val currentList = mutableListOf<PlaylistData>()
        var hasError = false

        val nationPlaylistUrls = MusicCategoryConstants().nationalPlaylistUrls
        nationPlaylistUrls.forEach { playlistId ->
            val result = newPipeRepository.fetchPlaylistResult(playlistId)
            when {
                result.isSuccess -> {
                    val playlistData = result.getOrNull()
                    playlistData?.let {
                        currentList.add(playlistData)
                        _nationalPlaylistDataState.value = UiState.Success(currentList.toList())
                    }
                }
                result.isFailure -> {
                    hasError = true
                }
            }
        }

        _nationalPlaylistDataState.value = when {
            hasError && currentList.isEmpty() -> UiState.Error("Failed to fetch playlists")
            hasError -> UiState.Error("Some playlists failed to load")
            else -> UiState.Success(currentList)
        }
    }

    fun fetchRecommendedPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        _recommendedPlaylistDataState.value = UiState.Loading

        val recommendedId = MusicCategoryConstants().recommendPlaylistChannelId
        val result = newPipeRepository.fetchPlaylistWithChannelId(recommendedId)

        _recommendedPlaylistDataState.value = when {
            result.isSuccess -> {
                val contents = result.getOrNull()
                contents?.let { playlists ->
                    if (playlists.isNotEmpty()) UiState.Success(playlists)
                    else UiState.Error("No playlists found")
                } ?: UiState.Error("No content found")
            }
            result.isFailure -> UiState.Error("${result.exceptionOrNull()}")
            else -> UiState.Error("${result.exceptionOrNull()}")

        }
    }

    fun fetchTypedPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        _typedPlaylistDataState.value = UiState.Loading

        val typedPlaylistId = MusicCategoryConstants().typedPlaylistChannelId
        val result = newPipeRepository.fetchPlaylistWithChannelId(typedPlaylistId)

        _typedPlaylistDataState.value = when {
            result.isSuccess -> {
                val contents = result.getOrNull()
                contents?.let { playlists ->
                    if (playlists.isNotEmpty()) UiState.Success(playlists)
                    else UiState.Error("No playlists found")
                } ?: UiState.Error("No content found")
            }
            result.isFailure -> UiState.Error("${result.exceptionOrNull()}")
            else -> UiState.Error("${result.exceptionOrNull()}")

        }
    }

}