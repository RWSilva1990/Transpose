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
fun EqSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isEqEnabled.collectAsState()
    val band1Gain by convertAudioEditViewModel.eqBand1Gain.collectAsState()
    val band2Gain by convertAudioEditViewModel.eqBand2Gain.collectAsState()
    val band3Gain by convertAudioEditViewModel.eqBand3Gain.collectAsState()
    val band4Gain by convertAudioEditViewModel.eqBand4Gain.collectAsState()
    val band5Gain by convertAudioEditViewModel.eqBand5Gain.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsEqEnabled() },
            onInitButton = { convertAudioEditViewModel.initEqValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_60hz),
                    displayValueText = String.format("%.0f dB", band1Gain),
                    onValueChange = { convertAudioEditViewModel.updateEqBandGain(0, it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.updateEqBandGain(0, 0f) },
                    currentValue = band1Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_250hz),
                    displayValueText = String.format("%.0f dB", band2Gain),
                    onValueChange = { convertAudioEditViewModel.updateEqBandGain(1, it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.updateEqBandGain(1, 0f) },
                    currentValue = band2Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_1khz),
                    displayValueText = String.format("%.0f dB", band3Gain),
                    onValueChange = { convertAudioEditViewModel.updateEqBandGain(2, it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.updateEqBandGain(2, 0f) },
                    currentValue = band3Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_4khz),
                    displayValueText = String.format("%.0f dB", band4Gain),
                    onValueChange = { convertAudioEditViewModel.updateEqBandGain(3, it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.updateEqBandGain(3, 0f) },
                    currentValue = band4Gain,
                    valueRange = -12f..12f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.eq_band_12khz),
                    displayValueText = String.format("%.0f dB", band5Gain),
                    onValueChange = { convertAudioEditViewModel.updateEqBandGain(4, it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.updateEqBandGain(4, 0f) },
                    currentValue = band5Gain,
                    valueRange = -12f..12f
                )
            }
        }
    }
}
