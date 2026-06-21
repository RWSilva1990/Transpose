package com.example.ui.screen.playlist_info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.model.youtube.video.Video
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.domain.repository.PlaylistRepository
import com.example.media.manager.MediaPlaybackManager
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.ui.common.PaginatedState
import com.example.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    private var loadedPlaylistId: String? = null
    private var initialLoadJob: Job? = null

    val currentPlaylistInfo = nowPlayingStateHolder.currentPlaylistInfo

    fun initializePlaylistPager(playlistId: String) {
        val currentState = playlistItemsState.value
        val isSamePlaylist = loadedPlaylistId == playlistId
        val isAlreadyResolved = currentState is PaginatedState.Success || currentState is PaginatedState.Loading

        if (isSamePlaylist && isAlreadyResolved) return

        Logger.i("PLAYLIST_ITEMS_INITIALIZE playlistId=$playlistId previousPlaylistId=$loadedPlaylistId")
        loadedPlaylistId = playlistId
        loadInitialPlaylistItems(playlistId)
    }

    fun retryPlaylistItems(playlistId: String) {
        loadedPlaylistId = playlistId
        loadInitialPlaylistItems(playlistId)
    }

    private fun loadInitialPlaylistItems(playlistId: String) {
        initialLoadJob?.cancel()
        initialLoadJob = viewModelScope.launch {
            _playlistItemsState.value = PaginatedState.Loading
            try {
                val result = playlistRepository.fetchPlaylistItemsResult(playlistId)
                if (result.isSuccess) {
                    val items = result.getOrElse { emptyList() }
                    val hasMore = playlistRepository.canLoadMorePlaylistItems()
                    Logger.i(
                        "PLAYLIST_ITEMS_INITIAL_RESULT playlistId=$playlistId " +
                            "count=${items.size} hasMore=$hasMore"
                    )

                    if (items.isEmpty()) {
                        Logger.e("PLAYLIST_ITEMS_INITIAL_EMPTY_FINAL playlistId=$playlistId hasMore=$hasMore")
                    }

                    _playlistItemsState.value = PaginatedState.Success(
                        items = items,
                        hasMore = hasMore,
                        isLoadingMore = false
                    )
                } else {
                    val exception = result.exceptionOrNull()
                    val message = exception?.message ?: "Unknown error occurred"
                    Logger.e("PLAYLIST_ITEMS_INITIAL_FAILED playlistId=$playlistId message=$message", exception)
                    _playlistItemsState.value = PaginatedState.Error(message)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Logger.e("PLAYLIST_ITEMS_INITIAL_EXCEPTION playlistId=$playlistId", e)
                _playlistItemsState.value = PaginatedState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadMorePlaylistItems() = viewModelScope.launch {
        val currentState = _playlistItemsState.value
        if (currentState !is PaginatedState.Success || currentState.isLoadingMore) return@launch
        if (!currentState.hasMore) return@launch

        _playlistItemsState.value = currentState.copy(isLoadingMore = true)

        try {
            val result = playlistRepository.loadMorePlaylistItems()
            if (result.isSuccess) {
                val newItems = result.getOrElse { emptyList() }
                if (newItems.isEmpty()) {
                    Logger.e(
                        "PLAYLIST_ITEMS_LOAD_MORE_EMPTY_FINAL " +
                            "currentCount=${currentState.items.size} hasMore=${playlistRepository.canLoadMorePlaylistItems()}"
                    )
                }
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

    val myPlaylists = myPlaylistDBRepository.getAllPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


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
