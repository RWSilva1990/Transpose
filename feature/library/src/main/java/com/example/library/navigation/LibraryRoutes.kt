package com.example.library.navigation

import android.net.Uri

sealed class LibraryRoutes(val route: String) {

    data object MyPlaylist : LibraryRoutes("library_my_playlist")

    data object MyPlaylistItem : LibraryRoutes("library_my_playlist_item/{playlistId}") {
        fun createRoute(playlistId: Long) = "library_my_playlist_item/$playlistId"
    }
    data object SearchResult : LibraryRoutes("library_search_result/{query}") {
        fun createRoute(query: String) = "library_search_result/${Uri.encode(query)}"
    }
    data object PlaylistInfo: LibraryRoutes("library_playlist_info/{playlistId}") {
        fun createRoute(playlistId: String) = "library_playlist_info/${Uri.encode(playlistId)}"
    }
    data object MyLocalFileItem: LibraryRoutes("library_my_local_file_item/{type}"){
        fun createRoute(type: String?) = "library_my_local_file_item/$type"
    }
    data object ChannelScreen : LibraryRoutes("library_channel_screen/{channelId}") {
        fun createRoute(channelId: String) = "library_channel_screen/${Uri.encode(channelId)}"
    }
    data object SettingsScreen : LibraryRoutes("settings_screen")

}