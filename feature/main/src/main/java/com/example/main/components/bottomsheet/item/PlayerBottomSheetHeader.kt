package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.trace
import com.example.main.MainViewModel
import com.example.main.R
import com.example.main.components.bottomsheet.GraphicsLayerConstants.PEEK_HEIGHT
import com.example.util.constants.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomSheetHeader(
    bottomSheetOffset: () -> Float,
    bottomSheetState: SheetState,
    mainViewModel: MainViewModel,
) {

    val coroutineScope = rememberCoroutineScope()

    val isPlaying by mainViewModel.isPlaying.collectAsState()
    val currentItem by mainViewModel.currentItem.collectAsState()

    val displayTitle = currentItem?.title ?: ""

    trace("PlayerBottomSheetHeader") {
        Row(
            modifier = Modifier
                .height(PEEK_HEIGHT),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.3f)
                    .background(AppColors.BlueBackground)
            )

            Text(
                text = displayTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 12.dp)
                    .graphicsLayer {
                        alpha = bottomSheetHeaderAlpha(bottomSheetOffset())
                    }
            )

            IconButton(
                onClick = { mainViewModel.playPause() },
                modifier = Modifier
                    .padding(end = 5.dp)
                    .graphicsLayer {
                        alpha = bottomSheetHeaderAlpha(bottomSheetOffset())
                    }
            ) {
                Icon(
                    painterResource(id = if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24),
                    contentDescription = "Play/Pause",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = {
                    mainViewModel.stopPlayback()
                    coroutineScope.launch { bottomSheetState.hide() }
                },
                modifier = Modifier
                    .graphicsLayer {
                        alpha = bottomSheetHeaderAlpha(bottomSheetOffset())
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

private fun bottomSheetHeaderAlpha(bottomSheetOffset: Float): Float {
    return if (bottomSheetOffset < 0) 1f else {
        when {
            bottomSheetOffset < 0.2f -> {
                val alphaValue = (0.2 - bottomSheetOffset) / 0.2
                alphaValue.toFloat()
            }
            else -> 0f
        }
    }
}
