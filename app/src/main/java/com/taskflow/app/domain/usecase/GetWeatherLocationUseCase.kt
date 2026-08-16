package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class GetWeatherLocationUseCase(private val weatherRepository: WeatherRepository) {
    operator fun invoke(): Flow<String?> = weatherRepository.locationCityName
}
