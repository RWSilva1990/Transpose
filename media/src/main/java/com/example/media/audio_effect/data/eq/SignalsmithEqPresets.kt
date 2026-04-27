package com.example.media.audio_effect.data.eq

data class SignalsmithEqPreset(
    val name: String,
    val nameKo: String,
    val gains: FloatArray,
    val description: String = "",
    val descriptionKo: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SignalsmithEqPreset
        return name == other.name && gains.contentEquals(other.gains)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + gains.contentHashCode()
        return result
    }
}

object SignalsmithEqPresets {

    const val PRESET_BASS_BOOST = 0
    const val PRESET_TREBLE_BOOST = 1
    const val PRESET_VOCAL_BOOST = 2
    const val PRESET_POP = 3
    const val PRESET_ROCK = 4
    const val PRESET_JAZZ = 5
    const val PRESET_CLASSICAL = 6
    const val PRESET_ELECTRONIC = 7
    const val PRESET_HIP_HOP = 8
    const val PRESET_ACOUSTIC = 9
    const val PRESET_R_AND_B = 10
    const val PRESET_LOUDNESS = 11
    const val PRESET_SPOKEN_WORD = 12
    const val PRESET_FLAT = 13

    val presets = listOf(
        SignalsmithEqPreset(
            name = "Bass Boost",
            nameKo = "저음 강조",
            gains = floatArrayOf(6f, 4f, 0f, 0f, 0f),
            description = "Enhanced bass for more punch",
            descriptionKo = "저음을 단단하게 끌어올린 부스트"
        ),
        SignalsmithEqPreset(
            name = "Treble Boost",
            nameKo = "고음 강조",
            gains = floatArrayOf(0f, 0f, 0f, 4f, 6f),
            description = "Crisp highs for more clarity",
            descriptionKo = "고음을 또렷하게 강조"
        ),
        SignalsmithEqPreset(
            name = "Vocal Boost",
            nameKo = "보컬 강조",
            gains = floatArrayOf(-2f, 0f, 4f, 3f, 1f),
            description = "Brings vocals forward in the mix",
            descriptionKo = "보컬을 믹스 앞으로 끌어내는 EQ"
        ),
        SignalsmithEqPreset(
            name = "Pop",
            nameKo = "팝",
            gains = floatArrayOf(2f, 1f, -1f, 2f, 3f),
            description = "Bright and punchy for pop music",
            descriptionKo = "팝에 어울리는 밝고 펀치 있는 톤"
        ),
        SignalsmithEqPreset(
            name = "Rock",
            nameKo = "록",
            gains = floatArrayOf(4f, 2f, 1f, 3f, 2f),
            description = "Full sound for rock and guitar music",
            descriptionKo = "록과 기타에 어울리는 꽉 찬 사운드"
        ),
        SignalsmithEqPreset(
            name = "Jazz",
            nameKo = "재즈",
            gains = floatArrayOf(3f, 1f, -1f, 1f, 2f),
            description = "Warm and smooth for jazz",
            descriptionKo = "재즈에 어울리는 따뜻하고 부드러운 톤"
        ),
        SignalsmithEqPreset(
            name = "Classical",
            nameKo = "클래식",
            gains = floatArrayOf(0f, 0f, 0f, 1f, 2f),
            description = "Natural response for orchestral music",
            descriptionKo = "오케스트라에 자연스러운 응답"
        ),
        SignalsmithEqPreset(
            name = "Electronic",
            nameKo = "일렉트로닉",
            gains = floatArrayOf(5f, 3f, 0f, 2f, 4f),
            description = "Powerful bass and crisp highs for EDM",
            descriptionKo = "EDM의 강한 저음과 또렷한 고음"
        ),
        SignalsmithEqPreset(
            name = "Hip-Hop",
            nameKo = "힙합",
            gains = floatArrayOf(6f, 4f, 1f, 2f, 1f),
            description = "Deep bass with clear vocal presence",
            descriptionKo = "깊은 저음과 명확한 보컬 존재감"
        ),
        SignalsmithEqPreset(
            name = "Acoustic",
            nameKo = "어쿠스틱",
            gains = floatArrayOf(2f, 1f, 0f, 2f, 3f),
            description = "Natural sound for acoustic instruments",
            descriptionKo = "어쿠스틱 악기의 자연스러운 톤"
        ),
        SignalsmithEqPreset(
            name = "R&B",
            nameKo = "R&B",
            gains = floatArrayOf(4f, 3f, 0f, 1f, 2f),
            description = "Smooth and warm for R&B and soul",
            descriptionKo = "R&B와 소울의 부드럽고 따뜻한 톤"
        ),
        SignalsmithEqPreset(
            name = "Loudness",
            nameKo = "라우드니스",
            gains = floatArrayOf(5f, 2f, 0f, 1f, 4f),
            description = "Bass and treble boost for low volume",
            descriptionKo = "낮은 볼륨용 저음·고음 보강"
        ),
        SignalsmithEqPreset(
            name = "Spoken Word",
            nameKo = "음성/팟캐스트",
            gains = floatArrayOf(-3f, -1f, 3f, 4f, 0f),
            description = "Optimized for speech clarity",
            descriptionKo = "음성 명료도에 최적화한 EQ"
        ),
        SignalsmithEqPreset(
            name = "Flat",
            nameKo = "평탄",
            gains = floatArrayOf(0f, 0f, 0f, 0f, 0f),
            description = "No equalization - neutral sound",
            descriptionKo = "EQ 없이 자연 그대로"
        )
    )

    val presetNames: List<String>
        get() = presets.map { it.name }

    val presetNamesKo: List<String>
        get() = presets.map { it.nameKo }

    fun getPreset(index: Int): SignalsmithEqPreset {
        return presets.getOrElse(index) { presets[PRESET_BASS_BOOST] }
    }

    fun getPresetByName(name: String): SignalsmithEqPreset? {
        return presets.find { it.name == name || it.nameKo == name }
    }
}
