package com.example.ui.screen.channel.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.youtube.channel.ChannelDetail
import com.example.transpose.core.ui.R
import com.example.util.TextFormatUtil

@Composable
fun ChannelHeader(
    channel: ChannelDetail,
    isScrolled: Boolean,
    selectedTabIndex: Int,
    tabTitles: Array<String>,
    onTabSelected: (Int) -> Unit
) {
    Column {
        Box(modifier = Modifier.height(160.dp)) {
            AsyncImage(
                model = channel.bannerUrl,
                contentDescription = "채널 배너",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 채널 아이콘
            AsyncImage(
                model = channel.avatarUrl,
                contentDescription = "채널 아이콘",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // 채널 정보 텍스트
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (channel.verified) {
                        // 인증 마크 아이콘 추가
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.Gray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }

                Text(
                    text = "@${channel.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                val subscriberCountFormats = stringArrayResource(R.array.subscriber_count_formats)
                Text(
                    text = TextFormatUtil.subscriberCountConverter(
                        channel.subscriberCount.toString(),
                        subscriberCountFormats
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

//            // 구독 버튼
//            Button(
//                onClick = { /* 구독 기능 구현 */ }
//            ) {
//                Text("구독")
//            }
        }

        // 채널 설명
        if (channel.description.isNotEmpty()) {
            Text(
                text = channel.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )


//            Text(
//                text = "더보기",
//                style = MaterialTheme.typography.bodyMedium,
//                color = Color.Gray,
//                modifier = Modifier
//                    .padding(horizontal = 16.dp, vertical = 4.dp)
//                    .clickable { /* 더보기 클릭 처리 */ }
//            )
        }

        if (!isScrolled) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (tabPositions.isNotEmpty() && selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.onSurface,
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            onTabSelected(index)
                        },
                        text = {
                            Text(
                                text = tab,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // 탭 아래 구분선
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray)
        )
    }
}
