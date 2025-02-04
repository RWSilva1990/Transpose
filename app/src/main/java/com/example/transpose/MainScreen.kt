package com.example.transpose

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.compose.rememberNavController
import com.example.transpose.components.appbar.MainAppBar
import com.example.transpose.components.appbar.SearchWidgetState
import com.example.transpose.components.bottom_navigation.BottomNavigationBar
import com.example.transpose.components.bottomsheet.PlayerBottomSheetScaffold
import com.example.transpose.navigation.helper.ConvertNavigationHelper
import com.example.transpose.navigation.helper.HomeNavigationHelper
import com.example.transpose.navigation.helper.LibraryNavigationHelper
import com.example.transpose.navigation.navhost.ConvertNavHost
import com.example.transpose.navigation.navhost.HomeNavHost
import com.example.transpose.navigation.navhost.LibraryNavHost
import com.example.transpose.navigation.route.ConvertRoutes
import com.example.transpose.navigation.route.HomeRoutes
import com.example.transpose.navigation.route.LibraryRoutes
import com.example.ui.components.bottom_navigation.MainTab
import com.example.util.constants.AppColors
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel
) {

    val permissionGranted by mainViewModel.permissionGranted.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultMap ->
        // resultMap: Map<String, Boolean> (권한 -> 허용/거부)
        val allGranted = resultMap.all { it.value }
        mainViewModel.setPermissionGranted(allGranted)
    }

    LaunchedEffect(permissionGranted) {
        if (!permissionGranted) {
            mainViewModel.requestPermissions { perms ->
                launcher.launch(perms)
            }
        }
    }

    val systemUiController = rememberSystemUiController()

    val suggestionKeywords by mainViewModel.suggestionKeywords.collectAsState()

    val (searchWidgetState, setSearchWidgetState) = remember {
        mutableStateOf(SearchWidgetState.CLOSED)
    }
    val (searchTextState, setSearchTextState) = remember {
        mutableStateOf("")
    }
    val (isSearchBarActive, setIsSearchBarActive) = remember {
        mutableStateOf(true)
    }

    val (normalizedOffset, setNormalizedOffset) = remember {
        mutableFloatStateOf(0f)
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val homeNavController = rememberNavController()
    val libraryNavController = rememberNavController()
    val convertNavController = rememberNavController()

    val homeNavigationHelper = remember {
        HomeNavigationHelper(homeNavController)
    }
    val libraryNavigationHelper = remember {
        LibraryNavigationHelper(libraryNavController)
    }
    val convertNavigationHelper = remember {
        ConvertNavigationHelper(convertNavController)
    }

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
                                    HomeRoutes.SearchResult.createRoute(
                                        it
                                    )
                                )

                            MainTab.Convert -> convertNavController.navigate(
                                ConvertRoutes.SearchResult.createRoute(
                                    it
                                )
                            )

                            MainTab.Library -> libraryNavController.navigate(
                                LibraryRoutes.SearchResult.createRoute(
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
            bottomSheetState = sheetState,
            normalizedOffset = normalizedOffset,
            searchWidgetState = searchWidgetState,
            setNormalizedOffset = setNormalizedOffset,
            mainViewModel = mainViewModel
        ) { playerBottomSheetScaffoldPadding ->
            when (selectedTab) {
                MainTab.Home -> {
                    HomeNavHost(
                        navController = homeNavController,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection),
                        homeNavigationHelper = homeNavigationHelper,
                        bottomSheetState = sheetState
                    )
                }

                MainTab.Library -> {
                    LibraryNavHost(
                        navController = libraryNavController,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection),
                        libraryNavigationHelper = libraryNavigationHelper,
                        bottomSheetState = sheetState
                    )
                }

                MainTab.Convert -> {
                    ConvertNavHost(
                        navController = convertNavController,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection),
                        convertNavigationHelper = convertNavigationHelper,
                        bottomSheetState = sheetState
                    )
                }
            }
        }

    }
}


