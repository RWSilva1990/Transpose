package com.example.data.di

import com.example.data.newpipe.YoutubeServiceManager
import com.example.data.newpipe.downloader.NewPipeDownloader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object YoutubeServiceModule {

    @Singleton
    @Provides
    fun provideYoutubeService(): YoutubeService {
        return ServiceList.YouTube.also {
            NewPipe.init(NewPipeDownloader())
        }
    }

    @Singleton
    @Provides
    fun provideYoutubeServiceManager(
        youtubeService: YoutubeService
    ): YoutubeServiceManager {
        return YoutubeServiceManager(youtubeService)
    }
}