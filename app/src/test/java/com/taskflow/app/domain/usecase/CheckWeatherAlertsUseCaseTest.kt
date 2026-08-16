package com.taskflow.app.domain.usecase

import com.taskflow.app.domain.model.WeatherAlert
import com.taskflow.app.domain.model.WeatherCondition
import com.taskflow.app.domain.model.WeatherForecast
import com.taskflow.app.fakes.FakeWeatherRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CheckWeatherAlertsUseCaseTest {

    @Test
    fun `sem previsao nao gera alertas`() = runTest {
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(emptyList()))

        assertTrue(useCase().isEmpty())
    }

    @Test
    fun `chuva acima do limiar gera alerta de chuva forte`() = runTest {
        val forecast = listOf(day(daysFromNow = 0, precipitationProbability = 80))
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        val alerts = useCase()

        assertTrue(alerts.any { it is WeatherAlert.HeavyRain })
    }

    @Test
    fun `chuva abaixo do limiar nao gera alerta`() = runTest {
        val forecast = listOf(day(daysFromNow = 0, precipitationProbability = 40))
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        assertTrue(useCase().none { it is WeatherAlert.HeavyRain })
    }

    @Test
    fun `umidade abaixo do limiar gera alerta de umidade baixa`() = runTest {
        val forecast = listOf(day(daysFromNow = 0, humidity = 20))
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        val alerts = useCase()

        assertTrue(alerts.any { it is WeatherAlert.LowHumidity })
    }

    @Test
    fun `salto de temperatura entre dias consecutivos gera alerta`() = runTest {
        val forecast = listOf(
            day(daysFromNow = 0, tempMax = 20.0),
            day(daysFromNow = 1, tempMax = 32.0)
        )
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        val alerts = useCase()

        assertTrue(alerts.any { it is WeatherAlert.TemperatureSwing })
    }

    @Test
    fun `variacao pequena de temperatura nao gera alerta`() = runTest {
        val forecast = listOf(
            day(daysFromNow = 0, tempMax = 20.0),
            day(daysFromNow = 1, tempMax = 24.0)
        )
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        assertTrue(useCase().none { it is WeatherAlert.TemperatureSwing })
    }

    @Test
    fun `queda de temperatura (nao so subida) tambem gera alerta`() = runTest {
        val forecast = listOf(
            day(daysFromNow = 0, tempMax = 32.0),
            day(daysFromNow = 1, tempMax = 20.0)
        )
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        assertTrue(useCase().any { it is WeatherAlert.TemperatureSwing })
    }

    // --- Casos de limite: exatamente no limiar (inclusivo) e um passo abaixo dele ---

    @Test
    fun `chuva exatamente no limiar de 70 por cento gera alerta`() = runTest {
        val forecast = listOf(day(daysFromNow = 0, precipitationProbability = 70))
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        assertTrue(useCase().any { it is WeatherAlert.HeavyRain })
    }

    @Test
    fun `chuva um ponto abaixo do limiar nao gera alerta`() = runTest {
        val forecast = listOf(day(daysFromNow = 0, precipitationProbability = 69))
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        assertTrue(useCase().none { it is WeatherAlert.HeavyRain })
    }

    @Test
    fun `umidade exatamente no limiar de 30 por cento gera alerta`() = runTest {
        val forecast = listOf(day(daysFromNow = 0, humidity = 30))
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        assertTrue(useCase().any { it is WeatherAlert.LowHumidity })
    }

    @Test
    fun `umidade um ponto acima do limiar nao gera alerta`() = runTest {
        val forecast = listOf(day(daysFromNow = 0, humidity = 31))
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        assertTrue(useCase().none { it is WeatherAlert.LowHumidity })
    }

    @Test
    fun `variacao de temperatura exatamente 8 graus gera alerta`() = runTest {
        val forecast = listOf(
            day(daysFromNow = 0, tempMax = 20.0),
            day(daysFromNow = 1, tempMax = 28.0)
        )
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        assertTrue(useCase().any { it is WeatherAlert.TemperatureSwing })
    }

    @Test
    fun `variacao de temperatura 0,1 grau abaixo do limiar nao gera alerta`() = runTest {
        val forecast = listOf(
            day(daysFromNow = 0, tempMax = 20.0),
            day(daysFromNow = 1, tempMax = 27.9)
        )
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        assertTrue(useCase().none { it is WeatherAlert.TemperatureSwing })
    }

    @Test
    fun `previsao com um unico dia nao quebra e nao gera alerta de variacao`() = runTest {
        val forecast = listOf(day(daysFromNow = 0))
        val useCase = CheckWeatherAlertsUseCase(fakeRepository(forecast))

        assertTrue(useCase().none { it is WeatherAlert.TemperatureSwing })
    }

    /** `initialCityName` sempre preenchido: `FakeWeatherRepository` só retorna a
     * previsão configurada quando há uma cidade, espelhando `WeatherRepositoryImpl`
     * de verdade — sem isso, `getWeeklyForecast()` sempre voltaria vazio. */
    private fun fakeRepository(forecast: List<WeatherForecast>) =
        FakeWeatherRepository(weeklyForecast = forecast, initialCityName = "Cidade Teste")

    private fun day(
        daysFromNow: Long,
        tempMax: Double = 25.0,
        tempMin: Double = 15.0,
        precipitationProbability: Int = 10,
        humidity: Int = 60
    ) = WeatherForecast(
        date = LocalDate.now().plusDays(daysFromNow),
        condition = WeatherCondition.CLEAR,
        temperatureMaxCelsius = tempMax,
        temperatureMinCelsius = tempMin,
        precipitationProbabilityPercent = precipitationProbability,
        humidityPercent = humidity
    )
}
