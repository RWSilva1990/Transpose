package com.example.media.audio_effect.data

data class LimiterPreset(
    val name: String,
    val nameKo: String,
    val description: String,
    val descriptionKo: String,
    val inputGainDb: Float,
    val limitDb: Float,
    val attackMs: Float,
    val releaseMs: Float
)

object LimiterPresets {
    val presets = listOf(
        LimiterPreset(
            name = "Soft",
            nameKo = "부드러운",
            description = "Gentle peak limiting",
            descriptionKo = "부드러운 피크 제한",
            inputGainDb = 0f,
            limitDb = -1f,
            attackMs = 10f,
            releaseMs = 100f
        ),
        LimiterPreset(
            name = "Moderate",
            nameKo = "보통",
            description = "Balanced limiting",
            descriptionKo = "균형 잡힌 제한",
            inputGainDb = 3f,
            limitDb = -0.5f,
            attackMs = 5f,
            releaseMs = 80f
        ),
        LimiterPreset(
            name = "Loud",
            nameKo = "큰 소리",
            description = "For maximum loudness",
            descriptionKo = "최대 음량을 위해",
            inputGainDb = 6f,
            limitDb = -0.3f,
            attackMs = 3f,
            releaseMs = 60f
        ),
        LimiterPreset(
            name = "Broadcast",
            nameKo = "방송용",
            description = "Broadcast standard limiting",
            descriptionKo = "방송 표준 제한",
            inputGainDb = 4f,
            limitDb = -1f,
            attackMs = 1f,
            releaseMs = 50f
        ),
        LimiterPreset(
            name = "Transparent",
            nameKo = "투명한",
            description = "Clean limiting with minimal coloring",
            descriptionKo = "최소한의 색상으로 깨끗한 제한",
            inputGainDb = 2f,
            limitDb = -0.5f,
            attackMs = 8f,
            releaseMs = 120f
        )
    )
}
