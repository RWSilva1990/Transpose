package com.example.media;

import android.content.Context;
import android.os.Bundle;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.session.CommandButton;
import androidx.media3.session.MediaSession;
import androidx.media3.session.SessionCommand;
import androidx.media3.session.SessionResult;
import com.example.media.audio_effect.AudioEffectHandlerImpl;
import com.example.util.Logger;
import com.google.common.util.concurrent.ListenableFuture;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0002J\u000e\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\bH\u0002J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J2\u0010\u0012\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\u00132\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014H\u0016J\u0018\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019H\u0016J.\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001f0\u00132\u0006\u0010\u001d\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010 \u001a\u00020\u000b2\u0006\u0010!\u001a\u00020\"H\u0016J\u0018\u0010#\u001a\u00020$2\u0006\u0010\u001d\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019H\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006%"}, d2 = {"Lcom/example/media/CustomMediaSessionCallback;", "Landroidx/media3/session/MediaSession$Callback;", "context", "Landroid/content/Context;", "audioEffectHandlerImpl", "Lcom/example/media/audio_effect/AudioEffectHandlerImpl;", "(Landroid/content/Context;Lcom/example/media/audio_effect/AudioEffectHandlerImpl;)V", "createCommandButton", "", "Landroidx/media3/session/CommandButton;", "createMyCustomCommands", "Landroidx/media3/session/SessionCommand;", "createOptimizedSource", "Landroidx/media3/exoplayer/source/MediaSource;", "uri", "", "dataSourceFactory", "Landroidx/media3/datasource/DataSource$Factory;", "onAddMediaItems", "Lcom/google/common/util/concurrent/ListenableFuture;", "", "Landroidx/media3/common/MediaItem;", "mediaSession", "Landroidx/media3/session/MediaSession;", "controller", "Landroidx/media3/session/MediaSession$ControllerInfo;", "mediaItems", "onConnect", "Landroidx/media3/session/MediaSession$ConnectionResult;", "session", "onCustomCommand", "Landroidx/media3/session/SessionResult;", "customCommand", "args", "Landroid/os/Bundle;", "onPostConnect", "", "media_debug"})
@androidx.media3.common.util.UnstableApi()
public final class CustomMediaSessionCallback implements androidx.media3.session.MediaSession.Callback {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.media.audio_effect.AudioEffectHandlerImpl audioEffectHandlerImpl = null;
    
    @javax.inject.Inject()
    public CustomMediaSessionCallback(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.example.media.audio_effect.AudioEffectHandlerImpl audioEffectHandlerImpl) {
        super();
    }
    
    private final java.util.List<androidx.media3.session.SessionCommand> createMyCustomCommands() {
        return null;
    }
    
    private final java.util.List<androidx.media3.session.CommandButton> createCommandButton() {
        return null;
    }
    
    @java.lang.Override()
    public void onPostConnect(@org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession session, @org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession.ControllerInfo controller) {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.google.common.util.concurrent.ListenableFuture<java.util.List<androidx.media3.common.MediaItem>> onAddMediaItems(@org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession mediaSession, @org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession.ControllerInfo controller, @org.jetbrains.annotations.NotNull()
    java.util.List<androidx.media3.common.MediaItem> mediaItems) {
        return null;
    }
    
    private final androidx.media3.exoplayer.source.MediaSource createOptimizedSource(java.lang.String uri, androidx.media3.datasource.DataSource.Factory dataSourceFactory) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.media3.session.MediaSession.ConnectionResult onConnect(@org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession session, @org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession.ControllerInfo controller) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.google.common.util.concurrent.ListenableFuture<androidx.media3.session.SessionResult> onCustomCommand(@org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession session, @org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession.ControllerInfo controller, @org.jetbrains.annotations.NotNull()
    androidx.media3.session.SessionCommand customCommand, @org.jetbrains.annotations.NotNull()
    android.os.Bundle args) {
        return null;
    }
}