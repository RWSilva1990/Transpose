package com.example.transpose.navigation.navhost

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.transpose.navigation.helper.ConvertNavigationHelper
import com.example.transpose.navigation.navgraph.convertNavGraph
import com.example.transpose.navigation.route.ConvertRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertNavHost(
    navController: NavHostController,
    modifier: Modifier,
    convertNavigationHelper: ConvertNavigationHelper,
    bottomSheetState: SheetState
) {
    NavHost(
        navController = navController,
        startDestination = ConvertRoutes.AudioEdit.route,
        modifier = modifier
    ) {
        convertNavGraph(
            bottomSheetState = bottomSheetState,
            convertNavigationHelper = convertNavigationHelper
        )
    }
}