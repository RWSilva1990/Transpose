package com.example.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.youtube.video.Video
import com.example.domain.repository.ChannelRepository
import com.example.domain.repository.MyPlaylistDBRepository
import com.example.domain.repository.PlaybackPreferencesRepository
import com.example.domain.repository.SuggestionKeywordRepository
import com.example.domain.repository.VideoRepository
import com.example.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainDataModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val myPlaylistDBRepository: MyPlaylistDBRepository,
    private val channelRepository: ChannelRepository,
    private val suggestionKeywordRepository: SuggestionKeywordRepository,
    private val playbackPreferencesRepository: PlaybackPreferencesRepository,
) : ViewModel() {

    private val _suggestionKeywords = MutableStateFlow<List<String>>(emptyList())
    val suggestionKeywords = _suggestionKeywords.asStateFlow()

    fun setSuggestionKeywords(query: String) = viewModelScope.launch(Dispatchers.IO) {
        val result = suggestionKeywordRepository.getSuggestionKeywords(query)
        if (result.isSuccess) {
            _suggestionKeywords.value = result.getOrNull() ?: emptyList()
        } else {
            Logger.d("Failed to fetch suggestion keywords: ${result.exceptionOrNull()}")
            _suggestionKeywords.value = emptyList()
        }
    }

    val currentVideoDetail = videoRepository.currentVideoDetail

    fun loadCurrentVideoDetail(videoId: String) = viewModelScope.launch(Dispatchers.IO) {
        videoRepository.fetchVideoDetail(videoId)
    }

    val myPlaylists = myPlaylistDBRepository.myPlaylists

    fun getAllMyPlaylists() = viewModelScope.launch(Dispatchers.IO) {
        myPlaylistDBRepository.getAllPlaylists()
    }

    fun addVideoToPlaylist(video: Video, playlistId: Long) =
        viewModelScope.launch(Dispatchers.IO) {
            myPlaylistDBRepository.addVideoToPlaylist(video, playlistId)
        }

}