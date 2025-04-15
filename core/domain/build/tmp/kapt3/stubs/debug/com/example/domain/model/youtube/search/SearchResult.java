package com.example.domain.model.youtube.search;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b7\u0018\u00002\u00020\u0001:\u0003\u0003\u0004\u0005B\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0002\u0082\u0001\u0003\u0006\u0007\b\u00a8\u0006\t"}, d2 = {"Lcom/example/domain/model/youtube/search/SearchResult;", "", "()V", "ChannelResult", "PlaylistResult", "VideoResult", "Lcom/example/domain/model/youtube/search/SearchResult$ChannelResult;", "Lcom/example/domain/model/youtube/search/SearchResult$PlaylistResult;", "Lcom/example/domain/model/youtube/search/SearchResult$VideoResult;", "domain_debug"})
public abstract class SearchResult {
    
    private SearchResult() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/example/domain/model/youtube/search/SearchResult$ChannelResult;", "Lcom/example/domain/model/youtube/search/SearchResult;", "channel", "Lcom/example/domain/model/youtube/channel/Channel;", "(Lcom/example/domain/model/youtube/channel/Channel;)V", "getChannel", "()Lcom/example/domain/model/youtube/channel/Channel;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "domain_debug"})
    public static final class ChannelResult extends com.example.domain.model.youtube.search.SearchResult {
        @org.jetbrains.annotations.NotNull()
        private final com.example.domain.model.youtube.channel.Channel channel = null;
        
        public ChannelResult(@org.jetbrains.annotations.NotNull()
        com.example.domain.model.youtube.channel.Channel channel) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.domain.model.youtube.channel.Channel getChannel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.domain.model.youtube.channel.Channel component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.domain.model.youtube.search.SearchResult.ChannelResult copy(@org.jetbrains.annotations.NotNull()
        com.example.domain.model.youtube.channel.Channel channel) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/example/domain/model/youtube/search/SearchResult$PlaylistResult;", "Lcom/example/domain/model/youtube/search/SearchResult;", "playlist", "Lcom/example/domain/model/youtube/playlist/Playlist;", "(Lcom/example/domain/model/youtube/playlist/Playlist;)V", "getPlaylist", "()Lcom/example/domain/model/youtube/playlist/Playlist;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "domain_debug"})
    public static final class PlaylistResult extends com.example.domain.model.youtube.search.SearchResult {
        @org.jetbrains.annotations.NotNull()
        private final com.example.domain.model.youtube.playlist.Playlist playlist = null;
        
        public PlaylistResult(@org.jetbrains.annotations.NotNull()
        com.example.domain.model.youtube.playlist.Playlist playlist) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.domain.model.youtube.playlist.Playlist getPlaylist() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.domain.model.youtube.playlist.Playlist component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.domain.model.youtube.search.SearchResult.PlaylistResult copy(@org.jetbrains.annotations.NotNull()
        com.example.domain.model.youtube.playlist.Playlist playlist) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0087\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\t\u0010\u0007\u001a\u00020\u0003H\u00c6\u0003J\u0013\u0010\b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00d6\u0003J\t\u0010\r\u001a\u00020\u000eH\u00d6\u0001J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/example/domain/model/youtube/search/SearchResult$VideoResult;", "Lcom/example/domain/model/youtube/search/SearchResult;", "video", "Lcom/example/domain/model/youtube/video/Video;", "(Lcom/example/domain/model/youtube/video/Video;)V", "getVideo", "()Lcom/example/domain/model/youtube/video/Video;", "component1", "copy", "equals", "", "other", "", "hashCode", "", "toString", "", "domain_debug"})
    public static final class VideoResult extends com.example.domain.model.youtube.search.SearchResult {
        @org.jetbrains.annotations.NotNull()
        private final com.example.domain.model.youtube.video.Video video = null;
        
        public VideoResult(@org.jetbrains.annotations.NotNull()
        com.example.domain.model.youtube.video.Video video) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.domain.model.youtube.video.Video getVideo() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.domain.model.youtube.video.Video component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.domain.model.youtube.search.SearchResult.VideoResult copy(@org.jetbrains.annotations.NotNull()
        com.example.domain.model.youtube.video.Video video) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}