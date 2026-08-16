package com.taskflow.app.domain.model

import java.time.LocalDate

sealed interface WeatherAlert {
    data class HeavyRain(val date: LocalDate, val probabilityPercent: Int) : WeatherAlert
    data class LowHumidity(val date: LocalDate, val humidityPercent: Int) : WeatherAlert
    data class TemperatureSwing(
        val fromDate: LocalDate,
        val toDate: LocalDate,
        val deltaCelsius: Double
    ) : WeatherAlert
}
