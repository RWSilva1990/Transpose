package com.example.convert.navigation

import android.net.Uri

sealed class ConvertRoutes(val route: String) {
    data object AudioEdit : ConvertRoutes("convert_audio_edit")

    data object SearchResult : ConvertRoutes("convert_search_result/{query}") {
        fun createRoute(query: String) = "convert_search_result/${Uri.encode(query)}"
    }

    data object PlaylistInfo : ConvertRoutes("convert_playlist_info/{playlistId}") {
        fun createRoute(playlistId: String) = "convert_playlist_info/${Uri.encode(playlistId)}"
    }

    data object ChannelScreen: ConvertRoutes("convert_channel_screen/{channelId}") {
        fun createRoute(channelId: String) = "convert_channel_screen/${Uri.encode(channelId)}"
    }
    data object SettingsScreen : ConvertRoutes("settings_screen")

}