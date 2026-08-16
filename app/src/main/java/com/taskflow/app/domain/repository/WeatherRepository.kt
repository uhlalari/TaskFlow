package com.taskflow.app.domain.repository

import com.taskflow.app.domain.model.CurrentWeather
import com.taskflow.app.domain.model.WeatherForecast
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getCurrentWeather(): CurrentWeather?
    suspend fun getWeeklyForecast(): List<WeatherForecast>
    val locationCityName: Flow<String?>
    suspend fun setLocation(cityName: String): Boolean
    suspend fun clearLocation()
}
