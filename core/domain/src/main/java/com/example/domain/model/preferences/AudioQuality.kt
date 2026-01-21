package com.example.domain.model.preferences

enum class AudioQuality(
    val displayName: String,
    val itag: Int,
    val bitrate: Int
) {
    LOW("48kbps", 139, 48),
    MEDIUM("128kbps", 140, 128),
    HIGH("160kbps", 251, 160);

    companion object {
        val default: AudioQuality = MEDIUM

        fun fromDisplayName(name: String): AudioQuality {
            return entries.find { it.displayName == name } ?: default
        }

        fun fromItag(itag: Int): AudioQuality? {
            return entries.find { it.itag == itag }
        }
    }
}