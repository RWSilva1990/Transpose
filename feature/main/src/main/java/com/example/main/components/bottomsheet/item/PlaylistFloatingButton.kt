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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.main.MainViewModel
import com.example.main.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistFloatingButton(
    mainViewModel: MainViewModel,
    bottomSheetState: SheetState,
    onClick: () -> Unit,
    bottomSheetOffset: () -> Float,
    modifier: Modifier = Modifier
) {
    val currentPlaylist by mainViewModel.currentPlaylist.collectAsState()
    val currentPlaylistInfo by mainViewModel.currentPlaylistInfo.collectAsState()

    if (currentPlaylist.isNotEmpty() && bottomSheetState.currentValue == SheetValue.Expanded) {
        Surface(
            modifier = modifier
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .graphicsLayer {
                    alpha = if (bottomSheetOffset() >= 0.95f) 1f else 0f
                },
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Playlist",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = currentPlaylistInfo?.title
                        ?: stringResource(id = R.string.main_playlist_title),
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )

                Text(
                    text = "${currentPlaylist.size}",
                    modifier = Modifier.padding(end = 4.dp),
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Show",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

}