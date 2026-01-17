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
import androidx.compose.runtime.collectAsState
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
import com.example.media.audio_effect.data.eq.SignalsmithEqPresets
import com.example.transpose.core.ui.R

@Composable
fun EqSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isEqEnabled.collectAsState()
    val currentPreset by convertAudioEditViewModel.eqPreset.collectAsState()
    val band1Gain by convertAudioEditViewModel.eqBand1Gain.collectAsState()
    val band2Gain by convertAudioEditViewModel.eqBand2Gain.collectAsState()
    val band3Gain by convertAudioEditViewModel.eqBand3Gain.collectAsState()
    val band4Gain by convertAudioEditViewModel.eqBand4Gain.collectAsState()
    val band5Gain by convertAudioEditViewModel.eqBand5Gain.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsEqEnabled() },
            onInitButton = { convertAudioEditViewModel.initEqValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Preset Selection Section
                EqPresetSection(
                    currentPreset = currentPreset,
                    isKorean = isKorean,
                    onPresetSelected = { convertAudioEditViewModel.updateEqPreset(it) }
                )

                // Current preset indicator
                if (currentPreset >= 0) {
                    val preset = SignalsmithEqPresets.getPreset(currentPreset)
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

                // Fine-tune sliders
                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_60hz),
                    displayValueText = String.format("%.0f dB", band1Gain),
                    onValueChange = { convertAudioEditViewModel.updateEqBandGain(0, it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.updateEqBandGain(0, 0f) },
                    currentValue = band1Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_250hz),
                    displayValueText = String.format("%.0f dB", band2Gain),
                    onValueChange = { convertAudioEditViewModel.updateEqBandGain(1, it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.updateEqBandGain(1, 0f) },
                    currentValue = band2Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_1khz),
                    displayValueText = String.format("%.0f dB", band3Gain),
                    onValueChange = { convertAudioEditViewModel.updateEqBandGain(2, it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.updateEqBandGain(2, 0f) },
                    currentValue = band3Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_4khz),
                    displayValueText = String.format("%.0f dB", band4Gain),
                    onValueChange = { convertAudioEditViewModel.updateEqBandGain(3, it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.updateEqBandGain(3, 0f) },
                    currentValue = band4Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_12khz),
                    displayValueText = String.format("%.0f dB", band5Gain),
                    onValueChange = { convertAudioEditViewModel.updateEqBandGain(4, it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.updateEqBandGain(4, 0f) },
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
                SignalsmithEqPresets.PRESET_FLAT,
                SignalsmithEqPresets.PRESET_BASS_BOOST,
                SignalsmithEqPresets.PRESET_TREBLE_BOOST,
                SignalsmithEqPresets.PRESET_VOCAL_BOOST
            ).forEach { presetIndex ->
                val preset = SignalsmithEqPresets.getPreset(presetIndex)
                EqPresetButton(
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
                EqPresetButton(
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
                EqPresetButton(
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
                SignalsmithEqPresets.PRESET_SPOKEN_WORD
            ).forEach { presetIndex ->
                val preset = SignalsmithEqPresets.getPreset(presetIndex)
                EqPresetButton(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == presetIndex,
                    onClick = { onPresetSelected(presetIndex) }
                )
            }
        }
    }
}

@Composable
private fun EqPresetButton(
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
