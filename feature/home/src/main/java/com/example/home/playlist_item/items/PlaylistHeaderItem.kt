package com.example.home.playlist_item.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.model.youtube.playlist.Playlist
import com.example.transpose.core.ui.R
@Composable
fun PlaylistHeaderItem(playlistData: Playlist?) {

    val context = LocalContext.current
    playlistData?.let { playlist ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 플레이리스트 썸네일
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = playlist.thumbnailUrl,
                    contentDescription = "플레이리스트 썸네일",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 플레이리스트 타이틀
            Text(
                text = playlist.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 게시자 정보
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {

                // 게시자 이름
                Text(
                    text = playlist.uploaderName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 동영상 개수
//            Text(
//                text = context.getString(R.string., playlist.streamCount),
//                style = MaterialTheme.typography.bodyMedium,
//                color = Color.Gray
//            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}