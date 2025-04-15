package com.example.media;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.OptIn;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import com.example.media.audio_effect.AudioEffectHandlerImpl;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0011\u001a\u00020\u0012H\u0002J\b\u0010\u0013\u001a\u00020\u0014H\u0002J\b\u0010\u0015\u001a\u00020\u0014H\u0017J\b\u0010\u0016\u001a\u00020\u0014H\u0016J\u0012\u0010\u0017\u001a\u0004\u0018\u00010\u00102\u0006\u0010\u0018\u001a\u00020\u0019H\u0016J\u0012\u0010\u001a\u001a\u00020\u00142\b\u0010\u001b\u001a\u0004\u0018\u00010\u001cH\u0016R\u001e\u0010\u0003\u001a\u00020\u00048\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u001e\u0010\t\u001a\u00020\n8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/example/media/MediaService;", "Landroidx/media3/session/MediaSessionService;", "()V", "audioEffectHandlerImpl", "Lcom/example/media/audio_effect/AudioEffectHandlerImpl;", "getAudioEffectHandlerImpl", "()Lcom/example/media/audio_effect/AudioEffectHandlerImpl;", "setAudioEffectHandlerImpl", "(Lcom/example/media/audio_effect/AudioEffectHandlerImpl;)V", "exoPlayer", "Landroidx/media3/exoplayer/ExoPlayer;", "getExoPlayer", "()Landroidx/media3/exoplayer/ExoPlayer;", "setExoPlayer", "(Landroidx/media3/exoplayer/ExoPlayer;)V", "mediaSession", "Landroidx/media3/session/MediaSession;", "createAppLauncherPendingIntent", "Landroid/app/PendingIntent;", "createNotificationChannel", "", "onCreate", "onDestroy", "onGetSession", "controllerInfo", "Landroidx/media3/session/MediaSession$ControllerInfo;", "onTaskRemoved", "rootIntent", "Landroid/content/Intent;", "media_debug"})
public final class MediaService extends androidx.media3.session.MediaSessionService {
    @org.jetbrains.annotations.Nullable()
    private androidx.media3.session.MediaSession mediaSession;
    @javax.inject.Inject()
    public androidx.media3.exoplayer.ExoPlayer exoPlayer;
    @javax.inject.Inject()
    public com.example.media.audio_effect.AudioEffectHandlerImpl audioEffectHandlerImpl;
    
    public MediaService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.media3.exoplayer.ExoPlayer getExoPlayer() {
        return null;
    }
    
    public final void setExoPlayer(@org.jetbrains.annotations.NotNull()
    androidx.media3.exoplayer.ExoPlayer p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.media.audio_effect.AudioEffectHandlerImpl getAudioEffectHandlerImpl() {
        return null;
    }
    
    public final void setAudioEffectHandlerImpl(@org.jetbrains.annotations.NotNull()
    com.example.media.audio_effect.AudioEffectHandlerImpl p0) {
    }
    
    @java.lang.Override()
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    public void onCreate() {
    }
    
    private final android.app.PendingIntent createAppLauncherPendingIntent() {
        return null;
    }
    
    private final void createNotificationChannel() {
    }
    
    @java.lang.Override()
    public void onTaskRemoved(@org.jetbrains.annotations.Nullable()
    android.content.Intent rootIntent) {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public androidx.media3.session.MediaSession onGetSession(@org.jetbrains.annotations.NotNull()
    androidx.media3.session.MediaSession.ControllerInfo controllerInfo) {
        return null;
    }
}