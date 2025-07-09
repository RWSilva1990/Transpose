package com.example.home.home_playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.constants.MusicCategoryConstants
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.repository.PlaylistRepository
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomePlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val nowPlayingStateHolder: NowPlayingStateHolder,
) : ViewModel() {

    private val _nationalPlaylistDataState =
        MutableStateFlow<UiState<List<Playlist>>>(UiState.Initial)
    val nationalPlaylistDataState: StateFlow<UiState<List<Playlist>>> =
        _nationalPlaylistDataState.asStateFlow()

    private val _recommendedPlaylistDataState =
        MutableStateFlow<UiState<List<Playlist>>>(UiState.Initial)
    val recommendedPlaylistDataState: StateFlow<UiState<List<Playlist>>> =
        _recommendedPlaylistDataState.asStateFlow()

    private val _typedPlaylistDataState = MutableStateFlow<UiState<List<Playlist>>>(UiState.Initial)
    val typedPlaylistDataState: StateFlow<UiState<List<Playlist>>> =
        _typedPlaylistDataState.asStateFlow()

    init {
        fetchNationalPlaylists()
        fetchRecommendedPlaylists()
        fetchTypedPlaylists()
    }

    fun setCurrentPlaylistInfo(playlist: Playlist) {
        nowPlayingStateHolder.setCurrentPlaylistInfo(playlist)
    }


    private fun fetchNationalPlaylists() = viewModelScope.launch {
        _nationalPlaylistDataState.value = UiState.Loading
        val currentList = mutableListOf<Playlist>()
        var hasError = false

        val nationPlaylistUrls = MusicCategoryConstants().nationalPlaylistUrls
        nationPlaylistUrls.forEach { playlistId ->
            val result = playlistRepository.fetchPlaylistResult(playlistId)
            when {
                result.isSuccess -> {
                    val playlist = result.getOrNull()
                    playlist?.let {
                        currentList.add(playlist)
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

    private fun fetchRecommendedPlaylists() = viewModelScope.launch {
        _recommendedPlaylistDataState.value = UiState.Loading

        val recommendedId = MusicCategoryConstants().recommendPlaylistChannelId
        val result = playlistRepository.fetchPlaylistWithChannelId(recommendedId)

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

    private fun fetchTypedPlaylists() = viewModelScope.launch {
        _typedPlaylistDataState.value = UiState.Loading

        val typedPlaylistId = MusicCategoryConstants().typedPlaylistChannelId
        val result = playlistRepository.fetchPlaylistWithChannelId(typedPlaylistId)

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