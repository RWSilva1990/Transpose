package com.example.home.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.home.home_playlist.HomePlaylistScreen
import com.example.ui.screen.channel.ChannelScreen
import com.example.ui.screen.playlist_info.PlaylistInfoScreen
import com.example.ui.screen.search_result.SearchResultScreen

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.homeNavGraph(
    bottomSheetState: SheetState,
    homeNavigationHelper: HomeNavigationHelper
) {

    composable(HomeRoutes.Playlist.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }) {
        HomePlaylistScreen(
            bottomSheetState = bottomSheetState,
            homePlaylistViewModel = hiltViewModel(),
            navigateToPlaylistItemScreen = { itemId ->
                homeNavigationHelper.navigateToPlaylistItem(itemId)
            },
            canGoBack = {
                homeNavigationHelper.canGoBack()
            },
            navigateToBack = {
                homeNavigationHelper.navigateBack()
            },
        )
    }
    composable(
        route = HomeRoutes.PlaylistInfo.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
    ) { backStackEntry ->
        val playlistId = backStackEntry.arguments?.getString("playlistId")
        PlaylistInfoScreen(
            bottomSheetState = bottomSheetState,
            playlistInfoViewModel = hiltViewModel(),
            playlistId = playlistId
        )
    }
    composable(
        route = HomeRoutes.SearchResult.route,
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
                homeNavigationHelper.navigateBack()
            },
        )
    }
    composable(
        route = HomeRoutes.ChannelScreen.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        arguments = listOf(navArgument("channelId") { type = NavType.StringType })
    ) { backStackEntry ->
        val channelId = backStackEntry.arguments?.getString("channelId")
        ChannelScreen(
            bottomSheetState = bottomSheetState,
            channelViewModel = hiltViewModel(),
            channelId = channelId,
            onNavigateToPlaylistInfoScreen = { playlistId ->
                homeNavigationHelper.navigateToPlaylistInfo(playlistId)
            }
        )
    }
}