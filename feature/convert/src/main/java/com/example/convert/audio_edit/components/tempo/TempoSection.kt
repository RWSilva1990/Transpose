package com.example.convert.audio_edit.components.tempo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.SliderSection
import java.util.Locale

@Composable
fun TempoSection(convertAudioEditViewModel: ConvertAudioEditViewModel) {
    val tempoValue by convertAudioEditViewModel.tempoValue.collectAsState()
    val actualValue = (tempoValue * 0.1) - 10.0

    val displayText = if (actualValue >= 0) {
        String.format(Locale.ROOT, "+%.1f", actualValue)
    } else {
        String.format(Locale.ROOT, "%.1f", actualValue)
    }

    SliderSection(
        title = "Tempo",
        displayValueText = displayText,
        onValueChange = { convertAudioEditViewModel.updateTempoValue(it) },
        onValueChangeFinished = {convertAudioEditViewModel.setTempo()},
        onReset = { convertAudioEditViewModel.initTempoValue() },
        currentValue = tempoValue,
        valueRange = 0f..200f
    )
}