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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class MyPlaylistDBRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val videoDao: VideoDao,
) : MyPlaylistDBRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _myPlaylists: StateFlow<List<MyPlaylist>> =
        playlistDao.getAllPlaylists()
            .map { playlistEntities ->
                MyPlaylistMapper.toMyPlaylistItems(playlistEntities)
            }
            .onEach { playlists ->
                Logger.d("getAllPlaylists: ${playlists.size} playlists fetched from DB")
            }
            .catch { exception ->
                Logger.e("Error fetching playlists", exception)
                emit(emptyList())
            }
            .stateIn(
                scope = repositoryScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    override fun getAllPlaylists(): Flow<List<MyPlaylist>> = _myPlaylists

    override fun getVideosForPlaylist(playlistId: Long): Flow<List<Video>> =
        videoDao.getVideosForPlaylist(playlistId)
            .map {
                MyPlaylistMapper.toVideos(it)
            }
            .catch {
                emit(emptyList())
            }

    override suspend fun createPlaylist(name: String) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
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


    override suspend fun deleteVideoFromPlaylist(playlistId: Long, video: Video) {
        try {
            playlistDao.deleteVideoFromPlaylist(playlistId, video.id)
        } catch (e: Exception) {
            Logger.d("deleteVideoFromPlaylist failed: ${e.message}")
        }
    }


}


