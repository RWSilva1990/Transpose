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
import com.example.media.audio_effect.data.eq.SignalsmithEqPresets
import com.example.transpose.core.ui.R

@Composable
fun EqSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isEqEnabled.collectAsStateWithLifecycle()
    val currentPreset by convertAudioEditViewModel.eqPreset.collectAsStateWithLifecycle()
    val band1Gain by convertAudioEditViewModel.eqBand1Gain.collectAsStateWithLifecycle()
    val band2Gain by convertAudioEditViewModel.eqBand2Gain.collectAsStateWithLifecycle()
    val band3Gain by convertAudioEditViewModel.eqBand3Gain.collectAsStateWithLifecycle()
    val band4Gain by convertAudioEditViewModel.eqBand4Gain.collectAsStateWithLifecycle()
    val band5Gain by convertAudioEditViewModel.eqBand5Gain.collectAsStateWithLifecycle()

    EqSectionContent(
        title = title,
        isEnabled = isEnabled,
        currentPreset = currentPreset,
        band1Gain = band1Gain,
        band2Gain = band2Gain,
        band3Gain = band3Gain,
        band4Gain = band4Gain,
        band5Gain = band5Gain,
        onToggleEnable = { convertAudioEditViewModel.updateIsEqEnabled() },
        onResetAll = { convertAudioEditViewModel.initEqValues() },
        onPresetSelected = { convertAudioEditViewModel.updateEqPreset(it) },
        onBandGainChange = { band, value -> convertAudioEditViewModel.updateEqBandGain(band, value) },
        onExpandChanged = onExpandChanged,
        modifier = modifier,
    )
}

@Composable
private fun EqSectionContent(
    title: String,
    isEnabled: Boolean,
    currentPreset: Int,
    band1Gain: Float,
    band2Gain: Float,
    band3Gain: Float,
    band4Gain: Float,
    band5Gain: Float,
    onToggleEnable: () -> Unit,
    onResetAll: () -> Unit,
    onPresetSelected: (Int) -> Unit,
    onBandGainChange: (Int, Float) -> Unit,
    onExpandChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val isKorean = LocalConfiguration.current.locales[0].language == "ko"

    val activePreset = SignalsmithEqPresets.getPreset(currentPreset)
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
                EqPresetSection(
                    currentPreset = currentPreset,
                    isKorean = isKorean,
                    onPresetSelected = onPresetSelected
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_60hz),
                    displayValueText = String.format("%.0f dB", band1Gain),
                    onValueChange = { onBandGainChange(0, it) },
                    onValueChangeFinished = { },
                    onReset = { onBandGainChange(0, activePreset.gains[0]) },
                    currentValue = band1Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_250hz),
                    displayValueText = String.format("%.0f dB", band2Gain),
                    onValueChange = { onBandGainChange(1, it) },
                    onValueChangeFinished = { },
                    onReset = { onBandGainChange(1, activePreset.gains[1]) },
                    currentValue = band2Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_1khz),
                    displayValueText = String.format("%.0f dB", band3Gain),
                    onValueChange = { onBandGainChange(2, it) },
                    onValueChangeFinished = { },
                    onReset = { onBandGainChange(2, activePreset.gains[2]) },
                    currentValue = band3Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_4khz),
                    displayValueText = String.format("%.0f dB", band4Gain),
                    onValueChange = { onBandGainChange(3, it) },
                    onValueChangeFinished = { },
                    onReset = { onBandGainChange(3, activePreset.gains[3]) },
                    currentValue = band4Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_12khz),
                    displayValueText = String.format("%.0f dB", band5Gain),
                    onValueChange = { onBandGainChange(4, it) },
                    onValueChangeFinished = { },
                    onReset = { onBandGainChange(4, activePreset.gains[4]) },
                    currentValue = band5Gain,
                    valueRange = -12f..12f
                )
            }
        }
    }
}

@Composable
private fun EqPresetSection(
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

        // Row 1: Basic presets
        Text(
            text = if (isKorean) "기본" else "Basic",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                SignalsmithEqPresets.PRESET_BASS_BOOST,
                SignalsmithEqPresets.PRESET_TREBLE_BOOST,
                SignalsmithEqPresets.PRESET_VOCAL_BOOST
            ).forEach { presetIndex ->
                val preset = SignalsmithEqPresets.getPreset(presetIndex)
                EffectPresetChip(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == presetIndex,
                    onClick = { onPresetSelected(presetIndex) }
                )
            }
        }

        // Row 2: Genre presets (Part 1)
        Text(
            text = if (isKorean) "장르" else "Genre",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                SignalsmithEqPresets.PRESET_POP,
                SignalsmithEqPresets.PRESET_ROCK,
                SignalsmithEqPresets.PRESET_JAZZ,
                SignalsmithEqPresets.PRESET_CLASSICAL,
                SignalsmithEqPresets.PRESET_ELECTRONIC
            ).forEach { presetIndex ->
                val preset = SignalsmithEqPresets.getPreset(presetIndex)
                EffectPresetChip(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == presetIndex,
                    onClick = { onPresetSelected(presetIndex) }
                )
            }
        }

        // Row 3: Genre presets (Part 2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                SignalsmithEqPresets.PRESET_HIP_HOP,
                SignalsmithEqPresets.PRESET_ACOUSTIC,
                SignalsmithEqPresets.PRESET_R_AND_B
            ).forEach { presetIndex ->
                val preset = SignalsmithEqPresets.getPreset(presetIndex)
                EffectPresetChip(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == presetIndex,
                    onClick = { onPresetSelected(presetIndex) }
                )
            }
        }

        // Row 4: Special purpose
        Text(
            text = if (isKorean) "특수 용도" else "Special",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                SignalsmithEqPresets.PRESET_LOUDNESS,
                SignalsmithEqPresets.PRESET_SPOKEN_WORD,
                SignalsmithEqPresets.PRESET_FLAT
            ).forEach { presetIndex ->
                val preset = SignalsmithEqPresets.getPreset(presetIndex)
                EffectPresetChip(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == presetIndex,
                    onClick = { onPresetSelected(presetIndex) }
                )
            }
        }
    }
}
