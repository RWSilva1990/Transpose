package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.playable.PlayableItem
import com.example.main.R
import com.example.main.components.bottomsheet.state.VideoDetailUiState
import com.example.util.ToastUtil
import com.example.util.constants.AppColors
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoInfoCardItem(
    currentItem: PlayableItem?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 5.dp)
    ) {
        VideoInfoSection(currentItem = currentItem)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelCardItem(
    currentItem: PlayableItem?,
    videoDetailUiState: VideoDetailUiState,
    myPlaylists: List<MyPlaylist>,
    onAddItemToPlaylist: (PlayableItem, Long) -> Unit,
    onNavigateToChannelScreen: (String) -> Unit,
    bottomSheetState: SheetState
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        ChannelSection(
            videoDetailUiState = videoDetailUiState,
            currentItem = currentItem,
            myPlaylists = myPlaylists,
            onAddItemToPlaylist = onAddItemToPlaylist,
            onNavigateToChannelScreen = onNavigateToChannelScreen,
            bottomSheetState = bottomSheetState
        )
    }
}

@Composable
fun AudioAdjustCardItem(
    pitchValue: Int,
    tempoValue: Int,
    onPitchPlusOne: () -> Unit,
    onPitchMinusOne: () -> Unit,
    onPitchInit: () -> Unit,
    onTempoPlusOne: () -> Unit,
    onTempoMinusOne: () -> Unit,
    onTempoInit: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 5.dp, bottom = 8.dp)
    ) {
        AudioAdjustCard(
            pitchValue = pitchValue,
            tempoValue = tempoValue,
            onPitchPlusOne = onPitchPlusOne,
            onPitchMinusOne = onPitchMinusOne,
            onPitchInit = onPitchInit,
            onTempoPlusOne = onTempoPlusOne,
            onTempoMinusOne = onTempoMinusOne,
            onTempoInit = onTempoInit
        )
    }
}

@Composable
private fun AudioAdjustCard(
    pitchValue: Int,
    tempoValue: Int,
    onPitchPlusOne: () -> Unit,
    onPitchMinusOne: () -> Unit,
    onPitchInit: () -> Unit,
    onTempoPlusOne: () -> Unit,
    onTempoMinusOne: () -> Unit,
    onTempoInit: () -> Unit,
) {
    val context = LocalContext.current
    val pitchActualValue = remember(pitchValue) { (pitchValue * 0.1) - 10.0 }
    val tempoActualValue = remember(tempoValue) { (tempoValue * 0.1) - 10.0 }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .width(4.dp)
                        .height(22.dp)
                        .background(AppColors.BlueBackground, RoundedCornerShape(4.dp))
                )
                Text(
                    text = stringResource(R.string.audio_adjust_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.audio_adjust_reset),
                    color = AppColors.BlueBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        onPitchInit()
                        onTempoInit()
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CompactAdjustControl(
                    title = stringResource(R.string.pitch_text),
                    onMinus = {
                        onPitchMinusOne()
                        ToastUtil.showShort(
                            context,
                            String.format(context.getString(R.string.pitch_minus_text), pitchActualValue - 1)
                        )
                    },
                    onReset = {
                        onPitchInit()
                        ToastUtil.showShort(
                            context,
                            context.getString(R.string.pitch_initialize_text, 0.0)
                        )
                    },
                    onPlus = {
                        onPitchPlusOne()
                        ToastUtil.showShort(
                            context,
                            context.getString(R.string.pitch_plus_text, pitchActualValue + 1)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                CompactAdjustControl(
                    title = stringResource(R.string.tempo_text),
                    onMinus = {
                        onTempoMinusOne()
                        val nextRate = 2.0.pow((tempoActualValue - 1.0) / 12.0)
                        ToastUtil.showShort(
                            context,
                            context.getString(R.string.tempo_minus_text, nextRate)
                        )
                    },
                    onReset = {
                        onTempoInit()
                        ToastUtil.showShort(
                            context,
                            context.getString(R.string.tempo_init_text, 1.0)
                        )
                    },
                    onPlus = {
                        onTempoPlusOne()
                        val nextRate = 2.0.pow((tempoActualValue + 1.0) / 12.0)
                        ToastUtil.showShort(
                            context,
                            context.getString(R.string.tempo_plus_text, nextRate)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactAdjustControl(
    title: String,
    onMinus: () -> Unit,
    onReset: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            CompactControlButton(
                iconRes = R.drawable.baseline_exposure_neg_1_24,
                contentDescription = "Decrease $title",
                onClick = onMinus,
                modifier = Modifier.weight(1f)
            )
            CompactControlButton(
                iconRes = R.drawable.baseline_replay_24,
                contentDescription = "Reset $title",
                onClick = onReset,
                modifier = Modifier.weight(1f)
            )
            CompactControlButton(
                iconRes = R.drawable.baseline_exposure_plus_1_24,
                contentDescription = "Increase $title",
                onClick = onPlus,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CompactControlButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF2F5FA))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = AppColors.BlueBackground
        )
    }
}
