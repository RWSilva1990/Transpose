package com.example.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.data.local.database.entity.PlaylistEntity

@Dao
interface PlaylistDao {
    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylists(): List<PlaylistEntity>


    @Query("DELETE FROM playlists WHERE playlistId =:playlistId")
    suspend fun deletePlaylist(playlistId: Long)
}