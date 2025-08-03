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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.main.MainViewModel
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import com.example.main.components.bottomsheet.state.VideoDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoQualityBottomSheet(
    mainViewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val videoDetailUiState by mainViewModel.videoDetailUiState.collectAsState()
    val currentQuality by mainViewModel.videoQuality.collectAsState()

    val itagResolutionMap = mapOf(
        137 to "1080p", 248 to "1080p",
        136 to "720p", 247 to "720p",
        135 to "480p", 244 to "480p",
        134 to "360p", 243 to "360p",
        133 to "240p", 242 to "240p",
        160 to "144p", 278 to "144p"
    )

    when (val state = videoDetailUiState) {
        is VideoDetailUiState.Success -> {
            val itagList = state.videoDetail?.videoOnlyStreams?.map { it.itag }
            val resolutions = itagList?.mapNotNull { itagResolutionMap[it] }?.distinct()

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
                            text = "화질",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )

                    QualityItem(
                        qualityLabel = "자동",
                        isSelected = currentQuality == "AUTO",
                        onClick = {
                            mainViewModel.setVideoQuality("AUTO")
                            onDismiss()
                        }
                    )

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )

                    resolutions?.forEach { quality ->
                        QualityItem(
                            qualityLabel = quality,  // "1080p", "720p" 등
                            isSelected = currentQuality == quality,
                            onClick = {
                                mainViewModel.setVideoQuality(quality)
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
                    contentDescription = "선택됨",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}