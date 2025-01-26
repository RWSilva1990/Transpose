package com.example.transpose.navigation.route

sealed class HomeRoutes(val route: String) {
    data object Playlist : HomeRoutes("home_playlist")
    data object PlaylistItem : HomeRoutes("home_playlist_item/{itemId}") {
        fun createRoute(itemId: String) = "home_playlist_item/$itemId"
    }
    data object SearchResult : HomeRoutes("home_search_result/{query}") {
        fun createRoute(query: String) = "home_search_result/$query"
    }
}