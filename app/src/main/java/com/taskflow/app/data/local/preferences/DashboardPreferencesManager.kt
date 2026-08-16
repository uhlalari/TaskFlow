package com.taskflow.app.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dashboardDataStore by preferencesDataStore(name = "dashboard_prefs")
interface DashboardPreferencesManager {
    val enabledWidgetKeys: Flow<List<String>>
    suspend fun setEnabledWidgetKeys(keys: List<String>)
}

class DashboardPreferencesManagerImpl(private val context: Context) : DashboardPreferencesManager {

    override val enabledWidgetKeys: Flow<List<String>> = context.dashboardDataStore.data.map { prefs ->
        prefs[ENABLED_WIDGETS_KEY]
            ?.split(WIDGET_SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?: DEFAULT_ENABLED_WIDGET_KEYS
    }

    override suspend fun setEnabledWidgetKeys(keys: List<String>) {
        context.dashboardDataStore.edit { it[ENABLED_WIDGETS_KEY] = keys.joinToString(WIDGET_SEPARATOR) }
    }

    private companion object {
        const val WIDGET_SEPARATOR = ","
        val ENABLED_WIDGETS_KEY = stringPreferencesKey("enabled_widgets")
        val DEFAULT_ENABLED_WIDGET_KEYS = listOf("OVERVIEW")
    }
}
