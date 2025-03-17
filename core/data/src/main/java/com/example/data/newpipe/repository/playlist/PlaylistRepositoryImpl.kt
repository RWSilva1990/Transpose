package com.example.data.newpipe.repository.playlist

import com.example.data.newpipe.extractor.playlist.PlaylistItemPager
import com.example.data.newpipe.extractor.playlist.PlaylistPager
import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.data.newpipe.repository.base.BaseNewPipeRepository
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.repository.PlaylistRepository
import javax.inject.Inject

class PlaylistRepositoryImpl @Inject constructor() : BaseNewPipeRepository(), PlaylistRepository {

    private var playlistPager: PlaylistPager? = null
    private var playlistItemPager: PlaylistItemPager? = null

    override suspend fun fetchPlaylistResult(playlistId: String): Result<Playlist> {
        return try {
            val linkHandler = getPlaylistHandler(playlistId)
            val playlistExtractor = youtubeService.getPlaylistExtractor(linkHandler)
            playlistExtractor.fetchPage()
            Result.success(InfoItemMapper.playlistExtractorToPlaylistData(playlistExtractor))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchPlaylistItemsResult(playlistId: String): Result<List<PlaylistItem>> {
        return try {
            val linkHandler = getPlaylistHandler(playlistId)
            val playlistExtractor = youtubeService.getPlaylistExtractor(linkHandler)
            playlistExtractor.fetchPage()
            val pager = PlaylistItemPager(youtubeService, playlistExtractor)
            playlistItemPager = pager
            Result.success(pager.getNextPage())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadMorePlaylistItems(): Result<List<PlaylistItem>> {
        val pager = playlistItemPager
            ?: return Result.failure(IllegalStateException("No PlaylistItemData initiated"))
        return try {
            Result.success(pager.getNextPage())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun canLoadMorePlaylistItems(): Boolean {
        return playlistItemPager?.isHasNextPage() == true
    }

    override suspend fun fetchPlaylistWithChannelId(channelId: String): Result<List<Playlist>?> {
        try {
            val channelLinkHandler = getChannelLinkHandler(channelId)
            val channelExtractor = getChannelExtractor(channelLinkHandler)
            channelExtractor.fetchPage()

            val playlistsTabLinkHandler = channelExtractor.tabs.find { it.contentFilters.contains("playlists") }

            if (playlistsTabLinkHandler != null) {
                val channelTabExtractor = getChannelTabExtractor(playlistsTabLinkHandler)
                val pager = PlaylistPager(youtubeService, channelTabExtractor)
                return Result.success(pager.getNextPage())
            }
            return Result.failure(Exception("No playlist in that channel Id"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}