package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.ExpandableSectionTitle
import com.example.media.audio_effect.data.reverb.SignalsmithReverbPresets
import com.example.transpose.core.ui.R
import java.util.Locale

@Composable
fun SignalsmithReverbSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isSignalsmithReverbEnabled.collectAsState()
    val currentPreset by convertAudioEditViewModel.signalsmithReverbPreset.collectAsState()
    val dry by convertAudioEditViewModel.signalsmithReverbDry.collectAsState()
    val wet by convertAudioEditViewModel.signalsmithReverbWet.collectAsState()
    val roomMs by convertAudioEditViewModel.signalsmithReverbRoomMs.collectAsState()
    val decaySec by convertAudioEditViewModel.signalsmithReverbDecaySec.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val isKorean = LocalConfiguration.current.locales[0].language == "ko"

    Column(
        modifier = modifier
            .clickable {
                isExpanded = !isExpanded
                onExpandChanged(isExpanded)
            }
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
                // Preset Selection Section
                ReverbPresetSection(
                    currentPreset = currentPreset,
                    isKorean = isKorean,
                    onPresetSelected = { convertAudioEditViewModel.updateSignalsmithReverbPreset(it) }
                )

                // Current preset indicator
                if (currentPreset >= 0) {
                    val preset = SignalsmithReverbPresets.getPreset(currentPreset)
                    Text(
                        text = if (isKorean) "현재: ${preset.nameKo}" else "Current: ${preset.name}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                } else {
                    Text(
                        text = if (isKorean) "현재: 사용자 정의" else "Current: Custom",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // Fine-tune sliders
                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_dry),
                    displayValueText = String.format(Locale.US, "%.2f", dry),
                    onValueChange = { convertAudioEditViewModel.updateSignalsmithReverbDry(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setSignalsmithReverbParams() },
                    onReset = { convertAudioEditViewModel.initSignalsmithReverbValues() },
                    currentValue = dry,
                    valueRange = 0f..4f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_wet),
                    displayValueText = String.format(Locale.US, "%.2f", wet),
                    onValueChange = { convertAudioEditViewModel.updateSignalsmithReverbWet(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setSignalsmithReverbParams() },
                    onReset = { convertAudioEditViewModel.initSignalsmithReverbValues() },
                    currentValue = wet,
                    valueRange = 0f..4f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_room_size),
                    displayValueText = String.format(Locale.US, "%.0f ms", roomMs),
                    onValueChange = { convertAudioEditViewModel.updateSignalsmithReverbRoomMs(it) },
                    onValueChangeFinished = { convertAudioEditViewModel.setSignalsmithReverbParams() },
                    onReset = { convertAudioEditViewModel.initSignalsmithReverbValues() },
                    currentValue = roomMs,
                    valueRange = 10f..200f
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.reverb_decay),
                    displayValueText = String.format(Locale.US, "%.2f sec", decaySec),
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

@Composable
private fun ReverbPresetSection(
    currentPreset: Int,
    isKorean: Boolean,
    onPresetSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (isKorean) "프리셋" else "Presets",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Row 1: Popular presets (most commonly used)
        Text(
            text = if (isKorean) "인기" else "Popular",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                SignalsmithReverbPresets.PRESET_DEFAULT,
                SignalsmithReverbPresets.PRESET_BATHROOM,
                SignalsmithReverbPresets.PRESET_STUDIO,
                SignalsmithReverbPresets.PRESET_CONCERT_HALL,
                SignalsmithReverbPresets.PRESET_CATHEDRAL
            ).forEach { presetIndex ->
                val preset = SignalsmithReverbPresets.getPreset(presetIndex)
                ReverbPresetButton(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == presetIndex,
                    onClick = { onPresetSelected(presetIndex) }
                )
            }
        }

        // Row 2: Large spaces
        Text(
            text = if (isKorean) "넓은 공간" else "Large Spaces",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                SignalsmithReverbPresets.PRESET_CHURCH,
                SignalsmithReverbPresets.PRESET_ARENA,
                SignalsmithReverbPresets.PRESET_WAREHOUSE,
                SignalsmithReverbPresets.PRESET_CAVE
            ).forEach { presetIndex ->
                val preset = SignalsmithReverbPresets.getPreset(presetIndex)
                ReverbPresetButton(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == presetIndex,
                    onClick = { onPresetSelected(presetIndex) }
                )
            }
        }

        // Row 3: Urban/Industrial
        Text(
            text = if (isKorean) "도시/산업" else "Urban/Industrial",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                SignalsmithReverbPresets.PRESET_TUNNEL,
                SignalsmithReverbPresets.PRESET_PARKING_GARAGE,
                SignalsmithReverbPresets.PRESET_SEWER,
                SignalsmithReverbPresets.PRESET_TELEPHONE
            ).forEach { presetIndex ->
                val preset = SignalsmithReverbPresets.getPreset(presetIndex)
                ReverbPresetButton(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == presetIndex,
                    onClick = { onPresetSelected(presetIndex) }
                )
            }
        }

        // Row 4: Special/Creative
        Text(
            text = if (isKorean) "특수 효과" else "Special Effects",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                SignalsmithReverbPresets.PRESET_UNDERWATER,
                SignalsmithReverbPresets.PRESET_FOREST,
                SignalsmithReverbPresets.PRESET_SPACESHIP,
                SignalsmithReverbPresets.PRESET_NONE
            ).forEach { presetIndex ->
                val preset = SignalsmithReverbPresets.getPreset(presetIndex)
                ReverbPresetButton(
                    label = if (isKorean) preset.nameKo else preset.name,
                    isSelected = currentPreset == presetIndex,
                    onClick = { onPresetSelected(presetIndex) }
                )
            }
        }
    }
}

@Composable
private fun ReverbPresetButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
    ) {
        Text(
            text = label,
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 12.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
