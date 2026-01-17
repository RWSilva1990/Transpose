package com.example.media.audio_effect.data

data class ChorusPreset(
    val name: String,
    val nameKo: String,
    val description: String,
    val descriptionKo: String,
    val mix: Float,
    val depthMs: Float,
    val detune: Float,
    val stereo: Float
)

object ChorusPresets {
    val presets = listOf(
        ChorusPreset(
            name = "Subtle",
            nameKo = "은은한",
            description = "Light chorus for gentle enhancement",
            descriptionKo = "부드러운 향상을 위한 가벼운 코러스",
            mix = 0.3f,
            depthMs = 8f,
            detune = 5f,
            stereo = 0.3f
        ),
        ChorusPreset(
            name = "Classic",
            nameKo = "클래식",
            description = "Traditional chorus sound",
            descriptionKo = "전통적인 코러스 사운드",
            mix = 0.5f,
            depthMs = 10f,
            detune = 10f,
            stereo = 0.5f
        ),
        ChorusPreset(
            name = "Rich",
            nameKo = "풍부한",
            description = "Full and warm chorus",
            descriptionKo = "풍부하고 따뜻한 코러스",
            mix = 0.6f,
            depthMs = 15f,
            detune = 15f,
            stereo = 0.7f
        ),
        ChorusPreset(
            name = "Wide",
            nameKo = "넓은",
            description = "Maximum stereo width",
            descriptionKo = "최대 스테레오 넓이",
            mix = 0.5f,
            depthMs = 12f,
            detune = 8f,
            stereo = 1.0f
        ),
        ChorusPreset(
            name = "Vintage",
            nameKo = "빈티지",
            description = "80s style chorus effect",
            descriptionKo = "80년대 스타일 코러스 효과",
            mix = 0.7f,
            depthMs = 20f,
            detune = 20f,
            stereo = 0.6f
        )
    )
}
