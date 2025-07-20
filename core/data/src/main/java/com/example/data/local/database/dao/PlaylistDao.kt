package com.example.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.data.local.database.entity.PlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>


    @Query("DELETE FROM playlists WHERE playlistId =:playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM videos WHERE playlistId = :playlistId AND id = :videoId")
    suspend fun deleteVideoFromPlaylist(playlistId: Long, videoId: String)

}