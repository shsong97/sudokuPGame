package com.sudokupgame.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface SettingsRepository {
    val settings: Flow<Settings>

    suspend fun update(transform: (Settings) -> Settings)
}

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SettingsRepository {

    private val dataStore = context.settingsDataStore

    override val settings: Flow<Settings> = dataStore.data.map { it.toSettings() }

    override suspend fun update(transform: (Settings) -> Settings) {
        dataStore.edit { prefs ->
            val new = transform(prefs.toSettings())
            prefs[HIGHLIGHT_SAME_DIGIT] = new.highlightSameDigit
            prefs[AUTO_REMOVE_NOTES] = new.autoRemoveNotes
            prefs[SHOW_TIMER] = new.showTimer
            prefs[VIBRATION] = new.vibration
            prefs[THEME_MODE] = new.themeMode.name
            prefs[LAST_GAME_MODE] = new.lastGameMode.name
        }
    }

    private fun Preferences.toSettings(): Settings {
        val defaults = Settings()
        return Settings(
            highlightSameDigit = this[HIGHLIGHT_SAME_DIGIT] ?: defaults.highlightSameDigit,
            autoRemoveNotes = this[AUTO_REMOVE_NOTES] ?: defaults.autoRemoveNotes,
            showTimer = this[SHOW_TIMER] ?: defaults.showTimer,
            vibration = this[VIBRATION] ?: defaults.vibration,
            themeMode = this[THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: defaults.themeMode,
            lastGameMode = this[LAST_GAME_MODE]?.let { runCatching { GameMode.valueOf(it) }.getOrNull() }
                ?: defaults.lastGameMode,
        )
    }

    private companion object {
        val HIGHLIGHT_SAME_DIGIT = booleanPreferencesKey("highlight_same_digit")
        val AUTO_REMOVE_NOTES = booleanPreferencesKey("auto_remove_notes")
        val SHOW_TIMER = booleanPreferencesKey("show_timer")
        val VIBRATION = booleanPreferencesKey("vibration")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LAST_GAME_MODE = stringPreferencesKey("last_game_mode")
    }
}
