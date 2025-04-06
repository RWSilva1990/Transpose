package com.example.data.newpipe.repository.search

import com.example.data.newpipe.extractor.search.ContentPager
import com.example.data.newpipe.repository.base.BaseNewPipeRepository
import com.example.domain.model.youtube.search.SearchResult
import com.example.domain.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor() : BaseNewPipeRepository(), SearchRepository {

    private var currentContentPager: ContentPager? = null

    override suspend fun search(query: String): Result<List<SearchResult>> {
        return try {
            val searchExtractor = youtubeService.getSearchExtractor(query)
            searchExtractor.fetchPage()
            val pager = ContentPager(youtubeService, searchExtractor)
            currentContentPager = pager
            Result.success(pager.getNextPage())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loadMoreSearchResults(): Result<List<SearchResult>> {
        val pager = currentContentPager
            ?: return Result.failure(IllegalStateException("No search initiated"))
        return try {
            Result.success(pager.getNextPage())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun canLoadMoreSearchResults(): Boolean {
        return currentContentPager?.isHasNextPage() == true
    }
}