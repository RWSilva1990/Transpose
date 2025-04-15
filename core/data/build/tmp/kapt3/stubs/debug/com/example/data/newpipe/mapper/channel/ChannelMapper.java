package com.example.data.newpipe.mapper.channel;

import com.example.data.newpipe.mapper.base.BaseMapper;
import com.example.domain.model.youtube.channel.Channel;
import com.example.domain.model.youtube.channel.ChannelDetail;
import com.example.domain.model.youtube.channel.ChannelTab;
import com.example.domain.model.youtube.search.SearchResult;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bJ\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f\u00a8\u0006\u0010"}, d2 = {"Lcom/example/data/newpipe/mapper/channel/ChannelMapper;", "", "()V", "channelExtractorToChannelDetail", "Lcom/example/domain/model/youtube/channel/ChannelDetail;", "extractor", "Lorg/schabi/newpipe/extractor/channel/ChannelExtractor;", "tabs", "", "Lcom/example/domain/model/youtube/channel/ChannelTab;", "channelInfoItemToSearchResultChannel", "Lcom/example/domain/model/youtube/search/SearchResult$ChannelResult;", "item", "Lorg/schabi/newpipe/extractor/channel/ChannelInfoItem;", "id", "", "data_debug"})
public final class ChannelMapper {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.data.newpipe.mapper.channel.ChannelMapper INSTANCE = null;
    
    private ChannelMapper() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.search.SearchResult.ChannelResult channelInfoItemToSearchResultChannel(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.channel.ChannelInfoItem item, @org.jetbrains.annotations.NotNull()
    java.lang.String id) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.youtube.channel.ChannelDetail channelExtractorToChannelDetail(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.channel.ChannelExtractor extractor, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.domain.model.youtube.channel.ChannelTab> tabs) {
        return null;
    }
}