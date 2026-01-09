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
fun AutowahSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isAutowahEnabled.collectAsState()
    val wah by convertAudioEditViewModel.autowahWah.collectAsState()
    val mix by convertAudioEditViewModel.autowahMix.collectAsState()
    val level by convertAudioEditViewModel.autowahLevel.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsAutowahEnabled() },
            onInitButton = { convertAudioEditViewModel.initAutowahValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.autowah_wah),
                    displayValueText = String.format("%.0f%%", wah * 100),
                    onValueChange = { convertAudioEditViewModel.updateAutowahWah(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setAutowahParams() },
                    onReset = { convertAudioEditViewModel.initAutowahValues() },
                    currentValue = wah,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.autowah_mix),
                    displayValueText = String.format("%.0f%%", mix),
                    onValueChange = { convertAudioEditViewModel.updateAutowahMix(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setAutowahParams() },
                    onReset = { convertAudioEditViewModel.initAutowahValues() },
                    currentValue = mix,
                    valueRange = 0f..100f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.autowah_level),
                    displayValueText = String.format("%.0f%%", level * 100),
                    onValueChange = { convertAudioEditViewModel.updateAutowahLevel(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setAutowahParams() },
                    onReset = { convertAudioEditViewModel.initAutowahValues() },
                    currentValue = level,
                    valueRange = 0f..1f
                )
            }
        }
    }
}
