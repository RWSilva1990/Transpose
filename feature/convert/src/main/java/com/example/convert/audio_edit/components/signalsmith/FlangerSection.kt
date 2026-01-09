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
fun FlangerSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isFlangerEnabled.collectAsState()
    val lfoFreq by convertAudioEditViewModel.flangerLfoFreq.collectAsState()
    val lfoDepth by convertAudioEditViewModel.flangerLfoDepth.collectAsState()
    val feedback by convertAudioEditViewModel.flangerFeedback.collectAsState()
    val delayMs by convertAudioEditViewModel.flangerDelayMs.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsFlangerEnabled() },
            onInitButton = { convertAudioEditViewModel.initFlangerValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.flanger_lfo_freq),
                    displayValueText = String.format("%.2f Hz", lfoFreq),
                    onValueChange = { convertAudioEditViewModel.updateFlangerLfoFreq(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setFlangerParams() },
                    onReset = { convertAudioEditViewModel.initFlangerValues() },
                    currentValue = lfoFreq,
                    valueRange = 0.01f..2f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.flanger_lfo_depth),
                    displayValueText = String.format("%.0f%%", lfoDepth * 100),
                    onValueChange = { convertAudioEditViewModel.updateFlangerLfoDepth(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setFlangerParams() },
                    onReset = { convertAudioEditViewModel.initFlangerValues() },
                    currentValue = lfoDepth,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.flanger_feedback),
                    displayValueText = String.format("%.0f%%", feedback * 100),
                    onValueChange = { convertAudioEditViewModel.updateFlangerFeedback(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setFlangerParams() },
                    onReset = { convertAudioEditViewModel.initFlangerValues() },
                    currentValue = feedback,
                    valueRange = 0f..0.99f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.flanger_delay),
                    displayValueText = String.format("%.1f ms", delayMs),
                    onValueChange = { convertAudioEditViewModel.updateFlangerDelayMs(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setFlangerParams() },
                    onReset = { convertAudioEditViewModel.initFlangerValues() },
                    currentValue = delayMs,
                    valueRange = 0.1f..7f
                )
            }
        }
    }
}
