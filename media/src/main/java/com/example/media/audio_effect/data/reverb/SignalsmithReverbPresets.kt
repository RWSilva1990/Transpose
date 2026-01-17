package com.example.media.audio_effect.data.reverb

/**
 * Signalsmith Reverb preset parameters
 *
 * @param dry Dry signal level (0.0 - 4.0)
 * @param wet Wet/reverb signal level (0.0 - 4.0)
 * @param roomMs Room size in milliseconds (10 - 200ms, ~1ms = 1 foot)
 * @param decaySec RT20 decay time in seconds (0.01 - 30s)
 */
data class SignalsmithReverbPreset(
    val name: String,
    val nameKo: String,
    val dry: Float,
    val wet: Float,
    val roomMs: Float,
    val decaySec: Float,
    val description: String = ""
)

object SignalsmithReverbPresets {

    // Preset indices
    const val PRESET_NONE = 0
    const val PRESET_DEFAULT = 1  // Subtle reverb - default when turning on
    const val PRESET_BATHROOM = 2
    const val PRESET_CATHEDRAL = 3
    const val PRESET_CAVE = 4
    const val PRESET_STUDIO = 5
    const val PRESET_CONCERT_HALL = 6
    const val PRESET_TUNNEL = 7
    const val PRESET_UNDERWATER = 8
    const val PRESET_TELEPHONE = 9
    const val PRESET_ARENA = 10
    const val PRESET_FOREST = 11
    const val PRESET_PARKING_GARAGE = 12
    const val PRESET_CHURCH = 13
    const val PRESET_WAREHOUSE = 14
    const val PRESET_SEWER = 15
    const val PRESET_SPACESHIP = 16

    val presets = listOf(
        // 0: None - Complete bypass (no reverb)
        SignalsmithReverbPreset(
            name = "None",
            nameKo = "없음",
            dry = 1.0f,
            wet = 0.0f,
            roomMs = 30f,
            decaySec = 0.5f,
            description = "No reverb effect"
        ),

        // 1: Default - Subtle, natural room ambience (default when turning on reverb)
        SignalsmithReverbPreset(
            name = "Default",
            nameKo = "기본",
            dry = 0.95f,
            wet = 0.2f,
            roomMs = 25f,
            decaySec = 0.5f,
            description = "Subtle room ambience, natural sounding"
        ),

        // 2: Bathroom - Small, bright, tile reflections
        SignalsmithReverbPreset(
            name = "Bathroom",
            nameKo = "욕실",
            dry = 0.9f,
            wet = 0.4f,
            roomMs = 15f,
            decaySec = 0.6f,
            description = "Small tiled bathroom with bright reflections"
        ),

        // 3: Cathedral - Large space, subtle majesty
        SignalsmithReverbPreset(
            name = "Cathedral",
            nameKo = "대성당",
            dry = 0.85f,
            wet = 0.5f,
            roomMs = 120f,
            decaySec = 2.5f,
            description = "Large cathedral with soaring ceilings"
        ),

        // 4: Cave - Dark, atmospheric
        SignalsmithReverbPreset(
            name = "Cave",
            nameKo = "동굴",
            dry = 0.8f,
            wet = 0.55f,
            roomMs = 100f,
            decaySec = 3.0f,
            description = "Deep cave with dark, mysterious reverb"
        ),

        // 5: Studio - Tight, controlled, professional
        SignalsmithReverbPreset(
            name = "Studio",
            nameKo = "스튜디오",
            dry = 1.0f,
            wet = 0.15f,
            roomMs = 20f,
            decaySec = 0.3f,
            description = "Professional recording studio"
        ),

        // 6: Concert Hall - Warm, musical
        SignalsmithReverbPreset(
            name = "Concert Hall",
            nameKo = "콘서트 홀",
            dry = 0.9f,
            wet = 0.4f,
            roomMs = 80f,
            decaySec = 1.8f,
            description = "Grand concert hall with warm acoustics"
        ),

        // 7: Tunnel - Subtle metallic echo
        SignalsmithReverbPreset(
            name = "Tunnel",
            nameKo = "터널",
            dry = 0.85f,
            wet = 0.4f,
            roomMs = 50f,
            decaySec = 1.5f,
            description = "Underground tunnel with metallic reflections"
        ),

        // 8: Underwater - Dreamy, muffled
        SignalsmithReverbPreset(
            name = "Underwater",
            nameKo = "수중",
            dry = 0.6f,
            wet = 0.7f,
            roomMs = 70f,
            decaySec = 2.0f,
            description = "Submerged underwater feeling"
        ),

        // 9: Telephone - Tiny box, very dry
        SignalsmithReverbPreset(
            name = "Telephone",
            nameKo = "전화기",
            dry = 1.1f,
            wet = 0.08f,
            roomMs = 10f,
            decaySec = 0.08f,
            description = "Tiny phone booth feeling"
        ),

        // 10: Arena - Large space, moderate
        SignalsmithReverbPreset(
            name = "Arena",
            nameKo = "경기장",
            dry = 0.85f,
            wet = 0.45f,
            roomMs = 130f,
            decaySec = 2.2f,
            description = "Large sports arena or stadium"
        ),

        // 11: Forest - Natural, scattered, organic
        SignalsmithReverbPreset(
            name = "Forest",
            nameKo = "숲",
            dry = 0.95f,
            wet = 0.25f,
            roomMs = 60f,
            decaySec = 0.8f,
            description = "Natural forest with scattered reflections"
        ),

        // 12: Parking Garage - Concrete, moderate
        SignalsmithReverbPreset(
            name = "Parking Garage",
            nameKo = "주차장",
            dry = 0.85f,
            wet = 0.35f,
            roomMs = 45f,
            decaySec = 1.2f,
            description = "Concrete parking structure"
        ),

        // 13: Church - Medium hall, spiritual
        SignalsmithReverbPreset(
            name = "Church",
            nameKo = "교회",
            dry = 0.88f,
            wet = 0.4f,
            roomMs = 70f,
            decaySec = 2.0f,
            description = "Traditional church with high ceilings"
        ),

        // 14: Warehouse - Industrial, empty
        SignalsmithReverbPreset(
            name = "Warehouse",
            nameKo = "창고",
            dry = 0.9f,
            wet = 0.3f,
            roomMs = 90f,
            decaySec = 1.5f,
            description = "Empty industrial warehouse"
        ),

        // 15: Sewer - Dark, echoey
        SignalsmithReverbPreset(
            name = "Sewer",
            nameKo = "하수도",
            dry = 0.8f,
            wet = 0.45f,
            roomMs = 35f,
            decaySec = 1.3f,
            description = "Underground sewer system"
        ),

        // 16: Spaceship - Sci-fi, metallic
        SignalsmithReverbPreset(
            name = "Spaceship",
            nameKo = "우주선",
            dry = 0.85f,
            wet = 0.35f,
            roomMs = 25f,
            decaySec = 0.9f,
            description = "Sci-fi spaceship interior"
        )
    )

    val presetNames: List<String>
        get() = presets.map { it.name }

    val presetNamesKo: List<String>
        get() = presets.map { it.nameKo }

    fun getPreset(index: Int): SignalsmithReverbPreset {
        return presets.getOrElse(index) { presets[PRESET_NONE] }
    }

    fun getPresetByName(name: String): SignalsmithReverbPreset? {
        return presets.find { it.name == name || it.nameKo == name }
    }
}
