package com.taskflow.app.data.mapper

import com.taskflow.app.data.remote.dto.CurrentWeatherDto
import com.taskflow.app.data.remote.dto.DailyForecastDto
import com.taskflow.app.data.remote.dto.ForecastResponseDto
import com.taskflow.app.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeatherMapperTest {

    @Test
    fun `daily nulo retorna lista vazia`() {
        assertTrue(ForecastResponseDto(current = null, daily = null).toDomain().isEmpty())
    }

    @Test
    fun `mapeia campos por indice nas listas paralelas`() {
        val dto = ForecastResponseDto(
            current = null,
            daily = DailyForecastDto(
                time = listOf("2024-01-01", "2024-01-02"),
                weatherCode = listOf(0, 61),
                temperatureMax = listOf(28.5, 22.0),
                temperatureMin = listOf(18.0, 16.5),
                precipitationProbabilityMax = listOf(5, 80),
                relativeHumidityMean = listOf(55, 90)
            )
        )

        val result = dto.toDomain()

        assertEquals(2, result.size)
        assertEquals(LocalDate.of(2024, 1, 1), result[0].date)
        assertEquals(WeatherCondition.CLEAR, result[0].condition)
        assertEquals(28.5, result[0].temperatureMaxCelsius, 0.01)
        assertEquals(WeatherCondition.RAIN, result[1].condition)
        assertEquals(80, result[1].precipitationProbabilityPercent)
    }

    @Test
    fun `dia com data invalida e descartado sem quebrar os demais`() {
        val dto = ForecastResponseDto(
            current = null,
            daily = DailyForecastDto(
                time = listOf("data-invalida", "2024-01-02"),
                weatherCode = listOf(0, 1),
                temperatureMax = listOf(28.5, 22.0),
                temperatureMin = listOf(18.0, 16.5),
                precipitationProbabilityMax = listOf(5, 10),
                relativeHumidityMean = listOf(55, 60)
            )
        )

        val result = dto.toDomain()

        assertEquals(1, result.size)
        assertEquals(LocalDate.of(2024, 1, 2), result[0].date)
    }

    @Test
    fun `dia sem temperatura maxima e descartado`() {
        val dto = ForecastResponseDto(
            current = null,
            daily = DailyForecastDto(
                time = listOf("2024-01-01", "2024-01-02"),
                weatherCode = listOf(0, 1),
                temperatureMax = listOf(28.5), // lista mais curta que `time`
                temperatureMin = listOf(18.0, 16.5),
                precipitationProbabilityMax = listOf(5, 10),
                relativeHumidityMean = listOf(55, 60)
            )
        )

        val result = dto.toDomain()

        assertEquals(1, result.size)
        assertEquals(LocalDate.of(2024, 1, 1), result[0].date)
    }

    @Test
    fun `codigo climatico desconhecido cai para nublado`() {
        val dto = ForecastResponseDto(
            current = null,
            daily = DailyForecastDto(
                time = listOf("2024-01-01"),
                weatherCode = listOf(999),
                temperatureMax = listOf(25.0),
                temperatureMin = listOf(15.0),
                precipitationProbabilityMax = listOf(0),
                relativeHumidityMean = listOf(50)
            )
        )

        assertEquals(WeatherCondition.CLOUDY, dto.toDomain().single().condition)
    }

    @Test
    fun `current nulo retorna null`() {
        val dto = ForecastResponseDto(current = null, daily = null)

        assertNull(dto.toCurrentWeatherDomain())
    }

    @Test
    fun `current sem temperatura retorna null`() {
        val dto = ForecastResponseDto(
            current = CurrentWeatherDto(temperature = null, weatherCode = 0),
            daily = null
        )

        assertNull(dto.toCurrentWeatherDomain())
    }

    @Test
    fun `mapeia temperatura e condicao de agora, nao a maxima do dia`() {
        val dto = ForecastResponseDto(
            current = CurrentWeatherDto(temperature = 20.3, weatherCode = 61),
            daily = DailyForecastDto(
                time = listOf("2024-01-01"),
                weatherCode = listOf(0),
                temperatureMax = listOf(28.0),
                temperatureMin = listOf(18.0),
                precipitationProbabilityMax = listOf(10),
                relativeHumidityMean = listOf(50)
            )
        )

        val current = dto.toCurrentWeatherDomain()

        assertEquals(20.3, current?.temperatureCelsius ?: 0.0, 0.01)
        assertEquals(WeatherCondition.RAIN, current?.condition)
    }

    @Test
    fun `current com codigo climatico ausente cai para nublado`() {
        val dto = ForecastResponseDto(
            current = CurrentWeatherDto(temperature = 18.0, weatherCode = null),
            daily = null
        )

        assertEquals(WeatherCondition.CLOUDY, dto.toCurrentWeatherDomain()?.condition)
    }
}
