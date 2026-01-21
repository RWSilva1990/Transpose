package com.example.media.audio_effect.data

data class StereoWidenerPreset(
    val name: String,
    val nameKo: String,
    val description: String,
    val descriptionKo: String,
    val width: Float
)

object StereoWidenerPresets {
    val presets = listOf(
        StereoWidenerPreset(
            name = "Mono",
            nameKo = "모노",
            description = "Collapse to center",
            descriptionKo = "중앙으로 축소",
            width = 0f
        ),
        StereoWidenerPreset(
            name = "Narrow",
            nameKo = "좁은",
            description = "Reduced stereo width",
            descriptionKo = "줄어든 스테레오 넓이",
            width = 0.5f
        ),
        StereoWidenerPreset(
            name = "Normal",
            nameKo = "보통",
            description = "Original stereo width",
            descriptionKo = "원래 스테레오 넓이",
            width = 1.0f
        ),
        StereoWidenerPreset(
            name = "Wide",
            nameKo = "넓은",
            description = "Enhanced stereo width",
            descriptionKo = "향상된 스테레오 넓이",
            width = 1.3f
        ),
        StereoWidenerPreset(
            name = "Extra Wide",
            nameKo = "매우 넓은",
            description = "Maximum stereo enhancement",
            descriptionKo = "최대 스테레오 향상",
            width = 1.5f
        )
    )
}
