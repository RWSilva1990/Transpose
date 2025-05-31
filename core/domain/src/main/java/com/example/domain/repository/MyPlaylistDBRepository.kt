package com.example.domain.repository

import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import kotlinx.coroutines.flow.StateFlow


interface MyPlaylistDBRepository {
    val myPlaylists: StateFlow<List<MyPlaylist>>
    val myPlaylistItems: StateFlow<List<Video>>
    suspend fun createPlaylist(name: String)
    suspend fun getAllPlaylists()
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addVideoToPlaylist(video: Video, playlistId: Long)
    suspend fun addVideoToPlaylist(video: VideoDetail, playlistId: Long)
    suspend fun getVideosForPlaylist(playlistId: Long)
    suspend fun deleteVideoFromPlaylist(playlistId: Long, video: Video)
}