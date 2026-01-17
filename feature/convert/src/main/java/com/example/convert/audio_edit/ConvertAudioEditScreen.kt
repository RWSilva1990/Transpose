package com.example.convert.audio_edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.convert.R
import com.example.convert.audio_edit.components.bassboost.BassBoostSection
import com.example.convert.audio_edit.components.equalizer.EqualizerSection
import com.example.convert.audio_edit.components.loudness_enhancer.LoudnessEnhancerSection
import com.example.convert.audio_edit.components.pitch.PitchSection
import com.example.convert.audio_edit.components.reverb.ReverbSection
import com.example.convert.audio_edit.components.signalsmith.ChorusSection
import com.example.convert.audio_edit.components.signalsmith.CompressorSection
import com.example.convert.audio_edit.components.signalsmith.EqSection
import com.example.convert.audio_edit.components.signalsmith.LimiterSection
import com.example.convert.audio_edit.components.signalsmith.SignalsmithReverbSection
import com.example.convert.audio_edit.components.signalsmith.SignalsmithVirtualizerSection
import com.example.convert.audio_edit.components.signalsmith.StereoWidenerSection
import com.example.convert.audio_edit.components.tempo.TempoSection
import com.example.convert.audio_edit.components.virtualizer.VirtualizerSection
import com.example.transpose.core.ui.R as CoreUiR
import kotlinx.coroutines.launch

// Section indices for auto-scroll (only for bottom sections)
private const val SECTION_STEREO_WIDENER = 13

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertAudioEditScreen(
    bottomSheetState: SheetState,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    navigateToBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }

    // Scroll helper function - scrolls to show expanded content
    val scrollToSection: (Int) -> Unit = remember(lazyListState) {
        { sectionIndex ->
            coroutineScope.launch {
                // Animate scroll to the section with some offset to show content below
                lazyListState.animateScrollToItem(
                    index = sectionIndex,
                    scrollOffset = -50 // Negative offset to show some space above
                )
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState
    ) {
        // Pitch & Tempo (always visible, no expand)
        item(key = "pitch") {
            PitchSection(
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "tempo") {
            TempoSection(
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "spacer1") {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Android Audio Effects
        item(key = "equalizer") {
            EqualizerSection(
                title = stringResource(id = R.string.equalizer_text),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "spacer2") {
            Spacer(modifier = Modifier.height(10.dp))
        }

        item(key = "reverb") {
            ReverbSection(
                title = stringResource(id = R.string.preset_reverb_text),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "bassboost") {
            BassBoostSection(
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "loudness") {
            LoudnessEnhancerSection(
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "virtualizer") {
            VirtualizerSection(
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "spacer3") {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Signalsmith Effects - Group 1
        item(key = "chorus") {
            ChorusSection(
                title = stringResource(id = CoreUiR.string.chorus_text),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "limiter") {
            LimiterSection(
                title = stringResource(id = CoreUiR.string.limiter_text),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "signalsmith_reverb") {
            SignalsmithReverbSection(
                title = stringResource(id = CoreUiR.string.signalsmith_reverb_text),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "spacer4") {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Signalsmith Effects - Group 2
        item(key = "eq") {
            EqSection(
                title = stringResource(id = CoreUiR.string.eq_title),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "compressor") {
            CompressorSection(
                title = stringResource(id = CoreUiR.string.compressor_title),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "hrtf") {
            SignalsmithVirtualizerSection(
                title = stringResource(id = CoreUiR.string.hrtf_virtualizer_title),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "spacer5") {
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Headphone Effects
        item(key = "stereo_widener") {
            StereoWidenerSection(
                title = stringResource(id = CoreUiR.string.stereo_widener_text),
                convertAudioEditViewModel = convertAudioEditViewModel,
                onExpandChanged = { isExpanded ->
                    if (isExpanded) scrollToSection(SECTION_STEREO_WIDENER)
                }
            )
        }

        // Bottom padding for BottomNavigation
        item(key = "spacer6") {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
