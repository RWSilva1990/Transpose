package com.example.setting.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.setting.SettingsScreen
import com.example.ui.screen.search_result.SearchResultScreen

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.settingNavGraph(
    bottomSheetState: SheetState,
    onUpdateCheckClick: () -> Unit,
    onContactClick: () -> Unit,
    settingsNavigationHelper: SettingsNavigationHelper,
    navigateToHomeTab: () -> Unit
) {
    composable(
        route = SettingsRoutes.Main.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        SettingsScreen(
            onUpdateCheckClick = onUpdateCheckClick,
            onContactClick = onContactClick
        )
    }

    composable(
        route = SettingsRoutes.SearchResult.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        arguments = listOf(navArgument("query") { type = NavType.StringType })
    ) { backStackEntry ->
        val query = backStackEntry.arguments?.getString("query")
        SearchResultScreen(
            bottomSheetState = bottomSheetState,
            searchResultViewModel = hiltViewModel(),
            query = query,
            navigateToBack = {
                settingsNavigationHelper.navigateBack()
            }
        )
    }
}