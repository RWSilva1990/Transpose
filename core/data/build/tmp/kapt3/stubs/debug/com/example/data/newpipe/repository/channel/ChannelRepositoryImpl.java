package com.example.data.newpipe.repository.channel;

import com.example.data.newpipe.extractor.channel.ChannelTabPager;
import com.example.data.newpipe.repository.base.BaseNewPipeRepository;
import com.example.domain.model.youtube.channel.ChannelDetail;
import com.example.domain.model.youtube.channel.ChannelTab;
import com.example.domain.model.youtube.channel.ChannelTabResult;
import com.example.domain.repository.ChannelRepository;
import com.example.util.Logger;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0006H\u0016J$\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\u0006H\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u000f\u0010\u0010J4\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u00120\f2\u0006\u0010\u000e\u001a\u00020\u00062\b\u0010\n\u001a\u0004\u0018\u00010\u0006H\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0014\u0010\u0015J*\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00130\u00120\f2\u0006\u0010\n\u001a\u00020\u0006H\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0017\u0010\u0010R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0018"}, d2 = {"Lcom/example/data/newpipe/repository/channel/ChannelRepositoryImpl;", "Lcom/example/data/newpipe/repository/base/BaseNewPipeRepository;", "Lcom/example/domain/repository/ChannelRepository;", "()V", "channelTabPagers", "", "", "Lcom/example/data/newpipe/extractor/channel/ChannelTabPager;", "canLoadMoreChannelTabContent", "", "contentType", "fetchChannelDetail", "Lkotlin/Result;", "Lcom/example/domain/model/youtube/channel/ChannelDetail;", "channelId", "fetchChannelDetail-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchChannelTabContent", "", "Lcom/example/domain/model/youtube/channel/ChannelTabResult;", "fetchChannelTabContent-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadMoreChannelTabContent", "loadMoreChannelTabContent-gIAlu-s", "data_debug"})
public final class ChannelRepositoryImpl extends com.example.data.newpipe.repository.base.BaseNewPipeRepository implements com.example.domain.repository.ChannelRepository {
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, com.example.data.newpipe.extractor.channel.ChannelTabPager> channelTabPagers = null;
    
    @javax.inject.Inject()
    public ChannelRepositoryImpl() {
        super();
    }
    
    @java.lang.Override()
    public boolean canLoadMoreChannelTabContent(@org.jetbrains.annotations.NotNull()
    java.lang.String contentType) {
        return false;
    }
}