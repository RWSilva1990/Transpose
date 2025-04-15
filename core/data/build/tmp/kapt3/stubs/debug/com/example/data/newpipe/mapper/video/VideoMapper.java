package com.example.data.newpipe.mapper.video;

import com.example.data.newpipe.mapper.base.BaseMapper;
import com.example.domain.model.youtube.channel.ChannelTabResult;
import com.example.domain.model.youtube.video.Video;
import com.example.domain.model.youtube.search.SearchResult;
import com.example.domain.model.youtube.video_detail.VideoDetail;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u001e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\bJ\u001e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\bJ\u001e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\bJ\u001e\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b\u00a8\u0006\u0014"}, d2 = {"Lcom/example/data/newpipe/mapper/video/VideoMapper;", "", "()V", "streamExtractorToVideoDetail", "Lcom/example/domain/model/youtube/video_detail/VideoDetail;", "extractor", "Lorg/schabi/newpipe/extractor/stream/StreamExtractor;", "uploaderId", "", "streamInfoItemToBasicVideoData", "Lcom/example/domain/model/youtube/video/Video;", "item", "Lorg/schabi/newpipe/extractor/stream/StreamInfoItem;", "videoId", "streamInfoItemToChannelTabResultShorts", "Lcom/example/domain/model/youtube/channel/ChannelTabResult$ShortsResult;", "streamInfoItemToChannelTabResultVideo", "Lcom/example/domain/model/youtube/channel/ChannelTabResult$VideoResult;", "streamInfoItemToSearchResultVideo", "Lcom/example/domain/model/youtube/search/SearchResult$VideoResult;", "data_debug"})
public final class VideoMapper {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.data.newpipe.mapper.video.VideoMapper INSTANCE = null;
    
    private VideoMapper() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.search.SearchResult.VideoResult streamInfoItemToSearchResultVideo(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.stream.StreamInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String videoId, @org.jetbrains.annotations.NotNull()
    java.lang.String uploaderId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.channel.ChannelTabResult.VideoResult streamInfoItemToChannelTabResultVideo(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.stream.StreamInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String videoId, @org.jetbrains.annotations.NotNull()
    java.lang.String uploaderId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.channel.ChannelTabResult.ShortsResult streamInfoItemToChannelTabResultShorts(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.stream.StreamInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String videoId, @org.jetbrains.annotations.NotNull()
    java.lang.String uploaderId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.video.Video streamInfoItemToBasicVideoData(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.stream.StreamInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String videoId, @org.jetbrains.annotations.NotNull()
    java.lang.String uploaderId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.video_detail.VideoDetail streamExtractorToVideoDetail(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.stream.StreamExtractor extractor, @org.jetbrains.annotations.NotNull()
    java.lang.String uploaderId) {
        return null;
    }
}