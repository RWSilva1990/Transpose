package com.example.home.home_playlist;

import androidx.lifecycle.ViewModel;
import com.example.domain.constants.MusicCategoryConstants;
import com.example.domain.model.youtube.playlist.Playlist;
import com.example.domain.repository.PlaylistRepository;
import com.example.media.manager.MediaPlaybackManager;
import com.example.ui.common.UiState;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u0016\u001a\u00020\u0017H\u0002J\b\u0010\u0018\u001a\u00020\u0017H\u0002J\b\u0010\u0019\u001a\u00020\u0017H\u0002J\u000e\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u000bR \u0010\u0007\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\f\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\r\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\u000e\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010\u0012\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R#\u0010\u0014\u001a\u0014\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\t0\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011\u00a8\u0006\u001d"}, d2 = {"Lcom/example/home/home_playlist/HomePlaylistViewModel;", "Landroidx/lifecycle/ViewModel;", "playlistRepository", "Lcom/example/domain/repository/PlaylistRepository;", "mediaPlaybackManager", "Lcom/example/media/manager/MediaPlaybackManager;", "(Lcom/example/domain/repository/PlaylistRepository;Lcom/example/media/manager/MediaPlaybackManager;)V", "_nationalPlaylistDataState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/ui/common/UiState;", "", "Lcom/example/domain/model/youtube/playlist/Playlist;", "_recommendedPlaylistDataState", "_typedPlaylistDataState", "nationalPlaylistDataState", "Lkotlinx/coroutines/flow/StateFlow;", "getNationalPlaylistDataState", "()Lkotlinx/coroutines/flow/StateFlow;", "recommendedPlaylistDataState", "getRecommendedPlaylistDataState", "typedPlaylistDataState", "getTypedPlaylistDataState", "fetchNationalPlaylists", "Lkotlinx/coroutines/Job;", "fetchRecommendedPlaylists", "fetchTypedPlaylists", "setCurrentPlaylistInfo", "", "playlist", "home_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class HomePlaylistViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.PlaylistRepository playlistRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.media.manager.MediaPlaybackManager mediaPlaybackManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.ui.common.UiState<java.util.List<com.example.domain.model.youtube.playlist.Playlist>>> _nationalPlaylistDataState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.UiState<java.util.List<com.example.domain.model.youtube.playlist.Playlist>>> nationalPlaylistDataState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.ui.common.UiState<java.util.List<com.example.domain.model.youtube.playlist.Playlist>>> _recommendedPlaylistDataState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.UiState<java.util.List<com.example.domain.model.youtube.playlist.Playlist>>> recommendedPlaylistDataState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.ui.common.UiState<java.util.List<com.example.domain.model.youtube.playlist.Playlist>>> _typedPlaylistDataState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.UiState<java.util.List<com.example.domain.model.youtube.playlist.Playlist>>> typedPlaylistDataState = null;
    
    @javax.inject.Inject()
    public HomePlaylistViewModel(@org.jetbrains.annotations.NotNull()
    com.example.domain.repository.PlaylistRepository playlistRepository, @org.jetbrains.annotations.NotNull()
    com.example.media.manager.MediaPlaybackManager mediaPlaybackManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.UiState<java.util.List<com.example.domain.model.youtube.playlist.Playlist>>> getNationalPlaylistDataState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.UiState<java.util.List<com.example.domain.model.youtube.playlist.Playlist>>> getRecommendedPlaylistDataState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.ui.common.UiState<java.util.List<com.example.domain.model.youtube.playlist.Playlist>>> getTypedPlaylistDataState() {
        return null;
    }
    
    public final void setCurrentPlaylistInfo(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.playlist.Playlist playlist) {
    }
    
    private final kotlinx.coroutines.Job fetchNationalPlaylists() {
        return null;
    }
    
    private final kotlinx.coroutines.Job fetchRecommendedPlaylists() {
        return null;
    }
    
    private final kotlinx.coroutines.Job fetchTypedPlaylists() {
        return null;
    }
}