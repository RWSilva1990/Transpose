package com.example.convert.navigation

import androidx.navigation.NavController

class ConvertNavigationHelper(
    private val navController: NavController
) {

    val canGoBack = { navController.previousBackStackEntry != null }

    /**
     * Convert 탭 -> 오디오 편집 화면
     */
    fun navigateToAudioEdit() {
        navController.navigate(ConvertRoutes.AudioEdit.route)
    }

    /**
     * Convert 탭 -> 검색 결과
     */
    fun navigateToSearchResult(query: String) {
        navController.navigate(ConvertRoutes.SearchResult.createRoute(query))
    }

    fun navigateToPlaylistInfo(playlistId: String) {
        navController.navigate(ConvertRoutes.PlaylistInfo.createRoute(playlistId))
    }

    // 뒤로 가기
    fun navigateBack() {
        navController.popBackStack()
    }
}