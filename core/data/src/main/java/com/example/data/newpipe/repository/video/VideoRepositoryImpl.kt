package com.example.data.newpipe.repository.video

import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.data.newpipe.repository.base.BaseNewPipeRepository
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.domain.repository.VideoRepository
import com.example.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor() : BaseNewPipeRepository(), VideoRepository {
    private val _currentVideoDetail = MutableStateFlow<VideoDetail?>(null)
    override val currentVideoDetail: StateFlow<VideoDetail?> get() = _currentVideoDetail

    override suspend fun fetchVideoDetail(videoId: String) {
        try {
            val extractor = getStreamExtractor(videoId)
            extractor.fetchPage()
            val uploaderId = getChannelId(extractor.uploaderUrl)
            _currentVideoDetail.value = InfoItemMapper.streamExtractorToVideoDetail(extractor, uploaderId)
        }
        catch (e: Exception){
            Logger.d("VideoRepositoryImpl fetchVideoDetail fail: ${e.message}")
        }


    }
}