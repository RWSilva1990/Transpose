package com.example.home.home_playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.constants.MusicCategoryConstants
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.repository.PlaylistRepository
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    fun retryNationalPlaylists() {
        fetchNationalPlaylists()
    }

    fun retryRecommendedPlaylists() {
        fetchRecommendedPlaylists()
    }

    fun retryTypedPlaylists() {
        fetchTypedPlaylists()
    }

    fun retryAllPlaylists() {
        fetchNationalPlaylists()
        fetchRecommendedPlaylists()
        fetchTypedPlaylists()
    }

    private fun fetchNationalPlaylists() = viewModelScope.launch {
        _nationalPlaylistDataState.value = UiState.Loading

        val nationPlaylistUrls = MusicCategoryConstants().nationalPlaylistUrls
        val playlists = coroutineScope {
            nationPlaylistUrls.map { playlistId ->
                async {
                    playlistRepository.fetchPlaylistResult(playlistId).getOrNull()
                }
            }.awaitAll().filterNotNull()
        }

        _nationalPlaylistDataState.value = if (playlists.isNotEmpty()) {
            UiState.Success(playlists)
        } else {
            UiState.Error(PLAYLIST_LOAD_ERROR)
        }
    }

    private fun fetchRecommendedPlaylists() = viewModelScope.launch {
        _recommendedPlaylistDataState.value = UiState.Loading

        val recommendedId = MusicCategoryConstants().recommendPlaylistChannelId
        _recommendedPlaylistDataState.value = fetchChannelPlaylistState(recommendedId)
    }

    private fun fetchTypedPlaylists() = viewModelScope.launch {
        _typedPlaylistDataState.value = UiState.Loading

        val typedPlaylistId = MusicCategoryConstants().typedPlaylistChannelId
        _typedPlaylistDataState.value = fetchChannelPlaylistState(typedPlaylistId)
    }

    private suspend fun fetchChannelPlaylistState(channelId: String): UiState<List<Playlist>> {
        val result = playlistRepository.fetchPlaylistWithChannelId(channelId)
        val playlists = result.getOrNull()
        return if (!playlists.isNullOrEmpty()) {
            UiState.Success(playlists)
        } else {
            UiState.Error(PLAYLIST_LOAD_ERROR)
        }
    }

    private companion object {
        const val PLAYLIST_LOAD_ERROR = "playlist_load_error"
    }
}
