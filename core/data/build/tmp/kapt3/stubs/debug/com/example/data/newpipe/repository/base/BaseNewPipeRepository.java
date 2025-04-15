package com.example.data.newpipe.repository.base;

import com.example.data.newpipe.downloader.NewPipeDownloader;
import com.example.data.newpipe.exception.NewPipeException;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.YoutubeService;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\nH\u0004J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0004J\u0010\u0010\u000f\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\bH\u0004J\u0010\u0010\u0011\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\bH\u0004J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\r\u001a\u00020\u000eH\u0004J\u0010\u0010\u0015\u001a\u00020\u000e2\u0006\u0010\u0016\u001a\u00020\bH\u0004J\u0010\u0010\u0017\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\bH\u0004J\u0010\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\bH\u0004J\u0010\u0010\u001b\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\bH\u0004J\u0010\u0010\u001c\u001a\u00020\b2\u0006\u0010\u001a\u001a\u00020\bH\u0004R\u0014\u0010\u0003\u001a\u00020\u0004X\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u001d"}, d2 = {"Lcom/example/data/newpipe/repository/base/BaseNewPipeRepository;", "", "()V", "youtubeService", "Lorg/schabi/newpipe/extractor/services/youtube/YoutubeService;", "getYoutubeService", "()Lorg/schabi/newpipe/extractor/services/youtube/YoutubeService;", "determineTabContentType", "", "contentFilters", "", "getChannelExtractor", "Lorg/schabi/newpipe/extractor/channel/ChannelExtractor;", "linkHandler", "Lorg/schabi/newpipe/extractor/linkhandler/ListLinkHandler;", "getChannelId", "url", "getChannelLinkHandler", "channelId", "getChannelTabExtractor", "Lorg/schabi/newpipe/extractor/channel/tabs/ChannelTabExtractor;", "getPlaylistHandler", "playlistId", "getPlaylistId", "getStreamExtractor", "Lorg/schabi/newpipe/extractor/stream/StreamExtractor;", "videoId", "getVideoId", "getVideoUrl", "data_debug"})
public abstract class BaseNewPipeRepository {
    @org.jetbrains.annotations.NotNull()
    private final org.schabi.newpipe.extractor.services.youtube.YoutubeService youtubeService = null;
    
    public BaseNewPipeRepository() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final org.schabi.newpipe.extractor.services.youtube.YoutubeService getYoutubeService() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final java.lang.String getVideoUrl(@org.jetbrains.annotations.NotNull()
    java.lang.String videoId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final org.schabi.newpipe.extractor.stream.StreamExtractor getStreamExtractor(@org.jetbrains.annotations.NotNull()
    java.lang.String videoId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final org.schabi.newpipe.extractor.linkhandler.ListLinkHandler getChannelLinkHandler(@org.jetbrains.annotations.NotNull()
    java.lang.String channelId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final java.lang.String getVideoId(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final java.lang.String getPlaylistId(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final java.lang.String getChannelId(@org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final org.schabi.newpipe.extractor.channel.ChannelExtractor getChannelExtractor(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.linkhandler.ListLinkHandler linkHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor getChannelTabExtractor(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.linkhandler.ListLinkHandler linkHandler) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final org.schabi.newpipe.extractor.linkhandler.ListLinkHandler getPlaylistHandler(@org.jetbrains.annotations.NotNull()
    java.lang.String playlistId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final java.lang.String determineTabContentType(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> contentFilters) {
        return null;
    }
}