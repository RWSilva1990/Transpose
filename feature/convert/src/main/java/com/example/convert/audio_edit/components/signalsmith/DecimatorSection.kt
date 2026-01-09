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
fun DecimatorSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
) {
    val isEnabled by convertAudioEditViewModel.isDecimatorEnabled.collectAsState()
    val bitcrush by convertAudioEditViewModel.decimatorBitcrush.collectAsState()
    val downsample by convertAudioEditViewModel.decimatorDownsample.collectAsState()
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
            onSwitchChange = { convertAudioEditViewModel.updateIsDecimatorEnabled() },
            onInitButton = { convertAudioEditViewModel.initDecimatorValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                FloatSliderSection(
                    title = stringResource(id = R.string.decimator_bitcrush),
                    displayValueText = String.format("%.0f%%", bitcrush * 100),
                    onValueChange = { convertAudioEditViewModel.updateDecimatorBitcrush(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setDecimatorParams() },
                    onReset = { convertAudioEditViewModel.initDecimatorValues() },
                    currentValue = bitcrush,
                    valueRange = 0f..1f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.decimator_downsample),
                    displayValueText = String.format("%.0f%%", downsample * 100),
                    onValueChange = { convertAudioEditViewModel.updateDecimatorDownsample(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setDecimatorParams() },
                    onReset = { convertAudioEditViewModel.initDecimatorValues() },
                    currentValue = downsample,
                    valueRange = 0f..1f
                )
            }
        }
    }
}
