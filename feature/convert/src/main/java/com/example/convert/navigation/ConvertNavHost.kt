package com.example.convert.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertNavHost(
    navController: NavHostController,
    modifier: Modifier,
    bottomSheetState: SheetState,
    onUpdateCheckClick: () -> Unit,
    onContactClick: () -> Unit,
    navigateToHomeTab: () -> Unit
) {
    val convertNavigationHelper = remember {
        ConvertNavigationHelper(navController)
    }
    NavHost(
        navController = navController,
        startDestination = ConvertRoutes.AudioEdit.route,
        modifier = modifier
    ) {
        convertNavGraph(
            bottomSheetState = bottomSheetState,
            convertNavigationHelper = convertNavigationHelper,
            navigateToHomeTab = navigateToHomeTab,
            onUpdateCheckClick = onUpdateCheckClick,
            onContactClick = onContactClick
        )
    }
}