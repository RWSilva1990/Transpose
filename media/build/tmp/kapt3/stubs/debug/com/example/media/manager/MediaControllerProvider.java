package com.example.media.manager;

import android.content.ComponentName;
import android.content.Context;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import com.example.media.MediaService;
import com.google.common.util.concurrent.MoreExecutors;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\f\u001a\u00020\rH\u0002J\u0006\u0010\u000e\u001a\u00020\rR\u0016\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u000f"}, d2 = {"Lcom/example/media/manager/MediaControllerProvider;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "_mediaController", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Landroidx/media3/session/MediaController;", "mediaController", "Lkotlinx/coroutines/flow/StateFlow;", "getMediaController", "()Lkotlinx/coroutines/flow/StateFlow;", "initializeController", "", "release", "media_debug"})
public final class MediaControllerProvider {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<androidx.media3.session.MediaController> _mediaController = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<androidx.media3.session.MediaController> mediaController = null;
    
    @javax.inject.Inject()
    public MediaControllerProvider(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<androidx.media3.session.MediaController> getMediaController() {
        return null;
    }
    
    private final void initializeController() {
    }
    
    public final void release() {
    }
}