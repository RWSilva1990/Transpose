package com.example.ui.util

import android.content.Context
import com.example.domain.model.preferences.AudioQuality
import com.example.domain.model.preferences.VideoQuality
import com.example.transpose.core.ui.R

fun VideoQuality.getDisplayString(context: Context): String {
    return context.getString(getDisplayStringResId())
}

fun VideoQuality.getDisplayStringResId(): Int {
    return when (this) {
        VideoQuality.AUTO -> R.string.video_quality_auto
        VideoQuality.P1080 -> R.string.video_quality_1080p
        VideoQuality.P720 -> R.string.video_quality_720p
        VideoQuality.P480 -> R.string.video_quality_480p
        VideoQuality.P360 -> R.string.video_quality_360p
        VideoQuality.P240 -> R.string.video_quality_240p
        VideoQuality.P144 -> R.string.video_quality_144p
    }
}

fun AudioQuality.getDisplayString(context: Context): String {
    return context.getString(getDisplayStringResId())
}

fun AudioQuality.getDisplayStringResId(): Int {
    return when (this) {
        AudioQuality.LOW -> R.string.audio_quality_low
        AudioQuality.MEDIUM -> R.string.audio_quality_medium
        AudioQuality.HIGH -> R.string.audio_quality_high
    }
}