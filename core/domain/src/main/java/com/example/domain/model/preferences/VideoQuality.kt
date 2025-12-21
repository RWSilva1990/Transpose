package com.example.domain.model.preferences

enum class VideoQuality(
    val displayName: String,
    val itag: Int?,
    val height: Int?
) {
    AUTO("자동", null, null),
    P1080("1080p", 137, 1080),
    P720("720p", 136, 720),
    P480("480p", 135, 480),
    P360("360p", 134, 360),
    P240("240p", 133, 240),
    P144("144p", 160, 144);

    val isAuto: Boolean get() = this == AUTO
    val requiresDash: Boolean get() = !isAuto

    companion object {
        fun fromDisplayName(name: String): VideoQuality {
            return entries.find { it.displayName == name } ?: AUTO
        }

        fun fromItag(itag: Int): VideoQuality? {
            return entries.find { it.itag == itag }
        }

        val selectableQualities: List<VideoQuality>
            get() = listOf(AUTO, P1080, P720, P480, P360)
    }
}