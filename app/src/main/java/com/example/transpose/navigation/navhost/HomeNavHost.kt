package com.example.transpose.navigation.navhost

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.transpose.navigation.helper.HomeNavigationHelper
import com.example.transpose.navigation.navgraph.homeNavGraph
import com.example.transpose.navigation.route.HomeRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNavHost(
    navController: NavHostController,
    modifier: Modifier,
    homeNavigationHelper: HomeNavigationHelper,
    bottomSheetState: SheetState
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoutes.Playlist.route,
        modifier = modifier
    ) {
        homeNavGraph(
            bottomSheetState = bottomSheetState,
            homeNavigationHelper = homeNavigationHelper
        )
    }
}