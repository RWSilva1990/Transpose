package com.example.transpose.navigation.helper

import androidx.navigation.NavController
import com.example.transpose.navigation.route.ConvertRoutes

class ConvertNavigationHelper(
    private val navController: NavController
) {

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

    // 뒤로 가기
    fun navigateBack() {
        navController.popBackStack()
    }
}