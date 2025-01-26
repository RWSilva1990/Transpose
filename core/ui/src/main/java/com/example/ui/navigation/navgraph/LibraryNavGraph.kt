package com.example.transpose.navigation.navgraph

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.library.search_result.LibrarySearchResultScreen
import com.example.transpose.MainViewModel
import com.example.transpose.MediaViewModel
import com.example.transpose.navigation.Route
import com.example.transpose.ui.screen.library.my_local_item.LibraryMyLocalItemScreen
import com.example.library.my_playlist.LibraryMyPlaylistScreen
import com.example.transpose.ui.screen.library.my_playlist_item.LibraryMyPlaylistItemScreen

fun NavGraphBuilder.libraryNavGraph(
    mainViewModel: MainViewModel,
    mediaViewModel: MediaViewModel,
) {
    composable(route = Route.Library.MyPlaylist.route) {
        LibraryMyPlaylistScreen(
            mainViewModel = mainViewModel,
            mediaViewModel = mediaViewModel,
            libraryMyPlaylistViewModel = hiltViewModel()
        )
    }
    composable(
        route = Route.Library.MyPlaylistItem.route,
        arguments = listOf(navArgument("itemId") { type = NavType.StringType })
    ) { backStackEntry ->
        val itemId = backStackEntry.arguments?.getString("itemId")

        LibraryMyPlaylistItemScreen(
            mainViewModel = mainViewModel,
            mediaViewModel = mediaViewModel,
            libraryMyPlaylistItemViewModel = hiltViewModel(),
            itemId = itemId
        )
    }
    composable(
        route = Route.Library.SearchResult.route,
        arguments = listOf(navArgument("query") { type = NavType.StringType })
    ) { backStackEntry ->
        val query = backStackEntry.arguments?.getString("query")
        LibrarySearchResultScreen(
            mainViewModel = mainViewModel,
            librarySearchResultViewModel = hiltViewModel(),
            mediaViewModel = mediaViewModel,
            query = query
        )
    }
    composable(
        route = Route.Library.MyLocalFileItem.route,
        arguments = listOf(navArgument("type") { type = NavType.StringType })
    ) { backStackEntry ->
        val type = backStackEntry.arguments?.getString("type")
        LibraryMyLocalItemScreen(
            mainViewModel = mainViewModel,
            libraryMyLocalItemViewModel = hiltViewModel(),
            mediaViewModel = mediaViewModel,
            type = type
        )
    }

}