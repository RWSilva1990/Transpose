package com.example.transpose.navigation.navgraph

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.home.home_playlist.HomePlaylistScreen
import com.example.home.playlist_item.HomePlaylistItemScreen
import com.example.transpose.navigation.helper.HomeNavigationHelper
import com.example.transpose.navigation.route.HomeRoutes
import com.example.ui.screen.search_result.SearchResultScreen

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.homeNavGraph(
    bottomSheetState: SheetState,
    homeNavigationHelper: HomeNavigationHelper
) {

    composable(HomeRoutes.Playlist.route) {
        HomePlaylistScreen(
            bottomSheetState = bottomSheetState,
            homePlaylistViewModel = hiltViewModel(),
            navigateToPlaylistItemScreen = { itemId ->
                homeNavigationHelper.navigateToPlaylistItem(itemId)
            },
            navigateToSearchResultScreen = { query ->
                homeNavigationHelper.navigateToSearchResult(query)
            },
            navigateToBack = {
                homeNavigationHelper.navigateBack()
            },
        )
    }
    composable(
        route = HomeRoutes.PlaylistItem.route,
        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
    ) { backStackEntry ->
        val itemId = backStackEntry.arguments?.getString("itemId")
        HomePlaylistItemScreen(
            bottomSheetState = bottomSheetState,
            homePlaylistItemViewModel = hiltViewModel(),
            itemId = itemId,
            navigateToBack = {
                homeNavigationHelper.navigateBack()
            },
        )
    }
    composable(
        route = HomeRoutes.SearchResult.route,
        arguments = listOf(navArgument("query") { type = NavType.StringType })
    ) { backStackEntry ->
        val query = backStackEntry.arguments?.getString("query")
        SearchResultScreen(
            bottomSheetState = bottomSheetState,
            searchResultViewModel = hiltViewModel(),
            query = query,
            navigateToBack = {
                homeNavigationHelper.navigateBack()
            },
        )
    }
}