package com.example.transpose.navigation.navhost

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.transpose.navigation.helper.LibraryNavigationHelper
import com.example.transpose.navigation.navgraph.libraryNavGraph
import com.example.transpose.navigation.route.LibraryRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryNavHost(
    navController: NavHostController,
    modifier: Modifier,
    bottomSheetState: SheetState,
    libraryNavigationHelper: LibraryNavigationHelper,

    ) {
    NavHost(
        navController = navController,
        startDestination = LibraryRoutes.MyPlaylist.route,
        modifier = modifier
    ) {
        libraryNavGraph(
            bottomSheetState = bottomSheetState,
            libraryNavigationHelper = libraryNavigationHelper
        )
    }
}