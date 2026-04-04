package com.example.convert.audio_edit.components.pitch

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.SliderSection
import java.util.Locale

@Composable
fun PitchSection(
    convertAudioEditViewModel: ConvertAudioEditViewModel
) {
    val pitchValue by convertAudioEditViewModel.pitchValue.collectAsStateWithLifecycle()

    val actualValue = (pitchValue * 0.1) - 10.0

    val displayText = if (actualValue >= 0) {
        String.format(Locale.ROOT, "+%.1f", actualValue)
    } else {
        String.format(Locale.ROOT, "%.1f", actualValue)
    }
    SliderSection(
        title = "Pitch",
        displayValueText = displayText,
        onValueChange = { convertAudioEditViewModel.updatePitchValue(it) },
        onValueChangeFinished = {convertAudioEditViewModel.setPitch()},
        onReset = { convertAudioEditViewModel.initPitchValue() },
        currentValue = pitchValue,
        valueRange = 0f..200f
    )
}