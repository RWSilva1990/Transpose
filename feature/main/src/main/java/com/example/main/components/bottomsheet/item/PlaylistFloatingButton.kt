package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.playable.PlayableItem
import com.example.main.R
import com.example.ui.theme.blendColors
import com.example.util.constants.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistFloatingButton(
    currentPlaylist: List<PlayableItem>,
    playlistTitle: String?,
    bottomSheetState: SheetState,
    onClick: () -> Unit,
    bottomSheetOffset: () -> Float,
    modifier: Modifier = Modifier
) {
    if (currentPlaylist.isNotEmpty() && bottomSheetState.currentValue == SheetValue.Expanded) {
        val offset = bottomSheetOffset().coerceIn(0f, 1f)
        val backgroundColor = blendColors(AppColors.StatusBarBackground, AppColors.CharcoalGray, offset)

        Surface(
            modifier = modifier
                .graphicsLayer {
                    alpha = if (bottomSheetOffset() >= 0.95f) 1f else 0f
                }
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Playlist",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = playlistTitle
                        ?: stringResource(id = R.string.main_playlist_title),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.White
                )

                Text(
                    text = "${currentPlaylist.size}",
                    modifier = Modifier.padding(end = 4.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Show",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
