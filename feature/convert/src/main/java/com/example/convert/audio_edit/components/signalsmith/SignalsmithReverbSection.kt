package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.common.EffectCard
import com.example.convert.audio_edit.components.common.EffectCardHeader
import com.example.convert.audio_edit.components.common.EffectPresetChips
import com.example.media.audio_effect.data.reverb.SignalsmithReverbPresets
import com.example.transpose.core.ui.R
import java.util.Locale

enum class ReverbParam { DRY, WET, ROOM_MS, DECAY_SEC, EARLY, DETUNE, LOW_CUT_HZ, HIGH_CUT_HZ, LOW_DAMP, HIGH_DAMP }

@Composable
fun SignalsmithReverbSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isSignalsmithReverbEnabled.collectAsStateWithLifecycle()
    val currentPreset by convertAudioEditViewModel.signalsmithReverbPreset.collectAsStateWithLifecycle()
    val dry by convertAudioEditViewModel.signalsmithReverbDry.collectAsStateWithLifecycle()
    val wet by convertAudioEditViewModel.signalsmithReverbWet.collectAsStateWithLifecycle()
    val roomMs by convertAudioEditViewModel.signalsmithReverbRoomMs.collectAsStateWithLifecycle()
    val decaySec by convertAudioEditViewModel.signalsmithReverbDecaySec.collectAsStateWithLifecycle()
    val early by convertAudioEditViewModel.signalsmithReverbEarly.collectAsStateWithLifecycle()
    val detune by convertAudioEditViewModel.signalsmithReverbDetune.collectAsStateWithLifecycle()
    val lowCutHz by convertAudioEditViewModel.signalsmithReverbLowCutHz.collectAsStateWithLifecycle()
    val highCutHz by convertAudioEditViewModel.signalsmithReverbHighCutHz.collectAsStateWithLifecycle()
    val lowDampRate by convertAudioEditViewModel.signalsmithReverbLowDampRate.collectAsStateWithLifecycle()
    val highDampRate by convertAudioEditViewModel.signalsmithReverbHighDampRate.collectAsStateWithLifecycle()

    SignalsmithReverbSectionContent(
        title = title,
        isEnabled = isEnabled,
        currentPreset = currentPreset,
        dry = dry,
        wet = wet,
        roomMs = roomMs,
        decaySec = decaySec,
        early = early,
        detune = detune,
        lowCutHz = lowCutHz,
        highCutHz = highCutHz,
        lowDampRate = lowDampRate,
        highDampRate = highDampRate,
        onToggleEnable = { convertAudioEditViewModel.updateIsSignalsmithReverbEnabled() },
        onResetAll = { convertAudioEditViewModel.initSignalsmithReverbValues() },
        onPresetSelected = { convertAudioEditViewModel.updateSignalsmithReverbPreset(it) },
        onParamChange = { param, value ->
            when (param) {
                ReverbParam.DRY -> convertAudioEditViewModel.updateSignalsmithReverbDry(value)
                ReverbParam.WET -> convertAudioEditViewModel.updateSignalsmithReverbWet(value)
                ReverbParam.ROOM_MS -> convertAudioEditViewModel.updateSignalsmithReverbRoomMs(value)
                ReverbParam.DECAY_SEC -> convertAudioEditViewModel.updateSignalsmithReverbDecaySec(value)
                ReverbParam.EARLY -> convertAudioEditViewModel.updateSignalsmithReverbEarly(value)
                ReverbParam.DETUNE -> convertAudioEditViewModel.updateSignalsmithReverbDetune(value)
                ReverbParam.LOW_CUT_HZ -> convertAudioEditViewModel.updateSignalsmithReverbLowCutHz(value)
                ReverbParam.HIGH_CUT_HZ -> convertAudioEditViewModel.updateSignalsmithReverbHighCutHz(value)
                ReverbParam.LOW_DAMP -> convertAudioEditViewModel.updateSignalsmithReverbLowDampRate(value)
                ReverbParam.HIGH_DAMP -> convertAudioEditViewModel.updateSignalsmithReverbHighDampRate(value)
            }
        },
        onCommitParams = { convertAudioEditViewModel.setSignalsmithReverbParams() },
        onExpandChanged = onExpandChanged,
        modifier = modifier,
    )
}

