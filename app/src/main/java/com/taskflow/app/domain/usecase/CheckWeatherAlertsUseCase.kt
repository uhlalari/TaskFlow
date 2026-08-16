package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.WeatherAlert
import com.taskflow.app.domain.repository.WeatherRepository
import kotlin.math.abs


private const val HEAVY_RAIN_THRESHOLD_PERCENT = 70


private const val LOW_HUMIDITY_THRESHOLD_PERCENT = 30

private const val TEMPERATURE_SWING_THRESHOLD_CELSIUS = 8.0

class CheckWeatherAlertsUseCase(private val weatherRepository: WeatherRepository) {
    suspend operator fun invoke(): List<WeatherAlert> {
        val forecast = weatherRepository.getWeeklyForecast().sortedBy { it.date }
        if (forecast.isEmpty()) return emptyList()

        val singleDayAlerts = forecast.flatMap { day ->
            listOfNotNull(
                WeatherAlert.HeavyRain(day.date, day.precipitationProbabilityPercent)
                    .takeIf { day.precipitationProbabilityPercent >= HEAVY_RAIN_THRESHOLD_PERCENT },
                WeatherAlert.LowHumidity(day.date, day.humidityPercent)
                    .takeIf { day.humidityPercent <= LOW_HUMIDITY_THRESHOLD_PERCENT }
            )
        }

        val temperatureSwingAlerts = forecast.zipWithNext().mapNotNull { (today, nextDay) ->
            val delta = nextDay.temperatureMaxCelsius - today.temperatureMaxCelsius
            WeatherAlert.TemperatureSwing(today.date, nextDay.date, delta)
                .takeIf { abs(delta) >= TEMPERATURE_SWING_THRESHOLD_CELSIUS }
        }

        return singleDayAlerts + temperatureSwingAlerts
    }
}
