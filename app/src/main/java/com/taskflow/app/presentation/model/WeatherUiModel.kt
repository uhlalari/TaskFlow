package com.taskflow.app.presentation.model

import androidx.compose.runtime.Immutable
import com.taskflow.app.domain.model.WeatherCondition

@Immutable
data class WeatherUiModel(
    val condition: WeatherCondition,
    val temperatureLabel: String
)
