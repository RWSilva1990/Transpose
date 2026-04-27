package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.domain.model.playable.PlayableItem
import com.example.main.components.bottomsheet.state.VideoDetailUiState
import com.example.util.constants.AppColors

@Composable
fun PlayerThumbnailView(
    videoDetailUiState: VideoDetailUiState,
    currentItem: PlayableItem?,
    modifier: Modifier = Modifier
) {
    if (currentItem is PlayableItem.Local) return

    when (val state = videoDetailUiState) {
        is VideoDetailUiState.Loading -> {
            Box(modifier = modifier.background(AppColors.LightGray)) {
                AsyncImage(
                    model = (currentItem as? PlayableItem.Remote)?.video?.thumbnailUrl,
                    contentDescription = "Video Thumbnail",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                    placeholder = ColorPainter(AppColors.LightGray),
                    error = ColorPainter(AppColors.LightGray)
                )
            }
        }
        else -> {}
    }
}
