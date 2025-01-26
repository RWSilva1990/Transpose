package com.example.domain.repository

import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.model.youtube.search.SearchResult
import com.example.domain.model.youtube.video_detail.VideoDetailData

interface NewPipeRepository {
    // 검색
    suspend fun search(query: String): Result<List<SearchResult>>
    suspend fun loadMoreSearchResults(): Result<List<SearchResult>>
    fun canLoadMoreSearchResults(): Boolean

    // 플레이리스트
    suspend fun fetchPlaylistResult(playlistId: String): Result<Playlist>
    suspend fun fetchPlaylistItemsResult(playlistId: String): Result<List<PlaylistItem>>

    // 스트림 정보
    suspend fun fetchVideoDetail(videoId: String): Result<VideoDetailData>
}