package com.example.library.my_playlist_item.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.playable.PlayableItem
import com.example.library.R
import com.example.transpose.core.ui.R as CoreUiR
import com.example.ui.components.dropdown_menu.DropDownMenu
import com.example.ui.components.image.ThumbnailImage
import com.example.util.TextFormatUtil

@Composable
fun PlaylistItem(
    item: PlayableItem,
    onClick: (PlayableItem) -> Unit,
    dropDownMenuClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val viewCountFormats = stringArrayResource(id = CoreUiR.array.view_count_formats)

    val title = item.title
    val subtitle = when (item) {
        is PlayableItem.Remote -> item.video.uploaderName ?: ""
        is PlayableItem.Local -> item.artist ?: stringResource(R.string.unknown_artist)
    }
    val detailText = remember(item, viewCountFormats) {
        when (item) {
            is PlayableItem.Remote -> {
                val viewCount = TextFormatUtil.viewCountCalculator(
                    viewCountFormats,
                    item.video.viewCount.toString()
                )
                val uploadDate = item.video.textualUploadDate.orEmpty()
                if (uploadDate.isBlank()) viewCount else "$viewCount • $uploadDate"
            }

            is PlayableItem.Local -> item.album ?: ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick(item) }
            .padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        ThumbnailImage(
            url = item.thumbnailUri,
            contentDescription = "Thumbnail",
            width = 142.dp,
            height = 80.dp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 20.dp)
        ) {
            Text(
                text = title,
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box {
            IconButton(onClick = { isExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }
            DropDownMenu(
                text = stringResource(R.string.delete),
                isExpanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                onClick = { dropDownMenuClick() }
            )
        }
    }
    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
}
