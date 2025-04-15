package com.example.data.local.repository;

import com.example.data.local.database.dao.PlaylistDao;
import com.example.data.local.database.dao.VideoDao;
import com.example.data.local.database.entity.PlaylistEntity;
import com.example.data.local.mapper.MyPlaylistMapper;
import com.example.domain.model.library.MyPlaylist;
import com.example.domain.model.youtube.video.Video;
import com.example.domain.model.youtube.video_detail.VideoDetail;
import com.example.domain.repository.MyPlaylistDBRepository;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\u000fJ\u0016\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0011\u001a\u00020\u0012H\u0096@\u00a2\u0006\u0002\u0010\u0013J\u0016\u0010\u0014\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\u0015J\u001e\u0010\u0016\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\t\u001a\u00020\nH\u0096@\u00a2\u0006\u0002\u0010\u0017J\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u001a0\u0019H\u0096@\u00a2\u0006\u0002\u0010\u001bJ\u001c\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\n0\u00192\u0006\u0010\u000b\u001a\u00020\fH\u0096@\u00a2\u0006\u0002\u0010\u0015R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/example/data/local/repository/MyPlaylistDBRepositoryImpl;", "Lcom/example/domain/repository/MyPlaylistDBRepository;", "playlistDao", "Lcom/example/data/local/database/dao/PlaylistDao;", "videoDao", "Lcom/example/data/local/database/dao/VideoDao;", "(Lcom/example/data/local/database/dao/PlaylistDao;Lcom/example/data/local/database/dao/VideoDao;)V", "addVideoToPlaylist", "", "video", "Lcom/example/domain/model/youtube/video/Video;", "playlistId", "", "(Lcom/example/domain/model/youtube/video/Video;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Lcom/example/domain/model/youtube/video_detail/VideoDetail;", "(Lcom/example/domain/model/youtube/video_detail/VideoDetail;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createPlaylist", "name", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deletePlaylist", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteVideoFromPlaylist", "(JLcom/example/domain/model/youtube/video/Video;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllPlaylists", "", "Lcom/example/domain/model/library/MyPlaylist;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getVideosForPlaylist", "data_debug"})
public final class MyPlaylistDBRepositoryImpl implements com.example.domain.repository.MyPlaylistDBRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.data.local.database.dao.PlaylistDao playlistDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.data.local.database.dao.VideoDao videoDao = null;
    
    @javax.inject.Inject()
    public MyPlaylistDBRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.example.data.local.database.dao.PlaylistDao playlistDao, @org.jetbrains.annotations.NotNull()
    com.example.data.local.database.dao.VideoDao videoDao) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object createPlaylist(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getAllPlaylists(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.domain.model.library.MyPlaylist>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deletePlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object addVideoToPlaylist(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video video, long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object addVideoToPlaylist(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video_detail.VideoDetail video, long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getVideosForPlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.domain.model.youtube.video.Video>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object deleteVideoFromPlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video video, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}