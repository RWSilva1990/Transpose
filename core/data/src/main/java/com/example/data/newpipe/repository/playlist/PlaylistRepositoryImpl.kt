package com.example.data.newpipe.repository.playlist

import com.example.data.di.IoDispatcher
import com.example.data.newpipe.extractor.playlist.PlaylistItemPager
import com.example.data.newpipe.extractor.playlist.PlaylistPager
import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.data.newpipe.repository.base.NewPipeManager
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.repository.PlaylistRepository
import com.example.util.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor(
    private val newPipeManager: NewPipeManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
): PlaylistRepository {

    private var playlistPager: PlaylistPager? = null
    private var playlistItemPager: PlaylistItemPager? = null

    override suspend fun fetchPlaylistResult(playlistId: String): Result<Playlist> {
        return withContext(ioDispatcher) {
            try {
                val linkHandler = newPipeManager.getPlaylistHandler(playlistId)
                val playlistExtractor = newPipeManager.youtubeService.getPlaylistExtractor(linkHandler)
                playlistExtractor.fetchPage()

                val playlist = InfoItemMapper.playlistExtractorToPlaylistData(playlistExtractor)
                Result.success(playlist)
            } catch (e: Exception) {
                Logger.e("Error fetching playlist for playlistId: $playlistId", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun fetchPlaylistItemsResult(playlistId: String): Result<List<PlaylistItem>> {
        return withContext(ioDispatcher) {
            try {
                val linkHandler = newPipeManager.getPlaylistHandler(playlistId)
                val playlistExtractor = newPipeManager.youtubeService.getPlaylistExtractor(linkHandler)
                playlistExtractor.fetchPage()

                val pager = PlaylistItemPager(newPipeManager.youtubeService, playlistExtractor)
                playlistItemPager = pager

                val items = pager.getNextPage()
                Result.success(items)
            } catch (e: Exception) {
                Logger.e("Error fetching playlist items for playlistId: $playlistId", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun loadMorePlaylistItems(): Result<List<PlaylistItem>> {
        return withContext(ioDispatcher) {
            try {
                val pager = playlistItemPager
                    ?: throw IllegalStateException("No PlaylistItemData initiated")

                val items = pager.getNextPage()
                Result.success(items)
            } catch (e: Exception) {
                Logger.e("Error loading more playlist items", e)
                Result.failure(e)
            }
        }
    }

    override fun canLoadMorePlaylistItems(): Boolean {
        return playlistItemPager?.isHasNextPage() == true
    }

    override suspend fun fetchPlaylistWithChannelId(channelId: String): Result<List<Playlist>?> {
        return withContext(ioDispatcher) {
            try {
                val channelLinkHandler = newPipeManager.getChannelLinkHandler(channelId)
                val channelExtractor = newPipeManager.getChannelExtractor(channelLinkHandler)
                channelExtractor.fetchPage()

                val playlistsTabLinkHandler = channelExtractor.tabs.find {
                    it.contentFilters.contains("playlists")
                }

                if (playlistsTabLinkHandler != null) {
                    val channelTabExtractor = newPipeManager.getChannelTabExtractor(playlistsTabLinkHandler)
                    val pager = PlaylistPager(newPipeManager.youtubeService, channelTabExtractor)

                    val playlists = pager.getNextPage()
                    Result.success(playlists)
                } else {
                    Logger.d("No playlist tab found for channelId: $channelId")
                    Result.failure(Exception("No playlist in that channel Id"))
                }
            } catch (e: Exception) {
                Logger.e("Error fetching playlists for channelId: $channelId", e)
                Result.failure(e)
            }
        }
    }
}