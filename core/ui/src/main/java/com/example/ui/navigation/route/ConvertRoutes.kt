package com.example.transpose.navigation.route

sealed class ConvertRoutes(val route: String) {
    data object AudioEdit : ConvertRoutes("convert_audio_edit")

    data object SearchResult : ConvertRoutes("convert_search_result/{query}") {
        fun createRoute(query: String) = "convert_search_result/$query"
    }
}