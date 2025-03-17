package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.main.MainViewModel
import com.example.transpose.core.ui.R

@Composable
fun PlaylistFloatingButton(
    playlistSize: Int,
    onClick: () -> Unit,
    normalizedOffset: Float,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {

    val currentPlaylistInfo by mainViewModel.currentPlaylistInfo.collectAsState()
    Surface(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .alpha(if (normalizedOffset >= 0.95f) 1f else 0f),
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
                text = currentPlaylistInfo?.title ?: stringResource(id = R.string.main_playlist_title),
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Text(
                text = "$playlistSize",
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