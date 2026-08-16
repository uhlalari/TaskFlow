package com.taskflow.app.domain.model

data class CurrentWeather(
    val temperatureCelsius: Double,
    val condition: WeatherCondition
)
