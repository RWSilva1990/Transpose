package com.example.media.audio_effect.data.eq

/**
 * Signalsmith EQ preset parameters for 5-band parametric equalizer
 *
 * Bands:
 * - Band 0: 60Hz (Low Bass)
 * - Band 1: 250Hz (Bass/Low Mid)
 * - Band 2: 1kHz (Mid)
 * - Band 3: 4kHz (High Mid/Presence)
 * - Band 4: 12kHz (High/Air)
 *
 * @param name English name
 * @param nameKo Korean name
 * @param gains Array of 5 gain values in dB (-12 to +12)
 * @param description Description of the preset effect
 */
data class SignalsmithEqPreset(
    val name: String,
    val nameKo: String,
    val gains: FloatArray,
    val description: String = ""
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

    // Preset indices
    const val PRESET_FLAT = 0
    const val PRESET_BASS_BOOST = 1
    const val PRESET_TREBLE_BOOST = 2
    const val PRESET_VOCAL_BOOST = 3
    const val PRESET_POP = 4
    const val PRESET_ROCK = 5
    const val PRESET_JAZZ = 6
    const val PRESET_CLASSICAL = 7
    const val PRESET_ELECTRONIC = 8
    const val PRESET_HIP_HOP = 9
    const val PRESET_ACOUSTIC = 10
    const val PRESET_R_AND_B = 11
    const val PRESET_LOUDNESS = 12
    const val PRESET_SPOKEN_WORD = 13

    val presets = listOf(
        // 0: Flat - No EQ applied
        SignalsmithEqPreset(
            name = "Flat",
            nameKo = "평탄",
            gains = floatArrayOf(0f, 0f, 0f, 0f, 0f),
            description = "No equalization - neutral sound"
        ),

        // 1: Bass Boost - Enhanced low frequencies
        SignalsmithEqPreset(
            name = "Bass Boost",
            nameKo = "저음 강조",
            gains = floatArrayOf(6f, 4f, 0f, 0f, 0f),
            description = "Enhanced bass for more punch"
        ),

        // 2: Treble Boost - Enhanced high frequencies
        SignalsmithEqPreset(
            name = "Treble Boost",
            nameKo = "고음 강조",
            gains = floatArrayOf(0f, 0f, 0f, 4f, 6f),
            description = "Crisp highs for more clarity"
        ),

        // 3: Vocal Boost - Enhanced mid frequencies for vocals
        SignalsmithEqPreset(
            name = "Vocal Boost",
            nameKo = "보컬 강조",
            gains = floatArrayOf(-2f, 0f, 4f, 3f, 1f),
            description = "Brings vocals forward in the mix"
        ),

        // 4: Pop - Balanced with slight V-curve
        SignalsmithEqPreset(
            name = "Pop",
            nameKo = "팝",
            gains = floatArrayOf(2f, 1f, -1f, 2f, 3f),
            description = "Bright and punchy for pop music"
        ),

        // 5: Rock - Strong mids and presence
        SignalsmithEqPreset(
            name = "Rock",
            nameKo = "록",
            gains = floatArrayOf(4f, 2f, 1f, 3f, 2f),
            description = "Full sound for rock and guitar music"
        ),

        // 6: Jazz - Warm with smooth highs
        SignalsmithEqPreset(
            name = "Jazz",
            nameKo = "재즈",
            gains = floatArrayOf(3f, 1f, -1f, 1f, 2f),
            description = "Warm and smooth for jazz"
        ),

        // 7: Classical - Natural and balanced
        SignalsmithEqPreset(
            name = "Classical",
            nameKo = "클래식",
            gains = floatArrayOf(0f, 0f, 0f, 1f, 2f),
            description = "Natural response for orchestral music"
        ),

        // 8: Electronic/EDM - Strong bass and highs
        SignalsmithEqPreset(
            name = "Electronic",
            nameKo = "일렉트로닉",
            gains = floatArrayOf(5f, 3f, 0f, 2f, 4f),
            description = "Powerful bass and crisp highs for EDM"
        ),

        // 9: Hip-Hop - Deep bass, clear vocals
        SignalsmithEqPreset(
            name = "Hip-Hop",
            nameKo = "힙합",
            gains = floatArrayOf(6f, 4f, 1f, 2f, 1f),
            description = "Deep bass with clear vocal presence"
        ),

        // 10: Acoustic - Natural, slightly warm
        SignalsmithEqPreset(
            name = "Acoustic",
            nameKo = "어쿠스틱",
            gains = floatArrayOf(2f, 1f, 0f, 2f, 3f),
            description = "Natural sound for acoustic instruments"
        ),

        // 11: R&B/Soul - Warm bass, smooth mids
        SignalsmithEqPreset(
            name = "R&B",
            nameKo = "R&B",
            gains = floatArrayOf(4f, 3f, 0f, 1f, 2f),
            description = "Smooth and warm for R&B and soul"
        ),

        // 12: Loudness - Compensates for low volume listening
        SignalsmithEqPreset(
            name = "Loudness",
            nameKo = "라우드니스",
            gains = floatArrayOf(5f, 2f, 0f, 1f, 4f),
            description = "Bass and treble boost for low volume"
        ),

        // 13: Spoken Word/Podcast - Clear voice
        SignalsmithEqPreset(
            name = "Spoken Word",
            nameKo = "음성/팟캐스트",
            gains = floatArrayOf(-3f, -1f, 3f, 4f, 0f),
            description = "Optimized for speech clarity"
        )
    )

    val presetNames: List<String>
        get() = presets.map { it.name }

    val presetNamesKo: List<String>
        get() = presets.map { it.nameKo }

    fun getPreset(index: Int): SignalsmithEqPreset {
        return presets.getOrElse(index) { presets[PRESET_FLAT] }
    }

    fun getPresetByName(name: String): SignalsmithEqPreset? {
        return presets.find { it.name == name || it.nameKo == name }
    }
}
