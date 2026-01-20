package com.example.domain.model.playable

import android.net.Uri
import com.example.domain.model.local_file.LocalFileData
import com.example.domain.model.youtube.video.Video

sealed class PlayableItem {
    abstract val id: String
    abstract val title: String
    abstract val thumbnailUri: String?
    abstract val duration: Long

    data class Remote(val video: Video) : PlayableItem() {
        override val id: String = video.id
        override val title: String = video.title
        override val thumbnailUri: String? = video.thumbnailUrl
        override val duration: Long = video.duration
    }

    data class Local(val localFile: LocalFileData) : PlayableItem() {
        override val id: String = "local_${localFile.id}"
        override val title: String = localFile.title
        override val thumbnailUri: String? = localFile.uri.toString()
        override val duration: Long = localFile.duration

        val artist: String? = localFile.artist
        val album: String? = localFile.album
        val uri: Uri = localFile.uri
        val filePath: String? = localFile.filePath
    }

    val isLocal: Boolean
        get() = this is Local

    val isRemote: Boolean
        get() = this is Remote

    companion object {
        fun fromVideo(video: Video): PlayableItem = Remote(video)
        fun fromLocalFile(localFile: LocalFileData): PlayableItem = Local(localFile)
    }
}
