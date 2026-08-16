package com.taskflow.app.fakes

import com.taskflow.app.domain.model.CurrentWeather
import com.taskflow.app.domain.model.WeatherForecast
import com.taskflow.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeWeatherRepository(
    private val currentWeather: CurrentWeather? = null,
    private val weeklyForecast: List<WeatherForecast> = emptyList(),
    initialCityName: String? = null,
    private val setLocationResult: Boolean = true
) : WeatherRepository {

    private val cityNameFlow = MutableStateFlow(initialCityName)

    // Mesma regra do WeatherRepositoryImpl real: sem cidade configurada, "sem
    // clima" — importante para simular o cenário de configurar a cidade depois de
    // o ViewModel já ter sido criado (ver TaskListViewModelTest).
    override suspend fun getCurrentWeather(): CurrentWeather? =
        if (cityNameFlow.value != null) currentWeather else null

    override suspend fun getWeeklyForecast(): List<WeatherForecast> =
        if (cityNameFlow.value != null) weeklyForecast else emptyList()

    override val locationCityName: StateFlow<String?> = cityNameFlow

    override suspend fun setLocation(cityName: String): Boolean {
        if (setLocationResult) cityNameFlow.value = cityName
        return setLocationResult
    }

    override suspend fun clearLocation() {
        cityNameFlow.value = null
    }
}
