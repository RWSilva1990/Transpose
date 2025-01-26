package com.example.ui.screen.search_result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.repository.NewPipeRepository
import com.example.transpose.utils.Logger
import com.example.ui.common.PaginatedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeSearchResultViewModel @Inject constructor(
    private val newPipeRepository: NewPipeRepository
) : ViewModel() {

    private val _searchResultsState = MutableStateFlow<PaginatedState>(PaginatedState.Initial)
    val searchResultsState = _searchResultsState.asStateFlow()

    fun initializeSearchPager(query: String) = viewModelScope.launch(Dispatchers.IO) {
        // 로딩 시작
        _searchResultsState.value = PaginatedState.Loading
        try {
            val result = newPipeRepository.search(query)
            if (result.isSuccess) {
                val items = result.getOrElse { emptyList() }
                _searchResultsState.value = PaginatedState.Success(
                    items = items,
                    hasMore = newPipeRepository.canLoadMoreSearchResults(),
                    isLoadingMore = false
                )
            } else {
                val exception = result.exceptionOrNull()
                _searchResultsState.value = PaginatedState.Error(
                    exception?.message ?: "Unknown error occurred"
                )
            }
        } catch (e: Exception) {
            Logger.e("Error initializing search pager", e)
            _searchResultsState.value = PaginatedState.Error(e.message ?: "Unknown error")
        }
    }

    fun loadMoreSearchResults() = viewModelScope.launch(Dispatchers.IO) {
        val currentState = _searchResultsState.value
        // 현재 상태가 Success가 아니거나 이미 로딩 중이면 추가 로드를 하지 않음
        if (currentState !is PaginatedState.Success || currentState.isLoadingMore) return@launch
        // 이미 더 가져올 데이터가 없는 경우도 종료
        if (!currentState.hasMore) return@launch

        // 로딩 중으로 상태 업데이트
        _searchResultsState.value = currentState.copy(isLoadingMore = true)

        try {
            val result = newPipeRepository.loadMoreSearchResults()
            if (result.isSuccess) {
                val newItems = result.getOrElse { emptyList() }
                // 기존 리스트에 새로 불러온 항목들 추가
                _searchResultsState.value = currentState.copy(
                    items = currentState.items + newItems,
                    hasMore = newPipeRepository.canLoadMoreSearchResults(),
                    isLoadingMore = false
                )
            } else {
                val exception = result.exceptionOrNull()
                _searchResultsState.value = PaginatedState.Error(
                    exception?.message ?: "Unknown error occurred"
                )
            }
        } catch (e: Exception) {
            Logger.e("Error loading more search results", e)
            _searchResultsState.value = PaginatedState.Error(e.message ?: "Unknown error")
        }
    }
}