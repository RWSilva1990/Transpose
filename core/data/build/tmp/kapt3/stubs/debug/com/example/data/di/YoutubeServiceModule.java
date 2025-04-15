package com.example.data.di;

import com.example.data.newpipe.YoutubeServiceManager;
import com.example.data.newpipe.downloader.NewPipeDownloader;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.services.youtube.YoutubeService;
import javax.inject.Singleton;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0004H\u0007\u00a8\u0006\b"}, d2 = {"Lcom/example/data/di/YoutubeServiceModule;", "", "()V", "provideYoutubeService", "Lorg/schabi/newpipe/extractor/services/youtube/YoutubeService;", "provideYoutubeServiceManager", "Lcom/example/data/newpipe/YoutubeServiceManager;", "youtubeService", "data_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class YoutubeServiceModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.data.di.YoutubeServiceModule INSTANCE = null;
    
    private YoutubeServiceModule() {
        super();
    }
    
    @javax.inject.Singleton()
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final org.schabi.newpipe.extractor.services.youtube.YoutubeService provideYoutubeService() {
        return null;
    }
    
    @javax.inject.Singleton()
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.example.data.newpipe.YoutubeServiceManager provideYoutubeServiceManager(@org.jetbrains.annotations.NotNull()
    org.schabi.newpipe.extractor.services.youtube.YoutubeService youtubeService) {
        return null;
    }
}