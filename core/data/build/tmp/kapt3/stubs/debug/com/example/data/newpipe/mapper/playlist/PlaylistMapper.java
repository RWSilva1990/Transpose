package com.example.data.newpipe.mapper.playlist;

import com.example.data.newpipe.mapper.base.BaseMapper;
import com.example.data.newpipe.mapper.video.VideoMapper;
import com.example.domain.model.youtube.channel.ChannelTabResult;
import com.example.domain.model.youtube.playlist.Playlist;
import com.example.domain.model.youtube.playlist.PlaylistItem;
import com.example.domain.model.youtube.search.SearchResult;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\r\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u001e\u0010\u0010\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\f2\u0006\u0010\u0014\u001a\u00020\f\u00a8\u0006\u0015"}, d2 = {"Lcom/example/data/newpipe/mapper/playlist/PlaylistMapper;", "", "()V", "playlistExtractorToPlaylistData", "Lcom/example/domain/model/youtube/playlist/Playlist;", "extractor", "Lorg/schabi/newpipe/extractor/playlist/PlaylistExtractor;", "playlistInfoItemToChannelTabResultPlaylist", "Lcom/example/domain/model/youtube/channel/ChannelTabResult$PlaylistResult;", "item", "Lorg/schabi/newpipe/extractor/playlist/PlaylistInfoItem;", "id", "", "playlistInfoItemToPlaylistData", "playlistInfoItemToSearchResultPlaylist", "Lcom/example/domain/model/youtube/search/SearchResult$PlaylistResult;", "streamInfoItemToPlaylistItemData", "Lcom/example/domain/model/youtube/playlist/PlaylistItem;", "Lorg/schabi/newpipe/extractor/stream/StreamInfoItem;", "videoId", "uploaderId", "data_debug"})
public final class PlaylistMapper {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.data.newpipe.mapper.playlist.PlaylistMapper INSTANCE = null;
    
    private PlaylistMapper() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.search.SearchResult.PlaylistResult playlistInfoItemToSearchResultPlaylist(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.playlist.PlaylistInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.channel.ChannelTabResult.PlaylistResult playlistInfoItemToChannelTabResultPlaylist(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.playlist.PlaylistInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String id) {
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
}