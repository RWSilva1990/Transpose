package com.example.data.repository

import com.example.data.local.database.dao.PlaylistDao
import com.example.data.local.database.dao.VideoDao
import com.example.data.local.database.entity.PlaylistEntity
import com.example.data.local.mapper.MyPlaylistMapper
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.example.domain.repository.MyPlaylistDBRepository
import javax.inject.Inject

class MyPlaylistDBRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val videoDao: VideoDao
) : MyPlaylistDBRepository {

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun getAllPlaylists(): List<MyPlaylist> {
        return MyPlaylistMapper.toMyPlaylistItem(playlistDao.getAllPlaylists())
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addVideoToPlaylist(video: BasicVideoData, playlistId: Long) {
        videoDao.insertVideo(MyPlaylistMapper.toVideoEntity(video, playlistId))
    }

    override suspend fun addVideoToPlaylist(video: VideoDetailData, playlistId: Long) {
        videoDao.insertVideo(MyPlaylistMapper.toVideoEntity(video, playlistId))
    }

    override suspend fun getVideosForPlaylist(playlistId: Long): List<BasicVideoData> {
        return videoDao.getVideosForPlaylist(playlistId)
            .map { MyPlaylistMapper.toBasicVideoData(it) }
    }

    override suspend fun deleteVideoFromPlaylist(playlistId: Long, basicVideoData: BasicVideoData) {
        playlistDao.deleteVideoFromPlaylist(playlistId, basicVideoData.id)
    }


}


