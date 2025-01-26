package com.example.data.newpipe.mapper

import com.example.data.newpipe.utils.NewPipeUtils
import com.example.domain.model.youtube.playlist.Playlist
import com.example.domain.model.youtube.playlist.PlaylistItem
import com.example.domain.model.youtube.search.SearchResult
import com.example.domain.model.youtube.video.BasicVideoData
import com.example.domain.model.youtube.video_detail.VideoDetailData
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

object InfoItemMapper {

    // SearchResult.Video
    fun streamInfoItemToDomain(item: StreamInfoItem, id: String): SearchResult.Video {
        return SearchResult.Video(
            basicVideoData = BasicVideoData(
                id = id,
                title = item.name,
                description = item.shortDescription ?: "",
                publishTimestamp = item.uploadDate?.date()?.time?.time,
                thumbnailUrl = NewPipeUtils.getHighestResolutionThumbnail(item.thumbnails.firstOrNull()?.url),
                infoType = item.infoType,
                uploaderName = item.uploaderName,
                uploaderUrl = item.uploaderUrl,
                uploaderAvatars = item.uploaderAvatars,
                uploaderVerified = item.isUploaderVerified,
                duration = item.duration,
                viewCount = item.viewCount,
                textualUploadDate = item.textualUploadDate,
                streamType = item.streamType,
                shortFormContent = item.isShortFormContent
            ),
        )
    }

    fun streamInfoItemTo(item: StreamInfoItem, id: String): PlaylistItem {
        return PlaylistItem(
            basicVideoData = BasicVideoData(
                id = id,
                title = item.name,
                description = item.shortDescription ?: "",
                publishTimestamp = item.uploadDate?.date()?.time?.time,
                thumbnailUrl = NewPipeUtils.getHighestResolutionThumbnail(item.thumbnails.firstOrNull()?.url),
                infoType = item.infoType,
                uploaderName = item.uploaderName,
                uploaderUrl = item.uploaderUrl,
                uploaderAvatars = item.uploaderAvatars,
                uploaderVerified = item.isUploaderVerified,
                duration = item.duration,
                viewCount = item.viewCount,
                textualUploadDate = item.textualUploadDate,
                streamType = item.streamType,
                shortFormContent = item.isShortFormContent
            ),
        )
    }

    // SearchResult.Playlist
    fun playlistInfoItemToDomain(item: PlaylistInfoItem, id: String): SearchResult.Playlist {
        return SearchResult.Playlist(
            id = id,
            title = item.name,
            description = item.description?.content ?: "",
            publishTimestamp = null,
            thumbnailUrl = NewPipeUtils.getHighestResolutionThumbnail(item.thumbnails.firstOrNull()?.url),
            infoType = item.infoType,
            uploaderName = item.uploaderName ?: "",
            uploaderUrl = item.uploaderUrl,
            uploaderVerified = item.isUploaderVerified,
            streamCount = item.streamCount,
            playlistType = item.playlistType
        )
    }
    // SearchResult.Channel
    fun channelInfoItemToDomain(item: ChannelInfoItem, id: String): SearchResult.Channel {
        return SearchResult.Channel(
            id = id,
            title = item.name,
            description = item.description ?: "",
            publishTimestamp = null,
            thumbnailUrl = NewPipeUtils.getHighestResolutionThumbnail(item.thumbnails.firstOrNull()?.url),
            infoType = item.infoType,
            subscriberCount = item.subscriberCount,
            streamCount = item.streamCount,
            verified = item.isVerified
        )
    }

    // Playlist
    fun playlistExtractorToDomain(extractor: PlaylistExtractor): Playlist {
        return Playlist(
            id = extractor.id,
            title = extractor.name,
            description = extractor.description.content,
            publishTimestamp = null,
            thumbnailUrl = NewPipeUtils.getHighestResolutionThumbnail(extractor.thumbnails.firstOrNull()?.url),
            infoType = InfoItem.InfoType.PLAYLIST,
            uploaderName = extractor.uploaderName,
            uploaderUrl = extractor.uploaderUrl,
            uploaderVerified = false,
            streamCount = extractor.streamCount,
            playlistType = null
        )
    }

    fun streamExtractorToVideoDetail(extractor: StreamExtractor): VideoDetailData{
        return VideoDetailData(
            id = extractor.id,
            title = extractor.name,
            videoStream = extractor.videoStreams.firstOrNull(),
            description = extractor.description.content,
            thumbnailUrl = NewPipeUtils.getHighestResolutionThumbnail(extractor.thumbnails.firstOrNull()?.url),
            uploaderName = extractor.uploaderName,
            uploaderUrl = extractor.uploaderUrl,
            uploaderAvatars = extractor.uploaderAvatars,
            uploaderSubscriberCount = extractor.uploaderSubscriberCount,
            publishTimestamp = extractor.uploadDate?.date()?.time?.time,
            publishedTimeText = extractor.textualUploadDate,
            viewCount = extractor.viewCount,
            likeCount = extractor.likeCount,
            dislikeCount = extractor.dislikeCount,
            relatedVideos = extractor.relatedItems?.items?.map {
                val relatedVideo = it as? StreamInfoItem
                BasicVideoData(
                    id = relatedVideo?.url ?: "",
                    title = relatedVideo?.name ?: "",
                    description = relatedVideo?.shortDescription ?: "",
                    publishTimestamp = relatedVideo?.uploadDate?.date()?.time?.time,
                    thumbnailUrl = NewPipeUtils.getHighestResolutionThumbnail(relatedVideo?.thumbnails?.firstOrNull()?.url),
                    infoType = relatedVideo?.infoType ?: InfoItem.InfoType.STREAM,
                    uploaderName = relatedVideo?.uploaderName,
                    uploaderUrl = relatedVideo?.uploaderUrl,
                    uploaderAvatars = relatedVideo?.uploaderAvatars,
                    uploaderVerified = relatedVideo?.isUploaderVerified,
                    duration = relatedVideo?.duration ?: 0,
                    viewCount = relatedVideo?.viewCount ?: 0,
                    textualUploadDate = relatedVideo?.textualUploadDate ?: "",
                    streamType = relatedVideo?.streamType,
                    shortFormContent = relatedVideo?.isShortFormContent ?: false
                )
            } ?: emptyList()

        )
    }
}