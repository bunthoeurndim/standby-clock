package com.naomi.standby.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherInfo(
    val temperatureC: Double?,
    val condition: String,
    val isDay: Boolean
) {
    companion object {
        val UNAVAILABLE = WeatherInfo(null, "Weather unavailable", true)
    }
}

/**
 * Free Open-Meteo forecast API — no API key needed.
 * Falls back to Phnom Penh coordinates when no location is available,
 * and to [WeatherInfo.UNAVAILABLE] when offline or the request fails.
 */
object WeatherRepository {
    private fun lastLocation(context: Context): Pair<Double, Double> {
        val fallback = 11.55 to 104.92 // Phnom Penh
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return fallback
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (loc != null) loc.latitude to loc.longitude else fallback
        } catch (_: SecurityException) {
            fallback
        }
    }

    suspend fun fetchCurrent(context: Context): WeatherInfo = withContext(Dispatchers.IO) {
        try {
            val (lat, lon) = lastLocation(context)
            val url = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,weather_code,is_day&timezone=auto"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext WeatherInfo.UNAVAILABLE
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val current = JSONObject(body).optJSONObject("current") ?: return@withContext WeatherInfo.UNAVAILABLE
            val temp = if (current.has("temperature_2m")) current.getDouble("temperature_2m") else null
            val code = current.optInt("weather_code", -1)
            val isDay = current.optInt("is_day", 1) == 1
            WeatherInfo(temp, weatherCodeToText(code), isDay)
        } catch (_: Exception) {
            WeatherInfo.UNAVAILABLE
        }
    }

    fun weatherCodeToText(code: Int): String = when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing drizzle"
        61, 63, 65 -> "Rain"
        66, 67 -> "Freezing rain"
        71, 73, 75 -> "Snow"
        77 -> "Snow grains"
        80, 81, 82 -> "Rain showers"
        85, 86 -> "Snow showers"
        95 -> "Thunderstorm"
        96, 99 -> "Thunderstorm with hail"
        else -> "Weather unavailable"
    }
}
