package com.example.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.preferences.RepeatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.playbackPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "playback_preferences"
)

class PlaybackPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
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

    val repeatMode: Flow<RepeatMode> = dataStore.data.map { preferences ->
        val value = preferences[REPEAT_MODE] ?: RepeatMode.OFF.value
        RepeatMode.fromValue(value)
    }

    val shuffleMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHUFFLE_MODE] ?: false
    }
}