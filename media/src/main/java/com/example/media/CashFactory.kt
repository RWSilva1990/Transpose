package com.example.media

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.BaseDataSource
import com.example.util.Logger

@OptIn(UnstableApi::class)
internal class CacheFactory(
    private val context: Context,
    private val transferListener: TransferListener,
    private val cache: SimpleCache,
    private val upstreamDataSourceFactory: DataSource.Factory
) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        Logger.d("CacheFactory.createDataSource: Creating data source")
        
        val upstreamDataSource = upstreamDataSourceFactory
            .createDataSource()
            .apply {
                if (this is BaseDataSource) {
                    addTransferListener(transferListener)
                }
            }

        val fileSource = FileDataSource()
        val dataSink = CacheDataSink(cache, 2 * 1024 * 1024L)

        return CacheDataSource(
            cache,
            upstreamDataSource,
            fileSource,
            dataSink,
            CACHE_FLAGS,
            null
        )
    }

    companion object {
        private const val CACHE_FLAGS: Int = CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
    }
}
