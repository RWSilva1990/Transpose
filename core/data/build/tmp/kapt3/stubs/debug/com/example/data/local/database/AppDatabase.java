package com.example.data.local.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.example.data.local.database.dao.PlaylistDao;
import com.example.data.local.database.dao.VideoDao;
import com.example.data.local.database.entity.PlaylistEntity;
import com.example.data.local.database.entity.VideoEntity;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\u0007"}, d2 = {"Lcom/example/data/local/database/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "playlistDao", "Lcom/example/data/local/database/dao/PlaylistDao;", "videoDao", "Lcom/example/data/local/database/dao/VideoDao;", "data_debug"})
@androidx.room.Database(entities = {com.example.data.local.database.entity.PlaylistEntity.class, com.example.data.local.database.entity.VideoEntity.class}, version = 2)
@androidx.room.TypeConverters(value = {com.example.data.local.database.Converters.class})
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.data.local.database.dao.PlaylistDao playlistDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.data.local.database.dao.VideoDao videoDao();
}