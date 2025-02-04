package com.example.convert.audio_edit.components.loudness_enhancer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.SliderSection

@Composable
fun LoudnessEnhancerSection(
    convertAudioEditViewModel: ConvertAudioEditViewModel
) {
    val loudnessEnhancerValue by convertAudioEditViewModel.loudnessEnhancerValue.collectAsState()

    SliderSection(
        title = "Loudness Enhancer",
        displayValueText = "+$loudnessEnhancerValue",
        onValueChange = { convertAudioEditViewModel.updateLoudnessEnhancerValue(it) },
        onValueChangeFinished = { convertAudioEditViewModel.setLoudnessEnhancer() },
        onReset = { convertAudioEditViewModel.initLoudnessEnhancerValue() },
        currentValue = loudnessEnhancerValue,
        valueRange = 0f..6000f
    )

}