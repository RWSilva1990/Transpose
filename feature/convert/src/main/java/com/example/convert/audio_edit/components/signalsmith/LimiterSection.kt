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
import androidx.compose.runtime.collectAsState
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
import com.example.media.audio_effect.data.LimiterPresets
import com.example.transpose.core.ui.R
import java.util.Locale

@Composable
fun LimiterSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isLimiterEnabled.collectAsState()
    val inputGainDb by convertAudioEditViewModel.limiterInputGainDb.collectAsState()
    val limitDb by convertAudioEditViewModel.limiterLimitDb.collectAsState()
    val attackMs by convertAudioEditViewModel.limiterAttackMs.collectAsState()
    val releaseMs by convertAudioEditViewModel.limiterReleaseMs.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedPresetIndex by rememberSaveable { mutableIntStateOf(-1) }

    // Get current locale for Korean/English
    val isKorean = Locale.getDefault().language == "ko"

    // Prepare preset items
    val presetItems = LimiterPresets.presets.map { preset ->
        PresetItem(
            name = if (isKorean) preset.nameKo else preset.name,
            description = if (isKorean) preset.descriptionKo else preset.description
        )
    }

    // Effect description
    val effectDescription = if (isKorean) {
        "소리가 너무 커지는 것을 방지합니다"
    } else {
        "Prevents audio from getting too loud"
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
            onSwitchChange = { convertAudioEditViewModel.updateIsLimiterEnabled() },
            onInitButton = {
                convertAudioEditViewModel.initLimiterValues()
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
                        val preset = LimiterPresets.presets[index]
                        convertAudioEditViewModel.applyLimiterPreset(
                            inputGainDb = preset.inputGainDb,
                            limitDb = preset.limitDb,
                            attackMs = preset.attackMs,
                            releaseMs = preset.releaseMs
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Manual controls
                FloatSliderSection(
                    title = stringResource(id = R.string.limiter_input_gain),
                    displayValueText = String.format("%.1f dB", inputGainDb),
                    onValueChange = {
                        convertAudioEditViewModel.updateLimiterInputGainDb(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setLimiterParams() },
                    onReset = {
                        convertAudioEditViewModel.initLimiterValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = inputGainDb,
                    valueRange = -12f..24f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.limiter_limit),
                    displayValueText = String.format("%.1f dB", limitDb),
                    onValueChange = {
                        convertAudioEditViewModel.updateLimiterLimitDb(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setLimiterParams() },
                    onReset = {
                        convertAudioEditViewModel.initLimiterValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = limitDb,
                    valueRange = -24f..0f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.limiter_attack),
                    displayValueText = String.format("%.1f ms", attackMs),
                    onValueChange = {
                        convertAudioEditViewModel.updateLimiterAttackMs(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setLimiterParams() },
                    onReset = {
                        convertAudioEditViewModel.initLimiterValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = attackMs,
                    valueRange = 1f..50f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.limiter_release),
                    displayValueText = String.format("%.0f ms", releaseMs),
                    onValueChange = {
                        convertAudioEditViewModel.updateLimiterReleaseMs(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setLimiterParams() },
                    onReset = {
                        convertAudioEditViewModel.initLimiterValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = releaseMs,
                    valueRange = 0f..250f
                )
            }
        }
    }
}
