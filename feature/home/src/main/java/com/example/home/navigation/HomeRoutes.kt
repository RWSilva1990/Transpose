package com.example.home.navigation

import android.net.Uri

sealed class HomeRoutes(val route: String) {
    data object Playlist : HomeRoutes("home_playlist")
    data object PlaylistInfo : HomeRoutes("home_playlist_info/{playlistId}") {
        fun createRoute(playlistId: String) = "home_playlist_info/${Uri.encode(playlistId)}"
    }
    data object SearchResult : HomeRoutes("home_search_result/{query}") {
        fun createRoute(query: String) = "home_search_result/${Uri.encode(query)}"
    }
    data object ChannelScreen : HomeRoutes("home_channel_screen/{channelId}") {
        fun createRoute(channelId: String) = "home_channel_screen/${Uri.encode(channelId)}"
    }
    data object SettingsScreen : HomeRoutes("settings_screen")

}