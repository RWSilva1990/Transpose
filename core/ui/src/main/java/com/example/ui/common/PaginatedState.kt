package com.example.ui.common

import com.example.domain.model.youtube.search.SearchResult

sealed class PaginatedState{
    data object Initial : PaginatedState()
    data object Loading : PaginatedState()
    data class Success(
        val items: List<SearchResult>,
        val hasMore: Boolean,
        val isLoadingMore: Boolean = false,
        val loadMoreError: String? = null
    ) : PaginatedState()
    data class Error(val message: String) : PaginatedState()
}