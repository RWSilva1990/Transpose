package com.example.data.newpipe.repository.search

import com.example.data.di.IoDispatcher
import com.example.data.newpipe.extractor.search.ContentPager
import com.example.data.newpipe.repository.base.NewPipeManager
import com.example.domain.model.youtube.search.SearchResult
import com.example.domain.repository.SearchRepository
import com.example.util.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val newPipeManager: NewPipeManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SearchRepository {

    private var currentContentPager: ContentPager? = null

    override suspend fun search(query: String): Result<List<SearchResult>> {
        return withContext(ioDispatcher){
            try {
                val searchExtractor = newPipeManager.youtubeService.getSearchExtractor(query)
                searchExtractor.fetchPage()
                val pager = ContentPager(
                    newPipeManager.youtubeService,
                    searchExtractor,
                    initialPageFetched = true
                )
                currentContentPager = pager
                Result.success(pager.getNextPage())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun loadMoreSearchResults(): Result<List<SearchResult>> {
        return withContext(ioDispatcher) {
            try {
                val pager = currentContentPager
                    ?: throw IllegalStateException("No search pager initiated")

                val nextPages = pager.getNextPage()
                Result.success(nextPages)
            } catch (e: Exception) {
                Logger.e("Error loading more search results", e)
                Result.failure(e)
            }
        }
    }

    override fun canLoadMoreSearchResults(): Boolean {
        return currentContentPager?.isHasNextPage() == true
    }
}
