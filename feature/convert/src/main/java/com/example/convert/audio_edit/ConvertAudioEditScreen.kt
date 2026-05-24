package com.example.convert.audio_edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.convert.audio_edit.components.pitch.PitchSection
import com.example.convert.audio_edit.components.signalsmith.ChorusSection
import com.example.convert.audio_edit.components.signalsmith.EqSection
import com.example.convert.audio_edit.components.signalsmith.ReverbPlusSection
import com.example.convert.audio_edit.components.signalsmith.SignalsmithReverbSection
import com.example.convert.audio_edit.components.signalsmith.ToneFilterSection
import com.example.convert.audio_edit.components.signalsmith.VocalRemovalSection
import com.example.convert.audio_edit.components.tempo.TempoSection
import kotlinx.coroutines.launch
import com.example.transpose.core.ui.R as CoreUiR

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
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

        item(key = "vocal_removal") {
            VocalRemovalSection(
                title = stringResource(id = CoreUiR.string.vocal_removal_text),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "reverb_plus") {
            ReverbPlusSection(
                title = stringResource(id = CoreUiR.string.reverb_plus_text),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "signalsmith_reverb") {
            SignalsmithReverbSection(
                title = stringResource(id = CoreUiR.string.signalsmith_reverb_text),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "tone_filter") {
            ToneFilterSection(
                title = stringResource(id = CoreUiR.string.tone_filter_title),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "eq") {
            EqSection(
                title = stringResource(id = CoreUiR.string.eq_title),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }

        item(key = "chorus") {
            ChorusSection(
                title = stringResource(id = CoreUiR.string.chorus_text),
                convertAudioEditViewModel = convertAudioEditViewModel
            )
        }
    }
}
