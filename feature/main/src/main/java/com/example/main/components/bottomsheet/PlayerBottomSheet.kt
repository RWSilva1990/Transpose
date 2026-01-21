package com.example.main.components.bottomsheet

import android.widget.ImageButton
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.util.trace
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ui.util.getDisplayStringResId
import com.example.main.MainViewModel
import com.example.main.components.bottomsheet.GraphicsLayerConstants.PEEK_HEIGHT
import com.example.main.components.bottomsheet.item.PlayerBottomSheetHeader
import com.example.main.components.bottomsheet.item.PlayerLoadingIndicator
import com.example.main.components.bottomsheet.item.PlayerThumbnailView
import com.example.main.components.bottomsheet.item.PlaylistFloatingButton
import com.example.main.components.bottomsheet.item.VideoDetailPanel
import com.example.util.constants.AppColors
import kotlinx.coroutines.launch
import kotlin.math.pow


object GraphicsLayerConstants {
    const val FULLY_EXPANDED = 0f
    const val SCALE_THRESHOLD = 0.2f
    const val MIN_SCALE = 0.3f

    val PEEK_HEIGHT = 56.dp
    val DEFAULT_HEIGHT = 250.dp
}


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomSheet(
    mainViewModel: MainViewModel,
    bottomSheetState: SheetState,
    bottomSheetOffset: () -> Float,
    onNavigateToChannelScreen: (String) -> Unit
) = trace("PlayerBottomSheet") {

    val coroutineScope = rememberCoroutineScope()
    var showPlaylistModal by remember { mutableStateOf(false) }
    var showQualityModal by remember { mutableStateOf(false) }
    var isControllerVisible by remember { mutableStateOf(false) }
    val mediaController by mainViewModel.mediaControllerFlow.collectAsState()

//    val bottomSheetAlpha = remember(bottomSheetOffset) {
//        if (bottomSheetOffset < 0) 1f else {
//            when {
//                bottomSheetOffset < 0.2f -> {
//                    val alphaValue = (0.2 - bottomSheetOffset) / 0.2
//                    alphaValue
//                }
//
//                else -> 0f
//            }
//        }
//    }

//    val mainBackgroundAlpha = remember(bottomSheetOffset) {
//        if (bottomSheetOffset < 0) 1f else {
//            bottomSheetOffset.pow(3).coerceAtLeast(0f)
//        }
//    }

    var playerViewHeight by remember { mutableIntStateOf(0) }

    if (showPlaylistModal) {
        trace("PlaylistModalBottomSheet") {
            PlaylistModalBottomSheetContainer(
                onDismiss = { showPlaylistModal = false },
                mainViewModel = mainViewModel,
                playerViewHeight = playerViewHeight
            )
        }
    }

    if (showQualityModal) {
        VideoQualityBottomSheet(
            mainViewModel = mainViewModel,
            onDismiss = { showQualityModal = false }
        )
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        trace("MainPlayerLayout") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GraphicsLayerConstants.DEFAULT_HEIGHT)
                    .graphicsLayer {
                        scaleY = calculateScaleFactorY(bottomSheetOffset())
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    }
                    .background(AppColors.BlueBackground)
                    .clickable {
                        coroutineScope.launch {
                            bottomSheetState.expand()
                        }
                    })
        }

        PlayerBottomSheetHeader(
            bottomSheetOffset = bottomSheetOffset,
            bottomSheetState = bottomSheetState,
            mainViewModel = mainViewModel
        )

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GraphicsLayerConstants.DEFAULT_HEIGHT)
                    .onGloballyPositioned { coordinates ->
                        playerViewHeight = coordinates.size.height
                    }
                    .graphicsLayer {
                        scaleX = when {
                            bottomSheetOffset() < 0f -> GraphicsLayerConstants.MIN_SCALE
                            bottomSheetOffset() < 0.2f -> calculateDefaultScaleX(bottomSheetOffset())
                            else -> 1f
                        }
                        scaleY = calculateScaleFactorY(bottomSheetOffset())
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                trace("PlayerView") {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                                keepScreenOn = true
                                setControllerVisibilityListener(
                                    PlayerView.ControllerVisibilityListener { visibility ->
                                        isControllerVisible = visibility == android.view.View.VISIBLE
                                    }
                                )

                                post {
                                    val settingButtons = findViewById<ImageButton>(
                                        androidx.media3.ui.R.id.exo_settings
                                    )
                                    settingButtons.setOnClickListener {
                                        showQualityModal = true
                                    }
                                }
                            }
                        }, update = { view ->
                            mediaController?.let { controller ->
                                view.player = controller
                            } ?: run {
                                view.player = null
                            }
                            view.useController = when (bottomSheetState.currentValue) {
                                SheetValue.Expanded -> true
                                else -> false
                            }
                        }, modifier = Modifier.fillMaxSize()
                    )
                }
                trace("PlayerThumbnailView") {
                    PlayerThumbnailView(
                        mainViewModel = mainViewModel,
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics { contentDescription = "PlayerThumbnailView" })
                }
                trace("PlayerLoadingIndicator") {
                    PlayerLoadingIndicator(
                        mainViewModel = mainViewModel, modifier = Modifier.align(Alignment.Center)
                    )
                }
                if (isControllerVisible && bottomSheetState.currentValue == SheetValue.Expanded) {
                    QualityIndicatorButton(
                        mainViewModel = mainViewModel,
                        onClick = { showQualityModal = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 12.dp, top = 12.dp)
                    )
                }
            }
            VideoDetailPanel(
                mainViewModel = mainViewModel,
                onNavigateToChannelScreen = onNavigateToChannelScreen,
                bottomSheetState = bottomSheetState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = -playerViewHeight * (1 - calculateScaleFactorY(
                            bottomSheetOffset()
                        ))
                        alpha = if (bottomSheetOffset() < 0) 1f else bottomSheetOffset().pow(3)
                            .coerceAtLeast(0f)
                    }
            )
        }
        PlaylistFloatingButton(
            mainViewModel = mainViewModel,
            bottomSheetState = bottomSheetState,
            onClick = { showPlaylistModal = true },
            bottomSheetOffset = bottomSheetOffset,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

    }
}


private fun Modifier.changeMainBackgroundAlpha(bottomSheetOffset: Float): Modifier {
    if (bottomSheetOffset < 0) return alpha(1f)
    return this.alpha((bottomSheetOffset.pow(3)).coerceAtLeast(0f))


}

private fun calculateDefaultScaleX(bottomSheetOffset: Float): Float {
    val t = (bottomSheetOffset / 0.2f).coerceIn(0f, 1f)
    val easedT = t * t * t
    return GraphicsLayerConstants.MIN_SCALE + (1f - GraphicsLayerConstants.MIN_SCALE) * easedT
}

private fun calculateScaleFactorY(bottomSheetOffset: Float): Float {
    val minScale = PEEK_HEIGHT.value / GraphicsLayerConstants.DEFAULT_HEIGHT.value
    return when {
        bottomSheetOffset <= GraphicsLayerConstants.FULLY_EXPANDED -> minScale
        else -> lerp(start = minScale, stop = 1f, fraction = bottomSheetOffset)
    }
}

@Composable
private fun QualityIndicatorButton(
    mainViewModel: MainViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQuality by mainViewModel.videoQuality.collectAsState()

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = Color.Black.copy(alpha = 0.6f)
    ) {
        Text(
            text = stringResource(currentQuality.getDisplayStringResId()),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}