package com.example.transpose.components.bottomsheet.item

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.example.transpose.R
import com.example.transpose.utils.TextFormatUtil
import com.example.transpose.utils.ToastUtil
import com.example.transpose.utils.constants.AppColors
import com.valentinilk.shimmer.shimmer


@Composable
fun ChannelSection(
    currentVideoData: BasicVideoData?,
    currentVideoDetailData: VideoDetailData?
) {

    if (currentVideoData == null) {
        ChannelSectionShimmer()
    } else {
        ChannelSectionContent(currentVideoData, currentVideoDetailData)

    }


}

@Composable
fun ChannelSectionContent(
    currentVideoData: BasicVideoData,
    currentVideoDetailData: VideoDetailData?
) {
    val subscriberCountFormats = rememberStringArrayResource(R.array.subscriber_count_formats)
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { ToastUtil.showNotImplemented(context) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = currentVideoData.uploaderAvatars,
            contentDescription = "Channel Avatar",
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .padding(10.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = currentVideoData.uploaderName ?: "",
            modifier = Modifier
                .widthIn(max = 150.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Black
        )
        Text(
            text = TextFormatUtil.subscriberCountConverter(
                currentVideoDetailData?.uploaderSubscriberCount.toString(),
                subscriberArray = subscriberCountFormats
            ),

            modifier = Modifier
                .padding(start = 10.dp, end = 20.dp),
            maxLines = 1,

            color = AppColors.DescriptionColor
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { /* 채워 넣어야함 */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color.Gray),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .padding(end = 15.dp)
                .width(60.dp)
                .height(30.dp)
        ) {
            Text(
                text = stringResource(id = R.string.add_button_text),
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ChannelSectionShimmer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .shimmer(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .padding(10.dp)
                .background(Color.LightGray)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .padding(end = 15.dp)
                .background(AppColors.GrayBackground)
        )
        Spacer(modifier = Modifier.weight(1f))
        // 버튼은 shimmer 상태에서 제거됨
    }
}



