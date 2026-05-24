package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.playable.PlayableItem
import com.example.main.R
import com.example.util.TextFormatUtil
import com.valentinilk.shimmer.shimmer

@Composable
fun VideoInfoSection(currentItem: PlayableItem?) {
    val viewCountFormats = rememberStringArrayResource(R.array.view_count_formats)

    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
    ) {
        if (currentItem == null) {
            FullShimmerEffect()
        } else {
            VideoInfoContent(
                item = currentItem,
                viewCountFormats = viewCountFormats
            )
        }
    }
}

@Composable
private fun VideoInfoContent(
    item: PlayableItem,
    viewCountFormats: Array<String>
) {
    val formattedViewInfo = remember(item, viewCountFormats) {
        when (item) {
            is PlayableItem.Remote -> {
                TextFormatUtil.formatVideoMeta(
                    viewCountStringArray = viewCountFormats,
                    viewCount = item.video.viewCount,
                    textualUploadDate = item.video.textualUploadDate,
                    publishTimestamp = item.video.publishTimestamp
                )
            }
            is PlayableItem.Local -> {
                item.artist ?: ""
            }
        }
    }

    Text(
        text = item.title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )

    Text(
        text = formattedViewInfo,
        modifier = Modifier.padding(top = 5.dp, start = 10.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun FullShimmerEffect() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shimmer()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(start = 10.dp, end = 10.dp)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(16.dp)
                .padding(start = 10.dp)
                .background(Color.LightGray)
        )
    }
}

@Composable
fun PartialShimmerEffect() {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .height(16.dp)
            .padding(start = 10.dp, top = 5.dp)
            .shimmer()
            .background(Color.LightGray)
    )
}

@Composable
fun rememberStringArrayResource(resourceId: Int): Array<String> {
    val context = LocalContext.current
    return remember(resourceId) {
        context.resources.getStringArray(resourceId)
    }
}
