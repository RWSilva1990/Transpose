package com.example.convert.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.convert.audio_edit.ConvertAudioEditScreen
import com.example.ui.screen.channel.ChannelScreen
import com.example.ui.screen.playlist_info.PlaylistInfoScreen
import com.example.ui.screen.search_result.SearchResultScreen
import com.example.ui.screen.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.convertNavGraph(
    bottomSheetState: SheetState,
    onUpdateCheckClick: () -> Unit,
    onContactClick: () -> Unit,
    convertNavigationHelper: ConvertNavigationHelper,
    navigateToHomeTab: () -> Unit
) {
    composable(
        route = ConvertRoutes.AudioEdit.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
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
                convertNavigationHelper.navigateBack()
            }
        )
    }

    composable(
        route = ConvertRoutes.PlaylistInfo.route,
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
        route = ConvertRoutes.ChannelScreen.route,
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
                convertNavigationHelper.navigateToPlaylistInfo(playlistId)
            }
        )
    }

    composable(
        route = ConvertRoutes.SettingsScreen.route,
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