package com.example.media.di;

import android.content.Context;
import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import com.example.media.manager.AudioEffectsManager;
import com.example.media.CustomMediaSourceFactory;
import com.example.media.manager.MediaControllerProvider;
import com.example.media.manager.MediaPlaybackManager;
import com.example.media.audio_effect.AudioEffectHandlerImpl;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0012\u0010\r\u001a\u00020\u000e2\b\b\u0001\u0010\u000f\u001a\u00020\u0010H\u0007J\"\u0010\u0011\u001a\u00020\u00122\b\b\u0001\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u000eH\u0007J\u0012\u0010\u0015\u001a\u00020\f2\b\b\u0001\u0010\u000f\u001a\u00020\u0010H\u0007J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u000b\u001a\u00020\fH\u0007J\u0010\u0010\u0018\u001a\u00020\b2\u0006\u0010\u0019\u001a\u00020\u0012H\u0007\u00a8\u0006\u001a"}, d2 = {"Lcom/example/media/di/MediaModule;", "", "()V", "provideAudioAttributes", "Landroidx/media3/common/AudioAttributes;", "provideAudioEffectHandler", "Lcom/example/media/audio_effect/AudioEffectHandlerImpl;", "player", "Landroidx/media3/common/Player;", "provideAudioEffectsManager", "Lcom/example/media/manager/AudioEffectsManager;", "mediaControllerProvider", "Lcom/example/media/manager/MediaControllerProvider;", "provideCustomMediaSourceFactory", "Lcom/example/media/CustomMediaSourceFactory;", "context", "Landroid/content/Context;", "provideExoPlayer", "Landroidx/media3/exoplayer/ExoPlayer;", "audioAttributes", "customMediaSourceFactory", "provideMediaControllerProvider", "provideMediaPlaybackManager", "Lcom/example/media/manager/MediaPlaybackManager;", "providePlayer", "exoPlayer", "media_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class MediaModule {
    
    public MediaModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final androidx.media3.common.AudioAttributes provideAudioAttributes() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    @org.jetbrains.annotations.NotNull()
    public final com.example.media.CustomMediaSourceFactory provideCustomMediaSourceFactory(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
    @org.jetbrains.annotations.NotNull()
    public final androidx.media3.exoplayer.ExoPlayer provideExoPlayer(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    androidx.media3.common.AudioAttributes audioAttributes, @org.jetbrains.annotations.NotNull()
    com.example.media.CustomMediaSourceFactory customMediaSourceFactory) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final androidx.media3.common.Player providePlayer(@org.jetbrains.annotations.NotNull()
    androidx.media3.exoplayer.ExoPlayer exoPlayer) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.example.media.audio_effect.AudioEffectHandlerImpl provideAudioEffectHandler(@org.jetbrains.annotations.NotNull()
    androidx.media3.common.Player player) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.example.media.manager.MediaControllerProvider provideMediaControllerProvider(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @javax.inject.Singleton()
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.example.media.manager.MediaPlaybackManager provideMediaPlaybackManager(@org.jetbrains.annotations.NotNull()
    com.example.media.manager.MediaControllerProvider mediaControllerProvider) {
        return null;
    }
    
    @javax.inject.Singleton()
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.example.media.manager.AudioEffectsManager provideAudioEffectsManager(@org.jetbrains.annotations.NotNull()
    com.example.media.manager.MediaControllerProvider mediaControllerProvider) {
        return null;
    }
}