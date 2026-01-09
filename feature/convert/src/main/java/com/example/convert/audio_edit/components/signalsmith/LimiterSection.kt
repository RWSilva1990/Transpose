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
fun LimiterSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isLimiterEnabled.collectAsState()
    val inputGainDb by convertAudioEditViewModel.limiterInputGainDb.collectAsState()
    val limitDb by convertAudioEditViewModel.limiterLimitDb.collectAsState()
    val attackMs by convertAudioEditViewModel.limiterAttackMs.collectAsState()
    val releaseMs by convertAudioEditViewModel.limiterReleaseMs.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsLimiterEnabled() },
            onInitButton = { convertAudioEditViewModel.initLimiterValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.limiter_input_gain),
                    displayValueText = String.format("%.1f dB", inputGainDb),
                    onValueChange = { convertAudioEditViewModel.updateLimiterInputGainDb(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setLimiterParams() },
                    onReset = { convertAudioEditViewModel.initLimiterValues() },
                    currentValue = inputGainDb,
                    valueRange = -12f..24f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.limiter_limit),
                    displayValueText = String.format("%.1f dB", limitDb),
                    onValueChange = { convertAudioEditViewModel.updateLimiterLimitDb(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setLimiterParams() },
                    onReset = { convertAudioEditViewModel.initLimiterValues() },
                    currentValue = limitDb,
                    valueRange = -24f..0f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.limiter_attack),
                    displayValueText = String.format("%.1f ms", attackMs),
                    onValueChange = { convertAudioEditViewModel.updateLimiterAttackMs(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setLimiterParams() },
                    onReset = { convertAudioEditViewModel.initLimiterValues() },
                    currentValue = attackMs,
                    valueRange = 1f..50f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.limiter_release),
                    displayValueText = String.format("%.0f ms", releaseMs),
                    onValueChange = { convertAudioEditViewModel.updateLimiterReleaseMs(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setLimiterParams() },
                    onReset = { convertAudioEditViewModel.initLimiterValues() },
                    currentValue = releaseMs,
                    valueRange = 0f..250f
                )
            }
        }
    }
}
