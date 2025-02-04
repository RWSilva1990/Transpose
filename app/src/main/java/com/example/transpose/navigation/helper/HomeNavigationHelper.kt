package com.example.transpose.navigation.helper

import androidx.navigation.NavController
import com.example.transpose.navigation.route.HomeRoutes

class HomeNavigationHelper(
    private val navController: NavController
) {

    // 홈 플레이리스트 화면으로 이동
    fun navigateToPlaylist() {
        // HomeRoutes.Playlist.route = "home_playlist"
        navController.navigate(HomeRoutes.Playlist.route)
    }

    // 플레이리스트 아이템 상세
    fun navigateToPlaylistItem(itemId: String) {
        // HomeRoutes.PlaylistItem.createRoute(itemId) = "home_playlist_item/$itemId"
        navController.navigate(HomeRoutes.PlaylistItem.createRoute(itemId))
    }

    // 홈 검색 결과
    fun navigateToSearchResult(query: String) {
        navController.navigate(HomeRoutes.SearchResult.createRoute(query))
    }

    // 뒤로 가기
    fun navigateBack() {
        navController.popBackStack()
    }
}