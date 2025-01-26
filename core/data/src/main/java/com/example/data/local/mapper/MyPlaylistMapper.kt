package com.example.data.local.mapper

import com.example.data.local.database.entity.PlaylistEntity
import com.example.data.local.database.entity.VideoEntity
import com.example.domain.model.common.VideoItem
import com.example.domain.model.library.MyPlaylistItem
import com.example.domain.model.youtube.search.SearchResult
import com.example.domain.model.youtube.video_detail.VideoDetailData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.stream.StreamType

object MyPlaylistMapper {
    fun toMyPlaylistItem(playlistEntities: List<PlaylistEntity>): List<MyPlaylistItem> {
        val myPlaylistItems = playlistEntities.map {
            MyPlaylistItem(
                playlistId = it.playlistId,
                name = it.name
            )
        }
        return myPlaylistItems
    }

    fun toVideoItem(videoEntity: VideoEntity): VideoItem {
        val type = object : TypeToken<List<Image>>() {}.type
        val uploaderAvatars: List<Image>? = Gson().fromJson(videoEntity.uploaderAvatars, type)

        return VideoItem(
            id = videoEntity.id,
            title = videoEntity.title,
            description = videoEntity.description,
            publishTimestamp = videoEntity.publishTimestamp,
            thumbnailUrl = videoEntity.thumbnailUrl,
            uploaderName = videoEntity.uploaderName,
            uploaderUrl = videoEntity.uploaderUrl,
            uploaderAvatars = uploaderAvatars,
            uploaderVerified = videoEntity.uploaderVerified,
            duration = videoEntity.duration,
            viewCount = videoEntity.viewCount,
            textualUploadDate = videoEntity.textualUploadDate,
            streamType = StreamType.valueOf(videoEntity.streamType),
            shortFormContent = videoEntity.shortFormContent
        )
    }

    fun toVideoEntity(video: SearchResult.Video, playlistId: Long): VideoEntity{
        return VideoEntity(
            id = video.id,
            playlistId = playlistId,
            title = video.title,
            description = video.description,
            publishTimestamp = video.publishTimestamp,
            thumbnailUrl = video.thumbnailUrl,
            uploaderName = video.uploaderName,
            uploaderUrl = video.uploaderUrl,
            uploaderAvatars = Gson().toJson(video.uploaderAvatars),
            uploaderVerified = video.uploaderVerified,
            duration = video.duration,
            viewCount = video.viewCount,
            textualUploadDate = video.textualUploadDate,
            streamType = video.streamType.name,
            shortFormContent = video.shortFormContent
        )
    }

    fun toVideoEntity(video: VideoDetailData, playlistId: Long): VideoEntity {
        return VideoEntity(
            id = video.id,
            playlistId = playlistId,
            title = video.title,
            description = video.description,
            publishTimestamp = video.publishTimestamp,
            thumbnailUrl = video.thumbnailUrl,
            uploaderName = video.uploaderName,
            uploaderUrl = video.uploaderUrl,
            uploaderAvatars = Gson().toJson(video.uploaderAvatars),
            uploaderVerified = video.uploaderSubscriberCount != null,
            duration = 0,
            viewCount = video.viewCount ?: 0,
            textualUploadDate = video.publishedTimeText,
            streamType = StreamType.NONE.name,
            shortFormContent = false
        )
    }


}