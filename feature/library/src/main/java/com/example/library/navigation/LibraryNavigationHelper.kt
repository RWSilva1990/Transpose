package com.example.library.navigation

import androidx.navigation.NavController

class LibraryNavigationHelper(
    private val navController: NavController
) {
    /**
     * 라이브러리 탭 -> 내 플레이리스트 화면
     */
    fun navigateToMyPlaylist() {
        navController.navigate(LibraryRoutes.MyPlaylist.route)
    }

    /**
     * 라이브러리 탭 -> 내 플레이리스트 아이템 상세
     */
    fun navigateToMyPlaylistItem(itemId: Long) {
        navController.navigate(LibraryRoutes.MyPlaylistItem.createRoute(itemId))
    }

    /**
     * 라이브러리 탭 -> 검색 결과
     */
    fun navigateToSearchResult(query: String) {
        navController.navigate(LibraryRoutes.SearchResult.createRoute(query))
    }

    /**
     * 라이브러리 탭 -> 내 로컬 파일 화면
     */
    fun navigateToMyLocalFileItem(type: String) {
        navController.navigate(LibraryRoutes.MyLocalFileItem.createRoute(type))
    }

    fun navigateToPlaylistInfo(playlistId: String) {
        navController.navigate(LibraryRoutes.PlaylistInfo.createRoute(playlistId))
    }

    // 뒤로 가기
    fun navigateBack() {
        navController.popBackStack()
    }
}