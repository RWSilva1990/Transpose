package com.example.data.newpipe.extractor.playlist;

import com.example.data.newpipe.extractor.base.Pager;
import com.example.data.newpipe.exception.NewPipeException;
import com.example.data.newpipe.mapper.InfoItemMapper;
import com.example.domain.model.youtube.playlist.PlaylistItem;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00030\u0001B\u001d\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u000e\u0010\u0006\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u001e\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\u000e\u0010\u000b\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00020\fH\u0014J\u0018\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u000eH\u0002\u00a8\u0006\u0012"}, d2 = {"Lcom/example/data/newpipe/extractor/playlist/PlaylistItemPager;", "Lcom/example/data/newpipe/extractor/base/Pager;", "Lorg/schabi/newpipe/extractor/InfoItem;", "Lcom/example/domain/model/youtube/playlist/PlaylistItem;", "streamingService", "Lorg/schabi/newpipe/extractor/StreamingService;", "extractor", "Lorg/schabi/newpipe/extractor/ListExtractor;", "(Lorg/schabi/newpipe/extractor/StreamingService;Lorg/schabi/newpipe/extractor/ListExtractor;)V", "extract", "", "page", "Lorg/schabi/newpipe/extractor/ListExtractor$InfoItemsPage;", "getId", "", "handler", "Lorg/schabi/newpipe/extractor/linkhandler/LinkHandlerFactory;", "url", "data_debug"})
public final class PlaylistItemPager extends com.example.data.newpipe.extractor.base.Pager<org.schabi.newpipe.extractor.InfoItem, com.example.domain.model.youtube.playlist.PlaylistItem> {
    
    public PlaylistItemPager(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.StreamingService streamingService, @org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.ListExtractor<? extends org.schabi.newpipe.extractor.InfoItem> extractor) {
        super(null, null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    protected java.util.List<com.example.domain.model.youtube.playlist.PlaylistItem> extract(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage<? extends org.schabi.newpipe.extractor.InfoItem> page) {
        return null;
    }
    
    private final java.lang.String getId(org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory handler, java.lang.String url) {
        return null;
    }
}