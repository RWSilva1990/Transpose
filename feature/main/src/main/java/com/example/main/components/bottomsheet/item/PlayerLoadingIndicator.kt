package com.example.main.components.bottomsheet.item

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.main.MainViewModel
import com.example.main.components.bottomsheet.state.VideoDetailUiState
import com.example.util.constants.AppColors

@Composable
fun PlayerLoadingIndicator(
    mainViewModel: MainViewModel, modifier: Modifier = Modifier
) {
    val videoDetailUiState by mainViewModel.videoDetailUiState.collectAsState()

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