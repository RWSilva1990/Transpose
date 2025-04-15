package com.example.media;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MergingMediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy;
import com.example.util.Logger;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0016J\b\u0010\u000f\u001a\u00020\u0010H\u0016J\u0010\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\bH\u0016J\u0010\u0010\u0012\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\nH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/example/media/CustomMediaSourceFactory;", "Landroidx/media3/exoplayer/source/MediaSource$Factory;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "dataSourceFactory", "Landroidx/media3/datasource/DefaultDataSource$Factory;", "drmSessionManagerProvider", "Landroidx/media3/exoplayer/drm/DrmSessionManagerProvider;", "loadErrorHandlingPolicy", "Landroidx/media3/exoplayer/upstream/LoadErrorHandlingPolicy;", "createMediaSource", "Landroidx/media3/exoplayer/source/MediaSource;", "mediaItem", "Landroidx/media3/common/MediaItem;", "getSupportedTypes", "", "setDrmSessionManagerProvider", "setLoadErrorHandlingPolicy", "media_debug"})
@androidx.annotation.OptIn(markerClass = {androidx.media3.common.util.UnstableApi.class})
public final class CustomMediaSourceFactory implements androidx.media3.exoplayer.source.MediaSource.Factory {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.media3.datasource.DefaultDataSource.Factory dataSourceFactory = null;
    @org.jetbrains.annotations.Nullable()
    private androidx.media3.exoplayer.drm.DrmSessionManagerProvider drmSessionManagerProvider;
    @org.jetbrains.annotations.Nullable()
    private androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy loadErrorHandlingPolicy;
    
    public CustomMediaSourceFactory(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.media3.exoplayer.source.MediaSource.Factory setDrmSessionManagerProvider(@org.jetbrains.annotations.NotNull()
    androidx.media3.exoplayer.drm.DrmSessionManagerProvider drmSessionManagerProvider) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.media3.exoplayer.source.MediaSource.Factory setLoadErrorHandlingPolicy(@org.jetbrains.annotations.NotNull()
    androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy loadErrorHandlingPolicy) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public int[] getSupportedTypes() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public androidx.media3.exoplayer.source.MediaSource createMediaSource(@org.jetbrains.annotations.NotNull()
    androidx.media3.common.MediaItem mediaItem) {
        return null;
    }
}