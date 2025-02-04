package com.example.domain.repository

import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData


interface MyPlaylistDBRepository {
    suspend fun createPlaylist(name: String): Long
    suspend fun getAllPlaylists(): List<MyPlaylist>
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addVideoToPlaylist(video: BasicVideoData, playlistId: Long)
    suspend fun addVideoToPlaylist(video: VideoDetailData, playlistId: Long)
    suspend fun getVideosForPlaylist(playlistId: Long): List<BasicVideoData>
    suspend fun deleteVideoFromPlaylist(playlistId: Long, basicVideoData: BasicVideoData)
}