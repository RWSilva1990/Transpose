package com.example.convert.audio_edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.convert.R
import com.example.convert.audio_edit.ConvertAudioEditViewModel
import com.example.convert.audio_edit.components.bassboost.BassBoostSection
import com.example.convert.audio_edit.components.equalizer.EqualizerSection
import com.example.convert.audio_edit.components.loudness_enhancer.LoudnessEnhancerSection
import com.example.convert.audio_edit.components.pitch.PitchSection
import com.example.convert.audio_edit.components.reverb.ReverbSection
import com.example.convert.audio_edit.components.signalsmith.AutowahSection
import com.example.convert.audio_edit.components.signalsmith.ChorusSection
import com.example.convert.audio_edit.components.signalsmith.CompressorSection
import com.example.convert.audio_edit.components.signalsmith.CrunchSection
import com.example.convert.audio_edit.components.signalsmith.DecimatorSection
import com.example.convert.audio_edit.components.signalsmith.EqSection
import com.example.convert.audio_edit.components.signalsmith.FlangerSection
import com.example.convert.audio_edit.components.signalsmith.LimiterSection
import com.example.convert.audio_edit.components.signalsmith.PhaserSection
import com.example.convert.audio_edit.components.signalsmith.SignalsmithReverbSection
import com.example.convert.audio_edit.components.signalsmith.SignalsmithVirtualizerSection
import com.example.convert.audio_edit.components.signalsmith.TremoloSection
import com.example.convert.audio_edit.components.tempo.TempoSection
import com.example.convert.audio_edit.components.virtualizer.VirtualizerSection
import com.example.transpose.core.ui.R as CoreUiR
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertAudioEditScreen(
    bottomSheetState: SheetState,
    convertAudioEditViewModel: ConvertAudioEditViewModel,
    navigateToBack: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    BackHandler(
        enabled = bottomSheetState.currentValue == SheetValue.Expanded
    ) {
        coroutineScope.launch {
            bottomSheetState.partialExpand()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        PitchSection(
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        TempoSection(
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        Spacer(modifier = Modifier.height(10.dp))

        EqualizerSection(
            title = stringResource(id = R.string.equalizer_text),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        Spacer(modifier = Modifier.height(10.dp))
        ReverbSection(
            title = stringResource(id = R.string.preset_reverb_text),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        BassBoostSection(
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        LoudnessEnhancerSection(
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        VirtualizerSection(
            convertAudioEditViewModel = convertAudioEditViewModel
        )

        Spacer(modifier = Modifier.height(10.dp))
        ChorusSection(
            title = stringResource(id = CoreUiR.string.chorus_text),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        LimiterSection(
            title = stringResource(id = CoreUiR.string.limiter_text),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        SignalsmithReverbSection(
            title = stringResource(id = CoreUiR.string.signalsmith_reverb_text),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        CrunchSection(
            title = stringResource(id = CoreUiR.string.crunch_text),
            convertAudioEditViewModel = convertAudioEditViewModel
        )

        Spacer(modifier = Modifier.height(10.dp))
        EqSection(
            title = stringResource(id = CoreUiR.string.eq_title),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        CompressorSection(
            title = stringResource(id = CoreUiR.string.compressor_title),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        SignalsmithVirtualizerSection(
            title = stringResource(id = CoreUiR.string.hrtf_virtualizer_title),
            convertAudioEditViewModel = convertAudioEditViewModel
        )

        Spacer(modifier = Modifier.height(10.dp))
        PhaserSection(
            title = stringResource(id = CoreUiR.string.phaser_title),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        FlangerSection(
            title = stringResource(id = CoreUiR.string.flanger_title),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        TremoloSection(
            title = stringResource(id = CoreUiR.string.tremolo_title),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        AutowahSection(
            title = stringResource(id = CoreUiR.string.autowah_title),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
        DecimatorSection(
            title = stringResource(id = CoreUiR.string.decimator_title),
            convertAudioEditViewModel = convertAudioEditViewModel
        )
    }


}

