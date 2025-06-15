package com.example.domain.repository

import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail


interface MyPlaylistDBRepository {
    suspend fun createPlaylist(name: String)
    suspend fun getAllPlaylists(): List<MyPlaylist>
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addVideoToPlaylist(video: Video, playlistId: Long)
    suspend fun addVideoToPlaylist(video: VideoDetail, playlistId: Long)
    suspend fun getVideosForPlaylist(playlistId: Long): List<Video>
    suspend fun deleteVideoFromPlaylist(playlistId: Long, video: Video)
}