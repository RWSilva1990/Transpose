package com.example.domain.repository;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\bf\u0018\u00002\u00020\u0001J\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0004H\u00a6@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u000f\u001a\u00020\bH\u00a6@\u00a2\u0006\u0002\u0010\u0010R\u0018\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0018\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/example/domain/repository/PlaybackPreferencesRepository;", "", "repeatMode", "Lkotlinx/coroutines/flow/Flow;", "Lcom/example/domain/model/preferences/RepeatMode;", "getRepeatMode", "()Lkotlinx/coroutines/flow/Flow;", "shuffleMode", "", "getShuffleMode", "setRepeatMode", "", "mode", "(Lcom/example/domain/model/preferences/RepeatMode;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setShuffleMode", "enabled", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "domain_debug"})
public abstract interface PlaybackPreferencesRepository {
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object setRepeatMode(@org.jetbrains.annotations.NotNull()
    com.example.domain.model.preferences.RepeatMode mode, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object setShuffleMode(boolean enabled, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<com.example.domain.model.preferences.RepeatMode> getRepeatMode();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Boolean> getShuffleMode();
}