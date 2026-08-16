package com.taskflow.app.presentation.dashboard

data class DashboardSettingsState(
    val enabledWidgets: List<DashboardWidgetType> = emptyList(),
    val availableWidgets: List<DashboardWidgetType> = emptyList(),
    val isLoading: Boolean = true,
    val weatherCityName: String? = null,
    val isSavingWeatherCity: Boolean = false,
    val weatherCityError: Boolean = false
)
