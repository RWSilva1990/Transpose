package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
fun SignalsmithVirtualizerSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isHrtfEnabled.collectAsState()
    val intensity by convertAudioEditViewModel.hrtfIntensity.collectAsState()
    val azimuth by convertAudioEditViewModel.hrtfAzimuth.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsHrtfEnabled() },
            onInitButton = { convertAudioEditViewModel.initHrtfValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.hrtf_intensity),
                    displayValueText = String.format("%.0f%%", intensity * 100),
                    onValueChange = { convertAudioEditViewModel.updateHrtfIntensity(it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.initHrtfValues() },
                    currentValue = intensity,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.hrtf_azimuth),
                    displayValueText = String.format("%d°", azimuth),
                    onValueChange = { convertAudioEditViewModel.updateHrtfAzimuth(it.toInt()) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.initHrtfValues() },
                    currentValue = azimuth.toFloat(),
                    valueRange = 0f..180f
                )
            }
        }
    }
}
