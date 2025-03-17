package com.example.data.newpipe.repository.video

import com.example.data.newpipe.mapper.InfoItemMapper
import com.example.data.newpipe.repository.base.BaseNewPipeRepository
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.domain.repository.VideoRepository
import javax.inject.Inject

class VideoRepositoryImpl @Inject constructor() : BaseNewPipeRepository(), VideoRepository {

    override suspend fun fetchVideoDetail(videoId: String): Result<VideoDetail> {
        return try {
            val extractor = getStreamExtractor(videoId)
            extractor.fetchPage()
            val uploaderId = getChannelId(extractor.uploaderUrl)

            Result.success(
                InfoItemMapper.streamExtractorToVideoDetail(extractor, uploaderId)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}