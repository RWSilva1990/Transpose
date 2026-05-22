package com.example.ui.screen.channel.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.youtube.channel.ChannelTabResult
import com.example.ui.components.image.ThumbnailImage

@Composable
fun PlaylistItem(
    playlist: ChannelTabResult.PlaylistResult,
    onClick: () -> Unit
) {
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
                url = playlist.playlist.thumbnailUrl,
                contentDescription = "플레이리스트 썸네일",
                width = 330.dp,
                height = 186.dp,
                modifier = Modifier,
                fillMaxSize = true,
            )

            // 플레이리스트 정보 오버레이
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxHeight(0.3f)
                    .width(80.dp)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "재생목록",
                        tint = Color.White
                    )
                    Text(
                        text = "${playlist.playlist.streamCount}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 플레이리스트 정보
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.playlist.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${playlist.playlist.uploaderName} • ${playlist.playlist.streamCount}개의 동영상",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

//            IconButton(
//                onClick = {  },
//                modifier = Modifier.align(Alignment.Top)
//            ) {
//                Icon(
//                    Icons.Default.MoreVert,
//                    contentDescription = "더보기",
//                    tint = Color.Gray
//                )
//            }
        }
    }
}
