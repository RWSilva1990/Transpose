package com.example.main

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.navigation.compose.rememberNavController
import com.example.transpose.navigation.Route
import com.example.transpose.ui.components.bottom_navigation.BottomNavigationBar
import com.example.transpose.ui.components.bottomsheet.PlayerBottomSheetScaffold
import com.example.transpose.utils.constants.AppColors
import com.example.ui.components.appbar.MainAppBar
import com.example.ui.components.appbar.SearchWidgetState
import com.example.ui.components.bottom_navigation.MainTab
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel
) {
    val systemUiController = rememberSystemUiController()

    val (searchWidgetState, setSearchWidgetState) = remember {
        mutableStateOf(SearchWidgetState.CLOSED)
    }
    val (searchTextState, setSearchTextState) = remember {
        mutableStateOf("")
    }
    val (isSearchBarActive, setIsSearchBarActive) = remember {
        mutableStateOf(true)
    }

    val (suggestionKeywords, setSuggestionKeywords) = remember {
        mutableStateOf<List<String>>(emptyList())
    }

    val (normalizedOffset, setNormalizedOffset) = remember {
        mutableStateOf(0f)
    }

    val (bottomSheetValue, setBottomSheetValue) = remember {
        mutableStateOf(SheetValue.Hidden)
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val homeNavController = rememberNavController()
    val libraryNavController = rememberNavController()
    val convertNavController = rememberNavController()
    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Home) }


    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val behaviorConsumed =
                    scrollBehavior.nestedScrollConnection.onPreScroll(available, source)
                return behaviorConsumed
            }
        }
    }

    SideEffect {
        systemUiController.setStatusBarColor(
            color = AppColors.StatusBarBackground,
        )

        systemUiController.setNavigationBarColor(
            color = AppColors.BlueBackground,
        )
    }

//        BackHandler {
//            if (bottomSheetState == SheetValue.Expanded) {
//                mainViewModel.partialExpandBottomSheet()
//            } else {
//                when (selectedTab.route) {
//                    Route.Home.route -> {
//                        this.moveTaskToBack(true)
//                    }
//
//                    Route.Convert.route -> {
//
//                    }
//
//                    Route.Library.route -> {
//                        navigationViewModel.changeMainCurrentRoute(Route.Home.route)
//                    }
//                }
//            }
//        }


    Scaffold(containerColor = Color.White, bottomBar = {
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = { tab -> selectedTab = tab },
            searchWidgetState = searchWidgetState,
            normalizedOffset = normalizedOffset
        )
    }) { innerPadding ->
        PlayerBottomSheetScaffold(
            topAppBar = {
                MainAppBar(
                    searchWidgetState = searchWidgetState,
                    searchTextState = searchTextState,
                    onTextChange = {
                        setSearchTextState(it)
                        mainViewModel.getSuggestionKeyword(it)
                    },
                    onTextClearClicked = {
                        mainViewModel.clearSuggestionKeywords()
                        setSearchTextState("")
                    },
                    onCloseClicked = { setSearchWidgetState(SearchWidgetState.CLOSED) },
                    onSearchClicked = {
                        when (selectedTab) {
                            MainTab.Home ->
                                homeNavController.navigate(
                                    Route.Home.SearchResult.createRoute(
                                        it
                                    )
                                )

                            MainTab.Convert -> convertNavController.navigate(
                                Route.Home.SearchResult.createRoute(
                                    it
                                )
                            )

                            MainTab.Library -> libraryNavController.navigate(
                                Route.Home.SearchResult.createRoute(
                                    it
                                )
                            )
                        }
                    },
                    onSearchTriggered = { setSearchWidgetState(SearchWidgetState.OPENED) },
                    suggestionKeywords = suggestionKeywords,
                    isSearchBarExpanded = isSearchBarActive,
                    onSearchBarActiveChanged = { setIsSearchBarActive(it) },
                    scrollBehavior = scrollBehavior,

                    )
            },
            innerPadding = innerPadding,
        ) { playerBottomSheetScaffoldPadding ->

        }
    }


}