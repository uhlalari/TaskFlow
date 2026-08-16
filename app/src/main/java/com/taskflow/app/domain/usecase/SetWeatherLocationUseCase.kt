package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.WeatherLocationValidationException
import com.taskflow.app.domain.repository.WeatherRepository

class SetWeatherLocationUseCase(private val weatherRepository: WeatherRepository) {
    suspend operator fun invoke(cityName: String): Boolean {
        val trimmedName = cityName.trim()
        if (trimmedName.isBlank()) throw WeatherLocationValidationException.EmptyName

        return weatherRepository.setLocation(trimmedName)
    }
}
