package org.example.app.i18n

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

/**
 * LocaleManager handles runtime language switching and persistence using DataStore.
 * - Persists ISO language code (e.g., "en", "es")
 * - Applies locale to an Activity via configuration override
 *
 * PUBLIC INTERFACE:
 * - observeLanguage(): Flow<String> to observe persisted language code
 * - setLanguage(context, lang): suspend to persist the language
 * - applyLocale(activity, lang): apply locale to current activity configuration
 * - getSupportedLocale(lang): maps input to supported Locale
 */

// DataStore delegate at app Context level (file-scoped extension)
private val Context.dataStore by preferencesDataStore(name = "settings")

object LocaleManager {

    private val KEY_LANGUAGE = stringPreferencesKey("language_code")

    // PUBLIC_INTERFACE
    fun observeLanguage(context: Context): Flow<String> {
        /** Returns a Flow of the selected language code, defaulting to system's language or "en". */
        val defaultLang = getDefaultLanguage()
        return context.dataStore.data.map { prefs ->
            prefs[KEY_LANGUAGE] ?: defaultLang
        }
    }

    // PUBLIC_INTERFACE
    suspend fun setLanguage(context: Context, languageCode: String) {
        /** Persists the selected language code ("en" or "es"). */
        context.dataStore.edit { settings ->
            settings[KEY_LANGUAGE] = normalize(languageCode)
        }
    }

    // PUBLIC_INTERFACE
    fun applyLocale(activity: Activity, languageCode: String) {
        /**
         * Applies the locale to the given Activity by updating its configuration context.
         * Must be called on the UI thread before Composables read string resources.
         */
        val locale = getSupportedLocale(languageCode)
        updateActivityLocale(activity, locale)
    }

    @VisibleForTesting
    internal fun getDefaultLanguage(): String {
        // Use device default but limit to supported set (en/es)
        val deviceLang = Locale.getDefault().language
        return normalize(deviceLang)
    }

    // Map arbitrary input to supported "en" or "es"
    private fun normalize(code: String): String {
        return when (code.lowercase(Locale.ROOT)) {
            "es", "es_es", "es-mx", "es-ar" -> "es"
            else -> "en"
        }
    }

    // PUBLIC_INTERFACE
    fun getSupportedLocale(languageCode: String): Locale {
        /** Returns a Locale instance corresponding to supported languages. */
        return when (normalize(languageCode)) {
            "es" -> Locale("es")
            else -> Locale("en")
        }
    }

    private fun updateActivityLocale(activity: Activity, locale: Locale) {
        val resources = activity.resources
        val config: Configuration = Configuration(resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            config.setLocale(locale)
            activity.applyOverrideConfiguration(config)
        } else {
            @Suppress("DEPRECATION")
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}
