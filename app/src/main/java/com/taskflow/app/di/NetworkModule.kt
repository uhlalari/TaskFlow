package com.taskflow.app.di

import com.taskflow.app.data.remote.OpenMeteoForecastApi
import com.taskflow.app.data.remote.OpenMeteoGeocodingApi
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val FORECAST_BASE_URL = "https://api.open-meteo.com/"
private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
private const val NETWORK_TIMEOUT_SECONDS = 10L

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    single<OpenMeteoForecastApi> {
        Retrofit.Builder()
            .baseUrl(FORECAST_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoForecastApi::class.java)
    }

    single<OpenMeteoGeocodingApi> {
        Retrofit.Builder()
            .baseUrl(GEOCODING_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenMeteoGeocodingApi::class.java)
    }
}
