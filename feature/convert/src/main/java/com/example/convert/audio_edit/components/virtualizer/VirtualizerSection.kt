package com.example.convert.audio_edit.components.virtualizer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.SliderSection

@Composable
fun VirtualizerSection(
    convertAudioEditViewModel: ConvertAudioEditViewModel
) {
    val virtualizerValue by convertAudioEditViewModel.virtualizerValue.collectAsState()
    SliderSection(
        title = "Virtualizer",
        displayValueText = "+$virtualizerValue",
        onValueChange = { convertAudioEditViewModel.updateVirtualizerValue(it) },
        onValueChangeFinished = { convertAudioEditViewModel.setVirtualizer() },
        onReset = { convertAudioEditViewModel.initVirtualizerValue() },
        currentValue = virtualizerValue,
        valueRange = 0f..1000f
    )
}