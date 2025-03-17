package com.example.domain.repository

import com.example.domain.model.youtube.search.SearchResult

interface SearchRepository {
    suspend fun search(query: String): Result<List<SearchResult>>
    suspend fun loadMoreSearchResults(): Result<List<SearchResult>>
    fun canLoadMoreSearchResults(): Boolean
}