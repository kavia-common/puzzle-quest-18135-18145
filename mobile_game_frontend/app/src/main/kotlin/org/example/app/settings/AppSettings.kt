package org.example.app.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * AppSettings provides a DataStore-backed offline settings repository.
 * Settings covered: sound enabled, music enabled ( volume can be added later ), and haptics enabled.
 * All APIs are suspend/Flow for coroutine-friendly usage.
 */

// DataStore instance separate from LocaleManager's (can share file, but keep concerns split)
private val Context.settingsStore by preferencesDataStore(name = "user_prefs")

object AppSettings {
    private val KEY_SOUND = booleanPreferencesKey("sound_enabled")
    private val KEY_MUSIC = booleanPreferencesKey("music_enabled")
    private val KEY_HAPTIC = booleanPreferencesKey("haptic_enabled")

    // Optional for the future:
    private val KEY_SFX_VOLUME = floatPreferencesKey("sfx_volume")
    private val KEY_MUSIC_VOLUME = floatPreferencesKey("music_volume")
    private val KEY_LAST_PLAYED_LEVEL = intPreferencesKey("last_played_level")

    // PUBLIC_INTERFACE
    fun observeSoundEnabled(context: Context): Flow<Boolean> {
        /** Observe whether sound effects are enabled. Default: true. */
        return context.settingsStore.data.map { it[KEY_SOUND] ?: true }
    }

    // PUBLIC_INTERFACE
    fun observeMusicEnabled(context: Context): Flow<Boolean> {
        /** Observe whether music is enabled. Default: true. */
        return context.settingsStore.data.map { it[KEY_MUSIC] ?: true }
    }

    // PUBLIC_INTERFACE
    fun observeHapticEnabled(context: Context): Flow<Boolean> {
        /** Observe whether haptic feedback is enabled. Default: true. */
        return context.settingsStore.data.map { it[KEY_HAPTIC] ?: true }
    }

    // PUBLIC_INTERFACE
    suspend fun setSoundEnabled(context: Context, enabled: Boolean) {
        /** Persist sound enabled setting. */
        context.settingsStore.edit { it[KEY_SOUND] = enabled }
    }

    // PUBLIC_INTERFACE
    suspend fun setMusicEnabled(context: Context, enabled: Boolean) {
        /** Persist music enabled setting. */
        context.settingsStore.edit { it[KEY_MUSIC] = enabled }
    }

    // PUBLIC_INTERFACE
    suspend fun setHapticEnabled(context: Context, enabled: Boolean) {
        /** Persist haptic enabled setting. */
        context.settingsStore.edit { it[KEY_HAPTIC] = enabled }
    }

    // Additional helpers (optional; safe defaults)

    fun observeLastPlayedLevel(context: Context): Flow<Int> {
        return context.settingsStore.data.map { it[KEY_LAST_PLAYED_LEVEL] ?: 1 }
    }

    suspend fun setLastPlayedLevel(context: Context, level: Int) {
        context.settingsStore.edit { it[KEY_LAST_PLAYED_LEVEL] = level }
    }

    fun observeSfxVolume(context: Context): Flow<Float> {
        return context.settingsStore.data.map { it[KEY_SFX_VOLUME] ?: 1.0f }
    }

    suspend fun setSfxVolume(context: Context, volume: Float) {
        context.settingsStore.edit { it[KEY_SFX_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    fun observeMusicVolume(context: Context): Flow<Float> {
        return context.settingsStore.data.map { it[KEY_MUSIC_VOLUME] ?: 1.0f }
    }

    suspend fun setMusicVolume(context: Context, volume: Float) {
        context.settingsStore.edit { it[KEY_MUSIC_VOLUME] = volume.coerceIn(0f, 1f) }
    }
}
