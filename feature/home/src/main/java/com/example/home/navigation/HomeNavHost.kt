package com.example.home.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNavHost(
    navController: NavHostController,
    modifier: Modifier,
    onUpdateCheckClick: () -> Unit,
    onContactClick: () -> Unit,
    bottomSheetState: SheetState,
) {

    val homeNavigationHelper = remember {
        HomeNavigationHelper(navController)
    }

    NavHost(
        navController = navController,
        startDestination = HomeRoutes.Playlist.route,
        modifier = modifier
    ) {
        homeNavGraph(
            bottomSheetState = bottomSheetState,
            homeNavigationHelper = homeNavigationHelper,
            onUpdateCheckClick = onUpdateCheckClick,
            onContactClick = onContactClick
        )
    }
}