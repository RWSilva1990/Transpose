package com.example.data.newpipe.mapper;

import com.example.data.newpipe.mapper.channel.ChannelMapper;
import com.example.data.newpipe.mapper.playlist.PlaylistMapper;
import com.example.data.newpipe.mapper.video.VideoMapper;
import com.example.domain.model.youtube.channel.ChannelDetail;
import com.example.domain.model.youtube.channel.ChannelTab;
import com.example.domain.model.youtube.channel.ChannelTabResult;
import com.example.domain.model.youtube.playlist.Playlist;
import com.example.domain.model.youtube.playlist.PlaylistItem;
import com.example.domain.model.youtube.search.SearchResult;
import com.example.domain.model.youtube.video_detail.VideoDetail;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bJ\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\u000e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0005\u001a\u00020\u0012J\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\f\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u000fJ\u0016\u0010\u0017\u001a\u00020\u00112\u0006\u0010\f\u001a\u00020\u00152\u0006\u0010\u000e\u001a\u00020\u000fJ\u0016\u0010\u0018\u001a\u00020\u00192\u0006\u0010\f\u001a\u00020\u00152\u0006\u0010\u000e\u001a\u00020\u000fJ\u0016\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0005\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u000fJ\u001e\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\f\u001a\u00020 2\u0006\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u000fJ\u001e\u0010!\u001a\u00020\"2\u0006\u0010\f\u001a\u00020 2\u0006\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u000fJ\u001e\u0010#\u001a\u00020$2\u0006\u0010\f\u001a\u00020 2\u0006\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u000fJ\u001e\u0010%\u001a\u00020&2\u0006\u0010\f\u001a\u00020 2\u0006\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u001d\u001a\u00020\u000f\u00a8\u0006\'"}, d2 = {"Lcom/example/data/newpipe/mapper/InfoItemMapper;", "", "()V", "channelExtractorToChannelDetail", "Lcom/example/domain/model/youtube/channel/ChannelDetail;", "extractor", "Lorg/schabi/newpipe/extractor/channel/ChannelExtractor;", "tabs", "", "Lcom/example/domain/model/youtube/channel/ChannelTab;", "channelInfoItemToSearchResultChannel", "Lcom/example/domain/model/youtube/search/SearchResult$ChannelResult;", "item", "Lorg/schabi/newpipe/extractor/channel/ChannelInfoItem;", "id", "", "playlistExtractorToPlaylistData", "Lcom/example/domain/model/youtube/playlist/Playlist;", "Lorg/schabi/newpipe/extractor/playlist/PlaylistExtractor;", "playlistInfoItemToChannelTabResultPlaylist", "Lcom/example/domain/model/youtube/channel/ChannelTabResult$PlaylistResult;", "Lorg/schabi/newpipe/extractor/playlist/PlaylistInfoItem;", "videoId", "playlistInfoItemToPlaylistData", "playlistInfoItemToSearchResultPlaylist", "Lcom/example/domain/model/youtube/search/SearchResult$PlaylistResult;", "streamExtractorToVideoDetail", "Lcom/example/domain/model/youtube/video_detail/VideoDetail;", "Lorg/schabi/newpipe/extractor/stream/StreamExtractor;", "uploaderId", "streamInfoItemToChannelTabResultShorts", "Lcom/example/domain/model/youtube/channel/ChannelTabResult$ShortsResult;", "Lorg/schabi/newpipe/extractor/stream/StreamInfoItem;", "streamInfoItemToChannelTabResultVideo", "Lcom/example/domain/model/youtube/channel/ChannelTabResult$VideoResult;", "streamInfoItemToPlaylistItemData", "Lcom/example/domain/model/youtube/playlist/PlaylistItem;", "streamInfoItemToSearchResultVideo", "Lcom/example/domain/model/youtube/search/SearchResult$VideoResult;", "data_debug"})
public final class InfoItemMapper {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.data.newpipe.mapper.InfoItemMapper INSTANCE = null;
    
    private InfoItemMapper() {
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
    public final com.example.domain.model.youtube.search.SearchResult.ChannelResult channelInfoItemToSearchResultChannel(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.channel.ChannelInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.search.SearchResult.PlaylistResult playlistInfoItemToSearchResultPlaylist(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.playlist.PlaylistInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.video_detail.VideoDetail streamExtractorToVideoDetail(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.stream.StreamExtractor extractor, @org.jetbrains.annotations.NotNull()
    java.lang.String uploaderId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.playlist.Playlist playlistInfoItemToPlaylistData(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.playlist.PlaylistInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.playlist.Playlist playlistExtractorToPlaylistData(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.playlist.PlaylistExtractor extractor) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.playlist.PlaylistItem streamInfoItemToPlaylistItemData(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.stream.StreamInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String videoId, @org.jetbrains.annotations.NotNull()
    java.lang.String uploaderId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.channel.ChannelDetail channelExtractorToChannelDetail(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.channel.ChannelExtractor extractor, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.domain.model.youtube.channel.ChannelTab> tabs) {
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
    public final com.example.domain.model.youtube.channel.ChannelTabResult.PlaylistResult playlistInfoItemToChannelTabResultPlaylist(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.playlist.PlaylistInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String videoId) {
        return null;
    }
}