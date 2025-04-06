package com.example.convert.audio_edit.components.bassboost

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.transpose.core.ui.R
import com.example.convert.audio_edit.components.SliderSection

@Composable
fun BassBoostSection(convertAudioEditViewModel: ConvertAudioEditViewModel) {
    val bassBoostValue by convertAudioEditViewModel.bassBoostValue.collectAsState()
    SliderSection(
        title = stringResource(id = R.string.bass_text),
        displayValueText = "+$bassBoostValue",
        onValueChange = { convertAudioEditViewModel.updateBassBoostValue(it) },
        onValueChangeFinished = { convertAudioEditViewModel.setBassBoost() },
        onReset = { convertAudioEditViewModel.initBassBoostValue() },
        currentValue = bassBoostValue,
        valueRange = 0f..1000f
    )
}