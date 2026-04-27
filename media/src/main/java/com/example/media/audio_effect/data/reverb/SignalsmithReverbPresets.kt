package com.example.media.audio_effect.data.reverb

data class SignalsmithReverbPreset(
    val name: String,
    val nameKo: String,
    val dry: Float,
    val wet: Float,
    val roomMs: Float,
    val decaySec: Float,
    val early: Float,
    val detune: Float,
    val lowCutHz: Float,
    val highCutHz: Float,
    val lowDampRate: Float,
    val highDampRate: Float,
    val description: String = "",
    val descriptionKo: String = ""
)

object SignalsmithReverbPresets {

    const val PRESET_DEFAULT = 0
    const val PRESET_HALL = 1
    const val PRESET_VOCAL = 2
    const val PRESET_VINTAGE = 3
    const val PRESET_ROOM = 4
    const val PRESET_SNAP = 5
    const val PRESET_UNDERWATER = 6
    const val PRESET_AIR = 7

    val presets = listOf(
        SignalsmithReverbPreset(
            name = "default", nameKo = "기본",
            dry = 1.0f, wet = 0.35f, roomMs = 80f, decaySec = 1.0f,
            early = 1.0f, detune = 2.0f, lowCutHz = 80f, highCutHz = 12000f,
            lowDampRate = 1.6f, highDampRate = 2.5f,
            description = "A neutral reverb that fits any source",
            descriptionKo = "어떤 소스에도 어울리는 자연스러운 리버브"
        ),
        SignalsmithReverbPreset(
            name = "lush", nameKo = "홀",
            dry = 1.0f, wet = 0.65f, roomMs = 160f, decaySec = 1.0f,
            early = 0.6f, detune = 3.0f, lowCutHz = 80f, highCutHz = 11000f,
            lowDampRate = 1.5f, highDampRate = 2.2f,
            description = "A heavy reverb that drenches the sound like a concert hall",
            descriptionKo = "콘서트 홀처럼 풍성하고 깊은 잔향"
        ),
        SignalsmithReverbPreset(
            name = "preDelay", nameKo = "보컬",
            dry = 1.0f, wet = 0.30f, roomMs = 180f, decaySec = 1.3f,
            early = 0.6f, detune = 2.0f, lowCutHz = 90f, highCutHz = 12000f,
            lowDampRate = 1.7f, highDampRate = 2.4f,
            description = "A reverb that keeps vocals clear up front and opens up behind",
            descriptionKo = "보컬을 또렷하게 살리며 뒤로 공간을 여는 리버브"
        ),
        SignalsmithReverbPreset(
            name = "slapback", nameKo = "빈티지",
            dry = 1.0f, wet = 0.55f, roomMs = 100f, decaySec = 0.25f,
            early = 2.0f, detune = 1.0f, lowCutHz = 120f, highCutHz = 10000f,
            lowDampRate = 2.0f, highDampRate = 3.0f,
            description = "A single-bounce echo effect — 50s rockabilly slap",
            descriptionKo = "50년대 록커빌리 스타일의 한 번 튕기는 슬랩 에코"
        ),
        SignalsmithReverbPreset(
            name = "drumRoom", nameKo = "룸",
            dry = 1.0f, wet = 0.45f, roomMs = 120f, decaySec = 1.1f,
            early = 2.0f, detune = 1.5f, lowCutHz = 120f, highCutHz = 12000f,
            lowDampRate = 1.8f, highDampRate = 2.5f,
            description = "A room-mic reverb that punches up drums and snares",
            descriptionKo = "드럼·스네어를 펀치 있게 살리는 룸 사운드"
        ),
        SignalsmithReverbPreset(
            name = "gated", nameKo = "스냅",
            dry = 1.0f, wet = 0.40f, roomMs = 80f, decaySec = 0.9f,
            early = 1.6f, detune = 1.5f, lowCutHz = 100f, highCutHz = 11000f,
            lowDampRate = 3.0f, highDampRate = 4.5f,
            description = "A gated-drum effect that cuts the tail off abruptly — 80s style",
            descriptionKo = "80년대 게이트 드럼처럼 잔향 꼬리를 끊어주는 효과"
        ),
        SignalsmithReverbPreset(
            name = "underwater", nameKo = "수중",
            dry = 0.7f, wet = 0.50f, roomMs = 90f, decaySec = 2.0f,
            early = 0.8f, detune = 4.0f, lowCutHz = 50f, highCutHz = 2200f,
            lowDampRate = 1.5f, highDampRate = 2.0f,
            description = "A dark, muffled reverb that sounds like it's heard underwater",
            descriptionKo = "물속에서 듣는 듯한 어둡고 무거운 잔향"
        ),
        SignalsmithReverbPreset(
            name = "shimmer", nameKo = "에어",
            dry = 1.0f, wet = 0.32f, roomMs = 110f, decaySec = 2.2f,
            early = 0.5f, detune = 14.0f, lowCutHz = 100f, highCutHz = 9000f,
            lowDampRate = 1.6f, highDampRate = 2.2f,
            description = "A reverb with a slightly detuned tail that drifts and shimmers",
            descriptionKo = "살짝 디튠된 꼬리가 떠다니며 반짝이는 리버브"
        )
    )

    val presetNames: List<String>
        get() = presets.map { it.name }

    val presetNamesKo: List<String>
        get() = presets.map { it.nameKo }

    fun getPreset(index: Int): SignalsmithReverbPreset {
        return presets.getOrElse(index) { presets[PRESET_DEFAULT] }
    }

    fun getPresetByName(name: String): SignalsmithReverbPreset? {
        return presets.find { it.name == name || it.nameKo == name }
    }
}
