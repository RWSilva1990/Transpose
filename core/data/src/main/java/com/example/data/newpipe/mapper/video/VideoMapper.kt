package com.example.data.newpipe.mapper.video

import com.example.data.newpipe.mapper.base.BaseMapper
import com.example.domain.model.youtube.channel.ChannelTabResult
import com.example.domain.model.youtube.search.SearchResult
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import com.example.util.Logger
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem

object VideoMapper {

    fun streamInfoItemToSearchResultVideo(
        item: StreamInfoItem,
        videoId: String,
        uploaderId: String
    ): SearchResult.VideoResult {
        return SearchResult.VideoResult(
            video = streamInfoItemToBasicVideoData(item, videoId, uploaderId)
        )
    }

    fun streamInfoItemToChannelTabResultVideo(
        item: StreamInfoItem,
        videoId: String,
        uploaderId: String
    ): ChannelTabResult.VideoResult {
        return ChannelTabResult.VideoResult(
            video = streamInfoItemToBasicVideoData(item, videoId, uploaderId)
        )
    }

    fun streamInfoItemToChannelTabResultShorts(
        item: StreamInfoItem,
        videoId: String,
        uploaderId: String
    ): ChannelTabResult.ShortsResult {
        return ChannelTabResult.ShortsResult(
            video = streamInfoItemToBasicVideoData(item, videoId, uploaderId)
        )
    }


    fun streamInfoItemToBasicVideoData(
        item: StreamInfoItem,
        videoId: String,
        uploaderId: String
    ): Video {
        return Video(
            id = videoId,
            title = item.name,
            description = item.shortDescription ?: "",
            publishTimestamp = item.uploadDate?.offsetDateTime()?.toInstant()?.toEpochMilli(),
            thumbnailUrl = BaseMapper.getHighestResThumbnail(item.thumbnails.firstOrNull()?.url),
            infoType = "Stream",
            uploaderName = item.uploaderName,
            uploaderUrl = uploaderId,
            uploaderAvatarUrl = item.uploaderAvatars.firstOrNull()?.url,
            uploaderVerified = item.isUploaderVerified,
            duration = item.duration,
            viewCount = item.viewCount,
            textualUploadDate = item.textualUploadDate,
            streamType = item.streamType?.name,
            shortFormContent = item.isShortFormContent
        )
    }

    fun streamExtractorToVideoDetail(
        extractor: StreamExtractor,
        uploaderId: String,
        video: Video
    ): VideoDetail {
        val videoStreams = extractor.videoStreams
        val videoOnlyUrls = extractor.videoOnlyStreams.toList()
        val audioOnlyUrls = extractor.audioStreams.toList()
        Logger.d("VideoMapper HLS URL: ${extractor.hlsUrl}")

        return VideoDetail(
            id = extractor.id,
            title = extractor.name,
            videoStreamContent = videoStreams.firstOrNull()?.content,
            videoOnlyStreams = videoOnlyUrls,
            audioOnlyStreams = audioOnlyUrls,
            duration = video.duration,
            description = extractor.description.content,
            thumbnailUrl = BaseMapper.getHighestResThumbnail(extractor.thumbnails.firstOrNull()?.url),
            uploaderName = extractor.uploaderName,
            uploaderId = uploaderId,
            uploaderAvatarUrl = extractor.uploaderAvatars.first().url,
            uploaderSubscriberCount = extractor.uploaderSubscriberCount,
            publishTimestamp = extractor.uploadDate?.offsetDateTime()?.toInstant()?.toEpochMilli(),
            publishedTimeText = extractor.textualUploadDate,
            viewCount = extractor.viewCount,
            likeCount = extractor.likeCount,
            dislikeCount = extractor.dislikeCount,
            relatedVideos = extractor.relatedItems?.items?.map {
                val relatedVideo = it as? StreamInfoItem
                Video(
                    id = relatedVideo?.url ?: "",
                    title = relatedVideo?.name ?: "",
                    description = relatedVideo?.shortDescription ?: "",
                    publishTimestamp = relatedVideo?.uploadDate?.offsetDateTime()?.toInstant()?.toEpochMilli(),
                    thumbnailUrl = BaseMapper.getHighestResThumbnail(relatedVideo?.thumbnails?.firstOrNull()?.url),
                    infoType = "Stream",
                    uploaderName = relatedVideo?.uploaderName,
                    uploaderUrl = relatedVideo?.uploaderUrl,
                    uploaderAvatarUrl = relatedVideo?.uploaderAvatars?.first()?.url,
                    uploaderVerified = relatedVideo?.isUploaderVerified,
                    duration = relatedVideo?.duration ?: 0,
                    viewCount = relatedVideo?.viewCount ?: 0,
                    textualUploadDate = relatedVideo?.textualUploadDate ?: "",
                    streamType = relatedVideo?.streamType?.name,
                    shortFormContent = relatedVideo?.isShortFormContent ?: false
                )
            } ?: emptyList()
        )
    }
}