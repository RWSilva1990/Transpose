package com.example.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.preferences.AudioQuality
import com.example.domain.model.preferences.RepeatMode
import com.example.domain.model.preferences.VideoQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

val Context.playbackPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "playback_preferences"
)

class PlaybackPreferences(private val dataStore: DataStore<Preferences>) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val VIDEO_QUALITY = stringPreferencesKey("video_quality")
        val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
    }

    suspend fun setRepeatMode(mode: RepeatMode) {
        dataStore.edit { preferences ->
            preferences[REPEAT_MODE] = mode.value
        }
    }

    suspend fun setShuffleMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHUFFLE_MODE] = enabled
        }
    }

    suspend fun setVideoQuality(quality: VideoQuality) {
        dataStore.edit { preferences ->
            preferences[VIDEO_QUALITY] = quality.name
        }
    }

    suspend fun setAudioQuality(quality: AudioQuality) {
        dataStore.edit { preferences ->
            preferences[AUDIO_QUALITY] = quality.name
        }
    }

    val repeatMode: StateFlow<RepeatMode> = dataStore.data
        .map { preferences ->
            val value = preferences[REPEAT_MODE] ?: RepeatMode.OFF.value
            RepeatMode.fromValue(value)
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RepeatMode.OFF
        )

    val shuffleMode: StateFlow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SHUFFLE_MODE] ?: false
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val videoQuality: StateFlow<VideoQuality> = dataStore.data
        .map { preferences ->
            val name = preferences[VIDEO_QUALITY] ?: VideoQuality.AUTO.name
            try {
                VideoQuality.valueOf(name)
            } catch (e: IllegalArgumentException) {
                VideoQuality.AUTO
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VideoQuality.AUTO
        )

    val audioQuality: StateFlow<AudioQuality> = dataStore.data
        .map { preferences ->
            val name = preferences[AUDIO_QUALITY] ?: AudioQuality.MEDIUM.name
            try {
                AudioQuality.valueOf(name)
            } catch (e: IllegalArgumentException) {
                AudioQuality.MEDIUM
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AudioQuality.MEDIUM
        )
}