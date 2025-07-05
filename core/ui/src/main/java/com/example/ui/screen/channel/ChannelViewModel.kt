package com.example.ui.screen.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.youtube.channel.ChannelDetail
import com.example.domain.model.youtube.channel.ChannelTabResult
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.video.Video
import com.example.domain.repository.ChannelRepository
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.media.manager.MediaPlaybackManager
import com.example.media.state_holder.NowPlayingStateHolder
import com.example.ui.common.PaginatedState
import com.example.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val mediaPlaybackManager: MediaPlaybackManager,
    private val nowPlayingStateHolder: NowPlayingStateHolder,
    private val myPlaylistDBRepository: MyPlaylistDBRepository
) : ViewModel() {

    private val _channelDetail = MutableStateFlow<ChannelDetail?>(null)
    val channelDetail = _channelDetail.asStateFlow()

    private val _channelTabVideosState =
        MutableStateFlow<PaginatedState<ChannelTabResult>>(PaginatedState.Initial)
    val channelTabVideos = _channelTabVideosState.asStateFlow()

    private val _channelTabShortsState =
        MutableStateFlow<PaginatedState<ChannelTabResult>>(PaginatedState.Initial)
    val channelTabShorts = _channelTabShortsState.asStateFlow()

    private val _channelTabPlaylistsState =
        MutableStateFlow<PaginatedState<ChannelTabResult>>(PaginatedState.Initial)
    val channelTabPlaylists = _channelTabPlaylistsState.asStateFlow()

    private val _isChannelDetailDataLoading = MutableStateFlow(false)
    val isChannelDetailDataLoading = _isChannelDetailDataLoading.asStateFlow()


    fun loadChannelDetail(channelId: String) = viewModelScope.launch(Dispatchers.IO) {
        if (_channelDetail.value != null) {
            return@launch
        }
        _isChannelDetailDataLoading.value = true
        try {
            val result = channelRepository.fetchChannelDetail(channelId)
            result.onSuccess { detail ->
                _channelDetail.value = detail
                detail.tabs.firstOrNull()?.let { tab ->
                    loadTabContent(channelId, tab.contentType)
                }
            }
        } catch (e: Exception) {
        } finally {
            _isChannelDetailDataLoading.value = false
        }
    }

    fun onTabChanged(channelId: String, contentType: String?) = viewModelScope.launch(Dispatchers.IO) {
        if (contentType == null) return@launch

        Logger.d("탭 전환: $contentType")

        updateHasMoreState(contentType)

        loadTabContent(channelId, contentType)
    }

    private fun updateHasMoreState(contentType: String) {
        val hasMore = channelRepository.canLoadMoreChannelTabContent(contentType)
        Logger.d("탭 $contentType hasMore 상태 업데이트: $hasMore")

        when (contentType) {
            VIDEOS -> {
                val currentState = _channelTabVideosState.value
                if (currentState is PaginatedState.Success) {
                    _channelTabVideosState.value = currentState.copy(hasMore = hasMore)
                    Logger.d("Videos 탭의 hasMore 상태가 $hasMore 로 업데이트되었습니다.")
                }
            }
            SHORTS -> {
                val currentState = _channelTabShortsState.value
                if (currentState is PaginatedState.Success) {
                    _channelTabShortsState.value = currentState.copy(hasMore = hasMore)
                    Logger.d("Shorts 탭의 hasMore 상태가 $hasMore 로 업데이트되었습니다.")
                }
            }
            PLAYLISTS -> {
                val currentState = _channelTabPlaylistsState.value
                if (currentState is PaginatedState.Success) {
                    _channelTabPlaylistsState.value = currentState.copy(hasMore = hasMore)
                    Logger.d("Playlists 탭의 hasMore 상태가 $hasMore 로 업데이트되었습니다.")
                }
            }
        }
    }

    fun loadTabContent(channelId: String, contentType: String?) =
        viewModelScope.launch(Dispatchers.IO) {
            if (contentType == null) {
                Logger.d("loadTabContent - contentType이 null입니다")
                return@launch
            }
            Logger.d("Loading tab content for $contentType")

            val targetStateFlow = when (contentType) {
                VIDEOS -> _channelTabVideosState
                SHORTS -> _channelTabShortsState
                PLAYLISTS -> _channelTabPlaylistsState
                else -> null
            }

            if (targetStateFlow == null && contentType != HOME && contentType != LIVESTREAMS) {
                return@launch
            }

            if (targetStateFlow != null) {
                val currentState = targetStateFlow.value
                if (currentState is PaginatedState.Success && currentState.items.isNotEmpty()) {
                    Logger.d("탭 $contentType 에 이미 콘텐츠가 있습니다. hasMore 상태만 업데이트합니다.")
                    updateHasMoreState(contentType)
                    return@launch
                }
            }

            targetStateFlow?.value = PaginatedState.Loading

            try {
                val result = channelRepository.fetchChannelTabContent(channelId, contentType)

                result.onSuccess { content ->
                    when (contentType) {
                        VIDEOS -> _channelTabVideosState.value = PaginatedState.Success(
                            items = content,
                            hasMore = channelRepository.canLoadMoreChannelTabContent(contentType)
                        )

                        SHORTS -> _channelTabShortsState.value = PaginatedState.Success(
                            items = content,
                            hasMore = channelRepository.canLoadMoreChannelTabContent(contentType)
                        )

                        PLAYLISTS -> _channelTabPlaylistsState.value = PaginatedState.Success(
                            items = content,
                            hasMore = channelRepository.canLoadMoreChannelTabContent(contentType)
                        )

                        HOME, LIVESTREAMS -> {
                        }
                    }

                }.onFailure { error ->
                    val errorMessage = error.message ?: "Unknown error occurred"
                    when (contentType) {
                        VIDEOS -> _channelTabVideosState.value = PaginatedState.Error(errorMessage)
                        SHORTS -> _channelTabShortsState.value = PaginatedState.Error(errorMessage)
                        PLAYLISTS -> _channelTabPlaylistsState.value =
                            PaginatedState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error occurred"
                targetStateFlow?.value = PaginatedState.Error(errorMessage)
            }
        }

    fun loadMoreContent(channelId: String, contentType: String?) = viewModelScope.launch(Dispatchers.IO) {
        if (contentType == null) {
            Logger.d("loadMoreContent - contentType이 null입니다")
            return@launch
        }

        Logger.d("Loading more content for $contentType")
        val currentState = when (contentType) {
            VIDEOS -> _channelTabVideosState.value
            SHORTS -> _channelTabShortsState.value
            PLAYLISTS -> _channelTabPlaylistsState.value
            else -> {
                Logger.d("지원되지 않는 contentType: $contentType")
                return@launch
            }
        }

        val canLoadMore = channelRepository.canLoadMoreChannelTabContent(contentType)
        Logger.d("currentState is Success: ${currentState is PaginatedState.Success}")
        Logger.d("isLoadingMore: ${if (currentState is PaginatedState.Success) currentState.isLoadingMore else "N/A"}")
        Logger.d("hasMore: ${if (currentState is PaginatedState.Success) currentState.hasMore else "N/A"}")
        Logger.d("canLoadMoreChannelTabContent: $canLoadMore")

        if (currentState !is PaginatedState.Success ||
            currentState.isLoadingMore ||
            !currentState.hasMore ||
            !canLoadMore
        ) {
            Logger.d("로드 불가: Success=${currentState is PaginatedState.Success}, isLoadingMore=${if (currentState is PaginatedState.Success) currentState.isLoadingMore else "N/A"}, hasMore=${if (currentState is PaginatedState.Success) currentState.hasMore else "N/A"}, canLoadMore=$canLoadMore")
            return@launch
        }

        when (contentType) {
            VIDEOS -> _channelTabVideosState.value = currentState.copy(isLoadingMore = true)
            SHORTS -> _channelTabShortsState.value = currentState.copy(isLoadingMore = true)
            PLAYLISTS -> _channelTabPlaylistsState.value = currentState.copy(isLoadingMore = true)
        }

        try {
            val result = channelRepository.loadMoreChannelTabContent(contentType)

            result.onSuccess { additionalContent ->
                Logger.d("Successfully loaded more content for $contentType")
                when (contentType) {
                    VIDEOS -> {
                        val currentItems =
                            (_channelTabVideosState.value as PaginatedState.Success).items
                        _channelTabVideosState.value = PaginatedState.Success(
                            items = currentItems + additionalContent,
                            hasMore = channelRepository.canLoadMoreChannelTabContent(contentType),
                            isLoadingMore = false
                        )
                    }

                    SHORTS -> {
                        val currentItems =
                            (_channelTabShortsState.value as PaginatedState.Success).items
                        _channelTabShortsState.value = PaginatedState.Success(
                            items = currentItems + additionalContent,
                            hasMore = channelRepository.canLoadMoreChannelTabContent(contentType),
                            isLoadingMore = false
                        )
                    }

                    PLAYLISTS -> {
                        val currentItems =
                            (_channelTabPlaylistsState.value as PaginatedState.Success).items
                        _channelTabPlaylistsState.value = PaginatedState.Success(
                            items = currentItems + additionalContent,
                            hasMore = channelRepository.canLoadMoreChannelTabContent(contentType),
                            isLoadingMore = false
                        )
                    }
                }

            }.onFailure { error ->
                val errorMessage = error.message ?: "Failed to load more content"
                Logger.d("Failed to load more content for $contentType: $errorMessage")
                when (contentType) {
                    VIDEOS -> {
                        val currentItems =
                            (_channelTabVideosState.value as PaginatedState.Success).items
                        _channelTabVideosState.value = PaginatedState.Success(
                            items = currentItems,
                            hasMore = true,
                            isLoadingMore = false,
                            loadMoreError = errorMessage
                        )
                    }

                    SHORTS -> {
                        val currentItems =
                            (_channelTabShortsState.value as PaginatedState.Success).items
                        _channelTabShortsState.value = PaginatedState.Success(
                            items = currentItems,
                            hasMore = true,
                            isLoadingMore = false,
                            loadMoreError = errorMessage
                        )
                    }

                    PLAYLISTS -> {
                        val currentItems =
                            (_channelTabPlaylistsState.value as PaginatedState.Success).items
                        _channelTabPlaylistsState.value = PaginatedState.Success(
                            items = currentItems,
                            hasMore = true,
                            isLoadingMore = false,
                            loadMoreError = errorMessage
                        )
                    }
                }
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error occurred"
            Logger.d("Failed to load more content for $contentType: $errorMessage")
            when (contentType) {
                VIDEOS -> {
                    val currentItems =
                        (_channelTabVideosState.value as? PaginatedState.Success)?.items
                            ?: emptyList()
                    _channelTabVideosState.value = PaginatedState.Success(
                        items = currentItems,
                        hasMore = true,
                        isLoadingMore = false,
                        loadMoreError = errorMessage
                    )
                }

                SHORTS -> {
                    val currentItems =
                        (_channelTabShortsState.value as? PaginatedState.Success)?.items
                            ?: emptyList()
                    _channelTabShortsState.value = PaginatedState.Success(
                        items = currentItems,
                        hasMore = true,
                        isLoadingMore = false,
                        loadMoreError = errorMessage
                    )
                }

                PLAYLISTS -> {
                    val currentItems =
                        (_channelTabPlaylistsState.value as? PaginatedState.Success)?.items
                            ?: emptyList()
                    _channelTabPlaylistsState.value = PaginatedState.Success(
                        items = currentItems,
                        hasMore = true,
                        isLoadingMore = false,
                        loadMoreError = errorMessage
                    )
                }
            }
        }
    }

    fun setPlaylistInfo(playlist: Playlist){
        nowPlayingStateHolder.setCurrentPlaylistInfo(playlist)
    }

    fun playSingleVideo(video: Video) {
        mediaPlaybackManager.playSingleVideo(video)
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

    companion object ContentType {
        const val VIDEOS = "videos"
        const val SHORTS = "shorts"
        const val PLAYLISTS = "playlists"
        const val LIVESTREAMS = "livestreams"
        const val HOME = "home"
        const val COMMUNITY = "community"
    }
}



