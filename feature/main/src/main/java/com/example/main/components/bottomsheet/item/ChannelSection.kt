package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
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

    Surface(
        onClick = {
            onNavigateToChannelScreen(videoDetail.uploaderId ?: "")
            coroutineScope.launch {
                bottomSheetState.partialExpand()
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = videoDetail.uploaderAvatarUrl ?: "",
                contentDescription = "Channel Avatar",
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
            ColumnTextBlock(
                title = videoDetail.uploaderName ?: "",
                subtitle = formattedSubscriberCount,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onAddButtonClicked,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.BlueBackground),
                elevation = ButtonDefaults.buttonElevation(0.dp),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.add_button_text),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
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
            .padding(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                        .background(AppColors.GrayBackground, RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                ColumnTextBlock(
                    title = localItem.title,
                    subtitle = localItem.artist ?: localItem.album ?: "",
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onAddButtonClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.BlueBackground),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.add_button_text),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnTextBlock(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = AppColors.DescriptionColor
        )
    }
}

@Composable
fun ChannelSectionShimmer() {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .shimmer(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.56f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(7.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.38f)
                        .height(13.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .width(68.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.LightGray)
            )
        }
    }
}
