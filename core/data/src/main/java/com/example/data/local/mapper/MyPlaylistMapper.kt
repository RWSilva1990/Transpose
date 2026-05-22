package com.example.data.local.mapper

import android.net.Uri
import com.example.data.local.database.entity.PlaylistEntity
import com.example.data.local.database.entity.VideoEntity
import com.example.domain.model.library.MyPlaylist
import com.example.domain.model.local_file.LocalFileData
import com.example.domain.model.playable.PlayableItem
import com.example.domain.model.youtube.video.Video
import com.example.domain.model.youtube.video_detail.VideoDetail
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
        return videoEntities.filter { !it.isLocal }.map { toVideo(it) }
    }

    fun toPlayableItems(videoEntities: List<VideoEntity>): List<PlayableItem> {
        return videoEntities.map { entity ->
            if (entity.isLocal) {
                toLocalPlayableItem(entity)
            } else {
                toRemotePlayableItem(entity)
            }
        }
    }

    private fun toRemotePlayableItem(entity: VideoEntity): PlayableItem.Remote {
        return PlayableItem.Remote(toVideo(entity))
    }

    private fun toLocalPlayableItem(entity: VideoEntity): PlayableItem.Local {
        val localFileData = LocalFileData(
            id = entity.id.removePrefix("local_").toLongOrNull() ?: 0L,
            title = entity.title,
            uri = Uri.parse(entity.localUri ?: ""),
            filePath = entity.localFilePath,
            mimeType = "",
            size = 0L,
            duration = entity.duration,
            artist = entity.artist,
            album = entity.album,
            year = null,
            genre = null,
            composer = null,
            albumArtist = null,
            width = null,
            height = null,
            resolution = null,
            dateTaken = null,
            dateAdded = 0L,
            dateModified = 0L
        )
        return PlayableItem.Local(localFileData)
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
            shortFormContent = video.shortFormContent,
            isLocal = false
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
            duration = video.duration,
            viewCount = video.viewCount ?: 0,
            textualUploadDate = video.publishedTimeText,
            streamType = StreamType.VIDEO_STREAM.name,
            shortFormContent = false,
            isLocal = false
        )
    }

    fun toVideoEntity(localFile: LocalFileData, playlistId: Long): VideoEntity {
        return VideoEntity(
            id = "local_${localFile.id}",
            playlistId = playlistId,
            title = localFile.title,
            description = "",
            publishTimestamp = localFile.dateAdded,
            thumbnailUrl = localFile.uri.toString(),
            uploaderName = localFile.artist,
            uploaderUrl = null,
            uploaderAvatarUrl = null,
            uploaderVerified = false,
            duration = localFile.duration,
            viewCount = 0,
            textualUploadDate = null,
            streamType = null,
            shortFormContent = false,
            isLocal = true,
            localUri = localFile.uri.toString(),
            localFilePath = localFile.filePath,
            artist = localFile.artist,
            album = localFile.album
        )
    }

    fun toVideoEntity(playableItem: PlayableItem, playlistId: Long): VideoEntity {
        return when (playableItem) {
            is PlayableItem.Remote -> toVideoEntity(playableItem.video, playlistId)
            is PlayableItem.Local -> toVideoEntity(playableItem.localFile, playlistId)
        }
    }
}
