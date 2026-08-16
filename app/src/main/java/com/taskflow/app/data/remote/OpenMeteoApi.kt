package com.taskflow.app.data.remote

import com.taskflow.app.data.remote.dto.ForecastResponseDto
import com.taskflow.app.data.remote.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoForecastApi {
    @GET("v1/forecast")
    suspend fun getDailyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = DEFAULT_CURRENT_FIELDS,
        @Query("daily") daily: String = DEFAULT_DAILY_FIELDS,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = FORECAST_DAYS
    ): ForecastResponseDto

    companion object {
        private const val FORECAST_DAYS = 7
        private const val DEFAULT_CURRENT_FIELDS = "temperature_2m,weathercode"
        private const val DEFAULT_DAILY_FIELDS =
            "weathercode,temperature_2m_max,temperature_2m_min," +
                "precipitation_probability_max,relative_humidity_2m_mean"
    }
}

interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") cityName: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "pt",
        @Query("format") format: String = "json"
    ): GeocodingResponseDto
}
