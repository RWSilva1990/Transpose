package com.example.main.components.bottomsheet

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.main.BuildConfig
import com.example.main.MainDataModel
import com.example.main.MainPlayerViewModel
import com.example.main.MainUiStateViewModel
import com.example.main.MainViewModel
import com.example.main.components.appbar.SearchBarState
import com.example.util.Logger

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PlayerBottomSheetScaffold(
    parentScaffoldHeightPx: Float,
    topAppBar: @Composable() (() -> Unit)? = null,
    bottomSheetState: SheetState,
    innerPadding: PaddingValues,
    mainUiStateViewModel: MainUiStateViewModel,
    mainDataModel: MainDataModel,
    mainPlayerViewModel: MainPlayerViewModel,
    onNavigateToChannelScreen: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val density = LocalDensity.current


    val sheetPeekHeightPxWhenSearchBarClosed = with(density) {
        (innerPadding.calculateBottomPadding() + 56.dp).toPx()
    }

    val sheetPeekHeightPxWhenSearchBarOpened = with(density) {
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

    val searchBarState by mainUiStateViewModel.searchBarState.collectAsState()
    val bottomSheetOffset by mainUiStateViewModel.bottomSheetOffset.collectAsState()

    LaunchedEffect(bottomSheetState, searchBarState, parentScaffoldHeightPx) {
        snapshotFlow { bottomSheetState.requireOffset() }
            .collect { currentOffset ->
                try {
                    //parentScaffoldHeightPx의 값은, keyboardInfo.height 값이 계산되어 적용되어 계산됨.
                    val partiallyExpandedOffset = when (searchBarState) {
                        SearchBarState.CLOSED ->
                            parentScaffoldHeightPx - sheetPeekHeightPxWhenSearchBarClosed

                        SearchBarState.OPENED -> {
                            parentScaffoldHeightPx - sheetPeekHeightPxWhenSearchBarOpened
                        }
                    }

                    val finalProgress =
                        calculateDragProgress(
                            currentOffset,
                            partiallyExpandedOffset,
                            parentScaffoldHeightPx
                        )


                    mainUiStateViewModel.updateBottomSheetOffset(finalProgress)

                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Logger.d("BottomSheet error: ${e.message}")
                    }
                }
            }
    }

    val sheetPeekHeight = remember(bottomSheetState.currentValue, searchBarState) {
        when (bottomSheetState.currentValue) {
            SheetValue.Hidden -> 1.dp
            else -> if (searchBarState == SearchBarState.CLOSED) {
                searchBarClosedSheetPeekHeight
            } else {
                searchBarOpenedSheetPeekHeight
            }
        }
    }

    val scaffoldBottomPadding = remember(bottomSheetState.currentValue, bottomSheetOffset) {
        when {
            bottomSheetState.currentValue == SheetValue.Hidden -> 0.dp

            bottomSheetOffset >= 1.0f -> 56.dp

            bottomSheetOffset <= 0.0f -> {
                val progress = (-bottomSheetOffset * 25).coerceIn(0f, 1f)
                (56 * (1 - progress)).dp
            }

            else -> {
                val progress = bottomSheetOffset.coerceIn(0f, 1f)
                (56 * progress).dp
            }
        }
    }

    LaunchedEffect(bottomSheetState) {
        snapshotFlow { bottomSheetState.currentValue }.collect {
            when (it) {
                SheetValue.Hidden -> {
                    mainPlayerViewModel.stopPlayback()
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
                mainUiStateViewModel = mainUiStateViewModel,
                mainDataModel = mainDataModel,
                mainPlayerViewModel = mainPlayerViewModel,
                bottomSheetState = bottomSheetState,
                normalizedOffset = bottomSheetOffset,
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



