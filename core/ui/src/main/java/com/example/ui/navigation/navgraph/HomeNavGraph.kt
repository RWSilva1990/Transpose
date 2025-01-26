package com.example.transpose.navigation.navgraph

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.home.home_playlist.HomePlaylistScreen
import com.example.home.playlist_info.HomePlaylistItemScreen
import com.example.home.search_result.HomeSearchResultScreen
import com.example.transpose.MainViewModel
import com.example.transpose.MediaViewModel
import com.example.transpose.navigation.Route

fun NavGraphBuilder.homeNavGraph(
    mainViewModel: MainViewModel,
    mediaViewModel: MediaViewModel,
) {

    composable(Route.Home.Playlist.route) {
        HomePlaylistScreen(
            mainViewModel = mainViewModel,
            homePlaylistViewModel = hiltViewModel()
        )
    }
    composable(
        route = Route.Home.PlaylistItem.route,
        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
    ) { backStackEntry ->
        val itemId = backStackEntry.arguments?.getString("itemId")
        HomePlaylistItemScreen(
            mainViewModel = mainViewModel,
            homePlaylistItemViewModel = hiltViewModel(),
            mediaViewModel = mediaViewModel,
            itemId = itemId
        )
    }
    composable(
        route = Route.Home.SearchResult.route,
        arguments = listOf(navArgument("query") { type = NavType.StringType })
    ) { backStackEntry ->
        val query = backStackEntry.arguments?.getString("query")
        HomeSearchResultScreen(
            mainViewModel = mainViewModel,
            homeSearchResultViewModel = hiltViewModel(),
            mediaViewModel = mediaViewModel,
            query = query
        )
    }
}