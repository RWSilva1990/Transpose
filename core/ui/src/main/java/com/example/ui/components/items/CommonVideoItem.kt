package com.example.ui.components.items

import android.os.Trace
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.youtube.video.Video
import com.example.transpose.core.ui.R
import com.example.ui.components.dropdown_menu.DropDownMenu
import com.example.ui.components.image.ThumbnailImage
import com.example.util.TextFormatUtil

@Composable
fun CommonVideoItem(
    item: Video,
    onClick: (Video) -> Unit,
    dropDownMenuClick: () -> Unit,

    ) {
    var isExpanded by remember {
        mutableStateOf(false)
    }
    val myStringArray = stringArrayResource(id = R.array.view_count_formats)

    if (item.id != ""){
        Row(
            modifier = Modifier
                .semantics { contentDescription = "CommonVideoItem" }
                .fillMaxWidth()
                .height(100.dp)
                .clickable {
                    Trace.beginSection("CommonVideoItem_Click")
                    onClick(item)
                    Trace.endSection()}
                .padding(vertical = 10.dp, horizontal = 10.dp)
        ) {
            ThumbnailImage(
                url = item.thumbnailUrl,
                contentDescription = "Thumbnail",
                width = 150.dp,
                height = 80.dp,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)),
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
                    text = item.uploaderName ?: "",
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${TextFormatUtil.viewCountCalculator(myStringArray, item.viewCount.toString())} • ${item.textualUploadDate}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Box(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                IconButton(
                    onClick = { isExpanded = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }
                DropDownMenu(text = stringResource(id = R.string.video_pop_up_menu_add_playlist_text), isExpanded = isExpanded, onDismissRequest = { isExpanded = false }, onClick = {dropDownMenuClick()})
            }

        }
        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
    }


}