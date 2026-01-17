package com.example.media.audio_effect.data

/**
 * Audio Effect Presets with user-friendly names and descriptions
 * Focused on music listening experience
 */

// ============== STEREO WIDENER ==============
data class StereoWidenerPreset(
    val name: String,
    val nameKo: String,
    val description: String,
    val descriptionKo: String,
    val width: Float  // 0.0-2.0, 1.0 = original
)

object StereoWidenerPresets {
    val presets = listOf(
        StereoWidenerPreset(
            name = "Mono",
            nameKo = "모노",
            description = "Convert to mono (single channel)",
            descriptionKo = "모노로 변환 (단일 채널)",
            width = 0f
        ),
        StereoWidenerPreset(
            name = "Narrow",
            nameKo = "좁게",
            description = "Reduced stereo width for focused sound",
            descriptionKo = "집중된 소리를 위한 좁은 스테레오",
            width = 0.5f
        ),
        StereoWidenerPreset(
            name = "Original",
            nameKo = "원본",
            description = "Original stereo width",
            descriptionKo = "원본 스테레오 폭",
            width = 1.0f
        ),
        StereoWidenerPreset(
            name = "Wide",
            nameKo = "넓게",
            description = "Enhanced stereo width",
            descriptionKo = "확장된 스테레오 폭",
            width = 1.5f
        ),
        StereoWidenerPreset(
            name = "Extra Wide",
            nameKo = "매우 넓게",
            description = "Maximum stereo expansion",
            descriptionKo = "최대 스테레오 확장",
            width = 2.0f
        )
    )
}

// ============== CHORUS ==============
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
            name = "Subtle Thicken",
            nameKo = "살짝 두껍게",
            description = "Slight doubling for fullness",
            descriptionKo = "소리를 살짝 두껍게",
            mix = 0.3f, depthMs = 5f, detune = 5f, stereo = 0.3f
        ),
        ChorusPreset(
            name = "Dreamy",
            nameKo = "몽환적",
            description = "Floating, dreamy atmosphere",
            descriptionKo = "둥둥 떠다니는 몽환적 분위기",
            mix = 0.5f, depthMs = 15f, detune = 12f, stereo = 0.7f
        ),
        ChorusPreset(
            name = "80s Ballad",
            nameKo = "80년대 발라드",
            description = "Classic 80s power ballad",
            descriptionKo = "80년대 파워 발라드 느낌",
            mix = 0.6f, depthMs = 20f, detune = 15f, stereo = 0.8f
        ),
        ChorusPreset(
            name = "Wide Stereo",
            nameKo = "넓은 스테레오",
            description = "Expansive stereo width",
            descriptionKo = "좌우로 넓게 퍼지는 사운드",
            mix = 0.4f, depthMs = 10f, detune = 8f, stereo = 1.0f
        )
    )
}

// ============== COMPRESSOR ==============
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
            name = "Gentle Leveling",
            nameKo = "부드러운 레벨링",
            description = "Subtle volume evening",
            descriptionKo = "볼륨을 살짝 균일하게",
            thresholdDb = -15f, ratio = 2f, attackMs = 20f, releaseMs = 200f, makeupGainDb = 2f
        ),
        CompressorPreset(
            name = "Podcast Voice",
            nameKo = "팟캐스트 음성",
            description = "Clear, consistent voice",
            descriptionKo = "깔끔하고 일정한 음성",
            thresholdDb = -20f, ratio = 4f, attackMs = 10f, releaseMs = 100f, makeupGainDb = 4f
        ),
        CompressorPreset(
            name = "Punchy",
            nameKo = "펀치감",
            description = "Adds punch and energy",
            descriptionKo = "에너지 있는 펀치감 추가",
            thresholdDb = -12f, ratio = 6f, attackMs = 5f, releaseMs = 80f, makeupGainDb = 3f
        ),
        CompressorPreset(
            name = "Heavy Squeeze",
            nameKo = "강한 압축",
            description = "Aggressive compression",
            descriptionKo = "강하게 압축해서 균일하게",
            thresholdDb = -25f, ratio = 10f, attackMs = 2f, releaseMs = 50f, makeupGainDb = 6f
        )
    )
}

// ============== LIMITER ==============
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
            name = "Safe Ceiling",
            nameKo = "안전한 천장",
            description = "Prevents clipping",
            descriptionKo = "클리핑 방지",
            inputGainDb = 0f, limitDb = -1f, attackMs = 5f, releaseMs = 50f
        ),
        LimiterPreset(
            name = "Louder",
            nameKo = "더 크게",
            description = "Increase perceived loudness",
            descriptionKo = "체감 음량 증가",
            inputGainDb = 6f, limitDb = -0.5f, attackMs = 2f, releaseMs = 30f
        ),
        LimiterPreset(
            name = "Broadcast",
            nameKo = "방송용",
            description = "Broadcast-ready levels",
            descriptionKo = "방송에 적합한 레벨",
            inputGainDb = 3f, limitDb = -1f, attackMs = 1f, releaseMs = 100f
        ),
        LimiterPreset(
            name = "Aggressive",
            nameKo = "공격적",
            description = "Maximum loudness",
            descriptionKo = "최대 음량",
            inputGainDb = 9f, limitDb = -0.3f, attackMs = 0.5f, releaseMs = 20f
        )
    )
}

// ============== EFFECT DESCRIPTIONS ==============
object EffectDescriptions {
    data class EffectInfo(
        val title: String,
        val titleKo: String,
        val shortDesc: String,
        val shortDescKo: String
    )

    val descriptions = mapOf(
        "stereoWidener" to EffectInfo(
            "Stereo Widener", "스테레오 확장",
            "Adjusts the stereo width for a wider or narrower soundstage",
            "스테레오 이미지를 넓히거나 좁혀 공간감을 조절합니다"
        ),
        "chorus" to EffectInfo(
            "Chorus", "코러스",
            "Makes it sound like multiple voices singing together",
            "여러 목소리가 함께 노래하는 것처럼 들립니다"
        ),
        "compressor" to EffectInfo(
            "Compressor", "컴프레서",
            "Evens out loud and quiet parts for consistent volume",
            "큰 소리는 줄이고 작은 소리는 키워서 볼륨을 균일하게"
        ),
        "limiter" to EffectInfo(
            "Limiter", "리미터",
            "Prevents audio from getting too loud",
            "소리가 너무 커지는 것을 방지합니다"
        ),
        "reverb" to EffectInfo(
            "Reverb", "리버브",
            "Simulates room acoustics and space",
            "다양한 공간의 잔향을 시뮬레이션합니다"
        ),
        "eq" to EffectInfo(
            "Equalizer", "이퀄라이저",
            "Adjust bass, mids, and treble",
            "저음, 중음, 고음을 조절합니다"
        ),
        "vocalRemove" to EffectInfo(
            "Vocal Remove", "보컬 제거",
            "Reduces center-panned vocals in stereo music",
            "스테레오 음악에서 중앙의 보컬을 줄입니다"
        )
    )
}
