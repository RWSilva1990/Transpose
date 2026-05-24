package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.common.EffectCard
import com.example.transpose.core.ui.R
import com.example.util.constants.AppColors

@Composable
fun VocalRemovalSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isVocalRemovalEnabled.collectAsState()
    val isSupported by convertAudioEditViewModel.isVocalRemovalSupported.collectAsState()
    val mix by convertAudioEditViewModel.vocalRemovalMix.collectAsState()
    val isVocalOnlyMode by convertAudioEditViewModel.isVocalOnlyMode.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val vocalOnlyInteractionSource = remember { MutableInteractionSource() }

    val toggle = {
        isExpanded = !isExpanded
        onExpandChanged(isExpanded)
    }

    EffectCard(modifier = modifier, onClick = toggle) {
        VocalRemovalHeader(
            title = title,
            isExpanded = isExpanded,
            isEnabled = isEnabled,
            isSupported = isSupported,
            onToggleEnable = { convertAudioEditViewModel.updateIsVocalRemovalEnabled() },
            onReset = { convertAudioEditViewModel.initVocalRemovalValues() },
        )

        Text(
            text = stringResource(
                id = if (isSupported) {
                    R.string.vocal_removal_foreground_notice
                } else {
                    R.string.vocal_removal_unsupported_notice
                }
            ),
            fontSize = 12.sp,
            color = if (isSupported) {
                AppColors.BlueBackground.copy(alpha = 0.86f)
            } else {
                AppColors.DescriptionColor
            },
            lineHeight = 16.sp,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        if (isExpanded) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text(
                    text = stringResource(
                        id = if (isSupported) {
                            R.string.vocal_removal_background_notice
                        } else {
                            R.string.vocal_removal_unsupported_detail
                        }
                    ),
                    fontSize = 12.sp,
                    color = AppColors.BlueBackground.copy(alpha = 0.86f),
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                FloatSliderSection(
                    title = stringResource(id = R.string.vocal_removal_mix),
                    displayValueText = String.format("%.0f%%", mix * 100),
                    onValueChange = { convertAudioEditViewModel.updateVocalRemovalMix(it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.initVocalRemovalValues() },
                    currentValue = mix,
                    valueRange = 0f..1f,
                    enabled = isSupported
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = vocalOnlyInteractionSource,
                            indication = null,
                            enabled = isSupported,
                            onClick = { convertAudioEditViewModel.updateIsVocalOnlyMode() }
                        )
                        .padding(start = 15.dp, end = 15.dp, top = 0.dp, bottom = 6.dp)
                ) {
                    Checkbox(
                        checked = isVocalOnlyMode,
                        onCheckedChange = null,
                        enabled = isSupported,
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppColors.BlueBackground,
                            uncheckedColor = AppColors.DescriptionColor,
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        text = stringResource(id = R.string.vocal_removal_vocal_only),
                        fontSize = 13.sp,
                        color = if (isSupported) AppColors.BlueBackground else AppColors.DescriptionColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun VocalRemovalHeader(
    title: String,
    isExpanded: Boolean,
    isEnabled: Boolean,
    isSupported: Boolean,
    onToggleEnable: () -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = AppColors.DescriptionColor,
            )
        }
        Switch(
            checked = isEnabled,
            enabled = isSupported,
            onCheckedChange = { onToggleEnable() },
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = AppColors.DescriptionColor,
                uncheckedBorderColor = AppColors.DescriptionColor,
            ),
        )
        IconButton(onClick = onReset, enabled = isSupported) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reset $title",
                tint = AppColors.DescriptionColor,
            )
        }
    }
}
