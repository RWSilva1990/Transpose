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
import com.example.media.audio_effect.data.StereoWidenerPresets
import com.example.transpose.core.ui.R
import java.util.Locale

@Composable
fun StereoWidenerSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isStereoWidenerEnabled.collectAsState()
    val width by convertAudioEditViewModel.stereoWidenerWidth.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedPresetIndex by rememberSaveable { mutableIntStateOf(-1) }

    // Get current locale for Korean/English
    val isKorean = Locale.getDefault().language == "ko"

    // Prepare preset items
    val presetItems = StereoWidenerPresets.presets.map { preset ->
        PresetItem(
            name = if (isKorean) preset.nameKo else preset.name,
            description = if (isKorean) preset.descriptionKo else preset.description
        )
    }

    // Effect description
    val effectDescription = if (isKorean) {
        "스테레오 이미지를 넓히거나 좁혀 공간감을 조절합니다"
    } else {
        "Adjusts the stereo width for a wider or narrower soundstage"
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
            onSwitchChange = { convertAudioEditViewModel.updateIsStereoWidenerEnabled() },
            onInitButton = {
                convertAudioEditViewModel.initStereoWidenerValues()
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
                        val preset = StereoWidenerPresets.presets[index]
                        convertAudioEditViewModel.applyStereoWidenerPreset(
                            width = preset.width
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Manual controls
                FloatSliderSection(
                    title = stringResource(id = R.string.stereo_widener_width),
                    displayValueText = when {
                        width < 0.5f -> if (isKorean) "모노" else "Mono"
                        width < 1.0f -> String.format("%.0f%% (${if (isKorean) "좁음" else "Narrow"})", width * 100)
                        width == 1.0f -> if (isKorean) "원본" else "Original"
                        else -> String.format("%.0f%% (${if (isKorean) "넓음" else "Wide"})", width * 100)
                    },
                    onValueChange = {
                        convertAudioEditViewModel.updateStereoWidenerWidth(it)
                        selectedPresetIndex = -1
                    },
                    onValueChangeFinished = { convertAudioEditViewModel.setStereoWidenerParams() },
                    onReset = {
                        convertAudioEditViewModel.initStereoWidenerValues()
                        selectedPresetIndex = -1
                    },
                    currentValue = width,
                    valueRange = 0f..2f
                )
            }
        }
    }
}
