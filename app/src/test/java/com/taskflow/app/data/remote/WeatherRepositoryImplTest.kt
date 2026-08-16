package com.taskflow.app.data.remote

import com.taskflow.app.data.local.preferences.WeatherLocationData
import com.taskflow.app.fakes.FakeWeatherPreferencesManager
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Testa `WeatherRepositoryImpl` contra um servidor HTTP real em memória
 * (`MockWebServer`), não contra a Open-Meteo de verdade — cobre a integração
 * Retrofit + Gson + mapeamento de ponta a ponta sem depender de rede externa
 * instável durante o build/CI.
 */
class WeatherRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var preferencesManager: FakeWeatherPreferencesManager
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        preferencesManager = FakeWeatherPreferencesManager()
        repository = WeatherRepositoryImpl(
            forecastApi = retrofit.create(OpenMeteoForecastApi::class.java),
            geocodingApi = retrofit.create(OpenMeteoGeocodingApi::class.java),
            preferencesManager = preferencesManager
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getCurrentWeather sem localizacao configurada retorna null`() = runTest {
        assertNull(repository.getCurrentWeather())
    }

    @Test
    fun `getCurrentWeather mapeia a temperatura de agora, nao a maxima do dia`() = runTest {
        preferencesManager.setLocation(WeatherLocationData("Belo Horizonte", -19.92, -43.94))
        server.enqueue(MockResponse().setBody(FORECAST_RESPONSE_JSON))

        val result = repository.getCurrentWeather()

        assertEquals(20.3, result?.temperatureCelsius ?: 0.0, 0.01)
    }

    @Test
    fun `getCurrentWeather com erro 500 do servidor retorna null`() = runTest {
        preferencesManager.setLocation(WeatherLocationData("Belo Horizonte", -19.92, -43.94))
        server.enqueue(MockResponse().setResponseCode(500))

        assertNull(repository.getCurrentWeather())
    }

    @Test
    fun `getWeeklyForecast sem localizacao configurada retorna lista vazia`() = runTest {
        assertTrue(repository.getWeeklyForecast().isEmpty())
    }

    @Test
    fun `getWeeklyForecast mapeia a resposta da API`() = runTest {
        preferencesManager.setLocation(WeatherLocationData("São Paulo", -23.55, -46.63))
        server.enqueue(MockResponse().setBody(FORECAST_RESPONSE_JSON))

        val result = repository.getWeeklyForecast()

        assertEquals(1, result.size)
        assertEquals(28.5, result.first().temperatureMaxCelsius, 0.01)
    }

    @Test
    fun `getWeeklyForecast com erro 500 do servidor retorna lista vazia`() = runTest {
        preferencesManager.setLocation(WeatherLocationData("São Paulo", -23.55, -46.63))
        server.enqueue(MockResponse().setResponseCode(500))

        assertTrue(repository.getWeeklyForecast().isEmpty())
    }

    @Test
    fun `getWeeklyForecast com 404 retorna lista vazia sem propagar excecao`() = runTest {
        preferencesManager.setLocation(WeatherLocationData("São Paulo", -23.55, -46.63))
        server.enqueue(MockResponse().setResponseCode(404))

        assertTrue(repository.getWeeklyForecast().isEmpty())
    }

    @Test
    fun `getWeeklyForecast com corpo em formato inesperado retorna lista vazia`() = runTest {
        preferencesManager.setLocation(WeatherLocationData("São Paulo", -23.55, -46.63))
        server.enqueue(MockResponse().setBody("""{"isso": "nao é o formato esperado"}"""))

        assertTrue(repository.getWeeklyForecast().isEmpty())
    }

    @Test
    fun `setLocation com falha 500 na geocodificacao retorna false`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val success = repository.setLocation("São Paulo")

        assertFalse(success)
        assertNull(preferencesManager.location.value)
    }

    @Test
    fun `setLocation com cidade encontrada persiste coordenadas e retorna true`() = runTest {
        server.enqueue(MockResponse().setBody(GEOCODING_RESPONSE_JSON))

        val success = repository.setLocation("São Paulo")

        assertTrue(success)
        assertEquals("São Paulo", preferencesManager.location.value?.cityName)
    }

    @Test
    fun `setLocation com cidade nao encontrada retorna false e nao altera localizacao salva`() = runTest {
        server.enqueue(MockResponse().setBody("""{"results": []}"""))

        val success = repository.setLocation("Cidade Que Não Existe")

        assertFalse(success)
        assertNull(preferencesManager.location.value)
    }

    private companion object {
        val FORECAST_RESPONSE_JSON = """
            {
                "current": {
                    "temperature_2m": 20.3,
                    "weathercode": 61
                },
                "daily": {
                    "time": ["2024-01-01"],
                    "weathercode": [0],
                    "temperature_2m_max": [28.5],
                    "temperature_2m_min": [18.0],
                    "precipitation_probability_max": [10],
                    "relative_humidity_2m_mean": [55]
                }
            }
        """.trimIndent()

        val GEOCODING_RESPONSE_JSON = """
            {
                "results": [
                    {"name": "São Paulo", "latitude": -23.55, "longitude": -46.63}
                ]
            }
        """.trimIndent()
    }
}
