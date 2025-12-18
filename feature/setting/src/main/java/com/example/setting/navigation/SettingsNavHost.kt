package com.example.setting.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNavHost(
    bottomSheetState: SheetState,
    navController: NavHostController,
    onUpdateCheckClick: () -> Unit = {},
    onContactClick: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = SettingsRoutes.Main.route
    ) {
        settingNavGraph(
            bottomSheetState = bottomSheetState,
            settingsNavigationHelper = SettingsNavigationHelper(navController),
            navigateToHomeTab = { },
            onUpdateCheckClick = onUpdateCheckClick,
            onContactClick = onContactClick
        )
    }
}