@Composable
private fun SignalsmithReverbSectionContent(
    title: String,
    isEnabled: Boolean,
    currentPreset: Int,
    dry: Float,
    wet: Float,
    roomMs: Float,
    decaySec: Float,
    early: Float,
    detune: Float,
    lowCutHz: Float,
    highCutHz: Float,
    lowDampRate: Float,
    highDampRate: Float,
    onToggleEnable: () -> Unit,
    onResetAll: () -> Unit,
    onPresetSelected: (Int) -> Unit,
    onParamChange: (ReverbParam, Float) -> Unit,
    onCommitParams: () -> Unit,
    onExpandChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val isKorean = LocalConfiguration.current.locales[0].language == "ko"

    val activePreset = SignalsmithReverbPresets.getPreset(currentPreset)
    val presetName = if (isKorean) activePreset.nameKo else activePreset.name
    val presetNames = if (isKorean) {
        SignalsmithReverbPresets.presetNamesKo
    } else {
        SignalsmithReverbPresets.presetNames
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
                Text(
                    text = if (isKorean) activePreset.descriptionKo else activePreset.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                EffectPresetChips(
                    presets = presetNames,
                    selectedIndex = currentPreset,
                    onPresetSelected = onPresetSelected
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_dry),
                    displayValueText = "${(dry * 100).toInt()}%",
                    onValueChange = { onParamChange(ReverbParam.DRY, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbParam.DRY, activePreset.dry)
                        onCommitParams()
                    },
                    currentValue = dry,
                    valueRange = 0f..1.0f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_wet),
                    displayValueText = "${(wet * 100).toInt()}%",
                    onValueChange = { onParamChange(ReverbParam.WET, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbParam.WET, activePreset.wet)
                        onCommitParams()
                    },
                    currentValue = wet,
                    valueRange = 0f..1.0f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_room_size),
                    displayValueText = "${roomMs.toInt()} ms",
                    onValueChange = { onParamChange(ReverbParam.ROOM_MS, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbParam.ROOM_MS, activePreset.roomMs)
                        onCommitParams()
                    },
                    currentValue = roomMs,
                    valueRange = 10f..200f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_decay),
                    displayValueText = "${String.format(Locale.US, "%.1f", decaySec)} s",
                    onValueChange = { onParamChange(ReverbParam.DECAY_SEC, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbParam.DECAY_SEC, activePreset.decaySec)
                        onCommitParams()
                    },
                    currentValue = decaySec,
                    valueRange = 0.1f..30f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_early),
                    displayValueText = "${(early * 100).toInt()}%",
                    onValueChange = { onParamChange(ReverbParam.EARLY, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbParam.EARLY, activePreset.early)
                        onCommitParams()
                    },
                    currentValue = early,
                    valueRange = 0f..2.5f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_detune),
                    displayValueText = String.format(Locale.US, "%.1f", detune),
                    onValueChange = { onParamChange(ReverbParam.DETUNE, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbParam.DETUNE, activePreset.detune)
                        onCommitParams()
                    },
                    currentValue = detune,
                    valueRange = 0f..50f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_low_cut),
                    displayValueText = "${lowCutHz.toInt()} Hz",
                    onValueChange = { onParamChange(ReverbParam.LOW_CUT_HZ, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbParam.LOW_CUT_HZ, activePreset.lowCutHz)
                        onCommitParams()
                    },
                    currentValue = lowCutHz,
                    valueRange = 10f..500f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_high_cut),
                    displayValueText = "${highCutHz.toInt()} Hz",
                    onValueChange = { onParamChange(ReverbParam.HIGH_CUT_HZ, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbParam.HIGH_CUT_HZ, activePreset.highCutHz)
                        onCommitParams()
                    },
                    currentValue = highCutHz,
                    valueRange = 1000f..20000f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_low_damp),
                    displayValueText = String.format(Locale.US, "%.1f", lowDampRate),
                    onValueChange = { onParamChange(ReverbParam.LOW_DAMP, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbParam.LOW_DAMP, activePreset.lowDampRate)
                        onCommitParams()
                    },
                    currentValue = lowDampRate,
                    valueRange = 1f..10f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_high_damp),
                    displayValueText = String.format(Locale.US, "%.1f", highDampRate),
                    onValueChange = { onParamChange(ReverbParam.HIGH_DAMP, it) },
                    onValueChangeFinished = onCommitParams,
                    onReset = {
                        onParamChange(ReverbParam.HIGH_DAMP, activePreset.highDampRate)
                        onCommitParams()
                    },
                    currentValue = highDampRate,
                    valueRange = 1f..10f
                )
            }
        }
    }
}
