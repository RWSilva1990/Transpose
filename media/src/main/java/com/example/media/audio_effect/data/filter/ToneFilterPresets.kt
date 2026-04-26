package com.example.media.audio_effect.data.filter

data class ToneFilterPreset(
    val name: String,
    val nameKo: String,
    val lowCutHz: Float,
    val highCutHz: Float,
    val lowShelfDb: Float,
    val highShelfDb: Float,
    val description: String = "",
    val descriptionKo: String = ""
)

object ToneFilterPresets {
    const val PRESET_VOCAL_FOCUS = 0
    const val PRESET_RADIO = 1
    const val PRESET_PHONE = 2
    const val PRESET_BRIGHT = 3
    const val PRESET_AIR = 4
    const val PRESET_WARM = 5
    const val PRESET_DARK = 6
    const val PRESET_LOFI = 7
    const val PRESET_FLAT = 8

    val presets = listOf(
        ToneFilterPreset(
            "Vocal Focus", "\ubcf4\uceec \ud3ec\ucee4\uc2a4",
            650f, 11000f, 2.5f, -2.5f,
            "Focus on vocal range",
            "보컬 대역에 집중한 또렷한 톤"
        ),
        ToneFilterPreset(
            "Radio", "\ub77c\ub514\uc624",
            220f, 4800f, -4f, -2.5f,
            "AM/FM radio feel",
            "AM/FM 라디오 같은 좁은 대역감"
        ),
        ToneFilterPreset(
            "Phone", "\uc804\ud654",
            450f, 2800f, -8f, -6.5f,
            "Phone call sound",
            "전화 통화처럼 매우 좁은 대역"
        ),
        ToneFilterPreset(
            "Bright", "\ube0c\ub77c\uc774\ud2b8",
            70f, 17500f, -2.5f, 3.5f,
            "Crisp high-end boost",
            "고역을 강조한 또렷한 톤"
        ),
        ToneFilterPreset(
            "Air", "\uc5d0\uc5b4",
            25f, 20000f, -0.5f, 8.5f,
            "Ultra-high shimmer",
            "초고역의 반짝이는 공기감"
        ),
        ToneFilterPreset(
            "Warm", "\uc6cc\ubc0d",
            65f, 13000f, 4.5f, -3f,
            "Warm low-end tone",
            "저역을 살린 따뜻한 톤"
        ),
        ToneFilterPreset(
            "Dark", "\ub2e4\ud06c",
            35f, 6200f, 2f, -8f,
            "Heavy high-cut",
            "고역을 강하게 잘라낸 어두운 톤"
        ),
        ToneFilterPreset(
            "Lo-Fi", "\ub85c\ud30c\uc774",
            180f, 6200f, -1f, -4.5f,
            "Lo-fi character",
            "거칠고 빈티지한 로파이 캐릭터"
        ),
        ToneFilterPreset(
            "Flat", "\ud3c9\ud0c4",
            20f, 20000f, 0f, 0f,
            "No filtering",
            "필터 없이 자연 그대로"
        )
    )

    val presetNames: List<String> get() = presets.map { it.name }
    val presetNamesKo: List<String> get() = presets.map { it.nameKo }

    fun getPreset(index: Int): ToneFilterPreset {
        return presets.getOrElse(index) { presets[PRESET_VOCAL_FOCUS] }
    }
}
