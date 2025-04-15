package com.example.main;

import android.content.Context;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.media3.session.MediaController;
import com.example.domain.model.library.MyPlaylist;
import com.example.domain.model.preferences.RepeatMode;
import com.example.domain.model.youtube.video.Video;
import com.example.domain.model.youtube.video_detail.VideoDetail;
import com.example.domain.repository.ChannelRepository;
import com.example.domain.repository.MyPlaylistDBRepository;
import com.example.domain.repository.PlaybackPreferencesRepository;
import com.example.domain.repository.SuggestionKeywordRepository;
import com.example.domain.repository.VideoRepository;
import com.example.media.manager.AudioEffectsManager;
import com.example.media.manager.MediaPlaybackManager;
import com.example.util.Logger;
import com.example.util.PermissionUtils;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.FlowPreview;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00b2\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0002\u0010\u0011\n\u0002\b\n\b\u0007\u0018\u00002\u00020\u0001BQ\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u0012\u0006\u0010\u0010\u001a\u00020\u0011\u0012\b\b\u0001\u0010\u0012\u001a\u00020\u0013\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010D\u001a\u00020E2\u0006\u0010F\u001a\u00020&2\u0006\u0010G\u001a\u00020HJ\u0006\u0010I\u001a\u00020JJ\b\u0010K\u001a\u00020JH\u0002J\u000e\u0010L\u001a\u00020E2\u0006\u0010M\u001a\u00020:J\u0010\u0010N\u001a\u00020E2\u0006\u0010O\u001a\u00020:H\u0002J\u0006\u0010P\u001a\u00020EJ\u0006\u0010Q\u001a\u00020JJ\u0006\u0010R\u001a\u00020JJ*\u0010S\u001a\u00020J2\u0006\u0010T\u001a\u00020&2\u0010\b\u0002\u0010U\u001a\n\u0012\u0004\u0012\u00020&\u0018\u00010\u00192\b\b\u0002\u0010V\u001a\u00020\u001fJ\u0006\u0010W\u001a\u00020JJ\u0006\u0010X\u001a\u00020JJ\u0006\u0010Y\u001a\u00020JJ \u0010Z\u001a\u00020J2\u0018\u0010[\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020:0]\u0012\u0004\u0012\u00020J0\\J\u000e\u0010^\u001a\u00020J2\u0006\u0010_\u001a\u00020\u001cJ\u0006\u0010`\u001a\u00020JJ\u000e\u0010a\u001a\u00020J2\u0006\u0010b\u001a\u00020:J\u0006\u0010c\u001a\u00020JJ\u0006\u0010d\u001a\u00020JJ\u0006\u0010e\u001a\u00020JJ\u0006\u0010f\u001a\u00020JR\u0016\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00170\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u00190\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u001f0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0019\u0010\"\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010#0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010!R\u001d\u0010%\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020&0\u00190\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010!R\u0019\u0010(\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010&0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010!R\u0019\u0010*\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00170\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010!R\u0017\u0010,\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010!R\u0019\u0010-\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010.0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b/\u0010!R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u00100\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001a0\u00190\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u0010!R\u0017\u00102\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b3\u0010!R\u0017\u00104\u001a\b\u0012\u0004\u0012\u00020\u001f0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010!R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u00106\u001a\b\u0012\u0004\u0012\u0002070\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b8\u0010!R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u00109\u001a\b\u0012\u0004\u0012\u00020:0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b;\u0010!R\u0017\u0010<\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\b=\u0010!R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R#\u0010>\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020:0\u00190\u001e\u00a2\u0006\u000e\n\u0000\u0012\u0004\b?\u0010@\u001a\u0004\bA\u0010!R\u0017\u0010B\u001a\b\u0012\u0004\u0012\u00020\u001f0\u001e\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010!R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006g"}, d2 = {"Lcom/example/main/MainViewModel;", "Landroidx/lifecycle/ViewModel;", "savedStateHandle", "Landroidx/lifecycle/SavedStateHandle;", "mediaPlaybackManager", "Lcom/example/media/manager/MediaPlaybackManager;", "audioEffectsManager", "Lcom/example/media/manager/AudioEffectsManager;", "suggestionKeywordRepository", "Lcom/example/domain/repository/SuggestionKeywordRepository;", "videoRepository", "Lcom/example/domain/repository/VideoRepository;", "myPlaylistDBRepository", "Lcom/example/domain/repository/MyPlaylistDBRepository;", "channelRepository", "Lcom/example/domain/repository/ChannelRepository;", "playbackPreferencesRepository", "Lcom/example/domain/repository/PlaybackPreferencesRepository;", "context", "Landroid/content/Context;", "(Landroidx/lifecycle/SavedStateHandle;Lcom/example/media/manager/MediaPlaybackManager;Lcom/example/media/manager/AudioEffectsManager;Lcom/example/domain/repository/SuggestionKeywordRepository;Lcom/example/domain/repository/VideoRepository;Lcom/example/domain/repository/MyPlaylistDBRepository;Lcom/example/domain/repository/ChannelRepository;Lcom/example/domain/repository/PlaybackPreferencesRepository;Landroid/content/Context;)V", "_currentVideoDetail", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/domain/model/youtube/video_detail/VideoDetail;", "_myPlaylists", "", "Lcom/example/domain/model/library/MyPlaylist;", "_permissionGranted", "", "currentPlaylistIndex", "Lkotlinx/coroutines/flow/StateFlow;", "", "getCurrentPlaylistIndex", "()Lkotlinx/coroutines/flow/StateFlow;", "currentPlaylistInfo", "Lcom/example/domain/model/youtube/playlist/Playlist;", "getCurrentPlaylistInfo", "currentPlaylistItems", "Lcom/example/domain/model/youtube/video/Video;", "getCurrentPlaylistItems", "currentVideoData", "getCurrentVideoData", "currentVideoDetailData", "getCurrentVideoDetailData", "isPlaying", "mediaControllerFlow", "Landroidx/media3/session/MediaController;", "getMediaControllerFlow", "myPlaylists", "getMyPlaylists", "permissionGranted", "getPermissionGranted", "pitchValue", "getPitchValue", "repeatMode", "Lcom/example/domain/model/preferences/RepeatMode;", "getRepeatMode", "searchQuery", "", "getSearchQuery", "shuffleMode", "getShuffleMode", "suggestionKeywords", "getSuggestionKeywords$annotations", "()V", "getSuggestionKeywords", "tempoValue", "getTempoValue", "addVideoToPlaylist", "Lkotlinx/coroutines/Job;", "video", "playlistId", "", "applyPlaybackSettings", "", "checkPermissions", "fetchChannelInfo", "channelId", "fetchCurrentVideoDetailData", "videoId", "getAllMyPlaylists", "initPitchValue", "initTempoValue", "onMediaItemClick", "clickedItem", "playlistItems", "clickedIndex", "pitchMinusOne", "pitchPlusOne", "playPause", "requestPermissions", "launcher", "Lkotlin/Function1;", "", "setPermissionGranted", "granted", "stopPlayback", "storeSearchQuery", "query", "tempoMinusOne", "tempoPlusOne", "toggleRepeatMode", "toggleShuffleMode", "main_debug"})
@kotlin.OptIn(markerClass = {kotlinx.coroutines.ExperimentalCoroutinesApi.class})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class MainViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.SavedStateHandle savedStateHandle = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.media.manager.MediaPlaybackManager mediaPlaybackManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.media.manager.AudioEffectsManager audioEffectsManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.SuggestionKeywordRepository suggestionKeywordRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.VideoRepository videoRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.MyPlaylistDBRepository myPlaylistDBRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.ChannelRepository channelRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.domain.repository.PlaybackPreferencesRepository playbackPreferencesRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> searchQuery = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> suggestionKeywords = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _permissionGranted = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> permissionGranted = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.preferences.RepeatMode> repeatMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> shuffleMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<androidx.media3.session.MediaController> mediaControllerFlow = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isPlaying = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.video.Video> currentVideoData = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.playlist.Playlist> currentPlaylistInfo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.youtube.video.Video>> currentPlaylistItems = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> currentPlaylistIndex = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.domain.model.youtube.video_detail.VideoDetail> _currentVideoDetail = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.video_detail.VideoDetail> currentVideoDetailData = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.domain.model.library.MyPlaylist>> _myPlaylists = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.library.MyPlaylist>> myPlaylists = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> pitchValue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> tempoValue = null;
    
    @javax.inject.Inject()
    public MainViewModel(@org.jetbrains.annotations.NotNull()
    androidx.lifecycle.SavedStateHandle savedStateHandle, @org.jetbrains.annotations.NotNull()
    com.example.media.manager.MediaPlaybackManager mediaPlaybackManager, @org.jetbrains.annotations.NotNull()
    com.example.media.manager.AudioEffectsManager audioEffectsManager, @org.jetbrains.annotations.NotNull()
    com.example.domain.repository.SuggestionKeywordRepository suggestionKeywordRepository, @org.jetbrains.annotations.NotNull()
    com.example.domain.repository.VideoRepository videoRepository, @org.jetbrains.annotations.NotNull()
    com.example.domain.repository.MyPlaylistDBRepository myPlaylistDBRepository, @org.jetbrains.annotations.NotNull()
    com.example.domain.repository.ChannelRepository channelRepository, @org.jetbrains.annotations.NotNull()
    com.example.domain.repository.PlaybackPreferencesRepository playbackPreferencesRepository, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getSearchQuery() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<java.lang.String>> getSuggestionKeywords() {
        return null;
    }
    
    @kotlin.OptIn(markerClass = {kotlinx.coroutines.FlowPreview.class})
    @java.lang.Deprecated()
    public static void getSuggestionKeywords$annotations() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getPermissionGranted() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.preferences.RepeatMode> getRepeatMode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getShuffleMode() {
        return null;
    }
    
    private final void checkPermissions() {
    }
    
    public final void setPermissionGranted(boolean granted) {
    }
    
    public final void requestPermissions(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String[], kotlin.Unit> launcher) {
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
    public final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.video.Video> getCurrentVideoData() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.playlist.Playlist> getCurrentPlaylistInfo() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.domain.model.youtube.video.Video>> getCurrentPlaylistItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getCurrentPlaylistIndex() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.domain.model.youtube.video_detail.VideoDetail> getCurrentVideoDetailData() {
        return null;
    }
    
    private final kotlinx.coroutines.Job fetchCurrentVideoDetailData(java.lang.String videoId) {
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
    
    public final void playPause() {
    }
    
    public final void stopPlayback() {
    }
    
    public final void onMediaItemClick(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.youtube.video.Video clickedItem, @org.jetbrains.annotations.Nullable()
    java.util.List<com.example.domain.model.youtube.video.Video> playlistItems, int clickedIndex) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getPitchValue() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getTempoValue() {
        return null;
    }
    
    public final void pitchPlusOne() {
    }
    
    public final void pitchMinusOne() {
    }
    
    public final void initPitchValue() {
    }
    
    public final void tempoPlusOne() {
    }
    
    public final void initTempoValue() {
    }
    
    public final void tempoMinusOne() {
    }
    
    public final void storeSearchQuery(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.Job fetchChannelInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String channelId) {
        return null;
    }
    
    public final void toggleRepeatMode() {
    }
    
    public final void toggleShuffleMode() {
    }
    
    public final void applyPlaybackSettings() {
    }
}