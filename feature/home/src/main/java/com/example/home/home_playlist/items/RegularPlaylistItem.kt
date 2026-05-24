package com.example.home.home_playlist.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.youtube.playlist.Playlist
import com.example.ui.components.image.ThumbnailImage
import com.example.util.constants.AppColors

@Composable
fun RegularPlaylistItem(playlistData: Playlist,
                        onClick: (String) -> Unit) {
    val itemId = playlistData.id
    Column(
        modifier = Modifier
            .width(150.dp)
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick(itemId) }
    ) {
        ThumbnailImage(
            url = playlistData.thumbnailUrl,
            contentDescription = "Playlist Thumbnail",
            width = 150.dp,
            height = 84.dp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
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
