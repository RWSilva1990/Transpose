package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.common.EffectCard
import com.example.convert.audio_edit.components.common.EffectCardHeader
import com.example.convert.audio_edit.components.common.EffectPresetSelector
import com.example.convert.audio_edit.components.common.PresetItem
import com.example.media.audio_effect.data.reverb.ReverbPlusPresets
import com.example.transpose.core.ui.R
import java.util.Locale

enum class ReverbPlusParam { DRY, WET, ROOM_SIZE, DAMPING }

@Composable
fun ReverbPlusSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isReverbPlusEnabled.collectAsStateWithLifecycle()
    val currentPreset by convertAudioEditViewModel.reverbPlusPreset.collectAsStateWithLifecycle()
    val dry by convertAudioEditViewModel.reverbPlusDry.collectAsStateWithLifecycle()
    val wet by convertAudioEditViewModel.reverbPlusWet.collectAsStateWithLifecycle()
    val roomSize by convertAudioEditViewModel.reverbPlusRoomSize.collectAsStateWithLifecycle()
    val damping by convertAudioEditViewModel.reverbPlusDamping.collectAsStateWithLifecycle()

    ReverbPlusSectionContent(
        title = title,
        isEnabled = isEnabled,
        currentPreset = currentPreset,
        dry = dry,
        wet = wet,
        roomSize = roomSize,
        damping = damping,
        onToggleEnable = { convertAudioEditViewModel.updateIsReverbPlusEnabled() },
        onResetAll = { convertAudioEditViewModel.initReverbPlusValues() },
        onPresetSelected = { convertAudioEditViewModel.updateReverbPlusPreset(it) },
        onParamChange = { param, value ->
            when (param) {
                ReverbPlusParam.DRY -> convertAudioEditViewModel.updateReverbPlusDry(value)
                ReverbPlusParam.WET -> convertAudioEditViewModel.updateReverbPlusWet(value)
                ReverbPlusParam.ROOM_SIZE -> convertAudioEditViewModel.updateReverbPlusRoomSize(value)
                ReverbPlusParam.DAMPING -> convertAudioEditViewModel.updateReverbPlusDamping(value)
            }
        },
        onCommitParams = { convertAudioEditViewModel.setReverbPlusParams() },
        onExpandChanged = onExpandChanged,
        modifier = modifier,
    )
}

@Composable
private fun ReverbPlusSectionContent(
    title: String,
    isEnabled: Boolean,
    currentPreset: Int,
    dry: Float,
    wet: Float,
    roomSize: Float,
    damping: Float,
    onToggleEnable: () -> Unit,
    onResetAll: () -> Unit,
    onPresetSelected: (Int) -> Unit,
    onParamChange: (ReverbPlusParam, Float) -> Unit,
    onCommitParams: () -> Unit,
    onExpandChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val isKorean = Locale.getDefault().language == "ko"
    val activePreset = ReverbPlusPresets.getPreset(currentPreset)
    val presetName = if (isKorean) activePreset.nameKo else activePreset.name
    val presetItems = remember(isKorean) {
        ReverbPlusPresets.presets.map { preset ->
            PresetItem(
                name = if (isKorean) preset.nameKo else preset.name,
                description = if (isKorean) preset.descriptionKo else preset.description
            )
        }
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
            onReset = onResetAll,
        )

        if (isExpanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                EffectPresetSelector(
                    description = if (isKorean) activePreset.descriptionKo else activePreset.description,
                    presets = presetItems,
                    selectedIndex = currentPreset,
                    onPresetSelected = onPresetSelected
                )

                Spacer(modifier = Modifier.height(8.dp))

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_dry),
                    displayValueText = "${(dry * 100).toInt()}%",
                    onValueChange = { onParamChange(ReverbPlusParam.DRY, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbPlusParam.DRY, activePreset.dry)
                        onCommitParams()
                    },
                    currentValue = dry,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_wet),
                    displayValueText = "${(wet * 100).toInt()}%",
                    onValueChange = { onParamChange(ReverbPlusParam.WET, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbPlusParam.WET, activePreset.wet)
                        onCommitParams()
                    },
                    currentValue = wet,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_room_size),
                    displayValueText = "${(roomSize * 100).toInt()}%",
                    onValueChange = { onParamChange(ReverbPlusParam.ROOM_SIZE, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbPlusParam.ROOM_SIZE, activePreset.roomSize)
                        onCommitParams()
                    },
                    currentValue = roomSize,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_damping),
                    displayValueText = "${(damping * 100).toInt()}%",
                    onValueChange = { onParamChange(ReverbPlusParam.DAMPING, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbPlusParam.DAMPING, activePreset.damping)
                        onCommitParams()
                    },
                    currentValue = damping,
                    valueRange = 0f..1f
                )
            }
        }
    }
}
