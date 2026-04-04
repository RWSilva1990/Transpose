package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.ExpandableSectionTitle
import com.example.convert.audio_edit.components.common.EffectPresetSelector
import com.example.convert.audio_edit.components.common.PresetItem
import com.example.media.audio_effect.data.CompressorPresets
import com.example.transpose.core.ui.R
import java.util.Locale

@Composable
fun CompressorSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isCompressorEnabled.collectAsStateWithLifecycle()
    val thresholdDb by convertAudioEditViewModel.compThresholdDb.collectAsStateWithLifecycle()
    val ratio by convertAudioEditViewModel.compRatio.collectAsStateWithLifecycle()
    val attackMs by convertAudioEditViewModel.compAttackMs.collectAsStateWithLifecycle()
    val releaseMs by convertAudioEditViewModel.compReleaseMs.collectAsStateWithLifecycle()
    val makeupGainDb by convertAudioEditViewModel.compMakeupGainDb.collectAsStateWithLifecycle()
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedPresetIndex by rememberSaveable { mutableIntStateOf(-1) }

    // Get current locale for Korean/English
    val isKorean = Locale.getDefault().language == "ko"

    // Prepare preset items
    val presetItems = CompressorPresets.presets.map { preset ->
        PresetItem(
            name = if (isKorean) preset.nameKo else preset.name,
            description = if (isKorean) preset.descriptionKo else preset.description
        )
    }

    // Effect description
    val effectDescription = if (isKorean) {
        "큰 소리는 줄이고 작은 소리는 키워서 볼륨을 균일하게"
    } else {
        "Evens out loud and quiet parts for consistent volume"
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
            onSwitchChange = { convertAudioEditViewModel.updateIsCompressorEnabled() },
            onInitButton = {
                convertAudioEditViewModel.initCompressorValues()
                selectedPresetIndex = -1
            }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Preset selector with description
                EffectPresetSelector(
                    description = effectDescription,
                    presets = presetItems,
                    selectedIndex = selectedPresetIndex,
                    onPresetSelected = { index ->
                        selectedPresetIndex = index
                        val preset = CompressorPresets.presets[index]
                        convertAudioEditViewModel.applyCompressorPreset(
                            thresholdDb = preset.thresholdDb,
                            ratio = preset.ratio,
                            attackMs = preset.attackMs,
                            releaseMs = preset.releaseMs,
                            makeupGainDb = preset.makeupGainDb
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Manual controls
                FloatSliderSection(
                    title = stringResource(id = R.string.comp_threshold),
                    displayValueText = String.format("%.0f dB", thresholdDb),
                    onValueChange = {
                        convertAudioEditViewModel.updateCompThresholdDb(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setCompressorParams() },
                    onReset = {
                        convertAudioEditViewModel.initCompressorValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = thresholdDb,
                    valueRange = -60f..0f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.comp_ratio),
                    displayValueText = String.format("%.1f:1", ratio),
                    onValueChange = {
                        convertAudioEditViewModel.updateCompRatio(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setCompressorParams() },
                    onReset = {
                        convertAudioEditViewModel.initCompressorValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = ratio,
                    valueRange = 1f..20f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.comp_attack),
                    displayValueText = String.format("%.0f ms", attackMs),
                    onValueChange = {
                        convertAudioEditViewModel.updateCompAttackMs(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setCompressorParams() },
                    onReset = {
                        convertAudioEditViewModel.initCompressorValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = attackMs,
                    valueRange = 1f..100f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.comp_release),
                    displayValueText = String.format("%.0f ms", releaseMs),
                    onValueChange = {
                        convertAudioEditViewModel.updateCompReleaseMs(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setCompressorParams() },
                    onReset = {
                        convertAudioEditViewModel.initCompressorValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = releaseMs,
                    valueRange = 10f..500f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.comp_makeup_gain),
                    displayValueText = String.format("%.0f dB", makeupGainDb),
                    onValueChange = {
                        convertAudioEditViewModel.updateCompMakeupGainDb(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setCompressorParams() },
                    onReset = {
                        convertAudioEditViewModel.initCompressorValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = makeupGainDb,
                    valueRange = 0f..24f
                )
            }
        }
    }
}
