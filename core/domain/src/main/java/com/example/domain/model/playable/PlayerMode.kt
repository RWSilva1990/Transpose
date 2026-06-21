package com.example.domain.model.playable

enum class PlayerMode {
    AUDIO,
    VIDEO;

    fun displayOrder(): Int {
        return when (this) {
            AUDIO -> 0
            VIDEO -> 1
        }
    }
}

fun PlayableItem.defaultPlayerMode(): PlayerMode {
    return when (this) {
        is PlayableItem.Remote -> PlayerMode.VIDEO
        is PlayableItem.Local -> {
            val mimeType = localFile.mimeType.lowercase()
            when {
                mimeType.startsWith("audio/") -> PlayerMode.AUDIO
                mimeType.startsWith("video/") -> PlayerMode.VIDEO
                localFile.width != null && localFile.height != null -> PlayerMode.VIDEO
                else -> PlayerMode.AUDIO
            }
        }
    }
}

fun PlayableItem.supportedPlayerModes(): Set<PlayerMode> {
    return when (this) {
        is PlayableItem.Remote -> setOf(PlayerMode.AUDIO, PlayerMode.VIDEO)
        is PlayableItem.Local -> {
            val defaultMode = defaultPlayerMode()
            if (defaultMode == PlayerMode.VIDEO) {
                setOf(PlayerMode.AUDIO, PlayerMode.VIDEO)
            } else {
                setOf(PlayerMode.AUDIO)
            }
        }
    }
}
