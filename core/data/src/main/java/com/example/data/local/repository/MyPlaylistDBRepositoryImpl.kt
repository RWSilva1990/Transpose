package com.example.data.local.repository

import com.example.data.local.database.dao.PlaylistDao
import com.example.data.local.database.dao.VideoDao
import com.example.data.local.database.entity.PlaylistEntity
import com.example.data.local.mapper.MyPlaylistMapper
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.util.Logger
import javax.inject.Inject

class MyPlaylistDBRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val videoDao: VideoDao
) : MyPlaylistDBRepository {

    override suspend fun createPlaylist(name: String) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun getAllPlaylists(): List<MyPlaylist> {
        return MyPlaylistMapper.toMyPlaylistItem(playlistDao.getAllPlaylists())
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        try {
            playlistDao.deletePlaylist(playlistId)
        } catch (e: Exception) {
            Logger.d("deleteVideosForPlaylist failed: ${e.message}")
        }
    }

    override suspend fun addVideoToPlaylist(video: Video, playlistId: Long) {
        try {
            videoDao.insertVideo(MyPlaylistMapper.toVideoEntity(video, playlistId))
        } catch (e: Exception) {
            Logger.d("addVideoToPlaylist failed: ${e.message}")
        }
    }

    override suspend fun addVideoToPlaylist(video: VideoDetail, playlistId: Long) {
        try {
            videoDao.insertVideo(MyPlaylistMapper.toVideoEntity(video, playlistId))
        } catch (e: Exception) {
            Logger.d("addVideoToPlaylist failed: ${e.message}")
        }
    }

    override suspend fun getVideosForPlaylist(playlistId: Long): List<Video> {
        return videoDao.getVideosForPlaylist(playlistId)
            .map { MyPlaylistMapper.toVideoData(it) }
    }

    override suspend fun deleteVideoFromPlaylist(playlistId: Long, video: Video) {
        try {
            playlistDao.deleteVideoFromPlaylist(playlistId, video.id)
        } catch (e: Exception) {
            Logger.d("deleteVideoFromPlaylist failed: ${e.message}")
        }
    }


}


