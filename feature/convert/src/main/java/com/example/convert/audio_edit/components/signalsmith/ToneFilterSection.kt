package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.common.EffectCard
import com.example.convert.audio_edit.components.common.EffectCardHeader
import com.example.convert.audio_edit.components.common.EffectPresetChip
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

    ToneFilterSectionContent(
        title = title,
        isEnabled = isEnabled,
        currentPreset = currentPreset,
        lowCutHz = lowCutHz,
        highCutHz = highCutHz,
        lowShelfDb = lowShelfDb,
        highShelfDb = highShelfDb,
        onToggleEnable = { convertAudioEditViewModel.updateIsToneFilterEnabled() },
        onResetAll = { convertAudioEditViewModel.initToneFilterValues() },
        onPresetSelected = { convertAudioEditViewModel.updateToneFilterPreset(it) },
        onLowCutChange = { convertAudioEditViewModel.updateToneFilterLowCutHz(it) },
        onHighCutChange = { convertAudioEditViewModel.updateToneFilterHighCutHz(it) },
        onLowShelfChange = { convertAudioEditViewModel.updateToneFilterLowShelfDb(it) },
        onHighShelfChange = { convertAudioEditViewModel.updateToneFilterHighShelfDb(it) },
        onCommitParams = { convertAudioEditViewModel.setToneFilterParams() },
        onExpandChanged = onExpandChanged,
        modifier = modifier,
    )
}

@Composable
private fun ToneFilterSectionContent(
    title: String,
    isEnabled: Boolean,
    currentPreset: Int,
    lowCutHz: Float,
    highCutHz: Float,
    lowShelfDb: Float,
    highShelfDb: Float,
    onToggleEnable: () -> Unit,
    onResetAll: () -> Unit,
    onPresetSelected: (Int) -> Unit,
    onLowCutChange: (Float) -> Unit,
    onHighCutChange: (Float) -> Unit,
    onLowShelfChange: (Float) -> Unit,
    onHighShelfChange: (Float) -> Unit,
    onCommitParams: () -> Unit,
    onExpandChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val isKorean = LocalConfiguration.current.locales[0].language == "ko"

    val activePreset = ToneFilterPresets.getPreset(currentPreset)
    val presetName = if (isKorean) activePreset.nameKo else activePreset.name
    val toggle = {
        isExpanded = !isExpanded
        onExpandChanged(isExpanded)
    }

    EffectCard(modifier = modifier, onClick = toggle) {
        EffectCardHeader(
            title = title,
            presetName = presetName,
            isExpanded = isExpanded,
            isEnabled = isEnabled,
            onToggleExpand = toggle,
            onToggleEnable = { onToggleEnable() },
            onReset = onResetAll,
        )
        if (isExpanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text(
                    text = if (isKorean) activePreset.descriptionKo else activePreset.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                ToneFilterPresetSection(
                    currentPreset = currentPreset,
                    isKorean = isKorean,
                    onPresetSelected = onPresetSelected
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.tone_filter_low_cut),
                    displayValueText = String.format("%.0f Hz", lowCutHz),
                    onValueChange = onLowCutChange,
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onLowCutChange(activePreset.lowCutHz)
                        onCommitParams()
                    },
                    currentValue = lowCutHz,
                    valueRange = 20f..1200f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.tone_filter_high_cut),
                    displayValueText = String.format("%.0f Hz", highCutHz),
                    onValueChange = onHighCutChange,
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onHighCutChange(activePreset.highCutHz)
                        onCommitParams()
                    },
                    currentValue = highCutHz,
                    valueRange = 200f..20000f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.tone_filter_low_shelf),
                    displayValueText = String.format("%.1f dB", lowShelfDb),
                    onValueChange = onLowShelfChange,
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onLowShelfChange(activePreset.lowShelfDb)
                        onCommitParams()
                    },
                    currentValue = lowShelfDb,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.tone_filter_high_shelf),
                    displayValueText = String.format("%.1f dB", highShelfDb),
                    onValueChange = onHighShelfChange,
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onHighShelfChange(activePreset.highShelfDb)
                        onCommitParams()
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
                EffectPresetChip(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == index,
                    onClick = { onPresetSelected(index) }
                )
            }
        }
    }
}
