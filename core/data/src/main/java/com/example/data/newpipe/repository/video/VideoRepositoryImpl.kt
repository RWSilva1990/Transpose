package com.example.data.newpipe.repository.video

import com.example.data.di.IoDispatcher
import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.data.newpipe.repository.base.NewPipeManager
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.domain.repository.VideoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor(
    private val newPipeManager: NewPipeManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : VideoRepository {

    override suspend fun fetchVideoDetail(videoId: String): Result<VideoDetail> {
        return withContext(ioDispatcher){
            try {
                val extractor = newPipeManager.getStreamExtractor(videoId)
                extractor.fetchPage()
                val uploaderId = newPipeManager.getChannelId(extractor.uploaderUrl)

                Result.success(
                    InfoItemMapper.streamExtractorToVideoDetail(extractor, uploaderId)
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}