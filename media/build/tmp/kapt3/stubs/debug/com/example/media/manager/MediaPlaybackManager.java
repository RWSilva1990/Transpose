package com.example.media.manager;

import android.net.Uri;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.session.MediaController;
import com.example.domain.model.preferences.RepeatMode;
import com.example.domain.model.youtube.playlist.Playlist;
import com.example.domain.model.youtube.video.Video;
import com.example.domain.model.youtube.video_detail.VideoDetail;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u008a\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0017\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B#\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0007J\u0006\u00103\u001a\u000204J\u0010\u00105\u001a\u0002062\u0006\u00107\u001a\u00020\rH\u0002J\u001c\u00108\u001a\b\u0012\u0004\u0012\u0002060\f2\f\u00109\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u0002J\u0006\u0010:\u001a\u00020;J\u0018\u0010<\u001a\u0002042\b\u0010=\u001a\u0004\u0018\u000106H\u0082@\u00a2\u0006\u0002\u0010>J\b\u0010?\u001a\u000204H\u0002J\u0006\u0010@\u001a\u00020\u0016J*\u0010A\u001a\u0002042\u0006\u0010B\u001a\u00020\r2\u0010\b\u0002\u0010C\u001a\n\u0012\u0004\u0012\u00020\r\u0018\u00010\f2\b\b\u0002\u0010D\u001a\u00020\u000fJ\u0006\u0010E\u001a\u000204J\u0006\u0010F\u001a\u000204J\u0010\u0010G\u001a\u0002042\b\u0010H\u001a\u0004\u0018\u00010\u0011J\u000e\u0010I\u001a\u0002042\u0006\u0010J\u001a\u00020;J\u000e\u0010K\u001a\u0002042\u0006\u0010L\u001a\u00020\u0016J\u0006\u0010M\u001a\u000204J\u0010\u0010N\u001a\u0002042\u0006\u0010O\u001a\u00020(H\u0002J\u0018\u0010P\u001a\u0002042\u0006\u0010Q\u001a\u00020,2\b\u0010R\u001a\u0004\u0018\u00010SJ\u0010\u0010T\u001a\u0002042\u0006\u0010O\u001a\u00020(H\u0002J\u0012\u0010U\u001a\u0002042\b\u0010V\u001a\u0004\u0018\u00010\rH\u0002R\u000e\u0010\b\u001a\u00020\tX\u0082D\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00110\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\t0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0013\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\t0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00160\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001aR\u0019\u0010\u001d\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00110\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001aR\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\t0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001aR\u0019\u0010!\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001aR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010#\u001a\b\u0012\u0004\u0012\u00020\t0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001aR\u0017\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00160\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001aR\u000e\u0010&\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\'\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010(0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001aR\u001a\u0010*\u001a\u000e\u0012\u0004\u0012\u00020,\u0012\u0004\u0012\u00020\r0+X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010-\u001a\u00020.X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010/\u001a\u000200X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u00101\u001a\u0004\u0018\u000102X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006W"}, d2 = {"Lcom/example/media/manager/MediaPlaybackManager;", "", "controllerProvider", "Lcom/example/media/manager/MediaControllerProvider;", "defaultDispatcher", "Lkotlinx/coroutines/CoroutineDispatcher;", "mainDispatcher", "(Lcom/example/media/manager/MediaControllerProvider;Lkotlinx/coroutines/CoroutineDispatcher;Lkotlinx/coroutines/CoroutineDispatcher;)V", "STATE_UPDATE_THROTTLE_MS", "", "_currentPlaylist", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/example/domain/model/youtube/video/Video;", "_currentPlaylistIndex", "", "_currentPlaylistInfo", "Lcom/example/domain/model/youtube/playlist/Playlist;", "_currentPosition", "_currentVideoData", "_duration", "_isPlaying", "", "currentPlaylist", "Lkotlinx/coroutines/flow/StateFlow;", "getCurrentPlaylist", "()Lkotlinx/coroutines/flow/StateFlow;", "currentPlaylistIndex", "getCurrentPlaylistIndex", "currentPlaylistInfo", "getCurrentPlaylistInfo", "currentPosition", "getCurrentPosition", "currentVideoData", "getCurrentVideoData", "duration", "getDuration", "isPlaying", "lastStateUpdateTime", "mediaControllerFlow", "Landroidx/media3/session/MediaController;", "getMediaControllerFlow", "mediaItemCache", "", "", "playerListener", "Landroidx/media3/common/Player$Listener;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "updateMediaItemJob", "Lkotlinx/coroutines/Job;", "clearCurrentPlayback", "", "createMediaItem", "Landroidx/media3/common/MediaItem;", "video", "createMediaItems", "videoList", "getCurrentRepeatMode", "Lcom/example/domain/model/preferences/RepeatMode;", "handleMediaItemTransition", "mediaItem", "(Landroidx/media3/common/MediaItem;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "handleTrackEnded", "isShuffleModeEnabled", "onMediaItemClick", "clickedItem", "playlistItems", "clickedIndex", "playPause", "release", "setCurrentPlaylistInfo", "playlist", "setRepeatMode", "repeatMode", "setShuffleMode", "enabled", "stopPlayback", "throttledUpdatePlaybackState", "controller", "updateMediaItemWithFullInfo", "itemId", "videoDetail", "Lcom/example/domain/model/youtube/video_detail/VideoDetail;", "updatePlaybackState", "updateUiForPlayingMediaItem", "metadata", "media_debug"})
public final class MediaPlaybackManager {
    @org.jetbrains.annotations.NotNull()
    private final com.example.media.manager.MediaControllerProvider controllerProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineDispatcher defaultDispatcher = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineDispatcher mainDispatcher = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job updateMediaItemJob;
    private long lastStateUpdateTime = 0L;
    private final long STATE_UPDATE_THROTTLE_MS = 100L;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, com.example.domain.model.youtube.video.Video> mediaItemCache = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<androidx.media3.session.MediaController> mediaControllerFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isPlaying = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isPlaying = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _currentPosition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Long> currentPosition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _duration = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Long> duration = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.domain.model.youtube.playlist.Playlist> _currentPlaylistInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.playlist.Playlist> currentPlaylistInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.domain.model.youtube.video.Video> _currentVideoData = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.video.Video> currentVideoData = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.domain.model.youtube.video.Video>> _currentPlaylist = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.youtube.video.Video>> currentPlaylist = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _currentPlaylistIndex = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> currentPlaylistIndex = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.media3.common.Player.Listener playerListener = null;
    
