package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.playable.PlayableItem
import com.example.main.R
import com.example.util.TextFormatUtil

@Composable
fun PlaylistBottomSheetItem(
    item: PlayableItem,
    onClick: () -> Unit,
    isCurrentlyPlaying: Boolean,
) {
    val myStringArray = stringArrayResource(id = R.array.view_count_formats)

    val subtitle = when (item) {
        is PlayableItem.Remote -> item.video.uploaderName ?: ""
        is PlayableItem.Local -> item.artist ?: stringResource(R.string.unknown_artist)
    }

    val detailText = remember(item) {
        when (item) {
            is PlayableItem.Remote -> {
                val viewCount = TextFormatUtil.viewCountCalculator(
                    myStringArray,
                    item.video.viewCount.toString()
                )
                "$viewCount • ${item.video.textualUploadDate}"
            }
            is PlayableItem.Local -> item.album ?: ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() }
            .background(if (isCurrentlyPlaying) Color(0xFFE1E9F5) else Color.Transparent)
            .padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        AsyncImage(
            model = item.thumbnailUri,
            contentDescription = "Thumbnail",
            modifier = Modifier
                .width(150.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            placeholder = ColorPainter(Color.LightGray),
            error = ColorPainter(Color.LightGray)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp)
        ) {
            Text(
                text = item.title,
                fontSize = 12.sp,
                minLines = 2,
                maxLines = 2,
                lineHeight = 14.sp,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detailText,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
}