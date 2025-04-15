package com.example.ui.screen.channel;

import androidx.lifecycle.ViewModel;
import com.example.domain.model.library.MyPlaylist;
import com.example.domain.model.youtube.channel.ChannelDetail;
import com.example.domain.model.youtube.channel.ChannelTabResult;
import com.example.domain.model.youtube.playlist.Playlist;
import com.example.domain.model.youtube.video.Video;
import com.example.domain.repository.ChannelRepository;
import com.example.domain.repository.MyPlaylistDBRepository;
import com.example.media.manager.MediaPlaybackManager;
import com.example.ui.common.PaginatedState;
import com.example.util.Logger;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000x\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 72\u00020\u0001:\u00017B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0016\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(J\u0006\u0010)\u001a\u00020$J\u000e\u0010*\u001a\u00020$2\u0006\u0010+\u001a\u00020,J\u0018\u0010-\u001a\u00020$2\u0006\u0010+\u001a\u00020,2\b\u0010.\u001a\u0004\u0018\u00010,J\u0018\u0010/\u001a\u00020$2\u0006\u0010+\u001a\u00020,2\b\u0010.\u001a\u0004\u0018\u00010,J\u000e\u00100\u001a\u0002012\u0006\u0010%\u001a\u00020&J\u0018\u00102\u001a\u00020$2\u0006\u0010+\u001a\u00020,2\b\u0010.\u001a\u0004\u0018\u00010,J\u000e\u00103\u001a\u0002012\u0006\u00104\u001a\u000205J\u0010\u00106\u001a\u0002012\u0006\u0010.\u001a\u00020,H\u0002R\u0016\u0010\t\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u001d\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0019R\u001d\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0019R\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\u00120\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0019R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010!\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0019\u00a8\u00068"}, d2 = {"Lcom/example/ui/screen/channel/ChannelViewModel;", "Landroidx/lifecycle/ViewModel;", "channelRepository", "Lcom/example/domain/repository/ChannelRepository;", "mediaPlaybackManager", "Lcom/example/media/manager/MediaPlaybackManager;", "myPlaylistDBRepository", "Lcom/example/domain/repository/MyPlaylistDBRepository;", "(Lcom/example/domain/repository/ChannelRepository;Lcom/example/media/manager/MediaPlaybackManager;Lcom/example/domain/repository/MyPlaylistDBRepository;)V", "_channelDetail", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/domain/model/youtube/channel/ChannelDetail;", "_channelTabPlaylistsState", "Lcom/example/ui/common/PaginatedState;", "Lcom/example/domain/model/youtube/channel/ChannelTabResult;", "_channelTabShortsState", "_channelTabVideosState", "_isChannelDetailDataLoading", "", "_myPlaylists", "", "Lcom/example/domain/model/library/MyPlaylist;", "channelDetail", "Lkotlinx/coroutines/flow/StateFlow;", "getChannelDetail", "()Lkotlinx/coroutines/flow/StateFlow;", "channelTabPlaylists", "getChannelTabPlaylists", "channelTabShorts", "getChannelTabShorts", "channelTabVideos", "getChannelTabVideos", "isChannelDetailDataLoading", "myPlaylists", "getMyPlaylists", "addVideoToPlaylist", "Lkotlinx/coroutines/Job;", "video", "Lcom/example/domain/model/youtube/video/Video;", "playlistId", "", "getAllMyPlaylists", "loadChannelDetail", "channelId", "", "loadMoreContent", "contentType", "loadTabContent", "onMediaClicked", "", "onTabChanged", "setPlaylistInfo", "playlist", "Lcom/example/domain/model/youtube/playlist/Playlist;", "updateHasMoreState", "ContentType", "ui_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ChannelViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.ChannelRepository channelRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.media.manager.MediaPlaybackManager mediaPlaybackManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.MyPlaylistDBRepository myPlaylistDBRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.domain.model.youtube.channel.ChannelDetail> _channelDetail = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.channel.ChannelDetail> channelDetail = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.channel.ChannelTabResult>> _channelTabVideosState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.channel.ChannelTabResult>> channelTabVideos = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.channel.ChannelTabResult>> _channelTabShortsState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.channel.ChannelTabResult>> channelTabShorts = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.channel.ChannelTabResult>> _channelTabPlaylistsState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.channel.ChannelTabResult>> channelTabPlaylists = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isChannelDetailDataLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isChannelDetailDataLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.domain.model.library.MyPlaylist>> _myPlaylists = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.library.MyPlaylist>> myPlaylists = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String VIDEOS = "videos";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SHORTS = "shorts";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PLAYLISTS = "playlists";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String LIVESTREAMS = "livestreams";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String HOME = "home";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String COMMUNITY = "community";
    @org.jetbrains.annotations.NotNull()
    public static final com.example.ui.screen.channel.ChannelViewModel.ContentType ContentType = null;
    
    @javax.inject.Inject()
    public ChannelViewModel(@org.jetbrains.annotations.NotNull()
    com.example.domain.repository.ChannelRepository channelRepository, @org.jetbrains.annotations.NotNull()
    com.example.media.manager.MediaPlaybackManager mediaPlaybackManager, @org.jetbrains.annotations.NotNull()
    com.example.domain.repository.MyPlaylistDBRepository myPlaylistDBRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.channel.ChannelDetail> getChannelDetail() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.channel.ChannelTabResult>> getChannelTabVideos() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.channel.ChannelTabResult>> getChannelTabShorts() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.channel.ChannelTabResult>> getChannelTabPlaylists() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isChannelDetailDataLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job loadChannelDetail(@org.jetbrains.annotations.NotNull()
    java.lang.String channelId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job onTabChanged(@org.jetbrains.annotations.NotNull()
    java.lang.String channelId, @org.jetbrains.annotations.Nullable()
    java.lang.String contentType) {
        return null;
    }
    
    private final void updateHasMoreState(java.lang.String contentType) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job loadTabContent(@org.jetbrains.annotations.NotNull()
    java.lang.String channelId, @org.jetbrains.annotations.Nullable()
    java.lang.String contentType) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job loadMoreContent(@org.jetbrains.annotations.NotNull()
    java.lang.String channelId, @org.jetbrains.annotations.Nullable()
    java.lang.String contentType) {
        return null;
    }
    
    public final void setPlaylistInfo(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.playlist.Playlist playlist) {
    }
    
    public final void onMediaClicked(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video video) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.library.MyPlaylist>> getMyPlaylists() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job getAllMyPlaylists() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job addVideoToPlaylist(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video video, long playlistId) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/example/ui/screen/channel/ChannelViewModel$ContentType;", "", "()V", "COMMUNITY", "", "HOME", "LIVESTREAMS", "PLAYLISTS", "SHORTS", "VIDEOS", "ui_debug"})
    public static final class ContentType {
        
        private ContentType() {
            super();
        }
    }
}