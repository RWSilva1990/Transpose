package com.example.domain.model.preferences


enum class RepeatMode(val value: Int) {
    NONE(0),      // 반복 없음
    ALL(1),       // 전체 반복
    ONE(2);       // 한 곡 반복

    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value } ?: NONE
    }
}