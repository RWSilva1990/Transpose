package com.example.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.preferences.RepeatMode
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

    suspend fun setVideoQuality(quality: String) {
        dataStore.edit { preferences ->
            preferences[VIDEO_QUALITY] = quality
        }
    }

    suspend fun setAudioQuality(quality: String) {
        dataStore.edit { preferences ->
            preferences[AUDIO_QUALITY] = quality
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

    val videoQuality: StateFlow<String> = dataStore.data
        .map { preferences ->
            preferences[VIDEO_QUALITY] ?: "AUTO"
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "AUTO"
        )

    val audioQuality: StateFlow<String> = dataStore.data
        .map { preferences ->
            preferences[AUDIO_QUALITY] ?: "AUTO"
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "AUTO"
        )
}