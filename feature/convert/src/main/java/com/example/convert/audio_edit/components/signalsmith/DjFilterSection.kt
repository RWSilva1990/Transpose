package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.ExpandableSectionTitle
import com.example.transpose.core.ui.R
import java.util.Locale
import kotlin.math.abs

@Composable
fun DjFilterSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isDjFilterEnabled.collectAsState()
    val position by convertAudioEditViewModel.djFilterPosition.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    // Get current locale for Korean/English
    val isKorean = Locale.getDefault().language == "ko"

    // Effect description
    val effectDescription = if (isKorean) {
        "DJ 스타일의 하이패스/로우패스 필터 스윕"
    } else {
        "DJ-style highpass/lowpass filter sweep"
    }

    Column(
        modifier = modifier
            .clickable {
                isExpanded = !isExpanded
                onExpandChanged(isExpanded)
            }
            .fillMaxWidth()
    ) {
        ExpandableSectionTitle(
            isExpanded = isExpanded,
            title = title,
            isEnabled = isEnabled,
            onSwitchChange = { convertAudioEditViewModel.updateIsDjFilterEnabled() },
            onInitButton = {
                convertAudioEditViewModel.initDjFilterValues()
            }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Description
                Text(
                    text = effectDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter position slider (-1 to +1)
                FloatSliderSection(
                    title = stringResource(id = R.string.dj_filter_position),
                    displayValueText = when {
                        position < -0.05f -> {
                            val lpPercent = (abs(position) * 100).toInt()
                            if (isKorean) "로우패스 $lpPercent%" else "Lowpass $lpPercent%"
                        }
                        position > 0.05f -> {
                            val hpPercent = (position * 100).toInt()
                            if (isKorean) "하이패스 $hpPercent%" else "Highpass $hpPercent%"
                        }
                        else -> if (isKorean) "바이패스" else "Bypass"
                    },
                    onValueChange = {
                        convertAudioEditViewModel.updateDjFilterPosition(it)
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setDjFilterParams() },
                    onReset = {
                        convertAudioEditViewModel.initDjFilterValues()
                    },
                    currentValue = position,
                    valueRange = -1f..1f
                )
            }
        }
    }
}
