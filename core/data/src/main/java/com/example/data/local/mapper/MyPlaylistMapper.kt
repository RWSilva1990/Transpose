package com.example.data.local.mapper

import com.example.data.local.database.entity.PlaylistEntity
import com.example.data.local.database.entity.VideoEntity
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.stream.StreamType

object MyPlaylistMapper {

    fun toMyPlaylistItems(playlistEntities: List<PlaylistEntity>): List<MyPlaylist> {
        return playlistEntities.map {
            MyPlaylist(
                playlistId = it.playlistId,
                name = it.name
            )
        }
    }

    fun toVideos(videoEntities: List<VideoEntity>): List<Video> {
        return videoEntities.map { videoEntity ->
            toVideo(videoEntity)
        }
    }

    private fun toVideo(videoEntity: VideoEntity): Video {

        return Video(
            id = videoEntity.id,
            title = videoEntity.title,
            description = videoEntity.description,
            publishTimestamp = videoEntity.publishTimestamp,
            thumbnailUrl = videoEntity.thumbnailUrl,
            uploaderName = videoEntity.uploaderName,
            uploaderUrl = videoEntity.uploaderUrl,
            uploaderAvatarUrl = videoEntity.uploaderAvatarUrl,
            uploaderVerified = videoEntity.uploaderVerified,
            duration = videoEntity.duration,
            viewCount = videoEntity.viewCount,
            textualUploadDate = videoEntity.textualUploadDate,
            streamType = StreamType.VIDEO_STREAM.name,
            shortFormContent = videoEntity.shortFormContent,
            infoType = "Stream"
        )
    }

    fun toVideoEntity(video: Video, playlistId: Long): VideoEntity {
        return VideoEntity(
            id = video.id,
            playlistId = playlistId,
            title = video.title,
            description = video.description,
            publishTimestamp = video.publishTimestamp,
            thumbnailUrl = video.thumbnailUrl,
            uploaderName = video.uploaderName,
            uploaderUrl = video.uploaderUrl,
            uploaderAvatarUrl = video.uploaderAvatarUrl,
            uploaderVerified = video.uploaderVerified ?: false,
            duration = video.duration,
            viewCount = video.viewCount,
            textualUploadDate = video.textualUploadDate,
            streamType = StreamType.VIDEO_STREAM.name,
            shortFormContent = video.shortFormContent
        )
    }

    fun toVideoEntity(video: VideoDetail, playlistId: Long): VideoEntity {
        return VideoEntity(
            id = video.id,
            playlistId = playlistId,
            title = video.title,
            description = video.description,
            publishTimestamp = video.publishTimestamp,
            thumbnailUrl = video.thumbnailUrl,
            uploaderName = video.uploaderName,
            uploaderUrl = video.uploaderId,
            uploaderAvatarUrl = video.uploaderAvatarUrl,
            uploaderVerified = video.uploaderSubscriberCount != null,
            duration = 0,
            viewCount = video.viewCount ?: 0,
            textualUploadDate = video.publishedTimeText,
            streamType = StreamType.VIDEO_STREAM.name,
            shortFormContent = false
        )
    }


}