package com.example.data.newpipe.extractor.base;

import com.example.data.newpipe.exception.NewPipeException;
import okio.IOException;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\'\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u0002*\u0004\b\u0001\u0010\u00032\u00020\u0004B\u001d\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u000e\u0010\u0007\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00020\b\u00a2\u0006\u0002\u0010\tJ\u001e\u0010\u001a\u001a\b\u0012\u0004\u0012\u00028\u00010\u001b2\u000e\u0010\u001c\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00020\u001dH$J\u000e\u0010\u001e\u001a\b\u0012\u0004\u0012\u00028\u00010\u001bH\u0016J\u0006\u0010\u001f\u001a\u00020\u000fJ\u001e\u0010 \u001a\b\u0012\u0004\u0012\u00028\u00010\u001b2\u000e\u0010\u001c\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00020\u001dH\u0014R\u0014\u0010\n\u001a\u00020\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0016\u0010\u0007\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\u00020\u0013X\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0014\u0010\u0016\u001a\u00020\u000bX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\rR\u0014\u0010\u0005\u001a\u00020\u0006X\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019\u00a8\u0006!"}, d2 = {"Lcom/example/data/newpipe/extractor/base/Pager;", "I", "Lorg/schabi/newpipe/extractor/InfoItem;", "O", "", "streamingService", "Lorg/schabi/newpipe/extractor/StreamingService;", "extractor", "Lorg/schabi/newpipe/extractor/ListExtractor;", "(Lorg/schabi/newpipe/extractor/StreamingService;Lorg/schabi/newpipe/extractor/ListExtractor;)V", "channelLinkHandler", "Lorg/schabi/newpipe/extractor/linkhandler/LinkHandlerFactory;", "getChannelLinkHandler", "()Lorg/schabi/newpipe/extractor/linkhandler/LinkHandlerFactory;", "hasNextPage", "", "nextPage", "Lorg/schabi/newpipe/extractor/Page;", "playlistLinkHandler", "Lorg/schabi/newpipe/extractor/linkhandler/ListLinkHandlerFactory;", "getPlaylistLinkHandler", "()Lorg/schabi/newpipe/extractor/linkhandler/ListLinkHandlerFactory;", "streamLinkHandler", "getStreamLinkHandler", "getStreamingService", "()Lorg/schabi/newpipe/extractor/StreamingService;", "extract", "", "page", "Lorg/schabi/newpipe/extractor/ListExtractor$InfoItemsPage;", "getNextPage", "isHasNextPage", "process", "data_debug"})
public abstract class Pager<I extends org.schabi.newpipe.extractor.InfoItem, O extends java.lang.Object> {
    @org.jetbrains.annotations.NotNull()
    private final org.schabi.newpipe.extractor.StreamingService streamingService = null;
    @org.jetbrains.annotations.NotNull()
    private final org.schabi.newpipe.extractor.ListExtractor<? extends org.schabi.newpipe.extractor.InfoItem> extractor = null;
    @org.jetbrains.annotations.Nullable()
    private org.schabi.newpipe.extractor.Page nextPage;
    private boolean hasNextPage = true;
    @org.jetbrains.annotations.NotNull()
    private final org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory streamLinkHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory playlistLinkHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory channelLinkHandler = null;
    
    public Pager(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.StreamingService streamingService, @org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.ListExtractor<? extends org.schabi.newpipe.extractor.InfoItem> extractor) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final org.schabi.newpipe.extractor.StreamingService getStreamingService() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory getStreamLinkHandler() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory getPlaylistLinkHandler() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory getChannelLinkHandler() {
        return null;
    }
    
    public final boolean isHasNextPage() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public java.util.List<O> getNextPage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected java.util.List<O> process(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage<? extends org.schabi.newpipe.extractor.InfoItem> page) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected abstract java.util.List<O> extract(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage<? extends org.schabi.newpipe.extractor.InfoItem> page);
}