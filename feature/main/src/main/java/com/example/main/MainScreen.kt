package com.example.main

import UpdateDialog
import android.app.Activity
import android.content.Intent
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.convert.navigation.ConvertNavHost
import com.example.convert.navigation.ConvertRoutes
import com.example.home.navigation.HomeNavHost
import com.example.home.navigation.HomeRoutes
import com.example.library.navigation.LibraryNavHost
import com.example.library.navigation.LibraryRoutes
import com.example.main.components.appbar.MainAppBar
import com.example.main.components.appbar.SearchBarState
import com.example.main.components.bottom_navigation.BottomNavigationBar
import com.example.main.components.bottom_navigation.MainTab
import com.example.main.components.bottomsheet.PlayerBottomSheetScaffold
import com.example.util.Logger
import com.example.util.constants.AppColors
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
) {
    val mainViewModel = hiltViewModel<MainViewModel>()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val updateDialogState by mainViewModel.updateDialogState.collectAsState()

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

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val homeNavController = rememberNavController()
    val libraryNavController = rememberNavController()
    val convertNavController = rememberNavController()

    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Home) }


    var bottomSheetOffset by remember {
        mutableFloatStateOf(0f)
    }

    var searchBarState by remember {
        mutableStateOf(SearchBarState.CLOSED)
    }

    var parentScaffoldHeightPx by remember {
        mutableFloatStateOf(0f)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val behaviorConsumed =
                    scrollBehavior.nestedScrollConnection.onPreScroll(available, source)
                return behaviorConsumed
            }
        }
    }

    var isSheetLayoutComplete by remember { mutableStateOf(false) }

    var scaffoldInnerPaddingBottomPadding by remember { mutableStateOf(0.dp) }

    val density = LocalDensity.current

    LaunchedEffect(sheetState, searchBarState, parentScaffoldHeightPx) {
        if (!isSheetLayoutComplete) return@LaunchedEffect

        snapshotFlow { sheetState.requireOffset() }
            .collect { currentOffset ->
                try {
                    // parentScaffoldHeightPx의 값은 keyboardInfo.height 값이 계산되어 적용되어 계산됨. 까먹지 말자!!
                    // 부모 scaffold의 높이를 기준으로 pratiallyExpandedOffset 기준값을 설정!!
                    val partiallyExpandedOffset = when (searchBarState) {
                        SearchBarState.CLOSED ->
                            parentScaffoldHeightPx - with(density) {
                                (scaffoldInnerPaddingBottomPadding + 56.dp).toPx()
                            }

                        SearchBarState.OPENED -> {
                            parentScaffoldHeightPx - with(density) {
                                scaffoldInnerPaddingBottomPadding.toPx()
                            }
                        }
                    }

                    val finalProgress =
                        calculateDragProgress(
                            currentOffset,
                            partiallyExpandedOffset,
                            parentScaffoldHeightPx
                        )

                    bottomSheetOffset = finalProgress

                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Logger.d("BottomSheet error: ${e.message}")
                    }
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

    when (val state = updateDialogState) {
        is MainViewModel.UpdateDialogState.Hidden -> {
            // Nothing to show
        }

        is MainViewModel.UpdateDialogState.Visible -> {
            UpdateDialog(
                updateInfo = state.updateInfo,
                onUpdateClick = {
                    // GitHub 릴리즈 페이지 열기
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        state.updateInfo.updateUrl.toUri()
                    )
                    context.startActivity(intent)
                    mainViewModel.dismissUpdateDialog()
                },
                onDismiss = {
                    mainViewModel.dismissUpdateDialog()
                }
            )
        }
    }


    Scaffold(
        containerColor = Color.White,
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                parentScaffoldHeightPx = coordinates.size.height.toFloat()
            },
        bottomBar = {
            BottomNavigationBar(
                searchBarState = searchBarState,
                bottomSheetOffset = { bottomSheetOffset },
                selectedTab = selectedTab,
                onTabSelected = { tab -> selectedTab = tab },
            )
        }
    ) { innerPadding ->
        scaffoldInnerPaddingBottomPadding = innerPadding.calculateBottomPadding()

        PlayerBottomSheetScaffold(
            updateIsSheetLayoutComplete = { isSheetLayoutComplete = it },
            topAppBar = {
                MainAppBar(
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
                        searchBarState = SearchBarState.CLOSED
                    },
                    mainViewModel = mainViewModel,
                    searchBarState = searchBarState,
                    updateSearchBarState = {
                        searchBarState = it
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
            mainViewModel = mainViewModel,
            bottomSheetOffset = { bottomSheetOffset },
            searchBarState = searchBarState,
            innerPadding = innerPadding,
            bottomSheetState = sheetState,
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
}

private fun calculateDragProgress(
    currentOffset: Float,
    partiallyExpandedOffset: Float,
    hiddenOffset: Float
): Float {
    val expandedOffset = 0.0f

    val result = when {
        currentOffset <= expandedOffset -> {
            1f
        }

        currentOffset < partiallyExpandedOffset -> {
            val range = partiallyExpandedOffset - expandedOffset
            val position = currentOffset - expandedOffset
            val ratio = (position / range).coerceIn(0f, 1f)
            val result = 1f - ratio
            result
        }

        currentOffset <= hiddenOffset -> {
            val range = hiddenOffset - partiallyExpandedOffset
            val position = currentOffset - partiallyExpandedOffset
            val ratio = (position / range).coerceIn(0f, 1f)
            val result = -ratio
            result
        }

        else -> {
            -1f
        }
    }

    return result
}


