package com.example.main

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.convert.navigation.ConvertNavHost
import com.example.convert.navigation.ConvertRoutes
import com.example.home.navigation.HomeNavHost
import com.example.home.navigation.HomeRoutes
import com.example.library.navigation.LibraryNavHost
import com.example.library.navigation.LibraryRoutes
import com.example.main.components.appbar.MainAppBar
import com.example.main.components.appbar.SearchWidgetState
import com.example.main.components.bottom_navigation.BottomNavigationBar
import com.example.main.components.bottom_navigation.MainTab
import com.example.main.components.bottomsheet.PlayerBottomSheetScaffold
import com.example.util.constants.AppColors
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
) {
    val mainViewModel = hiltViewModel<MainViewModel>()

    val permissionGranted by mainViewModel.permissionGranted.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultMap ->
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

    val searchTextState by mainViewModel.searchQuery.collectAsStateWithLifecycle()

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
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    BackHandler {
        if (sheetState.currentValue == SheetValue.Expanded) {
            coroutineScope.launch {
                sheetState.partialExpand()
            }
            return@BackHandler
        }

        // 홈 탭 처리
        if (selectedTab == MainTab.Home) {
            if (homeNavController.previousBackStackEntry != null) {
                homeNavController.popBackStack()
                return@BackHandler
            } else {
                (context as? Activity)?.moveTaskToBack(false)
                return@BackHandler
            }
        }

        // 라이브러리 탭 처리
        if (selectedTab == MainTab.Library) {
            if (libraryNavController.previousBackStackEntry != null) {
                libraryNavController.popBackStack()
                return@BackHandler
            } else {
                selectedTab = MainTab.Home
                return@BackHandler
            }
        }

        // 변환 탭 처리
        if (selectedTab == MainTab.Convert) {
            if (convertNavController.previousBackStackEntry != null) {
                convertNavController.popBackStack()
                return@BackHandler
            } else {
                selectedTab = MainTab.Home
                return@BackHandler
            }
        }
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
                    onTextChange = mainViewModel::storeSearchQuery,
                    onTextClearClicked = {
                        mainViewModel.storeSearchQuery("")
                    },
                    onCloseClicked = {
                        setSearchWidgetState(SearchWidgetState.CLOSED)
                        mainViewModel.storeSearchQuery("")
                    },
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
            mainViewModel = mainViewModel,
            onNavigateToChannelScreen = { channelId ->
                when (selectedTab) {
                    MainTab.Home -> homeNavController.navigate(
                        HomeRoutes.ChannelScreen.createRoute(
                            channelId
                        )
                    )

                    MainTab.Convert -> convertNavController.navigate(
                        ConvertRoutes.ChannelScreen.createRoute(
                            channelId
                        )
                    )

                    MainTab.Library -> libraryNavController.navigate(
                        LibraryRoutes.ChannelScreen.createRoute(
                            channelId
                        )
                    )
                }
            }
        ) { playerBottomSheetScaffoldPadding ->
            when (selectedTab) {
                MainTab.Home -> {
                    HomeNavHost(
                        navController = homeNavController,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection)
                            .padding(bottom = innerPadding.calculateBottomPadding()),

                        bottomSheetState = sheetState
                    )
                }

                MainTab.Library -> {
                    LibraryNavHost(
                        navController = libraryNavController,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection)
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                        bottomSheetState = sheetState,
                        navigateToHomeTab = { selectedTab = MainTab.Home }
                    )
                }

                MainTab.Convert -> {
                    ConvertNavHost(
                        navController = convertNavController,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection)
                            .padding(bottom = innerPadding.calculateBottomPadding()),

                        bottomSheetState = sheetState,
                        navigateToHomeTab = { selectedTab = MainTab.Home }
                    )
                }
            }

            playerBottomSheetScaffoldPadding.calculateBottomPadding()
        }

    }
}


