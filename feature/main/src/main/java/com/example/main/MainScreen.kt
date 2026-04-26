package com.example.main

import UpdateDialog
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
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
import com.example.ui.theme.blendColors
import com.example.util.Logger
import com.example.util.ToastUtil
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
    val updateDialogState by mainViewModel.updateDialogState.collectAsStateWithLifecycle()
    val localSearchQuery by mainViewModel.localSearchQuery.collectAsStateWithLifecycle()
    val isLocalSearchActive by mainViewModel.isLocalSearchActive.collectAsStateWithLifecycle()
    val searchQuery by mainViewModel.searchQuery.collectAsStateWithLifecycle()
    val suggestionKeywords by mainViewModel.suggestionKeywords.collectAsStateWithLifecycle()

    val permissionGranted by mainViewModel.permissionGranted.collectAsStateWithLifecycle()

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

    LaunchedEffect(Unit) {
        mainViewModel.start()
    }

    // Toast event observer
    LaunchedEffect(Unit) {
        mainViewModel.toastEvent.collect { message ->
            ToastUtil.showShort(context, message)
        }
    }

    val systemUiController = rememberSystemUiController()

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false
    )

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    val homeNavController = rememberNavController()
    val libraryNavController = rememberNavController()
    val convertNavController = rememberNavController()

    var selectedTab by remember { mutableStateOf<MainTab>(MainTab.Home) }

    val activeNavController = when (selectedTab) {
        MainTab.Home -> homeNavController
        MainTab.Library -> libraryNavController
        MainTab.Convert -> convertNavController
    }

    val libraryCurrentBackStackEntry by libraryNavController.currentBackStackEntryAsState()
    val isOnLocalFilesScreen by remember {
        derivedStateOf {
            libraryCurrentBackStackEntry?.destination?.route?.startsWith("library_my_local_file_item") == true
                    && selectedTab == MainTab.Library
        }
    }

    LaunchedEffect(isOnLocalFilesScreen) {
        if (!isOnLocalFilesScreen) {
            mainViewModel.setLocalSearchActive(false)
        }
    }

    var bottomSheetOffset by remember {
        mutableFloatStateOf(-1f)
    }

    var searchBarState by remember {
        mutableStateOf(SearchBarState.CLOSED)
    }

    var parentScaffoldHeightPx by remember {
        mutableFloatStateOf(0f)
    }

    val nestedScrollConnection = remember(scrollBehavior) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return scrollBehavior.nestedScrollConnection.onPreScroll(available, source)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return scrollBehavior.nestedScrollConnection.onPostScroll(
                    consumed,
                    available,
                    source
                )
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return scrollBehavior.nestedScrollConnection.onPostFling(consumed, available)
            }
        }
    }

    var isSheetLayoutComplete by remember { mutableStateOf(false) }

    var scaffoldInnerPaddingBottomPadding by remember { mutableStateOf(0.dp) }

    val density = LocalDensity.current

    LaunchedEffect(sheetState, searchBarState, isLocalSearchActive, parentScaffoldHeightPx) {
        if (!isSheetLayoutComplete) return@LaunchedEffect

        snapshotFlow { sheetState.requireOffset() }
            .collect { currentOffset ->
                try {
                    // parentScaffoldHeightPx의 값은 keyboardInfo.height 값이 계산되어 적용되어 계산됨. 까먹지 말자!!
                    // 부모 scaffold의 높이를 기준으로 pratiallyExpandedOffset 기준값을 설정!!
                    // isLocalSearchActive가 true일 때도 SearchBarState.OPENED처럼 처리
                    val isSearchActive =
                        searchBarState == SearchBarState.OPENED || isLocalSearchActive
                    val partiallyExpandedOffset = if (!isSearchActive) {
                        parentScaffoldHeightPx - with(density) {
                            (scaffoldInnerPaddingBottomPadding + 56.dp).toPx()
                        }
                    } else {
                        parentScaffoldHeightPx - with(density) {
                            scaffoldInnerPaddingBottomPadding.toPx()
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

    val statusBarColor by remember {
        derivedStateOf {
            val ratio = bottomSheetOffset.coerceIn(0f, 1f)
            blendColors(AppColors.StatusBarBackground, AppColors.CharcoalGray, ratio)
        }
    }
    val navigationBarColor by remember {
        derivedStateOf {
            val ratio = bottomSheetOffset.coerceIn(0f, 1f)
            blendColors(AppColors.BlueBackground, AppColors.CharcoalGray, ratio)
        }
    }

    LaunchedEffect(statusBarColor, navigationBarColor) {
        systemUiController.setStatusBarColor(color = statusBarColor)
        systemUiController.setNavigationBarColor(color = navigationBarColor)
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
                onTabReselected = { tab ->
                    val navController = when (tab) {
                        MainTab.Home -> homeNavController
                        MainTab.Library -> libraryNavController
                        MainTab.Convert -> convertNavController
                    }
                    val startRoute = when (tab) {
                        MainTab.Home -> HomeRoutes.Playlist.route
                        MainTab.Library -> LibraryRoutes.MyPlaylist.route
                        MainTab.Convert -> ConvertRoutes.AudioEdit.route
                    }
                    navController.popBackStack(startRoute, inclusive = false)
                },
                isLocalSearchActive = isLocalSearchActive,
            )
        }
    ) { innerPadding ->
        scaffoldInnerPaddingBottomPadding = innerPadding.calculateBottomPadding()

        PlayerBottomSheetScaffold(
            updateIsSheetLayoutComplete = { isSheetLayoutComplete = it },
            topAppBar = {
                MainAppBar(
                    onSettingClicked = {
                        activeNavController.navigate(settingsRouteFor(selectedTab))
                    },
                    onSearchClicked = {
                        activeNavController.navigate(searchRouteFor(selectedTab, it))
                        searchBarState = SearchBarState.CLOSED
                    },
                    searchBarState = searchBarState,
                    updateSearchBarState = {
                        searchBarState = it
                    },
                    scrollBehavior = scrollBehavior,
                    isOnLocalFilesScreen = isOnLocalFilesScreen,
                    isLocalSearchActive = isLocalSearchActive,
                    localSearchQuery = localSearchQuery,
                    searchQuery = searchQuery,
                    suggestionKeywords = suggestionKeywords,
                    onUpdateLocalSearchQuery = mainViewModel::updateLocalSearchQuery,
                    onSetLocalSearchActive = mainViewModel::setLocalSearchActive,
                    onUpdateSearchQuery = mainViewModel::updateSearchQuery,
                    onClearSearchQuery = mainViewModel::clearSearchQuery,
                )
            },
            mainViewModel = mainViewModel,
            bottomSheetOffset = { bottomSheetOffset },
            searchBarState = searchBarState,
            innerPadding = innerPadding,
            bottomSheetState = sheetState,
            onNavigateToChannelScreen = { channelId ->
                activeNavController.navigate(channelRouteFor(selectedTab, channelId))
            },
            isLocalSearchActive = isLocalSearchActive,
            onStopPlayback = mainViewModel::stopPlayback,
        ) { playerBottomSheetScaffoldPadding ->
            val bottomNavHeightPx = with(density) { 56.dp.roundToPx() }

            when (selectedTab) {
                MainTab.Home -> {
                    HomeNavHost(
                        navController = homeNavController,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection)
                            .dynamicBottomPadding(
                                bottomNavHeightPx = bottomNavHeightPx,
                                bottomSheetOffset = { bottomSheetOffset },
                                isBottomNavHidden = isLocalSearchActive
                            ),
                        onUpdateCheckClick = {},
                        onContactClick = {},
                        bottomSheetState = sheetState
                    )
                }

                MainTab.Library -> {
                    LibraryNavHost(
                        navController = libraryNavController,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection)
                            .dynamicBottomPadding(
                                bottomNavHeightPx = bottomNavHeightPx,
                                bottomSheetOffset = { bottomSheetOffset },
                                isBottomNavHidden = isLocalSearchActive
                            ),
                        bottomSheetState = sheetState,
                        navigateToHomeTab = { selectedTab = MainTab.Home },
                        onUpdateCheckClick = {},
                        onContactClick = {},
                        localSearchQuery = localSearchQuery,
                        isLocalSearchActive = isLocalSearchActive,
                        onCloseLocalSearch = { mainViewModel.setLocalSearchActive(false) }
                    )
                }

                MainTab.Convert -> {
                    ConvertNavHost(
                        navController = convertNavController,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection)
                            .dynamicBottomPadding(
                                bottomNavHeightPx = bottomNavHeightPx,
                                bottomSheetOffset = { bottomSheetOffset },
                                isBottomNavHidden = isLocalSearchActive
                            ),
                        bottomSheetState = sheetState,
                        navigateToHomeTab = { selectedTab = MainTab.Home },
                        onUpdateCheckClick = {},
                        onContactClick = {},
                    )
                }
            }


            playerBottomSheetScaffoldPadding.calculateBottomPadding()
        }
    }



    BackHandler {
        // 1순위: BottomSheet가 확장된 경우 먼저 축소
        if (sheetState.currentValue == SheetValue.Expanded) {
            coroutineScope.launch {
                sheetState.partialExpand()
            }
            return@BackHandler
        }

        // 2순위: Local Search가 활성화된 경우 닫기
        if (isLocalSearchActive) {
            mainViewModel.setLocalSearchActive(false)
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

private fun settingsRouteFor(tab: MainTab) = when (tab) {
    MainTab.Home -> HomeRoutes.SettingsScreen.route
    MainTab.Library -> LibraryRoutes.SettingsScreen.route
    MainTab.Convert -> ConvertRoutes.SettingsScreen.route
}

private fun searchRouteFor(tab: MainTab, query: String) = when (tab) {
    MainTab.Home -> HomeRoutes.SearchResult.createRoute(query)
    MainTab.Library -> LibraryRoutes.SearchResult.createRoute(query)
    MainTab.Convert -> ConvertRoutes.SearchResult.createRoute(query)
}

private fun channelRouteFor(tab: MainTab, channelId: String) = when (tab) {
    MainTab.Home -> HomeRoutes.ChannelScreen.createRoute(channelId)
    MainTab.Library -> LibraryRoutes.ChannelScreen.createRoute(channelId)
    MainTab.Convert -> ConvertRoutes.ChannelScreen.createRoute(channelId)
}

/**
BottomSheet이 확장/축소될 때마다 Scaffold의 콘텐츠가 BottomNavigationBar에 가려지는 문제 해결을 위한 Modifier
- bottomNavHeightPx: BottomNavigationBar의 높이 (px 단위)
- isBottomNavHidden: Local Search가 활성화되어 BottomNavigationBar가 숨겨진 상태인지 여부
 */
private fun Modifier.dynamicBottomPadding(
    bottomNavHeightPx: Int,
    bottomSheetOffset: () -> Float,
    isBottomNavHidden: Boolean = false
): Modifier = this.layout { measurable, constraints ->

    val additionalPadding = if (isBottomNavHidden) 0 else bottomNavHeightPx

    val newMaxHeight = (constraints.maxHeight - additionalPadding).coerceAtLeast(0)
    val newMinHeight = (constraints.minHeight - additionalPadding).coerceIn(0, newMaxHeight)
    val adjustedConstraints = constraints.copy(
        minHeight = newMinHeight,
        maxHeight = newMaxHeight
    )
    val placeable = measurable.measure(adjustedConstraints)

    layout(placeable.width, placeable.height + additionalPadding) {
        placeable.placeRelative(0, 0)
    }
}
