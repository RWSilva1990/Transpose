package com.example.domain.repository

import com.example.domain.model.library.MyPlaylistItem
import com.example.domain.model.youtube.search.SearchResult
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.example.transpose.data.model.local_file.LocalFileData


interface MyPlaylistDBRepository {
    suspend fun createPlaylist(name: String): Long
    suspend fun getAllPlaylists(): List<MyPlaylistItem>
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addVideoToPlaylist(video: SearchResult.Video, playlistId: Long)
    suspend fun addVideoToPlaylist(video: VideoDetailData, playlistId: Long)
    suspend fun getVideosForPlaylist(playlistId: Long): List<LocalFileData>
    suspend fun deleteFile(localFile: LocalFileData)
}