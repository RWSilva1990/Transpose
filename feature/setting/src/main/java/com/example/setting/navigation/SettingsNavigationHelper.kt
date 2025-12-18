package com.example.setting.navigation

import androidx.navigation.NavController

class SettingsNavigationHelper(
    private val navController: NavController
) {
    /**
     * 세팅 탭 -> 검색 결과
     */
    fun navigateToSearchResult(query: String) {
        navController.navigate(SettingsRoutes.SearchResult.createRoute(query))
    }

    // 뒤로 가기
    fun navigateBack() {
        navController.popBackStack()
    }
}