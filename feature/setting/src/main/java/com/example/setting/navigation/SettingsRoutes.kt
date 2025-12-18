package com.example.setting.navigation

sealed class SettingsRoutes(val route: String) {
    data object Main : SettingsRoutes("settings_main")

    data object SearchResult : SettingsRoutes("settings_search_result/{query}") {
        fun createRoute(query: String) = "settings_search_result/$query"
    }
}