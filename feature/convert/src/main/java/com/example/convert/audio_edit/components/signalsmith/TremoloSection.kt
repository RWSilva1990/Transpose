package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.ExpandableSectionTitle
import com.example.transpose.core.ui.R

@Composable
fun TremoloSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isTremoloEnabled.collectAsState()
    val freq by convertAudioEditViewModel.tremoloFreq.collectAsState()
    val depth by convertAudioEditViewModel.tremoloDepth.collectAsState()
    val waveform by convertAudioEditViewModel.tremoloWaveform.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val waveformNames = listOf("Sine", "Triangle", "Saw", "Ramp", "Square")
    val waveformName = waveformNames.getOrElse(waveform) { "Sine" }

    Column(
        modifier = modifier
            .clickable { isExpanded = !isExpanded }
            .fillMaxWidth()
    ) {
        ExpandableSectionTitle(
            isExpanded = isExpanded,
            title = title,
            isEnabled = isEnabled,
            onSwitchChange = { convertAudioEditViewModel.updateIsTremoloEnabled() },
            onInitButton = { convertAudioEditViewModel.initTremoloValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.tremolo_freq),
                    displayValueText = String.format("%.1f Hz", freq),
                    onValueChange = { convertAudioEditViewModel.updateTremoloFreq(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setTremoloParams() },
                    onReset = { convertAudioEditViewModel.initTremoloValues() },
                    currentValue = freq,
                    valueRange = 0.5f..20f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.tremolo_depth),
                    displayValueText = String.format("%.0f%%", depth * 100),
                    onValueChange = { convertAudioEditViewModel.updateTremoloDepth(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setTremoloParams() },
                    onReset = { convertAudioEditViewModel.initTremoloValues() },
                    currentValue = depth,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.tremolo_waveform),
                    displayValueText = waveformName,
                    onValueChange = { convertAudioEditViewModel.updateTremoloWaveform(it.toInt()) },
                    onValueChangeFinished = { convertAudioEditViewModel.setTremoloParams() },
                    onReset = { convertAudioEditViewModel.initTremoloValues() },
                    currentValue = waveform.toFloat(),
                    valueRange = 0f..4f
                )
            }
        }
    }
}
