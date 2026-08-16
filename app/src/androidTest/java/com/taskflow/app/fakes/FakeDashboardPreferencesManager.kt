package com.taskflow.app.fakes

import com.taskflow.app.data.local.preferences.DashboardPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Cópia enxuta do fake usado nos testes unitários — ver nota em FakeTaskRepository. */
class FakeDashboardPreferencesManager(
    initialEnabledKeys: List<String> = listOf("OVERVIEW")
) : DashboardPreferencesManager {

    private val keysFlow = MutableStateFlow(initialEnabledKeys)

    override val enabledWidgetKeys: StateFlow<List<String>> = keysFlow

    override suspend fun setEnabledWidgetKeys(keys: List<String>) {
        keysFlow.value = keys
    }
}
