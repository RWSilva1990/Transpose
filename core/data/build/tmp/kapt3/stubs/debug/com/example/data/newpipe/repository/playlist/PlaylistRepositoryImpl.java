package com.example.data.newpipe.repository.playlist;

import com.example.data.newpipe.extractor.playlist.PlaylistItemPager;
import com.example.data.newpipe.extractor.playlist.PlaylistPager;
import com.example.data.newpipe.mapper.InfoItemMapper;
import com.example.data.newpipe.repository.base.BaseNewPipeRepository;
import com.example.domain.model.youtube.playlist.Playlist;
import com.example.domain.model.youtube.playlist.PlaylistItem;
import com.example.domain.repository.PlaylistRepository;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0003J\b\u0010\b\u001a\u00020\tH\u0016J*\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000b2\u0006\u0010\u000e\u001a\u00020\u000fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0010\u0010\u0011J$\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u000b2\u0006\u0010\u000e\u001a\u00020\u000fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0014\u0010\u0011J,\u0010\u0015\u001a\u0010\u0012\f\u0012\n\u0012\u0004\u0012\u00020\u0013\u0018\u00010\f0\u000b2\u0006\u0010\u0016\u001a\u00020\u000fH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0017\u0010\u0011J\"\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000bH\u0096@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0019\u0010\u001aR\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u001b"}, d2 = {"Lcom/example/data/newpipe/repository/playlist/PlaylistRepositoryImpl;", "Lcom/example/data/newpipe/repository/base/BaseNewPipeRepository;", "Lcom/example/domain/repository/PlaylistRepository;", "()V", "playlistItemPager", "Lcom/example/data/newpipe/extractor/playlist/PlaylistItemPager;", "playlistPager", "Lcom/example/data/newpipe/extractor/playlist/PlaylistPager;", "canLoadMorePlaylistItems", "", "fetchPlaylistItemsResult", "Lkotlin/Result;", "", "Lcom/example/domain/model/youtube/playlist/PlaylistItem;", "playlistId", "", "fetchPlaylistItemsResult-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "fetchPlaylistResult", "Lcom/example/domain/model/youtube/playlist/Playlist;", "fetchPlaylistResult-gIAlu-s", "fetchPlaylistWithChannelId", "channelId", "fetchPlaylistWithChannelId-gIAlu-s", "loadMorePlaylistItems", "loadMorePlaylistItems-IoAF18A", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class PlaylistRepositoryImpl extends com.example.data.newpipe.repository.base.BaseNewPipeRepository implements com.example.domain.repository.PlaylistRepository {
    @org.jetbrains.annotations.Nullable()
    private com.example.data.newpipe.extractor.playlist.PlaylistPager playlistPager;
    @org.jetbrains.annotations.Nullable()
    private com.example.data.newpipe.extractor.playlist.PlaylistItemPager playlistItemPager;
    
    @javax.inject.Inject()
    public PlaylistRepositoryImpl() {
        super();
    }
    
    @java.lang.Override()
    public boolean canLoadMorePlaylistItems() {
        return false;
    }
}