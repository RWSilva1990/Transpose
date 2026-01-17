package com.example.media.audio_effect.data

data class CompressorPreset(
    val name: String,
    val nameKo: String,
    val description: String,
    val descriptionKo: String,
    val thresholdDb: Float,
    val ratio: Float,
    val attackMs: Float,
    val releaseMs: Float,
    val makeupGainDb: Float
)

object CompressorPresets {
    val presets = listOf(
        CompressorPreset(
            name = "Gentle",
            nameKo = "부드러운",
            description = "Light compression for subtle control",
            descriptionKo = "미묘한 제어를 위한 가벼운 압축",
            thresholdDb = -15f,
            ratio = 2f,
            attackMs = 20f,
            releaseMs = 150f,
            makeupGainDb = 2f
        ),
        CompressorPreset(
            name = "Vocal",
            nameKo = "보컬",
            description = "Optimized for voice",
            descriptionKo = "보컬에 최적화",
            thresholdDb = -18f,
            ratio = 3f,
            attackMs = 10f,
            releaseMs = 100f,
            makeupGainDb = 4f
        ),
        CompressorPreset(
            name = "Punch",
            nameKo = "펀치",
            description = "Adds impact and punch",
            descriptionKo = "임팩트와 펀치감 추가",
            thresholdDb = -20f,
            ratio = 4f,
            attackMs = 5f,
            releaseMs = 80f,
            makeupGainDb = 5f
        ),
        CompressorPreset(
            name = "Heavy",
            nameKo = "강한",
            description = "Strong compression for loudness",
            descriptionKo = "라우드니스를 위한 강한 압축",
            thresholdDb = -25f,
            ratio = 6f,
            attackMs = 3f,
            releaseMs = 60f,
            makeupGainDb = 8f
        ),
        CompressorPreset(
            name = "Limiter",
            nameKo = "리미터",
            description = "Maximum compression as limiter",
            descriptionKo = "리미터로서의 최대 압축",
            thresholdDb = -10f,
            ratio = 10f,
            attackMs = 1f,
            releaseMs = 50f,
            makeupGainDb = 3f
        )
    )
}
