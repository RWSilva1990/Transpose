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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.main.MainViewModel
import com.example.main.components.appbar.SearchBarState

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PlayerBottomSheetScaffold(
    updateIsSheetLayoutComplete: (Boolean) -> Unit,
    topAppBar: @Composable() (() -> Unit)? = null,
    bottomSheetState: SheetState,
    innerPadding: PaddingValues,
    mainViewModel: MainViewModel,
    searchBarState: SearchBarState,
    bottomSheetOffset: () -> Float,
    onNavigateToChannelScreen: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val density = LocalDensity.current


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

            bottomSheetOffset() >= 1.0f -> 56.dp

            bottomSheetOffset() <= 0.0f -> {
                val progress = (-bottomSheetOffset() * 25).coerceIn(0f, 1f)
                (56 * (1 - progress)).dp
            }

            else -> {
                val progress = bottomSheetOffset().coerceIn(0f, 1f)
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
            .drawBehind { updateIsSheetLayoutComplete(true) }
            .padding(bottom = scaffoldBottomPadding),
        sheetContent = {
            PlayerBottomSheet(
                mainViewModel = mainViewModel,
                bottomSheetState = bottomSheetState,
                bottomSheetOffset = bottomSheetOffset,
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