    @javax.inject.Inject()
    public MediaPlaybackManager(@org.jetbrains.annotations.NotNull()
    com.example.media.manager.MediaControllerProvider controllerProvider, @org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.CoroutineDispatcher defaultDispatcher, @org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.CoroutineDispatcher mainDispatcher) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<androidx.media3.session.MediaController> getMediaControllerFlow() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isPlaying() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Long> getCurrentPosition() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Long> getDuration() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.playlist.Playlist> getCurrentPlaylistInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.video.Video> getCurrentVideoData() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.youtube.video.Video>> getCurrentPlaylist() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getCurrentPlaylistIndex() {
        return null;
    }
    
    private final void throttledUpdatePlaybackState(androidx.media3.session.MediaController controller) {
    }
    
    private final void updatePlaybackState(androidx.media3.session.MediaController controller) {
    }
    
    private final void handleTrackEnded() {
    }
    
    private final java.lang.Object handleMediaItemTransition(androidx.media3.common.MediaItem mediaItem, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void updateUiForPlayingMediaItem(com.example.domain.model.youtube.video.Video metadata) {
    }
    
    public final void onMediaItemClick(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video clickedItem, @org.jetbrains.annotations.Nullable()
    java.util.List<com.example.domain.model.youtube.video.Video> playlistItems, int clickedIndex) {
    }
    
    public final void clearCurrentPlayback() {
    }
    
    private final androidx.media3.common.MediaItem createMediaItem(com.example.domain.model.youtube.video.Video video) {
        return null;
    }
    
    private final java.util.List<androidx.media3.common.MediaItem> createMediaItems(java.util.List<com.example.domain.model.youtube.video.Video> videoList) {
        return null;
    }
    
    public final void updateMediaItemWithFullInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String itemId, @org.jetbrains.annotations.Nullable()
    com.example.domain.model.youtube.video_detail.VideoDetail videoDetail) {
    }
    
    public final void setCurrentPlaylistInfo(@org.jetbrains.annotations.Nullable()
    com.example.domain.model.youtube.playlist.Playlist playlist) {
    }
    
    public final void playPause() {
    }
    
    public final void stopPlayback() {
    }
    
    public final void setRepeatMode(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.preferences.RepeatMode repeatMode) {
    }
    
    public final void setShuffleMode(boolean enabled) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.domain.model.preferences.RepeatMode getCurrentRepeatMode() {
        return null;
    }
    
    public final boolean isShuffleModeEnabled() {
        return false;
    }
    
    public final void release() {
    }
}