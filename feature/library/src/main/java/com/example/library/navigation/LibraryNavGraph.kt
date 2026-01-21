package com.example.library.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import com.example.ui.screen.channel.ChannelScreen
import com.example.ui.screen.playlist_info.PlaylistInfoScreen
import com.example.ui.screen.search_result.SearchResultScreen
import com.example.ui.screen.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.libraryNavGraph(
    bottomSheetState: SheetState,
    libraryNavigationHelper: LibraryNavigationHelper,
    onUpdateCheckClick: () -> Unit,
    onContactClick: () -> Unit,
    navigateToHomeTab: () -> Unit,
    localSearchQuery: String = "",
    isLocalSearchActive: Boolean = false,
    onCloseLocalSearch: () -> Unit = {}
) {
    composable(route = LibraryRoutes.MyPlaylist.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        LibraryMyPlaylistScreen(
            bottomSheetState = bottomSheetState,
            libraryMyPlaylistViewModel = hiltViewModel(),
            navigateToMyPlaylistItemScreen = { playlistId ->
                libraryNavigationHelper.navigateToMyPlaylistItem(playlistId)
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
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
    ) {
        LibraryMyPlaylistItemScreen(
            bottomSheetState = bottomSheetState,
            libraryMyPlaylistItemViewModel = hiltViewModel(),
            navigateToBack = {
                libraryNavigationHelper.navigateBack()
            }
        )
    }
    composable(
        route = LibraryRoutes.SearchResult.route,
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
                libraryNavigationHelper.navigateBack()
            }
        )
    }

    composable(
        route = LibraryRoutes.PlaylistInfo.route,
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
        route = LibraryRoutes.MyLocalFileItem.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        arguments = listOf(navArgument("type") { type = NavType.StringType })
    ) { backStackEntry ->
        val type = backStackEntry.arguments?.getString("type")
        LibraryMyLocalItemScreen(
            bottomSheetState = bottomSheetState,
            libraryMyLocalItemViewModel = hiltViewModel(),
            type = type,
            navigateToBack = {
                libraryNavigationHelper.navigateBack()
            },
            localSearchQuery = localSearchQuery,
            isLocalSearchActive = isLocalSearchActive,
            onCloseLocalSearch = onCloseLocalSearch
        )
    }

    composable(
        route = LibraryRoutes.ChannelScreen.route,
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
                libraryNavigationHelper.navigateToPlaylistInfo(playlistId)
            }
        )
    }

    composable(
        route = LibraryRoutes.SettingsScreen.route,
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

}