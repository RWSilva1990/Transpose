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
fun PhaserSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isPhaserEnabled.collectAsState()
    val lfoFreq by convertAudioEditViewModel.phaserLfoFreq.collectAsState()
    val lfoDepth by convertAudioEditViewModel.phaserLfoDepth.collectAsState()
    val feedback by convertAudioEditViewModel.phaserFeedback.collectAsState()
    val poles by convertAudioEditViewModel.phaserPoles.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .clickable { isExpanded = !isExpanded }
            .fillMaxWidth()
    ) {
        ExpandableSectionTitle(
            isExpanded = isExpanded,
            title = title,
            isEnabled = isEnabled,
            onSwitchChange = { convertAudioEditViewModel.updateIsPhaserEnabled() },
            onInitButton = { convertAudioEditViewModel.initPhaserValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.phaser_lfo_freq),
                    displayValueText = String.format("%.2f Hz", lfoFreq),
                    onValueChange = { convertAudioEditViewModel.updatePhaserLfoFreq(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setPhaserParams() },
                    onReset = { convertAudioEditViewModel.initPhaserValues() },
                    currentValue = lfoFreq,
                    valueRange = 0.1f..5f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.phaser_lfo_depth),
                    displayValueText = String.format("%.0f%%", lfoDepth * 100),
                    onValueChange = { convertAudioEditViewModel.updatePhaserLfoDepth(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setPhaserParams() },
                    onReset = { convertAudioEditViewModel.initPhaserValues() },
                    currentValue = lfoDepth,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.phaser_feedback),
                    displayValueText = String.format("%.0f%%", feedback * 100),
                    onValueChange = { convertAudioEditViewModel.updatePhaserFeedback(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setPhaserParams() },
                    onReset = { convertAudioEditViewModel.initPhaserValues() },
                    currentValue = feedback,
                    valueRange = 0f..0.99f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.phaser_poles),
                    displayValueText = "$poles",
                    onValueChange = { convertAudioEditViewModel.updatePhaserPoles(it.toInt()) },
                    onValueChangeFinished = { convertAudioEditViewModel.setPhaserParams() },
                    onReset = { convertAudioEditViewModel.initPhaserValues() },
                    currentValue = poles.toFloat(),
                    valueRange = 1f..8f
                )
            }
        }
    }
}
