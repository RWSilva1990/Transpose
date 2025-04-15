package com.example.data.repository;

import com.example.data.local.preferences.PlaybackPreferences;
import com.example.domain.model.preferences.RepeatMode;
import com.example.domain.repository.PlaybackPreferencesRepository;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0007H\u0096@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\u000bH\u0096@\u00a2\u0006\u0002\u0010\u0013R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u001a\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0006X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\t\u00a8\u0006\u0014"}, d2 = {"Lcom/example/data/repository/PlaybackPreferencesRepositoryImpl;", "Lcom/example/domain/repository/PlaybackPreferencesRepository;", "playbackPreferences", "Lcom/example/data/local/preferences/PlaybackPreferences;", "(Lcom/example/data/local/preferences/PlaybackPreferences;)V", "repeatMode", "Lkotlinx/coroutines/flow/Flow;", "Lcom/example/domain/model/preferences/RepeatMode;", "getRepeatMode", "()Lkotlinx/coroutines/flow/Flow;", "shuffleMode", "", "getShuffleMode", "setRepeatMode", "", "mode", "(Lcom/example/domain/model/preferences/RepeatMode;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setShuffleMode", "enabled", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "data_debug"})
public final class PlaybackPreferencesRepositoryImpl implements com.example.domain.repository.PlaybackPreferencesRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.data.local.preferences.PlaybackPreferences playbackPreferences = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<com.example.domain.model.preferences.RepeatMode> repeatMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.lang.Boolean> shuffleMode = null;
    
    public PlaybackPreferencesRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.example.data.local.preferences.PlaybackPreferences playbackPreferences) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object setRepeatMode(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.preferences.RepeatMode mode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object setShuffleMode(boolean enabled, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<com.example.domain.model.preferences.RepeatMode> getRepeatMode() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.Flow<java.lang.Boolean> getShuffleMode() {
        return null;
    }
}