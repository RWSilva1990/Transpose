package com.example.main.components.bottomsheet.item

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.model.playable.PlayableItem
import com.example.main.components.bottomsheet.state.VideoDetailUiState
import com.example.util.constants.AppColors

@Composable
fun PlayerLoadingIndicator(
    videoDetailUiState: VideoDetailUiState,
    currentItem: PlayableItem?,
    modifier: Modifier = Modifier
) {
    if (currentItem is PlayableItem.Local) return

    when (val state = videoDetailUiState){
        is VideoDetailUiState.Loading -> {
            CircularProgressIndicator(
                modifier = modifier,
                color = AppColors.BlueBackground
            )
        }
        is VideoDetailUiState.Success -> {
        }
        is VideoDetailUiState.Error -> {
        }
    }
}
