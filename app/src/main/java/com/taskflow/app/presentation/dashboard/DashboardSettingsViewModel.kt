package com.taskflow.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.app.data.local.preferences.DashboardPreferencesManager
import com.taskflow.app.domain.usecase.GetWeatherLocationUseCase
import com.taskflow.app.domain.usecase.SetWeatherLocationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DashboardSettingsViewModel(
    private val dashboardPreferencesManager: DashboardPreferencesManager,
    private val getWeatherLocationUseCase: GetWeatherLocationUseCase,
    private val setWeatherLocationUseCase: SetWeatherLocationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardSettingsState())
    val state: StateFlow<DashboardSettingsState> = _state.asStateFlow()

    init {
        dashboardPreferencesManager.enabledWidgetKeys
            .onEach { keys ->
                val enabled = keys.toDashboardWidgetTypes()
                val available = DashboardWidgetType.entries.filterNot { it in enabled }
                _state.update { it.copy(enabledWidgets = enabled, availableWidgets = available, isLoading = false) }
            }
            .launchIn(viewModelScope)

        getWeatherLocationUseCase()
            .onEach { cityName -> _state.update { it.copy(weatherCityName = cityName) } }
            .launchIn(viewModelScope)
    }

    fun onSaveWeatherCity(cityName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSavingWeatherCity = true, weatherCityError = false) }
            val success = runCatching { setWeatherLocationUseCase(cityName) }.getOrDefault(false)
            _state.update { it.copy(isSavingWeatherCity = false, weatherCityError = !success) }
        }
    }

    fun onEnableWidget(type: DashboardWidgetType) {
        persist(_state.value.enabledWidgets + type)
    }

    fun onDisableWidget(type: DashboardWidgetType) {
        persist(_state.value.enabledWidgets - type)
    }

    fun onReorderEnabledWidgets(orderedWidgets: List<DashboardWidgetType>) {
        persist(orderedWidgets)
    }

    private fun persist(enabledWidgets: List<DashboardWidgetType>) {
        viewModelScope.launch {
            dashboardPreferencesManager.setEnabledWidgetKeys(enabledWidgets.map { it.name })
        }
    }

    private fun MutableStateFlow<DashboardSettingsState>.update(
        transform: (DashboardSettingsState) -> DashboardSettingsState
    ) {
        value = transform(value)
    }
}
