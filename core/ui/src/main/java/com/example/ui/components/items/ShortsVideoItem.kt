package com.example.ui.components.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.youtube.channel.ChannelTabResult
import com.example.domain.model.youtube.video.Video
import com.example.transpose.core.ui.R
import com.example.ui.components.image.ThumbnailImage
import com.example.util.TextFormatUtil

@Composable
fun ShortsVideoItem(
    video: Video,
    onClick: () -> Unit
) {
    val viewCountFormats = stringArrayResource(R.array.view_count_formats)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(bottom = 16.dp)
    ) {
        // 썸네일
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            ThumbnailImage(
                url = video.thumbnailUrl,
                contentDescription = "비디오 썸네일",
                width = 330.dp,
                height = 186.dp,
                modifier = Modifier,
            )

            // 영상 길이
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(8.dp)
//                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
//                    .padding(horizontal = 4.dp, vertical = 2.dp)
//            ) {
//                Text(
//                    text = "0초",
//                    color = Color.White,
//                    fontSize = 12.sp
//                )
//            }
        }

        // 비디오 정보
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = TextFormatUtil.viewCountCalculator(
                        viewCountFormats,
                        video.viewCount.toString()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { /* 더보기 메뉴 */ },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "더보기",
                    tint = Color.Gray
                )
            }
        }
    }
}