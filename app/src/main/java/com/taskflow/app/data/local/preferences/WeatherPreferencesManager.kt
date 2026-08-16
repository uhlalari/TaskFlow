package com.taskflow.app.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.weatherDataStore by preferencesDataStore(name = "weather_prefs")
data class WeatherLocationData(val cityName: String, val latitude: Double, val longitude: Double)
interface WeatherPreferencesManager {
    val location: Flow<WeatherLocationData?>
    suspend fun setLocation(location: WeatherLocationData)
    suspend fun clearLocation()
}

class WeatherPreferencesManagerImpl(private val context: Context) : WeatherPreferencesManager {

    override val location: Flow<WeatherLocationData?> = context.weatherDataStore.data.map { prefs ->
        val cityName = prefs[CITY_NAME_KEY]
        val latitude = prefs[LATITUDE_KEY]
        val longitude = prefs[LONGITUDE_KEY]
        if (cityName == null || latitude == null || longitude == null) {
            null
        } else {
            WeatherLocationData(cityName, latitude, longitude)
        }
    }

    override suspend fun setLocation(location: WeatherLocationData) {
        context.weatherDataStore.edit { prefs ->
            prefs[CITY_NAME_KEY] = location.cityName
            prefs[LATITUDE_KEY] = location.latitude
            prefs[LONGITUDE_KEY] = location.longitude
        }
    }

    override suspend fun clearLocation() {
        context.weatherDataStore.edit { it.clear() }
    }

    private companion object {
        val CITY_NAME_KEY = stringPreferencesKey("city_name")
        val LATITUDE_KEY = doublePreferencesKey("latitude")
        val LONGITUDE_KEY = doublePreferencesKey("longitude")
    }
}
