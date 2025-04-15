package com.example.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.data.local.database.entity.PlaylistEntity;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\u0007\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\u00052\u0006\u0010\u0010\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u0011\u00a8\u0006\u0012"}, d2 = {"Lcom/example/data/local/database/dao/PlaylistDao;", "", "deletePlaylist", "", "playlistId", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteVideoFromPlaylist", "videoId", "", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllPlaylists", "", "Lcom/example/data/local/database/entity/PlaylistEntity;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertPlaylist", "playlist", "(Lcom/example/data/local/database/entity/PlaylistEntity;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
@androidx.room.Dao()
public abstract interface PlaylistDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertPlaylist(@org.jetbrains.annotations.NotNull()
    com.example.data.local.database.entity.PlaylistEntity playlist, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM playlists")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllPlaylists(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.data.local.database.entity.PlaylistEntity>> $completion);
    
    @androidx.room.Query(value = "DELETE FROM playlists WHERE playlistId =:playlistId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deletePlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM videos WHERE playlistId = :playlistId AND id = :videoId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteVideoFromPlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    java.lang.String videoId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}