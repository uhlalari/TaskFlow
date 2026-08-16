package com.taskflow.app.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "theme_prefs")
class ThemeManager(private val context: Context) {

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DARK_THEME_KEY] ?: true
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[DARK_THEME_KEY] = enabled }
    }

    private companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }
}
