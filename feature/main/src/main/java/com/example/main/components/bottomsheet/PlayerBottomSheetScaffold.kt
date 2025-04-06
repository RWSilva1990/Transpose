package com.example.main.components.bottomsheet

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.main.BuildConfig
import com.example.main.MainViewModel
import com.example.main.components.appbar.SearchWidgetState
import com.example.util.Logger
import kotlin.math.abs

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PlayerBottomSheetScaffold(
    topAppBar: @Composable() (() -> Unit)? = null,
    mainViewModel: MainViewModel,
    bottomSheetState: SheetState,
    normalizedOffset: Float,
    setNormalizedOffset: (Float) -> Unit,
    searchWidgetState: SearchWidgetState,
    innerPadding: PaddingValues,
    onNavigateToChannelScreen: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    val screenHeightPx = with(density) { screenHeightDp.toPx() }

    val searchBarClosedSheetPeekHeight = remember(innerPadding) {
        innerPadding.calculateBottomPadding() + 56.dp
    }

    val navigationBarHeight = getNavigationBarHeightDp()
    val searchBarOpenedSheetPeekHeight = remember {
        navigationBarHeight + 56.dp
    }
    val navBarHeightPx =
        with(density) { WindowInsets.navigationBars.getBottom(density).toDp().toPx() }
    val partiallyExpandedOffsetPx = remember { mutableFloatStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()
    val isKeyboardVisible = keyboardAsState().value

    // 터치 감지를 위한 상태
    var isTouching by remember { mutableStateOf(false) }

    // 마지막 계산된 progress를 저장하기 위한 상태
    val lastProgressState = remember { mutableFloatStateOf(0f) }

    var userIsTouching by remember { mutableStateOf(false) }

    // 키보드가 보일 때 사용할 특별한 오프셋 값 계산
    val keyboardAdjustedOffset = remember(normalizedOffset, isKeyboardVisible) {
        if (isKeyboardVisible) {
            0f
        } else {
            normalizedOffset
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )


    val offsetObserver =
        remember(bottomSheetState, navBarHeightPx, screenHeightPx, isKeyboardVisible) {
            object : ViewTreeObserver.OnDrawListener {
                private var lastProgress = 0f
                private var hasInitializedPartialOffset = false

                override fun onDraw() {
                    try {
                        val currentOffset = bottomSheetState.requireOffset()

                        // 기준점 설정
                        val expandedOffset = 0f
                        val hiddenOffset = screenHeightPx

                        // PartiallyExpanded 상태의 오프셋 초기화
                        if (!hasInitializedPartialOffset &&
                            bottomSheetState.currentValue == SheetValue.PartiallyExpanded &&
                            currentOffset > 0
                        ) {
                            partiallyExpandedOffsetPx.floatValue = currentOffset
                            hasInitializedPartialOffset = true
                        }

                        // 안정적인 partiallyExpandedOffset 유지
                        var partiallyExpandedOffset = partiallyExpandedOffsetPx.floatValue
                        if (partiallyExpandedOffset <= 0 || partiallyExpandedOffset >= hiddenOffset) {
                            // 기본값 계산
                            val sheetPeekHeightPx = with(density) {
                                (innerPadding.calculateBottomPadding() + 56.dp).toPx()
                            }
                            partiallyExpandedOffset = screenHeightPx - sheetPeekHeightPx

                            // 범위 체크
                            if (partiallyExpandedOffset <= 0 || partiallyExpandedOffset >= hiddenOffset) {
                                partiallyExpandedOffset = hiddenOffset / 2
                            }
                        }

                        // 키보드 표시 여부에 따라 progress 계산 방식 결정
                        var finalProgress = if (isKeyboardVisible) {
                            // 키보드가 표시된 경우 현재 상태에 맞는 고정값 사용
                            when (bottomSheetState.currentValue) {
                                SheetValue.Expanded -> 1f
                                SheetValue.PartiallyExpanded -> 0f
                                SheetValue.Hidden -> -1f
                            }
                        } else {
                            // 키보드가 없을 때는 실제 오프셋 기반으로 계산
                            calculateDragProgress(
                                currentOffset,
                                expandedOffset,
                                partiallyExpandedOffset,
                                hiddenOffset
                            )
                        }

                        // searchWidgetState가 열려있고 PartiallyExpanded 상태일 때 강제로 0으로 설정
                        if (searchWidgetState == SearchWidgetState.OPENED &&
                            bottomSheetState.currentValue == SheetValue.PartiallyExpanded
                        ) {
                            finalProgress = 0f
                        }

                        // 변화가 있을 때만 업데이트
                        val threshold = 0.001f

                        if (abs(finalProgress - lastProgress) > threshold) {
                            setNormalizedOffset(finalProgress)
                            lastProgress = finalProgress

                            if (BuildConfig.DEBUG) {
//                            Logger.d("✅ ✅ Progress: $finalProgress | 상태: ${bottomSheetState.currentValue} | 키보드: $isKeyboardVisible | 검색바: ${searchWidgetState == SearchWidgetState.OPENED}")
                            }
                        }
                    } catch (e: Exception) {
                        if (BuildConfig.DEBUG) {
                            Logger.d("BottomSheet error: ${e.message}")
                        }
                    }
                }
            }
        }// 더 정확한 터치 감지를 위한 Modifier


    // offsetObserver 대신 LaunchedEffect를 사용한 구현
    LaunchedEffect(bottomSheetState, isKeyboardVisible, searchWidgetState) {
        // PartiallyExpanded 상태의 오프셋을 초기화하기 위한 변수
        var hasInitializedPartialOffset = false

        snapshotFlow { bottomSheetState.requireOffset() }
            .collect { currentOffset ->
                try {
                    // 기준점 설정
                    val expandedOffset = 0f
                    val hiddenOffset = screenHeightPx

                    // PartiallyExpanded 상태의 오프셋 초기화
                    if (!hasInitializedPartialOffset &&
                        bottomSheetState.currentValue == SheetValue.PartiallyExpanded &&
                        currentOffset > 0
                    ) {
                        partiallyExpandedOffsetPx.floatValue = currentOffset
                        hasInitializedPartialOffset = true
                    }

                    // 안정적인 partiallyExpandedOffset 유지
                    var partiallyExpandedOffset = partiallyExpandedOffsetPx.floatValue
                    if (partiallyExpandedOffset <= 0 || partiallyExpandedOffset >= hiddenOffset) {
                        // 기본값 계산
                        val sheetPeekHeightPx = with(density) {
                            (innerPadding.calculateBottomPadding() + 56.dp).toPx()
                        }
                        partiallyExpandedOffset = screenHeightPx - sheetPeekHeightPx

                        // 범위 체크
                        if (partiallyExpandedOffset <= 0 || partiallyExpandedOffset >= hiddenOffset) {
                            partiallyExpandedOffset = hiddenOffset / 2
                        }
                    }

                    // 키보드 표시 여부에 따라 progress 계산 방식 결정
                    var finalProgress = if (isKeyboardVisible) {
                        // 키보드가 표시된 경우 현재 상태에 맞는 고정값 사용
                        when (bottomSheetState.currentValue) {
                            SheetValue.Expanded -> 1f
                            SheetValue.PartiallyExpanded -> 0f
                            SheetValue.Hidden -> -1f
                        }
                    } else {
                        // 키보드가 없을 때는 실제 오프셋 기반으로 계산
                        calculateDragProgress(
                            currentOffset,
                            expandedOffset,
                            partiallyExpandedOffset,
                            hiddenOffset
                        )
                    }

                    // searchWidgetState가 열려있고 PartiallyExpanded 상태일 때 강제로 0으로 설정
                    if (searchWidgetState == SearchWidgetState.OPENED &&
                        bottomSheetState.currentValue == SheetValue.PartiallyExpanded
                    ) {
                        finalProgress = 0f
                    }

                    // 변화가 있을 때만 업데이트
                    val threshold = 0.001f
                    if (abs(finalProgress - lastProgressState.floatValue) > threshold) {
                        setNormalizedOffset(finalProgress)
                        lastProgressState.floatValue = finalProgress
                    }
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Logger.d("BottomSheet error: ${e.message}")
                    }
                }
            }
    }
    // 뷰 트리에 드로우 리스너 부착
    val view = LocalView.current
//    DisposableEffect(view, bottomSheetState) {
//        val observer = view.viewTreeObserver
//        observer.addOnDrawListener(offsetObserver)
//        onDispose {
//            observer.removeOnDrawListener(offsetObserver)
//        }
//    }

    val sheetPeekHeight = remember(bottomSheetState.currentValue, searchWidgetState) {
        when (bottomSheetState.currentValue) {
            SheetValue.Hidden -> 1.dp
            else -> if (searchWidgetState == SearchWidgetState.CLOSED) {
                searchBarClosedSheetPeekHeight
            } else {
                searchBarOpenedSheetPeekHeight
            }
        }
    }

    val scaffoldBottomPadding = remember(bottomSheetState.currentValue, normalizedOffset) {
        when {
            // Hidden 상태에서는 항상 패딩 0
            bottomSheetState.currentValue == SheetValue.Hidden -> 0.dp

            // Expanded 상태에서는 항상 패딩 56dp
            normalizedOffset >= 1.0f -> 56.dp

            // 드래그 중 계산
            normalizedOffset <= 0.0f -> {
                // 아래로 드래그할 때 패딩이 빠르게 사라지도록
                val progress = (-normalizedOffset * 25).coerceIn(0f, 1f)
                (56 * (1 - progress)).dp
            }

            // 0.0f < normalizedOffset < 1.0f 범위 (PartiallyExpanded에서 Expanded로 드래그 중)
            else -> {
                // 위로 드래그할 때 패딩이 점진적으로 늘어나도록
                val progress = normalizedOffset.coerceIn(0f, 1f)
                (56 * progress).dp
            }
        }
    }

    LaunchedEffect(bottomSheetState) {
        snapshotFlow { bottomSheetState.currentValue }.collect {
            when (it) {
                SheetValue.Hidden -> {
                    mainViewModel.stopPlayback()
                }

                SheetValue.Expanded -> {}
                SheetValue.PartiallyExpanded -> {}
            }
        }
    }

    BottomSheetScaffold(
        sheetContainerColor = Color.White,
        scaffoldState = scaffoldState,
        modifier = Modifier
            .padding(bottom = scaffoldBottomPadding),
        sheetContent = {
            PlayerBottomSheet(
                mainViewModel = mainViewModel,
                bottomSheetState = bottomSheetState,
                normalizedOffset = keyboardAdjustedOffset,
                onNavigateToChannelScreen = onNavigateToChannelScreen,
            )
        },
        sheetShape = RectangleShape,
        sheetPeekHeight = sheetPeekHeight,
        topBar = topAppBar,
        sheetSwipeEnabled = true,
        sheetDragHandle = null,
    ) { playerBottomSheetInnerPadding ->
        content(playerBottomSheetInnerPadding)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
private fun calculateDragProgress(
    currentOffset: Float,
    expandedOffset: Float,
    partiallyExpandedOffset: Float,
    hiddenOffset: Float
): Float {
    return when {
        currentOffset <= expandedOffset -> 1f

        currentOffset < partiallyExpandedOffset -> {
            val range = partiallyExpandedOffset - expandedOffset
            if (range > 0) {
                val position = currentOffset - expandedOffset
                val ratio = (position / range).coerceIn(0f, 1f)
                1f - ratio
            } else {
                0f
            }
        }

        currentOffset <= hiddenOffset -> {
            val range = hiddenOffset - partiallyExpandedOffset
            if (range > 0) {
                val position = currentOffset - partiallyExpandedOffset
                val ratio = (position / range).coerceIn(0f, 1f)
                -ratio
            } else {
                0f
            }
        }

        else -> -1f
    }
}

@Composable
fun getNavigationBarHeightDp(): Dp {
    val density = LocalDensity.current
    return with(density) {
        WindowInsets.navigationBars.getBottom(density).toDp()
    }
}


@Composable
fun keyboardAsState(): State<Boolean> {
    val keyboardState = remember { mutableStateOf(false) }
    val view = LocalView.current

    val viewTreeObserver = view.viewTreeObserver

    DisposableEffect(viewTreeObserver) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            keyboardState.value = if (keypadHeight > screenHeight * 0.15) {
                true
            } else {
                false
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)

        onDispose {
            viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }

    return keyboardState
}
