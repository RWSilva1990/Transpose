package com.example.main.components.bottomsheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.main.MainPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistModalBottomSheetContainer(
    onDismiss: () -> Unit,
    mainPlayerViewModel: MainPlayerViewModel,
    playerViewHeight: Int,
) {

    PlaylistModalBottomSheet(
        onDismiss = { },
        mainPlayerViewModel = mainPlayerViewModel,
        playerViewHeight = playerViewHeight
    )

}
