package com.example.home.home_playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.constants.MusicCategoryConstants
import com.example.domain.model.youtube.playlist.Playlist
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


    private val _nationalPlaylistState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Initial)
    val nationalPlaylistState: StateFlow<UiState<List<Playlist>>> = _nationalPlaylistState.asStateFlow()

    private val _recommendedPlaylistState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Initial)
    val recommendedPlaylistState: StateFlow<UiState<List<Playlist>>> = _recommendedPlaylistState.asStateFlow()

    private val _typedPlaylistState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Initial)
    val typedPlaylistState: StateFlow<UiState<List<Playlist>>> = _typedPlaylistState.asStateFlow()


    fun fetchNationalPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        _nationalPlaylistState.value = UiState.Loading
        val currentList = mutableListOf<Playlist>()
        var hasError = false

        val nationPlaylistUrls = MusicCategoryConstants().nationalPlaylistUrls
        nationPlaylistUrls.forEach { playlistId ->
            val result = newPipeRepository.fetchPlaylistResult(playlistId)
            when {
                result.isSuccess -> {
                    val playlistData = result.getOrNull()
                    playlistData?.let {
                        currentList.add(playlistData)
                        _nationalPlaylistState.value = UiState.Success(currentList.toList())
                    }
                }
                result.isFailure -> {
                    hasError = true
                }
            }
        }

        _nationalPlaylistState.value = when {
            hasError && currentList.isEmpty() -> UiState.Error("Failed to fetch playlists")
            hasError -> UiState.Error("Some playlists failed to load")
            else -> UiState.Success(currentList)
        }
    }

    fun fetchRecommendedPlaylists() = viewModelScope.launch(Dispatchers.IO) {
//        _recommendedPlaylistState.value = UiState.Loading
//
//        val recommendedId = MusicCategoryConstants().recommendPlaylistChannelId
//        val result = newPipeRepository.fetchPlaylistWithChannelId(recommendedId)
//
//        _recommendedPlaylistState.value = when {
//            result.isSuccess -> {
//                val contents = result.getOrNull()
//                contents?.let { contentList ->
//                    val playlists = contentList.filterIsInstance<NewPipePlaylistData>()
//                    if (playlists.isNotEmpty()) UiState.Success(playlists)
//                    else UiState.Error("No playlists found")
//                } ?: UiState.Error("No content found")
//            }
//            result.isFailure -> UiState.Error("${result.exceptionOrNull()}")
//            else -> UiState.Error("${result.exceptionOrNull()}")
//
//        }
    }

    fun fetchTypedPlaylists() = viewModelScope.launch(Dispatchers.IO) {
//        _typedPlaylistState.value = UiState.Loading
//
//        val typedPlaylistId = MusicCategoryRepository().typedPlaylistChannelId
//        val result = newPipeRepository.fetchPlaylistWithChannelId(typedPlaylistId)
//
//        _typedPlaylistState.value = when {
//            result.isSuccess -> {
//                val contents = result.getOrNull()
//                contents?.let { contentList ->
//                    val playlists = contentList.filterIsInstance<NewPipePlaylistData>()
//                    if (playlists.isNotEmpty()) UiState.Success(playlists)
//                    else UiState.Error("No playlists found")
//                } ?: UiState.Error("No content found")
//            }
//            result.isFailure -> UiState.Error("${result.exceptionOrNull()}")
//            else -> UiState.Error("${result.exceptionOrNull()}")
//
//        }
    }

}