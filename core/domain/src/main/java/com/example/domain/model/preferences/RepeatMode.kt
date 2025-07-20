package com.example.domain.model.preferences


enum class RepeatMode(val value: Int) {
    OFF(0),
    ALL(1),
    ONE(2);

    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value } ?: OFF
    }
}