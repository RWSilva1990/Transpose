package com.example.home.home_playlist.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.youtube.playlist.PlaylistData
import com.example.util.constants.AppColors

@Composable
fun RegularPlaylistItem(playlistData: PlaylistData,
                        onClick: (String) -> Unit) {
    val itemId = playlistData.id
    Column(
        modifier = Modifier
            .width(150.dp)
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(itemId) }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(playlistData.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Playlist Thumbnail",
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color.LightGray)
        )
        Text(
            text = playlistData.title,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )


        Text(
            text = playlistData.uploaderName,
            color = AppColors.DescriptionColor,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}