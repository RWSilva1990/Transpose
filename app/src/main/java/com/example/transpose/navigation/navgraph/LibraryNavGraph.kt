package com.example.transpose.navigation.navgraph

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.library.my_local_item.LibraryMyLocalItemScreen
import com.example.library.my_playlist.LibraryMyPlaylistScreen
import com.example.library.my_playlist_item.LibraryMyPlaylistItemScreen
import com.example.transpose.navigation.helper.LibraryNavigationHelper
import com.example.transpose.navigation.route.LibraryRoutes
import com.example.ui.screen.search_result.SearchResultScreen

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.libraryNavGraph(
    bottomSheetState: SheetState,
    libraryNavigationHelper: LibraryNavigationHelper
) {
    composable(route = LibraryRoutes.MyPlaylist.route) {
        LibraryMyPlaylistScreen(
            bottomSheetState = bottomSheetState,
            libraryMyPlaylistViewModel = hiltViewModel(),
            navigateToMyPlaylistItemScreen = { itemId ->
                libraryNavigationHelper.navigateToMyPlaylistItem(itemId)
            },
            navigateToSearchResultScreen = { query ->
                libraryNavigationHelper.navigateToSearchResult(query)
            },
            navigateToLocalFileScreen = { type ->
                libraryNavigationHelper.navigateToMyLocalFileItem(type)
            }
        )
    }
    composable(
        route = LibraryRoutes.MyPlaylistItem.route,
        arguments = listOf(navArgument("itemId") { type = NavType.LongType })
    ) { backStackEntry ->
        val itemId = backStackEntry.arguments?.getLong("itemId")
        LibraryMyPlaylistItemScreen(
            bottomSheetState = bottomSheetState,
            libraryMyPlaylistItemViewModel = hiltViewModel(),
            itemId = itemId,
            navigateToBack = {
                libraryNavigationHelper.navigateBack()
            }
        )
    }
    composable(
        route = LibraryRoutes.SearchResult.route,
        arguments = listOf(navArgument("query") { type = NavType.StringType })
    ) { backStackEntry ->
        val query = backStackEntry.arguments?.getString("query")
        SearchResultScreen(
            bottomSheetState = bottomSheetState,
            searchResultViewModel = hiltViewModel(),
            query = query,
            navigateToBack = {
                libraryNavigationHelper.navigateBack()
            }
        )
    }
    composable(
        route = LibraryRoutes.MyLocalFileItem.route,
        arguments = listOf(navArgument("type") { type = NavType.StringType })
    ) { backStackEntry ->
        val type = backStackEntry.arguments?.getString("type")
        LibraryMyLocalItemScreen(
            bottomSheetState = bottomSheetState,
            libraryMyLocalItemViewModel = hiltViewModel(),
            type = type,
            navigateToBack = {
                libraryNavigationHelper.navigateBack()
            }
        )
    }

}