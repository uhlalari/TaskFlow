package com.taskflow.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ForecastResponseDto(
    val current: CurrentWeatherDto?,
    val daily: DailyForecastDto?
)

data class CurrentWeatherDto(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("weathercode") val weatherCode: Int?
)

data class DailyForecastDto(
    val time: List<String> = emptyList(),
    @SerializedName("weathercode") val weatherCode: List<Int> = emptyList(),
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double> = emptyList(),
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double> = emptyList(),
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int> = emptyList(),
    @SerializedName("relative_humidity_2m_mean") val relativeHumidityMean: List<Int> = emptyList()
)
