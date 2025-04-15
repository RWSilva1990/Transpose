package com.example.data.di;

import com.example.data.local.repository.MyPlaylistDBRepositoryImpl;
import com.example.data.newpipe.repository.channel.ChannelRepositoryImpl;
import com.example.data.newpipe.repository.playlist.PlaylistRepositoryImpl;
import com.example.data.newpipe.repository.search.SearchRepositoryImpl;
import com.example.data.newpipe.repository.video.VideoRepositoryImpl;
import com.example.data.remote.repository.SuggestionKeywordRepositoryImpl;
import com.example.domain.repository.LocalFileRepository;
import com.example.domain.repository.MyPlaylistDBRepository;
import com.example.domain.repository.NewPipeRepository;
import com.example.domain.repository.SuggestionKeywordRepository;
import com.example.data.repository.LocalFileRepositoryImpl;
import com.example.domain.repository.ChannelRepository;
import com.example.domain.repository.PlaylistRepository;
import com.example.domain.repository.SearchRepository;
import com.example.domain.repository.VideoRepository;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\'J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\'J\u0010\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012H\'J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0016H\'J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aH\'J\u0010\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u001eH\'\u00a8\u0006\u001f"}, d2 = {"Lcom/example/data/di/RepositoryModule;", "", "()V", "bindChannelRepository", "Lcom/example/domain/repository/ChannelRepository;", "channelRepositoryImpl", "Lcom/example/data/newpipe/repository/channel/ChannelRepositoryImpl;", "bindLocalFileRepository", "Lcom/example/domain/repository/LocalFileRepository;", "localFileRepositoryImpl", "Lcom/example/data/repository/LocalFileRepositoryImpl;", "bindMyPlaylistDBRepository", "Lcom/example/domain/repository/MyPlaylistDBRepository;", "myPlaylistDBRepositoryImpl", "Lcom/example/data/local/repository/MyPlaylistDBRepositoryImpl;", "bindPlaylistRepository", "Lcom/example/domain/repository/PlaylistRepository;", "playlistRepositoryImpl", "Lcom/example/data/newpipe/repository/playlist/PlaylistRepositoryImpl;", "bindSearchRepository", "Lcom/example/domain/repository/SearchRepository;", "searchRepositoryImpl", "Lcom/example/data/newpipe/repository/search/SearchRepositoryImpl;", "bindSuggestionKeywordRepository", "Lcom/example/domain/repository/SuggestionKeywordRepository;", "suggestionKeywordRepositoryImpl", "Lcom/example/data/remote/repository/SuggestionKeywordRepositoryImpl;", "bindVideoRepository", "Lcom/example/domain/repository/VideoRepository;", "videoRepositoryImpl", "Lcom/example/data/newpipe/repository/video/VideoRepositoryImpl;", "data_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class RepositoryModule {
    
    public RepositoryModule() {
        super();
    }
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.domain.repository.LocalFileRepository bindLocalFileRepository(@org.jetbrains.annotations.NotNull()
    com.example.data.repository.LocalFileRepositoryImpl localFileRepositoryImpl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.domain.repository.SuggestionKeywordRepository bindSuggestionKeywordRepository(@org.jetbrains.annotations.NotNull()
    com.example.data.remote.repository.SuggestionKeywordRepositoryImpl suggestionKeywordRepositoryImpl);
    
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.domain.repository.MyPlaylistDBRepository bindMyPlaylistDBRepository(@org.jetbrains.annotations.NotNull()
    com.example.data.local.repository.MyPlaylistDBRepositoryImpl myPlaylistDBRepositoryImpl);
    
    @dagger.Binds()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.domain.repository.SearchRepository bindSearchRepository(@org.jetbrains.annotations.NotNull()
    com.example.data.newpipe.repository.search.SearchRepositoryImpl searchRepositoryImpl);
    
    @dagger.Binds()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.domain.repository.VideoRepository bindVideoRepository(@org.jetbrains.annotations.NotNull()
    com.example.data.newpipe.repository.video.VideoRepositoryImpl videoRepositoryImpl);
    
    @dagger.Binds()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.domain.repository.PlaylistRepository bindPlaylistRepository(@org.jetbrains.annotations.NotNull()
    com.example.data.newpipe.repository.playlist.PlaylistRepositoryImpl playlistRepositoryImpl);
    
    @dagger.Binds()
    @org.jetbrains.annotations.NotNull()
    public abstract com.example.domain.repository.ChannelRepository bindChannelRepository(@org.jetbrains.annotations.NotNull()
    com.example.data.newpipe.repository.channel.ChannelRepositoryImpl channelRepositoryImpl);
}