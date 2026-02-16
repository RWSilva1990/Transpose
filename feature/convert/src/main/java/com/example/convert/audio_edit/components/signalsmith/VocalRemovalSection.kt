package com.example.convert.audio_edit.components.signalsmith

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.ExpandableSectionTitle
import com.example.transpose.core.ui.R
import com.example.util.constants.AppColors
import java.util.Locale

@Composable
fun VocalRemovalSection(
    modifier: Modifier = Modifier,
    title: String,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    onExpandChanged: (Boolean) -> Unit = {}
) {
    val isEnabled by convertAudioEditViewModel.isVocalRemovalEnabled.collectAsState()
    val mix by convertAudioEditViewModel.vocalRemovalMix.collectAsState()
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val isKorean = Locale.getDefault().language == "ko"

    val effectDescription = if (isKorean) {
        "AI 모델로 보컬을 실시간 제거합니다. 슬라이더로 제거량을 조절하세요."
    } else {
        "Removes vocals in real-time using AI. Adjust the slider to control reduction."
    }

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
            onSwitchChange = { convertAudioEditViewModel.updateIsVocalRemovalEnabled() },
            onInitButton = { convertAudioEditViewModel.initVocalRemovalValues() }
        )

        AnimatedVisibility(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            visible = isExpanded,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = effectDescription,
                    fontSize = 12.sp,
                    color = AppColors.BlueBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FloatSliderSection(
                    title = stringResource(id = R.string.vocal_removal_mix),
                    displayValueText = String.format("%.0f%%", mix * 100),
                    onValueChange = { convertAudioEditViewModel.updateVocalRemovalMix(it) },
                    onValueChangeFinished = { },
                    onReset = { convertAudioEditViewModel.initVocalRemovalValues() },
                    currentValue = mix,
                    valueRange = 0f..1f
                )
            }
        }
    }
}
