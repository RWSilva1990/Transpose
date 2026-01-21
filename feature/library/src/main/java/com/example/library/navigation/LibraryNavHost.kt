package com.example.library.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryNavHost(
    navController: NavHostController,
    modifier: Modifier,
    bottomSheetState: SheetState,
    onUpdateCheckClick: () -> Unit,
    onContactClick: () -> Unit,
    navigateToHomeTab: () -> Unit,
    localSearchQuery: String = "",
    isLocalSearchActive: Boolean = false,
    onCloseLocalSearch: () -> Unit = {}
) {
    val libraryNavigationHelper = remember {
        LibraryNavigationHelper(navController)
    }
    NavHost(
        navController = navController,
        startDestination = LibraryRoutes.MyPlaylist.route,
        modifier = modifier
    ) {
        libraryNavGraph(
            bottomSheetState = bottomSheetState,
            libraryNavigationHelper = libraryNavigationHelper,
            navigateToHomeTab = navigateToHomeTab,
            onContactClick = onContactClick,
            onUpdateCheckClick = onUpdateCheckClick,
            localSearchQuery = localSearchQuery,
            isLocalSearchActive = isLocalSearchActive,
            onCloseLocalSearch = onCloseLocalSearch
        )
    }
}