package com.example.library.my_playlist_item;

import androidx.lifecycle.ViewModel;
import com.example.domain.model.youtube.video.Video;
import com.example.domain.repository.MyPlaylistDBRepository;
import com.example.media.manager.MediaPlaybackManager;
import com.example.util.Logger;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\nJ\u000e\u0010\u0014\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012J\u0016\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0013\u001a\u00020\n2\u0006\u0010\u0017\u001a\u00020\u0018R\u001a\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000e\u00a8\u0006\u0019"}, d2 = {"Lcom/example/library/my_playlist_item/LibraryMyPlaylistItemViewModel;", "Landroidx/lifecycle/ViewModel;", "myPlaylistDBRepository", "Lcom/example/domain/repository/MyPlaylistDBRepository;", "mediaPlaybackManager", "Lcom/example/media/manager/MediaPlaybackManager;", "(Lcom/example/domain/repository/MyPlaylistDBRepository;Lcom/example/media/manager/MediaPlaybackManager;)V", "_myPlaylistItems", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/example/domain/model/youtube/video/Video;", "myPlaylistItems", "Lkotlinx/coroutines/flow/StateFlow;", "getMyPlaylistItems", "()Lkotlinx/coroutines/flow/StateFlow;", "deleteVideo", "Lkotlinx/coroutines/Job;", "playlistId", "", "video", "getVideosForPlaylist", "onMediaItemClicked", "", "clickedIndex", "", "library_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class LibraryMyPlaylistItemViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.MyPlaylistDBRepository myPlaylistDBRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.media.manager.MediaPlaybackManager mediaPlaybackManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.domain.model.youtube.video.Video>> _myPlaylistItems = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.youtube.video.Video>> myPlaylistItems = null;
    
    @javax.inject.Inject()
    public LibraryMyPlaylistItemViewModel(@org.jetbrains.annotations.NotNull()
    com.example.domain.repository.MyPlaylistDBRepository myPlaylistDBRepository, @org.jetbrains.annotations.NotNull()
    com.example.media.manager.MediaPlaybackManager mediaPlaybackManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.youtube.video.Video>> getMyPlaylistItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job getVideosForPlaylist(long playlistId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job deleteVideo(long playlistId, @org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video video) {
        return null;
    }
    
    public final void onMediaItemClicked(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video video, int clickedIndex) {
    }
}