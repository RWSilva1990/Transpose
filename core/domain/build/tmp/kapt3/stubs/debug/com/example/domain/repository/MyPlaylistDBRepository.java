package com.example.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u00a6@\u00a2\u0006\u0002\u0010\bJ\u001e\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007H\u00a6@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000b\u001a\u00020\u00072\u0006\u0010\f\u001a\u00020\rH\u00a6@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a6@\u00a2\u0006\u0002\u0010\u0010J\u001e\u0010\u0011\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0004\u001a\u00020\u0005H\u00a6@\u00a2\u0006\u0002\u0010\u0012J\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014H\u00a6@\u00a2\u0006\u0002\u0010\u0016J\u001c\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00050\u00142\u0006\u0010\u0006\u001a\u00020\u0007H\u00a6@\u00a2\u0006\u0002\u0010\u0010\u00a8\u0006\u0018"}, d2 = {"Lcom/example/domain/repository/MyPlaylistDBRepository;", "", "addVideoToPlaylist", "", "video", "Lcom/example/domain/model/youtube/video/Video;", "playlistId", "", "(Lcom/example/domain/model/youtube/video/Video;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Lcom/example/domain/model/youtube/video_detail/VideoDetail;", "(Lcom/example/domain/model/youtube/video_detail/VideoDetail;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createPlaylist", "name", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deletePlaylist", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteVideoFromPlaylist", "(JLcom/example/domain/model/youtube/video/Video;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllPlaylists", "", "Lcom/example/domain/model/library/MyPlaylist;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getVideosForPlaylist", "domain_debug"})
public abstract interface MyPlaylistDBRepository {
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createPlaylist(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllPlaylists(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.domain.model.library.MyPlaylist>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deletePlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addVideoToPlaylist(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video video, long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addVideoToPlaylist(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video_detail.VideoDetail video, long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getVideosForPlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.domain.model.youtube.video.Video>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteVideoFromPlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video video, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}