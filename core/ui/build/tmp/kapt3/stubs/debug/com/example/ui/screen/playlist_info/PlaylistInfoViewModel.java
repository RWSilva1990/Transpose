package com.example.ui.screen.playlist_info;

import androidx.lifecycle.ViewModel;
import com.example.domain.model.library.MyPlaylist;
import com.example.domain.model.youtube.playlist.PlaylistItem;
import com.example.domain.model.youtube.video.Video;
import com.example.domain.repository.MyPlaylistDBRepository;
import com.example.domain.repository.PlaylistRepository;
import com.example.media.manager.MediaPlaybackManager;
import com.example.ui.common.PaginatedState;
import com.example.util.Logger;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001eJ\u0006\u0010\u001f\u001a\u00020\u001aJ\u000e\u0010 \u001a\u00020\u001a2\u0006\u0010\u001d\u001a\u00020!J\u0006\u0010\"\u001a\u00020\u001aJ$\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020\u001c2\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u001c0\u000b2\u0006\u0010\'\u001a\u00020(R\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u000b0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0014R\u001d\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0014R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006)"}, d2 = {"Lcom/example/ui/screen/playlist_info/PlaylistInfoViewModel;", "Landroidx/lifecycle/ViewModel;", "playlistRepository", "Lcom/example/domain/repository/PlaylistRepository;", "mediaPlaybackManager", "Lcom/example/media/manager/MediaPlaybackManager;", "myPlaylistDBRepository", "Lcom/example/domain/repository/MyPlaylistDBRepository;", "(Lcom/example/domain/repository/PlaylistRepository;Lcom/example/media/manager/MediaPlaybackManager;Lcom/example/domain/repository/MyPlaylistDBRepository;)V", "_myPlaylists", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/example/domain/model/library/MyPlaylist;", "_playlistItemsState", "Lcom/example/ui/common/PaginatedState;", "Lcom/example/domain/model/youtube/playlist/PlaylistItem;", "currentPlaylistInfo", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/example/domain/model/youtube/playlist/Playlist;", "getCurrentPlaylistInfo", "()Lkotlinx/coroutines/flow/StateFlow;", "myPlaylists", "getMyPlaylists", "playlistItemsState", "getPlaylistItemsState", "addVideoToPlaylist", "Lkotlinx/coroutines/Job;", "video", "Lcom/example/domain/model/youtube/video/Video;", "playlistId", "", "getAllMyPlaylists", "initializePlaylistPager", "", "loadMorePlaylistItems", "onMediaClicked", "", "item", "playlistItems", "clickedIndex", "", "ui_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class PlaylistInfoViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.PlaylistRepository playlistRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.media.manager.MediaPlaybackManager mediaPlaybackManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.MyPlaylistDBRepository myPlaylistDBRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.playlist.PlaylistItem>> _playlistItemsState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.playlist.PlaylistItem>> playlistItemsState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.playlist.Playlist> currentPlaylistInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.domain.model.library.MyPlaylist>> _myPlaylists = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.library.MyPlaylist>> myPlaylists = null;
    
    @javax.inject.Inject()
    public PlaylistInfoViewModel(@org.jetbrains.annotations.NotNull()
    com.example.domain.repository.PlaylistRepository playlistRepository, @org.jetbrains.annotations.NotNull()
    com.example.media.manager.MediaPlaybackManager mediaPlaybackManager, @org.jetbrains.annotations.NotNull()
    com.example.domain.repository.MyPlaylistDBRepository myPlaylistDBRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.PaginatedState<com.example.domain.model.youtube.playlist.PlaylistItem>> getPlaylistItemsState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.playlist.Playlist> getCurrentPlaylistInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job initializePlaylistPager(@org.jetbrains.annotations.NotNull()
    java.lang.String playlistId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job loadMorePlaylistItems() {
        return null;
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
    
    public final void onMediaClicked(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video item, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.domain.model.youtube.video.Video> playlistItems, int clickedIndex) {
    }
}