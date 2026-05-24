package com.example.home.home_playlist.items

import android.os.Trace
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.youtube.playlist.Playlist
import com.example.ui.components.image.ThumbnailImage
import com.example.util.constants.AppColors


@Composable
fun NationalPlaylistItem(
    playlistData: Playlist,
    onClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .semantics { contentDescription = "NationalPlaylistItem" }
            .width(330.dp)
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                Trace.beginSection("NationalPlaylistItem_Click")
                onClick(playlistData.id)
                Trace.endSection()}
    ) {
        ThumbnailImage(
            url = playlistData.thumbnailUrl,
            contentDescription = "Nation Icon",
            width = 330.dp,
            height = 186.dp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = playlistData.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )


        Text(
            text = playlistData.description,
            fontSize = 10.sp,
            color = AppColors.DescriptionColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )


        Text(
            text = playlistData.uploaderName,
            fontSize = 8.sp,
            color = AppColors.DescriptionColor,
        )

    }
}
