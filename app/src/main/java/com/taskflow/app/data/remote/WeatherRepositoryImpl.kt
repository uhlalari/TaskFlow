package com.taskflow.app.data.remote

import com.taskflow.app.data.local.preferences.WeatherLocationData
import com.taskflow.app.data.local.preferences.WeatherPreferencesManager
import com.taskflow.app.data.mapper.toCurrentWeatherDomain
import com.taskflow.app.data.mapper.toDomain
import com.taskflow.app.domain.model.CurrentWeather
import com.taskflow.app.domain.model.WeatherForecast
import com.taskflow.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WeatherRepositoryImpl(
    private val forecastApi: OpenMeteoForecastApi,
    private val geocodingApi: OpenMeteoGeocodingApi,
    private val preferencesManager: WeatherPreferencesManager
) : WeatherRepository {

    override val locationCityName: Flow<String?> = preferencesManager.location.map { it?.cityName }

    override suspend fun getCurrentWeather(): CurrentWeather? {
        val location = preferencesManager.location.first() ?: return null

        return runCatching {
            forecastApi.getDailyForecast(latitude = location.latitude, longitude = location.longitude)
                .toCurrentWeatherDomain()
        }.getOrNull()
    }

    override suspend fun getWeeklyForecast(): List<WeatherForecast> {
        val location = preferencesManager.location.first() ?: return emptyList()
        return runCatching {
            forecastApi.getDailyForecast(latitude = location.latitude, longitude = location.longitude).toDomain()
        }.getOrDefault(emptyList())
    }

    override suspend fun setLocation(cityName: String): Boolean {
        val result = runCatching { geocodingApi.search(cityName = cityName).results?.firstOrNull() }
            .getOrNull() ?: return false

        preferencesManager.setLocation(WeatherLocationData(cityName, result.latitude, result.longitude))
        return true
    }

    override suspend fun clearLocation() {
        preferencesManager.clearLocation()
    }
}
