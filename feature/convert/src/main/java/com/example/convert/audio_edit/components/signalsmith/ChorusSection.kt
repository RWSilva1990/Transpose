package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.common.EffectCard
import com.example.convert.audio_edit.components.common.EffectCardHeader
import com.example.convert.audio_edit.components.common.EffectPresetSelector
import com.example.convert.audio_edit.components.common.PresetItem
import com.example.media.audio_effect.data.ChorusPresets
import com.example.transpose.core.ui.R
import java.util.Locale

enum class ChorusParam { MIX, DEPTH_MS, DETUNE, STEREO }

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

    ChorusSectionContent(
        title = title,
        isEnabled = isEnabled,
        mix = mix,
        depthMs = depthMs,
        detune = detune,
        stereo = stereo,
        onToggleEnable = { convertAudioEditViewModel.updateIsChorusEnabled() },
        onResetAll = { convertAudioEditViewModel.initChorusValues() },
        onApplyPreset = { index ->
            val preset = ChorusPresets.presets[index]
            convertAudioEditViewModel.applyChorusPreset(
                mix = preset.mix,
                depthMs = preset.depthMs,
                detune = preset.detune,
                stereo = preset.stereo,
            )
        },
        onParamChange = { param, value ->
            when (param) {
                ChorusParam.MIX -> convertAudioEditViewModel.updateChorusMix(value)
                ChorusParam.DEPTH_MS -> convertAudioEditViewModel.updateChorusDepthMs(value)
                ChorusParam.DETUNE -> convertAudioEditViewModel.updateChorusDetune(value)
                ChorusParam.STEREO -> convertAudioEditViewModel.updateChorusStereo(value)
            }
        },
        onCommitParams = { convertAudioEditViewModel.setChorusParams() },
        onExpandChanged = onExpandChanged,
        modifier = modifier,
    )
}

@Composable
private fun ChorusSectionContent(
    title: String,
    isEnabled: Boolean,
    mix: Float,
    depthMs: Float,
    detune: Float,
    stereo: Float,
    onToggleEnable: () -> Unit,
    onResetAll: () -> Unit,
    onApplyPreset: (Int) -> Unit,
    onParamChange: (ChorusParam, Float) -> Unit,
    onCommitParams: () -> Unit,
    onExpandChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedPresetIndex by rememberSaveable { mutableIntStateOf(-1) }
    val isKorean = Locale.getDefault().language == "ko"

    val presetItems = remember(isKorean) {
        ChorusPresets.presets.map { preset ->
            PresetItem(
                name = if (isKorean) preset.nameKo else preset.name,
                description = if (isKorean) preset.descriptionKo else preset.description
            )
        }
    }

    val effectDescription = if (isKorean) {
        "여러 목소리가 함께 노래하는 것처럼 들립니다"
    } else {
        "Makes it sound like multiple voices singing together"
    }

    val presetName = if (selectedPresetIndex in ChorusPresets.presets.indices) {
        val p = ChorusPresets.presets[selectedPresetIndex]
        if (isKorean) p.nameKo else p.name
    } else {
        if (isKorean) "사용자 정의" else "Custom"
    }

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
            onReset = {
                onResetAll()
                selectedPresetIndex = -1
            },
        )
        if (isExpanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                EffectPresetSelector(
                    description = effectDescription,
                    presets = presetItems,
                    selectedIndex = selectedPresetIndex,
                    onPresetSelected = { index ->
                        selectedPresetIndex = index
                        onApplyPreset(index)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_mix),
                    displayValueText = String.format("%.0f%%", mix * 100),
                    onValueChange = {
                        onParamChange(ChorusParam.MIX, it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onResetAll()
                        selectedPresetIndex = -1
                    },
                    currentValue = mix,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_depth),
                    displayValueText = String.format("%.1f ms", depthMs),
                    onValueChange = {
                        onParamChange(ChorusParam.DEPTH_MS, it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onResetAll()
                        selectedPresetIndex = -1
                    },
                    currentValue = depthMs,
                    valueRange = 2f..50f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_detune),
                    displayValueText = String.format("%.0f cents", detune),
                    onValueChange = {
                        onParamChange(ChorusParam.DETUNE, it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onResetAll()
                        selectedPresetIndex = -1
                    },
                    currentValue = detune,
                    valueRange = 1f..50f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_stereo),
                    displayValueText = String.format("%.2f", stereo),
                    onValueChange = {
                        onParamChange(ChorusParam.STEREO, it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onResetAll()
                        selectedPresetIndex = -1
                    },
                    currentValue = stereo,
                    valueRange = 0f..1.5f
                )
            }
        }
    }
}
