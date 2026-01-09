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
fun SignalsmithReverbSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isSignalsmithReverbEnabled.collectAsState()
    val dry by convertAudioEditViewModel.signalsmithReverbDry.collectAsState()
    val wet by convertAudioEditViewModel.signalsmithReverbWet.collectAsState()
    val roomMs by convertAudioEditViewModel.signalsmithReverbRoomMs.collectAsState()
    val decaySec by convertAudioEditViewModel.signalsmithReverbDecaySec.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsSignalsmithReverbEnabled() },
            onInitButton = { convertAudioEditViewModel.initSignalsmithReverbValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_dry),
                    displayValueText = String.format("%.2f", dry),
                    onValueChange = { convertAudioEditViewModel.updateSignalsmithReverbDry(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setSignalsmithReverbParams() },
                    onReset = { convertAudioEditViewModel.initSignalsmithReverbValues() },
                    currentValue = dry,
                    valueRange = 0f..4f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_wet),
                    displayValueText = String.format("%.2f", wet),
                    onValueChange = { convertAudioEditViewModel.updateSignalsmithReverbWet(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setSignalsmithReverbParams() },
                    onReset = { convertAudioEditViewModel.initSignalsmithReverbValues() },
                    currentValue = wet,
                    valueRange = 0f..4f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_room_size),
                    displayValueText = String.format("%.0f ms", roomMs),
                    onValueChange = { convertAudioEditViewModel.updateSignalsmithReverbRoomMs(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setSignalsmithReverbParams() },
                    onReset = { convertAudioEditViewModel.initSignalsmithReverbValues() },
                    currentValue = roomMs,
                    valueRange = 10f..200f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_decay),
                    displayValueText = String.format("%.2f sec", decaySec),
                    onValueChange = { convertAudioEditViewModel.updateSignalsmithReverbDecaySec(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setSignalsmithReverbParams() },
                    onReset = { convertAudioEditViewModel.initSignalsmithReverbValues() },
                    currentValue = decaySec,
                    valueRange = 0.01f..30f
                )
            }
        }
    }
}
