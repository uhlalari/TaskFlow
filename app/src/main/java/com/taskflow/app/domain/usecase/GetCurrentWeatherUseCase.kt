package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.CurrentWeather
import com.taskflow.app.domain.repository.WeatherRepository

class GetCurrentWeatherUseCase(private val weatherRepository: WeatherRepository) {
    suspend operator fun invoke(): CurrentWeather? = weatherRepository.getCurrentWeather()
}
