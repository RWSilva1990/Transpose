package com.example.main.components.bottomsheet.item

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.playable.PlayableItem
import com.example.main.components.bottomsheet.state.VideoDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoInfoHeader(
    currentItem: PlayableItem?,
    videoDetailUiState: VideoDetailUiState,
    myPlaylists: List<MyPlaylist>,
    pitchValue: Int,
    tempoValue: Int,
    onPitchPlusOne: () -> Unit,
    onPitchMinusOne: () -> Unit,
    onPitchInit: () -> Unit,
    onTempoPlusOne: () -> Unit,
    onTempoMinusOne: () -> Unit,
    onTempoInit: () -> Unit,
    onAddItemToPlaylist: (PlayableItem, Long) -> Unit,
    onNavigateToChannelScreen: (String) -> Unit,
    bottomSheetState: SheetState
) {
    Column {
        VideoInfoSection(currentItem = currentItem)
        ChannelSection(
            videoDetailUiState = videoDetailUiState,
            currentItem = currentItem,
            myPlaylists = myPlaylists,
            onAddItemToPlaylist = onAddItemToPlaylist,
            onNavigateToChannelScreen = onNavigateToChannelScreen,
            bottomSheetState = bottomSheetState
        )
        PitchControlItem(
            pitchValue = pitchValue,
            onPlusOne = onPitchPlusOne,
            onMinusOne = onPitchMinusOne,
            onInit = onPitchInit
        )
        TempoControlItem(
            tempoValue = tempoValue,
            onPlusOne = onTempoPlusOne,
            onMinusOne = onTempoMinusOne,
            onInit = onTempoInit
        )
    }
}
