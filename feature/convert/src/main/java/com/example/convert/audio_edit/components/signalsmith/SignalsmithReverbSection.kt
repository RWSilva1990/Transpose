package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.convert.audio_edit.components.common.EffectCard
import com.example.convert.audio_edit.components.common.EffectCardHeader
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
                ReverbPresetSection(
                    currentPreset = currentPreset,
                    isKorean = isKorean,
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

@Composable
private fun ReverbPresetSection(
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
            listOf(
                SignalsmithReverbPresets.PRESET_DEFAULT,
                SignalsmithReverbPresets.PRESET_HALL,
                SignalsmithReverbPresets.PRESET_VOCAL,
                SignalsmithReverbPresets.PRESET_VINTAGE,
                SignalsmithReverbPresets.PRESET_ROOM,
                SignalsmithReverbPresets.PRESET_SNAP,
                SignalsmithReverbPresets.PRESET_UNDERWATER,
                SignalsmithReverbPresets.PRESET_AIR
            ).forEach { presetIndex ->
                val preset = SignalsmithReverbPresets.getPreset(presetIndex)
                ReverbPresetButton(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == presetIndex,
                    onClick = { onPresetSelected(presetIndex) }
                )
            }
        }
    }
}

@Composable
private fun ReverbPresetButton(
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
