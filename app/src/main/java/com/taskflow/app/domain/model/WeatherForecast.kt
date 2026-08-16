package com.taskflow.app.domain.model

import java.time.LocalDate

data class WeatherForecast(
    val date: LocalDate,
    val condition: WeatherCondition,
    val temperatureMaxCelsius: Double,
    val temperatureMinCelsius: Double,
    val precipitationProbabilityPercent: Int,
    val humidityPercent: Int
)

enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    RAIN,
    THUNDERSTORM,
    SNOW
}
