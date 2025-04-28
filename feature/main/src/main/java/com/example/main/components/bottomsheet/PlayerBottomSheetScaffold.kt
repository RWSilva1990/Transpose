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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.main.BuildConfig
import com.example.main.MainViewModel
import com.example.main.components.appbar.SearchWidgetState
import com.example.util.Logger

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PlayerBottomSheetScaffold(
    parentScaffoldHeightPx: Float,
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
    val density = LocalDensity.current


    val sheetPeekHeightPxWhenSearchBarClosed = with(density) {
        (innerPadding.calculateBottomPadding() + 56.dp).toPx()
    }

    val sheetPeekHeightPxWhenSearchBarOpened = with(density){
        innerPadding.calculateBottomPadding().toPx()
    }

    val searchBarClosedSheetPeekHeight = remember(innerPadding) {
        innerPadding.calculateBottomPadding() + 56.dp
    }

    val navigationBarHeight = with(density) {
        WindowInsets.navigationBars.getBottom(density).toDp()
    }
    val searchBarOpenedSheetPeekHeight = remember {
        navigationBarHeight + 56.dp
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState
    )

    LaunchedEffect(bottomSheetState, searchWidgetState, parentScaffoldHeightPx) {
        snapshotFlow { bottomSheetState.requireOffset() }
            .collect { currentOffset ->
                try {

                    //parentScaffoldHeightPx의 값은, keyboardInfo.height 값이 계산되어 적용되어 계산됨.

                    val partiallyExpandedOffset = when (searchWidgetState) {
                        SearchWidgetState.CLOSED ->
                            parentScaffoldHeightPx - sheetPeekHeightPxWhenSearchBarClosed
                        SearchWidgetState.OPENED -> {
                            parentScaffoldHeightPx - sheetPeekHeightPxWhenSearchBarOpened
                        }
                    }

                    Logger.d("currentOffset: $currentOffset, partiallyExpandedOffset: $partiallyExpandedOffset " +
                            "parentScaffoldHeightPx: $parentScaffoldHeightPx temp: $sheetPeekHeightPxWhenSearchBarOpened ")

                    val finalProgress =
                        calculateDragProgress(
                            currentOffset,
                            partiallyExpandedOffset,
                            parentScaffoldHeightPx
                        )


                    setNormalizedOffset(finalProgress)

                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Logger.d("BottomSheet error: ${e.message}")
                    }
                }
            }
    }

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
            bottomSheetState.currentValue == SheetValue.Hidden -> 0.dp

            normalizedOffset >= 1.0f -> 56.dp

            normalizedOffset <= 0.0f -> {
                val progress = (-normalizedOffset * 25).coerceIn(0f, 1f)
                (56 * (1 - progress)).dp
            }

            else -> {
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
                normalizedOffset = normalizedOffset,
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


private fun calculateDragProgress(
    currentOffset: Float,
    partiallyExpandedOffset: Float,
    hiddenOffset: Float
): Float {
    val expandedOffset = 0.0f
//    Logger.d("calculateDragProgress - INPUT: currentOffset=$currentOffset, partiallyExpandedOffset=$partiallyExpandedOffset, hiddenOffset=$hiddenOffset")

    val result = when {
        currentOffset <= expandedOffset -> {
//            Logger.d("Case 1: currentOffset <= expandedOffset, returning 1f")
            1f
        }

        currentOffset < partiallyExpandedOffset -> {
            val range = partiallyExpandedOffset - expandedOffset
            val position = currentOffset - expandedOffset
            val ratio = (position / range).coerceIn(0f, 1f)
            val result = 1f - ratio
//            Logger.d("Case 2: Between expanded and partial - range=$range, position=$position, ratio=$ratio, result=$result")
            result
        }

        currentOffset <= hiddenOffset -> {
            val range = hiddenOffset - partiallyExpandedOffset
            val position = currentOffset - partiallyExpandedOffset
            val ratio = (position / range).coerceIn(0f, 1f)
            val result = -ratio
//            Logger.d("Case 3: Between partial and hidden - range=$range, position=$position, ratio=$ratio, result=$result")
            result
        }

        else -> {
//            Logger.d("Case 4: Beyond hiddenOffset, returning -1f")
            -1f
        }
    }

    Logger.d("calculateDragProgress - FINAL RESULT: $result")
    return result
}

data class KeyboardInfo(
    val isVisible: Boolean,
    val height: Int
)

@Composable
fun keyboardInfoAsState(): State<KeyboardInfo> {
    val keyboardInfo = remember { mutableStateOf(KeyboardInfo(false, 0)) }
    val view = LocalView.current
    val viewTreeObserver = view.viewTreeObserver

    DisposableEffect(viewTreeObserver) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom - 72

            val isKeyboardVisible = keypadHeight > screenHeight * 0.15

            keyboardInfo.value = KeyboardInfo(
                isVisible = isKeyboardVisible,
                height = keypadHeight
            )
        }
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)

        onDispose {
            viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }

    return keyboardInfo
}
