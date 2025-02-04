package com.example.transpose.navigation.navgraph

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.convert.audio_edit.ConvertAudioEditScreen
import com.example.transpose.navigation.helper.ConvertNavigationHelper
import com.example.transpose.navigation.route.ConvertRoutes
import com.example.ui.screen.search_result.SearchResultScreen

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.convertNavGraph(
    bottomSheetState: SheetState,
    convertNavigationHelper: ConvertNavigationHelper
) {
    composable(
        route = ConvertRoutes.AudioEdit.route
    ) {
        ConvertAudioEditScreen(
            bottomSheetState = bottomSheetState,
            convertAudioEditViewModel = hiltViewModel(),
            navigateToBack = {
                convertNavigationHelper.navigateBack()
            }
        )
    }
    composable(
        route = ConvertRoutes.SearchResult.route,
        arguments = listOf(navArgument("query") { type = NavType.StringType })
    ) { backStackEntry ->
        val query = backStackEntry.arguments?.getString("query")

        SearchResultScreen(
            bottomSheetState = bottomSheetState,
            searchResultViewModel = hiltViewModel(),
            query = query,
            navigateToBack = {
                convertNavigationHelper.navigateBack()
            }
        )
    }
}