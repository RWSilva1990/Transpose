package com.example.main.components.bottomsheet.item

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.domain.model.playable.PlayableItem
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.domain.model.library.MyPlaylist
import com.example.main.R
import com.example.main.components.bottomsheet.state.VideoDetailUiState
import com.example.ui.components.dialog.AddItemToPlaylistDialog
import com.example.util.TextFormatUtil
import com.example.util.ToastUtil
import com.example.util.constants.AppColors
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelSection(
    videoDetailUiState: VideoDetailUiState,
    currentItem: PlayableItem?,
    myPlaylists: List<MyPlaylist>,
    onAddItemToPlaylist: (PlayableItem, Long) -> Unit,
    bottomSheetState: SheetState,
    onNavigateToChannelScreen: (String) -> Unit
) {
    var isShowingPlaylistDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    when (currentItem) {
        is PlayableItem.Local -> {
            LocalFileSectionContent(
                localItem = currentItem,
                onAddButtonClicked = { isShowingPlaylistDialog = true }
            )
        }
        is PlayableItem.Remote -> {
            when (val state = videoDetailUiState) {
                is VideoDetailUiState.Loading -> {
                    ChannelSectionShimmer()
                }
                is VideoDetailUiState.Success -> {
                    ChannelSectionContent(
                        videoDetail = state.videoDetail!!,
                        onAddButtonClicked = { isShowingPlaylistDialog = true },
                        onNavigateToChannelScreen = onNavigateToChannelScreen,
                        bottomSheetState = bottomSheetState
                    )
                }
                is VideoDetailUiState.Error -> {}
            }
        }
        null -> {
            ChannelSectionShimmer()
        }
    }

    if (isShowingPlaylistDialog) {
        AddItemToPlaylistDialog(
            playlists = myPlaylists,
            onDismiss = { isShowingPlaylistDialog = false },
            onPlaylistSelected = { playlistId ->
                currentItem?.let {
                    onAddItemToPlaylist(it, playlistId)
                    ToastUtil.showShort(
                        context = context,
                        message = context.getString(R.string.notify_video_added_to_playlist)
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelSectionContent(
    videoDetail: VideoDetail,
    onAddButtonClicked: () -> Unit,
    bottomSheetState: SheetState,
    onNavigateToChannelScreen: (String) -> Unit
) {
    val subscriberCountFormats = rememberStringArrayResource(R.array.subscriber_count_formats)
    val coroutineScope = rememberCoroutineScope()
    val formattedSubscriberCount = remember(videoDetail.uploaderSubscriberCount) {
        TextFormatUtil.subscriberCountConverter(
            videoDetail.uploaderSubscriberCount.toString(),
            subscriberArray = subscriberCountFormats
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable {
                onNavigateToChannelScreen(videoDetail.uploaderId ?: "")
                coroutineScope.launch {
                    bottomSheetState.partialExpand()
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = videoDetail.uploaderAvatarUrl ?: "",
            contentDescription = "Channel Avatar",
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .padding(10.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = videoDetail.uploaderName ?: "",
            modifier = Modifier
                .widthIn(max = 150.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Black
        )
        Text(
            text = formattedSubscriberCount,
            modifier = Modifier
                .padding(start = 10.dp, end = 20.dp),
            maxLines = 1,
            color = AppColors.DescriptionColor
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onAddButtonClicked,
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
fun LocalFileSectionContent(
    localItem: PlayableItem.Local,
    onAddButtonClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(60.dp)
                .padding(10.dp)
                .background(AppColors.GrayBackground, RoundedCornerShape(4.dp))
        )
        Text(
            text = localItem.artist ?: "",
            modifier = Modifier
                .widthIn(max = 150.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Black
        )
        Text(
            text = localItem.album ?: "",
            modifier = Modifier
                .padding(start = 10.dp, end = 20.dp),
            maxLines = 1,
            color = AppColors.DescriptionColor
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onAddButtonClicked,
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
    }
}
