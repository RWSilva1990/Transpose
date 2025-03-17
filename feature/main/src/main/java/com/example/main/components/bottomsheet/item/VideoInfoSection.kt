package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.domain.model.youtube.video.Video
import com.example.transpose.core.ui.R
import com.example.util.TextFormatUtil
import com.valentinilk.shimmer.shimmer

@Composable
fun VideoInfoSection(currentVideoData: Video?) {
    val viewCountFormats = rememberStringArrayResource(R.array.view_count_formats)
    val formattedText = remember(currentVideoData?.viewCount, currentVideoData?.textualUploadDate) {
        if (currentVideoData == null) "" else {
            "${
                TextFormatUtil.viewCountCalculator(
                    viewCountStringArray = viewCountFormats,
                    viewCountString = currentVideoData.viewCount.toString()
                )
            } • ${currentVideoData.textualUploadDate}"
        }
    }
    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
    ) {
        if (currentVideoData == null) {
            FullShimmerEffect()
        } else {
            Text(
                text = currentVideoData.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formattedText,
                modifier = Modifier.padding(top = 5.dp, start = 10.dp)
            )
        }
    }
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