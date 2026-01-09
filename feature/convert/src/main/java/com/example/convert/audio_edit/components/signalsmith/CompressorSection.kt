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
fun CompressorSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isCompressorEnabled.collectAsState()
    val thresholdDb by convertAudioEditViewModel.compThresholdDb.collectAsState()
    val ratio by convertAudioEditViewModel.compRatio.collectAsState()
    val attackMs by convertAudioEditViewModel.compAttackMs.collectAsState()
    val releaseMs by convertAudioEditViewModel.compReleaseMs.collectAsState()
    val makeupGainDb by convertAudioEditViewModel.compMakeupGainDb.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsCompressorEnabled() },
            onInitButton = { convertAudioEditViewModel.initCompressorValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.comp_threshold),
                    displayValueText = String.format("%.0f dB", thresholdDb),
                    onValueChange = { convertAudioEditViewModel.updateCompThresholdDb(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setCompressorParams() },
                    onReset = { convertAudioEditViewModel.initCompressorValues() },
                    currentValue = thresholdDb,
                    valueRange = -60f..0f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.comp_ratio),
                    displayValueText = String.format("%.1f:1", ratio),
                    onValueChange = { convertAudioEditViewModel.updateCompRatio(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setCompressorParams() },
                    onReset = { convertAudioEditViewModel.initCompressorValues() },
                    currentValue = ratio,
                    valueRange = 1f..20f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.comp_attack),
                    displayValueText = String.format("%.0f ms", attackMs),
                    onValueChange = { convertAudioEditViewModel.updateCompAttackMs(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setCompressorParams() },
                    onReset = { convertAudioEditViewModel.initCompressorValues() },
                    currentValue = attackMs,
                    valueRange = 1f..100f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.comp_release),
                    displayValueText = String.format("%.0f ms", releaseMs),
                    onValueChange = { convertAudioEditViewModel.updateCompReleaseMs(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setCompressorParams() },
                    onReset = { convertAudioEditViewModel.initCompressorValues() },
                    currentValue = releaseMs,
                    valueRange = 10f..500f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.comp_makeup_gain),
                    displayValueText = String.format("%.0f dB", makeupGainDb),
                    onValueChange = { convertAudioEditViewModel.updateCompMakeupGainDb(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setCompressorParams() },
                    onReset = { convertAudioEditViewModel.initCompressorValues() },
                    currentValue = makeupGainDb,
                    valueRange = 0f..24f
                )
            }
        }
    }
}
