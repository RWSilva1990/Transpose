package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.ExpandableSectionTitle
import com.example.media.audio_effect.data.filter.ToneFilterPresets
import com.example.transpose.core.ui.R

@Composable
fun ToneFilterSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isToneFilterEnabled.collectAsStateWithLifecycle()
    val currentPreset by convertAudioEditViewModel.toneFilterPreset.collectAsStateWithLifecycle()
    val lowCutHz by convertAudioEditViewModel.toneFilterLowCutHz.collectAsStateWithLifecycle()
    val highCutHz by convertAudioEditViewModel.toneFilterHighCutHz.collectAsStateWithLifecycle()
    val lowShelfDb by convertAudioEditViewModel.toneFilterLowShelfDb.collectAsStateWithLifecycle()
    val highShelfDb by convertAudioEditViewModel.toneFilterHighShelfDb.collectAsStateWithLifecycle()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val isKorean = LocalConfiguration.current.locales[0].language == "ko"

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
            onSwitchChange = { convertAudioEditViewModel.updateIsToneFilterEnabled() },
            onInitButton = { convertAudioEditViewModel.initToneFilterValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ToneFilterPresetSection(
                    currentPreset = currentPreset,
                    isKorean = isKorean,
                    onPresetSelected = { convertAudioEditViewModel.updateToneFilterPreset(it) }
                )

                if (currentPreset >= 0) {
                    val preset = ToneFilterPresets.getPreset(currentPreset)
                    Text(
                        text = if (isKorean) "현재: ${preset.nameKo}" else "Current: ${preset.name}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                } else {
                    Text(
                        text = if (isKorean) "현재: 사용자 정의" else "Current: Custom",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                FloatSliderSection(
                    title = stringResource(id = R.string.tone_filter_low_cut),
                    displayValueText = String.format("%.0f Hz", lowCutHz),
                    onValueChange = { convertAudioEditViewModel.updateToneFilterLowCutHz(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setToneFilterParams() },
                    onReset = {
                        convertAudioEditViewModel.updateToneFilterLowCutHz(20f)
                        convertAudioEditViewModel.setToneFilterParams()
                    },
                    currentValue = lowCutHz,
                    valueRange = 20f..1200f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.tone_filter_high_cut),
                    displayValueText = String.format("%.0f Hz", highCutHz),
                    onValueChange = { convertAudioEditViewModel.updateToneFilterHighCutHz(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setToneFilterParams() },
                    onReset = {
                        convertAudioEditViewModel.updateToneFilterHighCutHz(20000f)
                        convertAudioEditViewModel.setToneFilterParams()
                    },
                    currentValue = highCutHz,
                    valueRange = 200f..20000f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.tone_filter_low_shelf),
                    displayValueText = String.format("%.1f dB", lowShelfDb),
                    onValueChange = { convertAudioEditViewModel.updateToneFilterLowShelfDb(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setToneFilterParams() },
                    onReset = {
                        convertAudioEditViewModel.updateToneFilterLowShelfDb(0f)
                        convertAudioEditViewModel.setToneFilterParams()
                    },
                    currentValue = lowShelfDb,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.tone_filter_high_shelf),
                    displayValueText = String.format("%.1f dB", highShelfDb),
                    onValueChange = { convertAudioEditViewModel.updateToneFilterHighShelfDb(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setToneFilterParams() },
                    onReset = {
                        convertAudioEditViewModel.updateToneFilterHighShelfDb(0f)
                        convertAudioEditViewModel.setToneFilterParams()
                    },
                    currentValue = highShelfDb,
                    valueRange = -12f..12f
                )
            }
        }
    }
}

@Composable
private fun ToneFilterPresetSection(
    currentPreset: Int,
    isKorean: Boolean,
    onPresetSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (isKorean) "프리셋" else "Presets",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToneFilterPresets.presets.forEachIndexed { index, preset ->
                ToneFilterPresetButton(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == index,
                    onClick = { onPresetSelected(index) }
                )
            }
        }
    }
}

@Composable
private fun ToneFilterPresetButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
    ) {
        Text(
            text = label,
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 12.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
