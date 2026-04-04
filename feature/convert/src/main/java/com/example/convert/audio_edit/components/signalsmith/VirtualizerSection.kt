package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.ExpandableSectionTitle
import com.example.transpose.core.ui.R
import kotlin.math.abs

// Direction presets for easy selection
private enum class DirectionPreset(val azimuth: Int, val label: String) {
    FRONT(0, "Front"),
    LEFT(-90, "Left"),
    RIGHT(90, "Right"),
    BEHIND(180, "Behind"),
    FRONT_LEFT(-45, "F-Left"),
    FRONT_RIGHT(45, "F-Right"),
    BACK_LEFT(-135, "B-Left"),
    BACK_RIGHT(135, "B-Right")
}

@Composable
fun SignalsmithVirtualizerSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isHrtfEnabled.collectAsStateWithLifecycle()
    val intensity by convertAudioEditViewModel.hrtfIntensity.collectAsStateWithLifecycle()
    val azimuth by convertAudioEditViewModel.hrtfAzimuth.collectAsStateWithLifecycle()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

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

                // Direction preset buttons
                DirectionPresetSection(
                    currentAzimuth = azimuth,
                    onDirectionSelected = { convertAudioEditViewModel.updateHrtfAzimuth(it) }
                )

                // Fine-tune slider (optional, for advanced users)
                FloatSliderSection(
                    title = stringResource(id = R.string.hrtf_azimuth),
                    displayValueText = formatDirectionText(azimuth),
                    onValueChange = { convertAudioEditViewModel.updateHrtfAzimuth(it.toInt()) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.initHrtfValues() },
                    currentValue = azimuth.toFloat(),
                    valueRange = -180f..180f
                )
            }
        }
    }
}

@Composable
private fun DirectionPresetSection(
    currentAzimuth: Int,
    onDirectionSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Direction Preset",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Main directions (Front, Left, Right, Behind)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                DirectionPreset.FRONT,
                DirectionPreset.LEFT,
                DirectionPreset.RIGHT,
                DirectionPreset.BEHIND
            ).forEach { preset ->
                DirectionButton(
                    label = preset.label,
                    isSelected = currentAzimuth == preset.azimuth,
                    onClick = { onDirectionSelected(preset.azimuth) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Diagonal directions (optional row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                DirectionPreset.FRONT_LEFT,
                DirectionPreset.FRONT_RIGHT,
                DirectionPreset.BACK_LEFT,
                DirectionPreset.BACK_RIGHT
            ).forEach { preset ->
                DirectionButton(
                    label = preset.label,
                    isSelected = currentAzimuth == preset.azimuth,
                    onClick = { onDirectionSelected(preset.azimuth) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DirectionButton(
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
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White
    ) {
        Text(
            text = label,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 4.dp),
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray
        )
    }
}

/**
 * Format azimuth value as intuitive direction text
 * -180 = Behind, -90 = Left, 0 = Front, +90 = Right, +180 = Behind
 */
private fun formatDirectionText(azimuth: Int): String {
    return when {
        azimuth == 0 -> "Front"
        abs(azimuth) == 180 -> "Behind"
        azimuth in -179..-91 -> "${180 + azimuth}° Back-Left"
        azimuth in -90..-1 -> "${abs(azimuth)}° Left"
        azimuth in 1..90 -> "${azimuth}° Right"
        azimuth in 91..179 -> "${180 - azimuth}° Back-Right"
        else -> "${azimuth}°"
    }
}
