package com.example.data.local.mapper;

import com.example.data.local.database.entity.PlaylistEntity;
import com.example.data.local.database.entity.VideoEntity;
import com.example.domain.model.library.MyPlaylist;
import com.example.domain.model.youtube.video.Video;
import com.example.domain.model.youtube.video_detail.VideoDetail;
import org.schabi.newpipe.extractor.InfoItem.InfoType;
import org.schabi.newpipe.extractor.stream.StreamType;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u001a\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\b2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\bJ\u0016\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u000fJ\u0016\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u00102\u0006\u0010\u000e\u001a\u00020\u000f\u00a8\u0006\u0011"}, d2 = {"Lcom/example/data/local/mapper/MyPlaylistMapper;", "", "()V", "toBasicVideoData", "Lcom/example/domain/model/youtube/video/Video;", "videoEntity", "Lcom/example/data/local/database/entity/VideoEntity;", "toMyPlaylistItem", "", "Lcom/example/domain/model/library/MyPlaylist;", "playlistEntities", "Lcom/example/data/local/database/entity/PlaylistEntity;", "toVideoEntity", "video", "playlistId", "", "Lcom/example/domain/model/youtube/video_detail/VideoDetail;", "data_debug"})
public final class MyPlaylistMapper {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.data.local.mapper.MyPlaylistMapper INSTANCE = null;
    
    private MyPlaylistMapper() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.domain.model.library.MyPlaylist> toMyPlaylistItem(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.data.local.database.entity.PlaylistEntity> playlistEntities) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.video.Video toBasicVideoData(@org.jetbrains.annotations.NotNull()
    com.example.data.local.database.entity.VideoEntity videoEntity) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.data.local.database.entity.VideoEntity toVideoEntity(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video video, long playlistId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.data.local.database.entity.VideoEntity toVideoEntity(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video_detail.VideoDetail video, long playlistId) {
        return null;
    }
}