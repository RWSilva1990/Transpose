package com.example.main

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-local UI request used when playback is started from an external Android intent.
 * Normal playback inside Transpose does not touch this state.
 */
object ExternalPlayerUiRequest {
    private val _expandRequestId = MutableStateFlow(0L)
    val expandRequestId = _expandRequestId.asStateFlow()

    fun requestExpand() {
        _expandRequestId.value = _expandRequestId.value + 1L
    }
}
