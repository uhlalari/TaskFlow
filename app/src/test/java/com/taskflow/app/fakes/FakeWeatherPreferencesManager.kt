package com.taskflow.app.fakes

import com.taskflow.app.data.local.preferences.WeatherLocationData
import com.taskflow.app.data.local.preferences.WeatherPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeWeatherPreferencesManager(
    initialLocation: WeatherLocationData? = null
) : WeatherPreferencesManager {

    private val locationFlow = MutableStateFlow(initialLocation)

    override val location: StateFlow<WeatherLocationData?> = locationFlow

    override suspend fun setLocation(location: WeatherLocationData) {
        locationFlow.value = location
    }

    override suspend fun clearLocation() {
        locationFlow.value = null
    }
}
