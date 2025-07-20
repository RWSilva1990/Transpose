package com.example.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.database.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Query("SELECT * FROM videos WHERE playlistId = :playlistId")
    fun getVideosForPlaylist(playlistId: Long): Flow<List<VideoEntity>>

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteVideoById(videoId: String)
}