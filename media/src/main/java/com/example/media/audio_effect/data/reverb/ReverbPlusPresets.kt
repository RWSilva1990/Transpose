package com.example.media.audio_effect.data.reverb

data class ReverbPlusPreset(
    val name: String,
    val nameKo: String,
    val dry: Float,
    val wet: Float,
    val roomSize: Float,
    val damping: Float,
    val description: String,
    val descriptionKo: String
)

object ReverbPlusPresets {
    const val PRESET_BALANCED = 0
    const val PRESET_SUBTLE = 1
    const val PRESET_BATHROOM = 2
    const val PRESET_CATHEDRAL = 3
    const val PRESET_CONCERT_HALL = 4
    const val PRESET_PARKING_GARAGE = 5
    const val PRESET_UNDERWATER = 6

    val presets = listOf(
        ReverbPlusPreset(
            name = "Balanced", nameKo = "기본",
            dry = 1.0f, wet = 0.30f, roomSize = 0.50f, damping = 0.50f,
            description = "Classic Freeverb space for general use",
            descriptionKo = "대부분의 소스에 맞는 클래식 Freeverb 공간감"
        ),
        ReverbPlusPreset(
            name = "Subtle", nameKo = "은은하게",
            dry = 1.0f, wet = 0.20f, roomSize = 0.40f, damping = 0.60f,
            description = "Light ambience without pushing the sound back",
            descriptionKo = "소리를 뒤로 밀지 않는 가벼운 공간감"
        ),
        ReverbPlusPreset(
            name = "Bathroom", nameKo = "욕실",
            dry = 1.0f, wet = 0.40f, roomSize = 0.30f, damping = 0.20f,
            description = "Small bright reflections",
            descriptionKo = "작고 밝게 튀는 반사음"
        ),
        ReverbPlusPreset(
            name = "Cathedral", nameKo = "성당",
            dry = 1.0f, wet = 0.60f, roomSize = 0.95f, damping = 0.20f,
            description = "Large open tail with a long decay",
            descriptionKo = "크고 길게 퍼지는 잔향"
        ),
        ReverbPlusPreset(
            name = "Concert Hall", nameKo = "공연장",
            dry = 1.0f, wet = 0.50f, roomSize = 0.80f, damping = 0.40f,
            description = "Wide natural hall ambience",
            descriptionKo = "넓고 자연스러운 홀 공간감"
        ),
        ReverbPlusPreset(
            name = "Parking Garage", nameKo = "주차장",
            dry = 1.0f, wet = 0.55f, roomSize = 0.90f, damping = 0.30f,
            description = "Hard concrete-style reflections",
            descriptionKo = "콘크리트 공간처럼 단단한 반사음"
        ),
        ReverbPlusPreset(
            name = "Underwater", nameKo = "수중",
            dry = 1.0f, wet = 0.70f, roomSize = 0.85f, damping = 0.95f,
            description = "Dark and heavily damped tail",
            descriptionKo = "어둡고 둔탁하게 감쇠되는 잔향"
        )
    )

    fun getPreset(index: Int): ReverbPlusPreset {
        return presets.getOrElse(index) { presets[PRESET_BALANCED] }
    }
}
