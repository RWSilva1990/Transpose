package com.example.data.di

import android.content.Context
import com.example.data.newpipe.repository.NewPipeRepositoryImpl
import com.example.domain.repository.LocalFileRepository
import com.example.domain.repository.NewPipeRepository
import com.example.transpose.data.repository.local_file.LocalFileRepositoryImpl
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
    abstract fun bindNewPipeRepository(
        newPipeRepositoryImpl: NewPipeRepositoryImpl
    ): NewPipeRepository


    @Binds
    @Singleton
    abstract fun bindLocalFileRepository(
        localFileRepositoryImpl: LocalFileRepositoryImpl
    ): LocalFileRepository


}