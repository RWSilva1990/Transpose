package com.example.convert.audio_edit.components.tempo

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.common.EffectCard
import com.example.convert.audio_edit.components.common.PitchTempoCardHeader
import java.util.Locale

@Composable
fun TempoSection(
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    modifier: Modifier = Modifier,
) {
    val tempoValue by convertAudioEditViewModel.tempoValue.collectAsStateWithLifecycle()
    TempoSectionContent(
        tempoValue = tempoValue,
        onMinus = convertAudioEditViewModel::tempoMinusOne,
        onPlus = convertAudioEditViewModel::tempoPlusOne,
        onReset = convertAudioEditViewModel::initTempoValue,
        onValueChange = convertAudioEditViewModel::updateTempoValue,
        onValueChangeFinished = convertAudioEditViewModel::setTempo,
        modifier = modifier,
    )
}

@Composable
private fun TempoSectionContent(
    tempoValue: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onReset: () -> Unit,
    onValueChange: (Int) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val actualValue = (tempoValue * 0.1) - 10.0
    val displayText = if (actualValue >= 0) {
        String.format(Locale.ROOT, "+%.1f", actualValue)
    } else {
        String.format(Locale.ROOT, "%.1f", actualValue)
    }

    val toggle = { isExpanded = !isExpanded }
    EffectCard(modifier = modifier, onClick = toggle) {
        PitchTempoCardHeader(
            title = "Tempo",
            displayValue = displayText,
            isExpanded = isExpanded,
            onToggleExpand = toggle,
            onMinus = onMinus,
            onPlus = onPlus,
            onReset = onReset,
        )
        if (isExpanded) {
            Slider(
                value = tempoValue.toFloat(),
                valueRange = 0f..200f,
                onValueChange = { onValueChange(it.toInt()) },
                onValueChangeFinished = onValueChangeFinished,
                colors = SliderDefaults.colors(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
