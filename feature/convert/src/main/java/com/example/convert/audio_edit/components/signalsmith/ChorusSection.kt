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
import com.example.media.audio_effect.data.ChorusPresets
import com.example.transpose.core.ui.R
import java.util.Locale

@Composable
fun ChorusSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isChorusEnabled.collectAsStateWithLifecycle()
    val mix by convertAudioEditViewModel.chorusMix.collectAsStateWithLifecycle()
    val depthMs by convertAudioEditViewModel.chorusDepthMs.collectAsStateWithLifecycle()
    val detune by convertAudioEditViewModel.chorusDetune.collectAsStateWithLifecycle()
    val stereo by convertAudioEditViewModel.chorusStereo.collectAsStateWithLifecycle()
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedPresetIndex by rememberSaveable { mutableIntStateOf(-1) }

    // Get current locale for Korean/English
    val isKorean = Locale.getDefault().language == "ko"

    // Prepare preset items
    val presetItems = ChorusPresets.presets.map { preset ->
        PresetItem(
            name = if (isKorean) preset.nameKo else preset.name,
            description = if (isKorean) preset.descriptionKo else preset.description
        )
    }

    // Effect description
    val effectDescription = if (isKorean) {
        "여러 목소리가 함께 노래하는 것처럼 들립니다"
    } else {
        "Makes it sound like multiple voices singing together"
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
            onSwitchChange = { convertAudioEditViewModel.updateIsChorusEnabled() },
            onInitButton = {
                convertAudioEditViewModel.initChorusValues()
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
                        val preset = ChorusPresets.presets[index]
                        convertAudioEditViewModel.applyChorusPreset(
                            mix = preset.mix,
                            depthMs = preset.depthMs,
                            detune = preset.detune,
                            stereo = preset.stereo
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Manual controls
                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_mix),
                    displayValueText = String.format("%.0f%%", mix * 100),
                    onValueChange = {
                        convertAudioEditViewModel.updateChorusMix(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setChorusParams() },
                    onReset = {
                        convertAudioEditViewModel.initChorusValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = mix,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_depth),
                    displayValueText = String.format("%.1f ms", depthMs),
                    onValueChange = {
                        convertAudioEditViewModel.updateChorusDepthMs(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setChorusParams() },
                    onReset = {
                        convertAudioEditViewModel.initChorusValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = depthMs,
                    valueRange = 2f..50f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_detune),
                    displayValueText = String.format("%.0f cents", detune),
                    onValueChange = {
                        convertAudioEditViewModel.updateChorusDetune(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setChorusParams() },
                    onReset = {
                        convertAudioEditViewModel.initChorusValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = detune,
                    valueRange = 1f..50f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_stereo),
                    displayValueText = String.format("%.2f", stereo),
                    onValueChange = {
                        convertAudioEditViewModel.updateChorusStereo(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setChorusParams() },
                    onReset = {
                        convertAudioEditViewModel.initChorusValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = stereo,
                    valueRange = 0f..1.5f
                )
            }
        }
    }
}
