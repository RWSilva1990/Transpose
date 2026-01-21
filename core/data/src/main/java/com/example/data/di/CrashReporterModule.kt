package com.example.data.di

import com.example.data.firebase.crashlytics.FirebaseCrashReporter
import com.example.util.CrashReporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CrashReporterModule {

    @Binds
    @Singleton
    abstract fun bindCrashReporter(
        firebaseCrashReporter: FirebaseCrashReporter
    ): CrashReporter
}
