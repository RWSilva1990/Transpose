package com.example.main.components.bottomsheet

import android.widget.ImageButton
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.util.trace
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.main.MainViewModel
import com.example.main.components.bottomsheet.GraphicsLayerConstants.PEEK_HEIGHT
import com.example.main.components.bottomsheet.item.PlayerBottomSheetHeader
import com.example.main.components.bottomsheet.item.PlayerLoadingIndicator
import com.example.main.components.bottomsheet.item.PlayerThumbnailView
import com.example.main.components.bottomsheet.item.PlaylistFloatingButton
import com.example.main.components.bottomsheet.item.VideoDetailPanel
import com.example.ui.util.getDisplayStringResId
import com.example.util.constants.AppColors
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt


object GraphicsLayerConstants {
    const val FULLY_EXPANDED = 0f
    const val SCALE_THRESHOLD = 0.2f
    const val MIN_SCALE = 0.3f

    val PEEK_HEIGHT = 56.dp
    const val PLAYER_ASPECT_RATIO = 16f / 9f
}


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomSheet(
    mainViewModel: MainViewModel,
    bottomSheetState: SheetState,
    bottomSheetOffset: () -> Float,
    onNavigateToChannelScreen: (String) -> Unit,
    onVideoDetailTouchActiveChanged: (Boolean) -> Unit,
) = trace("PlayerBottomSheet") {

    // ===== Collect all states here =====
    val mediaController by mainViewModel.mediaControllerFlow.collectAsStateWithLifecycle()
    val isPlaying by mainViewModel.isPlaying.collectAsStateWithLifecycle()
    val currentItem by mainViewModel.currentItem.collectAsStateWithLifecycle()

    if (currentItem == null && bottomSheetState.currentValue == SheetValue.Hidden) {
        return@trace
    }
    val videoDetailUiState by mainViewModel.videoDetailUiState.collectAsStateWithLifecycle()
    val pitchValue by mainViewModel.pitchValue.collectAsStateWithLifecycle()
    val tempoValue by mainViewModel.tempoValue.collectAsStateWithLifecycle()
    val myPlaylists by mainViewModel.myPlaylists.collectAsStateWithLifecycle()
    val currentPlaylist by mainViewModel.currentPlaylist.collectAsStateWithLifecycle()
    val currentPlaylistInfo by mainViewModel.currentPlaylistInfo.collectAsStateWithLifecycle()
    val videoQuality by mainViewModel.videoQuality.collectAsStateWithLifecycle()
    val appliedVideoQuality by mainViewModel.appliedVideoQuality.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    var showPlaylistModal by remember { mutableStateOf(false) }
    var showQualityModal by remember { mutableStateOf(false) }
    var isControllerVisible by remember { mutableStateOf(false) }
    val playerHeight = LocalConfiguration.current.screenWidthDp.dp / GraphicsLayerConstants.PLAYER_ASPECT_RATIO
    val onVideoDetailTouchActiveChangedState by rememberUpdatedState(onVideoDetailTouchActiveChanged)

    val isSheetExpanded by remember(bottomSheetState) {
        derivedStateOf { bottomSheetState.currentValue == SheetValue.Expanded }
    }

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
            videoDetailUiState = videoDetailUiState,
            currentQuality = videoQuality,
            onSetVideoQuality = mainViewModel::setVideoQuality,
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
                    .height(playerHeight)
                    .graphicsLayer {
                        scaleY = calculateScaleFactorY(bottomSheetOffset(), playerHeight)
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
            isPlaying = isPlaying,
            displayTitle = currentItem?.title ?: "",
            onPlayPause = mainViewModel::playPause,
            onStop = mainViewModel::stopPlayback
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(playerHeight)
                .onGloballyPositioned { coordinates ->
                    playerViewHeight = coordinates.size.height
                }
                .graphicsLayer {
                    scaleX = when {
                        bottomSheetOffset() < 0f -> GraphicsLayerConstants.MIN_SCALE
                        bottomSheetOffset() < 0.2f -> calculateDefaultScaleX(bottomSheetOffset())
                        else -> 1f
                    }
                    scaleY = calculateScaleFactorY(bottomSheetOffset(), playerHeight)
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) {
            trace("PlayerView") {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            setBackgroundColor(android.graphics.Color.BLACK)
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            setShutterBackgroundColor(android.graphics.Color.BLACK)
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
                        val controller = mediaController
                        if (view.player != controller) {
                            view.player = controller
                        }
                        if (view.useController != isSheetExpanded) {
                            view.useController = isSheetExpanded
                        }
                    }, modifier = Modifier.fillMaxSize()
                )
            }
            trace("PlayerThumbnailView") {
                PlayerThumbnailView(
                    videoDetailUiState = videoDetailUiState,
                    currentItem = currentItem,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = "PlayerThumbnailView" })
            }
            trace("PlayerLoadingIndicator") {
                PlayerLoadingIndicator(
                    videoDetailUiState = videoDetailUiState,
                    currentItem = currentItem,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            if (isControllerVisible && isSheetExpanded) {
                QualityIndicatorButton(
                    videoQuality = appliedVideoQuality,
                    onClick = { showQualityModal = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 12.dp, top = 12.dp)
                )
            }
        }
        VideoDetailPanel(
            videoDetailUiState = videoDetailUiState,
            currentItem = currentItem,
            myPlaylists = myPlaylists,
            pitchValue = pitchValue,
            tempoValue = tempoValue,
            onPlayVideo = mainViewModel::playVideo,
            onPitchPlusOne = mainViewModel::pitchPlusOne,
            onPitchMinusOne = mainViewModel::pitchMinusOne,
            onPitchInit = mainViewModel::initPitchValue,
            onTempoPlusOne = mainViewModel::tempoPlusOne,
            onTempoMinusOne = mainViewModel::tempoMinusOne,
            onTempoInit = mainViewModel::initTempoValue,
            onAddItemToPlaylist = mainViewModel::addItemToPlaylist,
            onNavigateToChannelScreen = onNavigateToChannelScreen,
            bottomSheetState = bottomSheetState,
            reservePlaylistButtonSpace = currentPlaylist.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val visiblePlayerHeight = (playerViewHeight * calculateScaleFactorY(
                        bottomSheetOffset(),
                        playerHeight
                    )).roundToInt()
                    val detailTop = visiblePlayerHeight.coerceIn(0, constraints.maxHeight)
                    val detailHeight = (constraints.maxHeight - detailTop).coerceAtLeast(0)
                    val placeable = measurable.measure(
                        constraints.copy(
                            minHeight = 0,
                            maxHeight = detailHeight
                        )
                    )
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.placeRelative(0, detailTop)
                    }
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(
                            requireUnconsumed = false,
                            pass = PointerEventPass.Initial
                        )
                        onVideoDetailTouchActiveChangedState(true)
                        try {
                            do {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                            } while (event.changes.any { it.pressed })
                        } finally {
                            onVideoDetailTouchActiveChangedState(false)
                        }
                    }
                }
                .graphicsLayer {
                    alpha = if (bottomSheetOffset() < 0) 1f else bottomSheetOffset().pow(3)
                        .coerceAtLeast(0f)
                }
        )
        PlaylistFloatingButton(
            currentPlaylist = currentPlaylist,
            playlistTitle = currentPlaylistInfo?.title,
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
    if (bottomSheetOffset < 0) return this.graphicsLayer { alpha = 1f }
    return this.graphicsLayer { alpha = (bottomSheetOffset.pow(3)).coerceAtLeast(0f) }
}

private fun calculateDefaultScaleX(bottomSheetOffset: Float): Float {
    val t = (bottomSheetOffset / 0.2f).coerceIn(0f, 1f)
    val easedT = t * t * t
    return GraphicsLayerConstants.MIN_SCALE + (1f - GraphicsLayerConstants.MIN_SCALE) * easedT
}

private fun calculateScaleFactorY(bottomSheetOffset: Float, playerHeight: androidx.compose.ui.unit.Dp): Float {
    val minScale = PEEK_HEIGHT.value / playerHeight.value
    return when {
        bottomSheetOffset <= GraphicsLayerConstants.FULLY_EXPANDED -> minScale
        else -> lerp(start = minScale, stop = 1f, fraction = bottomSheetOffset)
    }
}

@Composable
private fun QualityIndicatorButton(
    videoQuality: com.example.domain.model.preferences.VideoQuality,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(4.dp),
        color = Color.Black.copy(alpha = 0.6f)
    ) {
        Text(
            text = stringResource(videoQuality.getDisplayStringResId()),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
