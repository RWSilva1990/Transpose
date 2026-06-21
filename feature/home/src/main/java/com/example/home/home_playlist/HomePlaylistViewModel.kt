package com.example.home.home_playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.constants.MusicCategoryConstants
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.repository.PlaylistRepository
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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

        val nationPlaylistUrls = MusicCategoryConstants().nationalPlaylistUrls.toList()
        val playlists = fetchPlaylistsInBatches(nationPlaylistUrls) { partialPlaylists ->
            if (partialPlaylists.isNotEmpty()) {
                _nationalPlaylistDataState.value = UiState.Success(partialPlaylists)
            }
        }

        _nationalPlaylistDataState.value = if (playlists.isNotEmpty()) {
            UiState.Success(playlists)
        } else {
            UiState.Error(PLAYLIST_LOAD_ERROR)
        }
    }

    private suspend fun fetchPlaylistsInBatches(
        playlistIds: List<String>,
        onPartialResult: (List<Playlist>) -> Unit = {}
    ): List<Playlist> {
        if (playlistIds.isEmpty()) return emptyList()

        val playlistsByIndex = sortedMapOf<Int, Playlist>()
        playlistIds.withIndex().chunked(PLAYLIST_FETCH_BATCH_SIZE).forEach { batch ->
            coroutineScope {
                val resultChannel = Channel<Pair<Int, Playlist?>>(capacity = batch.size)
                batch.forEach { indexedPlaylist ->
                    launch {
                        val playlist = playlistRepository
                            .fetchPlaylistResult(indexedPlaylist.value)
                            .getOrNull()
                        resultChannel.send(indexedPlaylist.index to playlist)
                    }
                }
                repeat(batch.size) {
                    val (index, playlist) = resultChannel.receive()
                    if (playlist != null) {
                        playlistsByIndex[index] = playlist
                        onPartialResult(playlistsByIndex.values.toList())
                    }
                }
                resultChannel.close()
            }
        }
        return playlistsByIndex.values.toList()
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
        const val PLAYLIST_FETCH_BATCH_SIZE = 2
    }
}
