package com.example.data.di

import com.example.data.local.repository.MyPlaylistDBRepositoryImpl
import com.example.data.newpipe.repository.channel.ChannelRepositoryImpl
import com.example.data.newpipe.repository.playlist.PlaylistRepositoryImpl
import com.example.data.newpipe.repository.search.SearchRepositoryImpl
import com.example.data.newpipe.repository.video.VideoRepositoryImpl
import com.example.data.remote.repository.SuggestionKeywordRepositoryImpl
import com.example.domain.repository.LocalFileRepository
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.domain.repository.NewPipeRepository
import com.example.domain.repository.SuggestionKeywordRepository
import com.example.data.repository.LocalFileRepositoryImpl
import com.example.domain.repository.ChannelRepository
import com.example.domain.repository.PlaylistRepository
import com.example.domain.repository.SearchRepository
import com.example.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLocalFileRepository(
        localFileRepositoryImpl: LocalFileRepositoryImpl
    ): LocalFileRepository

     @Binds
     @Singleton
     abstract fun bindSuggestionKeywordRepository(
         suggestionKeywordRepositoryImpl: SuggestionKeywordRepositoryImpl
     ): SuggestionKeywordRepository

    @Binds
    @Singleton
    abstract fun bindMyPlaylistDBRepository(
        myPlaylistDBRepositoryImpl: MyPlaylistDBRepositoryImpl
    ): MyPlaylistDBRepository

    @Binds
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    abstract fun bindVideoRepository(
        videoRepositoryImpl: VideoRepositoryImpl
    ): VideoRepository

    @Binds
    abstract fun bindPlaylistRepository(
        playlistRepositoryImpl: PlaylistRepositoryImpl
    ): PlaylistRepository

    @Binds
    abstract fun bindChannelRepository(
        channelRepositoryImpl: ChannelRepositoryImpl
    ): ChannelRepository


}