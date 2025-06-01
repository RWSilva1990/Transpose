package com.example.main

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.main.components.appbar.SearchBarState
import com.example.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_QUERY = "search_query"

@HiltViewModel
class MainUiStateViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {

    init {
        checkPermissions()
    }


    // Normalized BottomSheet offset value
    private val _bottomSheetOffset = MutableStateFlow(-1f)
    val bottomSheetOffset = _bottomSheetOffset.asStateFlow()

    fun updateBottomSheetOffset(offset: Float) {
        _bottomSheetOffset.value = offset
    }

    // SearchBar State
    private val _searchBarState = MutableStateFlow(SearchBarState.CLOSED)
    val searchBarState = _searchBarState.asStateFlow()

    val searchQuery: StateFlow<String> = savedStateHandle.getStateFlow(SEARCH_QUERY, "")

    fun updateSearchBarState(state: SearchBarState) {
        _searchBarState.value = state
    }

    fun storeSearchQuery(query: String) {
        viewModelScope.launch {
            savedStateHandle[SEARCH_QUERY] = query
        }
    }

    fun onCloseSearchBar() {
        storeSearchQuery("")
        updateSearchBarState(SearchBarState.CLOSED)
    }

    fun onClearSearchQuery() {
        storeSearchQuery("")
    }

    // Permission State
    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    private fun checkPermissions() {
        _permissionGranted.value = PermissionUtils.checkPermissions(context)
    }

    fun setPermissionGranted(granted: Boolean) {
        _permissionGranted.value = granted
    }

    fun requestPermissions(launcher: (Array<String>) -> Unit) {
        PermissionUtils.requestPermissions(launcher)
    }

}