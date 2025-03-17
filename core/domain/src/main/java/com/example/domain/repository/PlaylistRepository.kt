package com.example.domain.repository

import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.playlist.PlaylistItem

interface PlaylistRepository {
    suspend fun fetchPlaylistResult(playlistId: String): Result<Playlist>
    suspend fun fetchPlaylistItemsResult(playlistId: String): Result<List<PlaylistItem>>
    suspend fun loadMorePlaylistItems(): Result<List<PlaylistItem>>
    fun canLoadMorePlaylistItems(): Boolean
    suspend fun fetchPlaylistWithChannelId(channelId: String): Result<List<Playlist>?>
}