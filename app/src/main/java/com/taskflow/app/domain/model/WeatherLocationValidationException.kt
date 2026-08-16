package com.taskflow.app.domain.model

sealed class WeatherLocationValidationException(message: String) : Exception(message) {
    data object EmptyName : WeatherLocationValidationException("O nome da cidade não pode ser vazio")
}
