package com.taskflow.app.data.mapper

import com.taskflow.app.data.remote.dto.ForecastResponseDto
import com.taskflow.app.domain.model.CurrentWeather
import com.taskflow.app.domain.model.WeatherCondition
import com.taskflow.app.domain.model.WeatherForecast
import java.time.LocalDate
fun ForecastResponseDto.toCurrentWeatherDomain(): CurrentWeather? {
    val temperature = current?.temperature ?: return null
    return CurrentWeather(
        temperatureCelsius = temperature,
        condition = current.weatherCode.toWeatherCondition()
    )
}
fun ForecastResponseDto.toDomain(): List<WeatherForecast> {
    val daily = daily ?: return emptyList()

    return daily.time.indices.mapNotNull { index ->
        val date = runCatching { LocalDate.parse(daily.time[index]) }.getOrNull() ?: return@mapNotNull null
        val tempMax = daily.temperatureMax.getOrNull(index) ?: return@mapNotNull null
        val tempMin = daily.temperatureMin.getOrNull(index) ?: return@mapNotNull null

        WeatherForecast(
            date = date,
            condition = daily.weatherCode.getOrNull(index).toWeatherCondition(),
            temperatureMaxCelsius = tempMax,
            temperatureMinCelsius = tempMin,
            precipitationProbabilityPercent = daily.precipitationProbabilityMax.getOrNull(index) ?: 0,
            humidityPercent = daily.relativeHumidityMean.getOrNull(index) ?: 0
        )
    }
}
private fun Int?.toWeatherCondition(): WeatherCondition = when (this) {
    0 -> WeatherCondition.CLEAR
    1, 2 -> WeatherCondition.PARTLY_CLOUDY
    3 -> WeatherCondition.CLOUDY
    45, 48 -> WeatherCondition.FOG
    51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAIN
    71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
    95, 96, 99 -> WeatherCondition.THUNDERSTORM
    else -> WeatherCondition.CLOUDY
}
