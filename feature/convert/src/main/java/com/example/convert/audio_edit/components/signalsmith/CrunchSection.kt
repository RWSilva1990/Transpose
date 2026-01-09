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
fun CrunchSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isCrunchEnabled.collectAsState()
    val driveDb by convertAudioEditViewModel.crunchDriveDb.collectAsState()
    val fuzz by convertAudioEditViewModel.crunchFuzz.collectAsState()
    val toneHz by convertAudioEditViewModel.crunchToneHz.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsCrunchEnabled() },
            onInitButton = { convertAudioEditViewModel.initCrunchValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.crunch_drive),
                    displayValueText = String.format("%.1f dB", driveDb),
                    onValueChange = { convertAudioEditViewModel.updateCrunchDriveDb(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setCrunchParams() },
                    onReset = { convertAudioEditViewModel.initCrunchValues() },
                    currentValue = driveDb,
                    valueRange = -12f..40f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.crunch_fuzz),
                    displayValueText = String.format("%.0f%%", fuzz * 100),
                    onValueChange = { convertAudioEditViewModel.updateCrunchFuzz(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setCrunchParams() },
                    onReset = { convertAudioEditViewModel.initCrunchValues() },
                    currentValue = fuzz,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.crunch_tone),
                    displayValueText = String.format("%.0f Hz", toneHz),
                    onValueChange = { convertAudioEditViewModel.updateCrunchToneHz(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setCrunchParams() },
                    onReset = { convertAudioEditViewModel.initCrunchValues() },
                    currentValue = toneHz,
                    valueRange = 100f..20000f
                )
            }
        }
    }
}
