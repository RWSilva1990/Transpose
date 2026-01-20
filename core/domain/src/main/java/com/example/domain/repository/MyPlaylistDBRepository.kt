package com.example.domain.repository

import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.local_file.LocalFileData
import com.example.domain.model.playable.PlayableItem
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import kotlinx.coroutines.flow.Flow

interface MyPlaylistDBRepository {
    fun getAllPlaylists(): Flow<List<MyPlaylist>>
    fun getVideosForPlaylist(playlistId: Long): Flow<List<Video>>
    fun getPlayableItemsForPlaylist(playlistId: Long): Flow<List<PlayableItem>>
    suspend fun createPlaylist(name: String)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addVideoToPlaylist(video: Video, playlistId: Long)
    suspend fun addVideoToPlaylist(video: VideoDetail, playlistId: Long)
    suspend fun addLocalFileToPlaylist(localFile: LocalFileData, playlistId: Long)
    suspend fun addPlayableItemToPlaylist(item: PlayableItem, playlistId: Long)
    suspend fun deleteVideoFromPlaylist(playlistId: Long, video: Video)
    suspend fun deletePlayableItemFromPlaylist(playlistId: Long, itemId: String)
}