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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.layout.layout
import com.example.main.components.bottomsheet.GraphicsLayerConstants
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
import androidx.compose.ui.unit.Velocity
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
                return scrollBehavior.nestedScrollConnection.onPostScroll(consumed, available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return scrollBehavior.nestedScrollConnection.onPostFling(consumed, available)
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
                    onSettingClicked = {
                        activeNavController.navigate(settingsRouteFor(selectedTab))
                    },
                    onSearchClicked = {
                        activeNavController.navigate(searchRouteFor(selectedTab, it))
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
                activeNavController.navigate(channelRouteFor(selectedTab, channelId))
            }
        ) { playerBottomSheetScaffoldPadding ->
            val miniPlayerHeightPx = with(density) { GraphicsLayerConstants.PEEK_HEIGHT.roundToPx() }
            val bottomNavHeightPx = with(density) { 56.dp.roundToPx() }

            when (selectedTab) {
                MainTab.Home -> {
                    HomeNavHost(
                        navController = homeNavController,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection)
                            .dynamicBottomPadding(
                                miniPlayerHeightPx = miniPlayerHeightPx,
                                bottomNavHeightPx = bottomNavHeightPx,
                                bottomSheetOffset = { bottomSheetOffset }
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
                            .fillMaxSize()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection)
                            .dynamicBottomPadding(
                                miniPlayerHeightPx = miniPlayerHeightPx,
                                bottomNavHeightPx = bottomNavHeightPx,
                                bottomSheetOffset = { bottomSheetOffset }
                            ),
                        bottomSheetState = sheetState,
                        navigateToHomeTab = { selectedTab = MainTab.Home },
                        onUpdateCheckClick = {},
                        onContactClick = {},
                    )
                }

                MainTab.Convert -> {
                    ConvertNavHost(
                        navController = convertNavController,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .nestedScroll(nestedScrollConnection)
                            .dynamicBottomPadding(
                                miniPlayerHeightPx = miniPlayerHeightPx,
                                bottomNavHeightPx = bottomNavHeightPx,
                                bottomSheetOffset = { bottomSheetOffset }
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
 * BottomSheet offset에 따라 동적으로 bottom padding을 조절하는 Modifier
 *
 * bottomSheetOffset 값:
 * - 1.0 ~ 0.0: Expanded → PartiallyExpanded (mini player 높이만큼 하단 패딩)
 * - 0.0 ~ -1.0: PartiallyExpanded → Hidden (점진적으로 패딩 감소)
 *
 * BottomNavigation 패딩:
 * - offset <= 0: BottomNav가 완전히 보임 → 전체 높이 패딩
 * - offset >= 1: BottomNav가 숨겨짐 → 0 패딩
 * - 0 < offset < 1: 선형 보간
 */
private fun Modifier.dynamicBottomPadding(
    miniPlayerHeightPx: Int,
    bottomNavHeightPx: Int,
    bottomSheetOffset: () -> Float
): Modifier = this.layout { measurable, constraints ->
    val offset = bottomSheetOffset()

    // Mini player 패딩: offset >= 0일 때 전체, offset이 -1로 갈수록 0으로 감소
    val miniPlayerPadding = when {
        offset >= 0f -> miniPlayerHeightPx
        else -> {
            val progress = (offset + 1f).coerceIn(0f, 1f)
            (miniPlayerHeightPx * progress).toInt()
        }
    }

    // BottomNav 패딩 계산
    // - offset >= 0: Mini player가 BottomNav를 덮으므로 BottomNav 패딩 불필요
    // - offset < 0: Mini player가 사라지면서 BottomNav가 노출됨
    val additionalPadding = when {
        offset >= 0f -> miniPlayerPadding  // Mini player가 BottomNav를 덮음
        else -> {
            // Mini player가 사라지면서 BottomNav가 드러남
            // 둘 중 큰 값을 사용하여 컨텐츠가 가려지지 않도록 함
            maxOf(miniPlayerPadding, bottomNavHeightPx)
        }
    }

    // 하단 패딩을 위해 높이를 줄여서 측정
    val newMaxHeight = (constraints.maxHeight - additionalPadding).coerceAtLeast(0)
    val newMinHeight = (constraints.minHeight - additionalPadding).coerceIn(0, newMaxHeight)
    val adjustedConstraints = constraints.copy(
        minHeight = newMinHeight,
        maxHeight = newMaxHeight
    )
    val placeable = measurable.measure(adjustedConstraints)

    // 전체 높이는 컨텐츠 + 하단 패딩
    layout(placeable.width, placeable.height + additionalPadding) {
        placeable.placeRelative(0, 0)  // 컨텐츠는 상단에 고정
    }
}



