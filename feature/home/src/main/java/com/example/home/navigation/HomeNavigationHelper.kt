package com.example.home.navigation

import androidx.navigation.NavController

class HomeNavigationHelper(
    private val navController: NavController
) {

    val canGoBack = { navController.previousBackStackEntry != null }


    // 홈 플레이리스트 화면으로 이동
    fun navigateToPlaylist() {
        // HomeRoutes.Playlist.route = "home_playlist"
        navController.navigate(HomeRoutes.Playlist.route)
    }

    // 플레이리스트 아이템 상세
    fun navigateToPlaylistItem(itemId: String) {
        // HomeRoutes.PlaylistItem.createRoute(itemId) = "home_playlist_item/$itemId"
        navController.navigate(HomeRoutes.PlaylistInfo.createRoute(itemId))
    }

    // 홈 검색 결과
    fun navigateToSearchResult(query: String) {
        navController.navigate(HomeRoutes.SearchResult.createRoute(query))
    }

    fun navigateToPlaylistInfo(playlistId: String) {
        navController.navigate(HomeRoutes.PlaylistInfo.createRoute(playlistId))
    }

    // 뒤로 가기
    fun navigateBack() {
        navController.popBackStack()
    }
}