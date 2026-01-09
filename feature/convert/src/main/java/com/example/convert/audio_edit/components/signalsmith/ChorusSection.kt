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
fun ChorusSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isChorusEnabled.collectAsState()
    val mix by convertAudioEditViewModel.chorusMix.collectAsState()
    val depthMs by convertAudioEditViewModel.chorusDepthMs.collectAsState()
    val detune by convertAudioEditViewModel.chorusDetune.collectAsState()
    val stereo by convertAudioEditViewModel.chorusStereo.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsChorusEnabled() },
            onInitButton = { convertAudioEditViewModel.initChorusValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_mix),
                    displayValueText = String.format("%.0f%%", mix * 100),
                    onValueChange = { convertAudioEditViewModel.updateChorusMix(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setChorusParams() },
                    onReset = { convertAudioEditViewModel.initChorusValues() },
                    currentValue = mix,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_depth),
                    displayValueText = String.format("%.1f ms", depthMs),
                    onValueChange = { convertAudioEditViewModel.updateChorusDepthMs(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setChorusParams() },
                    onReset = { convertAudioEditViewModel.initChorusValues() },
                    currentValue = depthMs,
                    valueRange = 2f..50f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_detune),
                    displayValueText = String.format("%.0f cents", detune),
                    onValueChange = { convertAudioEditViewModel.updateChorusDetune(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setChorusParams() },
                    onReset = { convertAudioEditViewModel.initChorusValues() },
                    currentValue = detune,
                    valueRange = 1f..50f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.chorus_stereo),
                    displayValueText = String.format("%.2f", stereo),
                    onValueChange = { convertAudioEditViewModel.updateChorusStereo(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setChorusParams() },
                    onReset = { convertAudioEditViewModel.initChorusValues() },
                    currentValue = stereo,
                    valueRange = 0f..1.5f
                )
            }
        }
    }
}
