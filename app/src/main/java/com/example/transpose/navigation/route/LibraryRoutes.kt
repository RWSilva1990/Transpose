package com.example.transpose.navigation.route

sealed class LibraryRoutes(val route: String) {

    data object MyPlaylist : LibraryRoutes("library_my_playlist")

    data object MyPlaylistItem : LibraryRoutes("library_my_playlist_item/{itemId}") {
        fun createRoute(itemId: Long) = "library_my_playlist_item/$itemId"
    }
    data object SearchResult : LibraryRoutes("library_search_result/{query}") {
        fun createRoute(query: String) = "library_search_result/$query"
    }
    data object MyLocalFileItem: LibraryRoutes("library_my_local_file_item/{type}"){
        fun createRoute(type: String?) = "library_my_local_file_item/$type"

    }
}