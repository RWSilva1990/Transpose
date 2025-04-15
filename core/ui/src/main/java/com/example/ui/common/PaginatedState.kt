package com.example.ui.common

import com.example.domain.model.youtube.search.SearchResult

sealed class PaginatedState<out T>{
    data object Initial : PaginatedState<Nothing>()
    data object Loading : PaginatedState<Nothing>()
    data class Success<T>(
        val items: List<T>,
        val hasMore: Boolean,
        val isLoadingMore: Boolean = false,
        val loadMoreError: String? = null
    ) : PaginatedState<T>()
    data class Error(val message: String) : PaginatedState<Nothing>()
}