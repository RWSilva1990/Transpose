package com.example.main.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.example.domain.model.preferences.VideoQuality
import com.example.main.components.bottomsheet.state.VideoDetailUiState
import com.example.transpose.core.ui.R
import com.example.ui.util.getDisplayStringResId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoQualityBottomSheet(
    videoDetailUiState: VideoDetailUiState,
    currentQuality: VideoQuality,
    onSetVideoQuality: (VideoQuality) -> Unit,
    onDismiss: () -> Unit
) {
    // itag -> VideoQuality mapping
    val itagToQualityMap = mapOf(
        137 to VideoQuality.P1080, 248 to VideoQuality.P1080,
        136 to VideoQuality.P720, 247 to VideoQuality.P720,
        135 to VideoQuality.P480, 244 to VideoQuality.P480,
        134 to VideoQuality.P360, 243 to VideoQuality.P360,
        133 to VideoQuality.P240, 242 to VideoQuality.P240,
        160 to VideoQuality.P144, 278 to VideoQuality.P144
    )

    when (val state = videoDetailUiState) {
        is VideoDetailUiState.Success -> {
            val itagList = state.videoDetail?.videoOnlyStreams?.map { it.itag }
            val availableQualities = itagList
                ?.mapNotNull { itagToQualityMap[it] }
                ?.distinct()
                ?: emptyList()

            ModalBottomSheet(
                onDismissRequest = onDismiss,
                containerColor = MaterialTheme.colorScheme.surface,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.video_quality_title),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )

                    QualityItem(
                        qualityLabel = stringResource(R.string.video_quality_auto),
                        isSelected = currentQuality.isAuto,
                        onClick = {
                            onSetVideoQuality(VideoQuality.AUTO)
                            onDismiss()
                        }
                    )

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )

                    availableQualities.forEach { quality ->
                        QualityItem(
                            qualityLabel = stringResource(quality.getDisplayStringResId()),
                            isSelected = currentQuality == quality,
                            onClick = {
                                onSetVideoQuality(quality)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
        else -> {
            LaunchedEffect(Unit) {
                onDismiss()
            }
        }
    }
}

@Composable
fun QualityItem(
    qualityLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = qualityLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.video_quality_selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
