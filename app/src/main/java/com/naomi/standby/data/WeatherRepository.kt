package com.naomi.standby.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class HourlyForecast(val hour: Int, val temperatureC: Double, val code: Int)

data class WeatherInfo(
    val temperatureC: Double?,
    val condition: String,
    val isDay: Boolean,
    val feelsLikeC: Double? = null,
    val humidityPct: Int? = null,
    val windKph: Double? = null,
    val highC: Double? = null,
    val lowC: Double? = null,
    val sunrise: String? = null,
    val sunset: String? = null,
    val hourly: List<HourlyForecast> = emptyList(),
    val cityName: String = "Current location"
) {
    companion object {
        val UNAVAILABLE = WeatherInfo(null, "Weather unavailable", true)
    }
}

/**
 * Free Open-Meteo forecast API — no API key needed.
 * The original app used WeatherAPI.com; this build uses Open-Meteo and
 * labels it honestly ("Powered by Open-Meteo").
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

    suspend fun fetchCurrent(context: Context): WeatherInfo = fetchFull(context)

    suspend fun fetchFull(context: Context): WeatherInfo = withContext(Dispatchers.IO) {
        try {
            val (lat, lon) = lastLocation(context)
            val usingFallback = lat == 11.55 && lon == 104.92
            val url = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m" +
                    "&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset" +
                    "&hourly=temperature_2m,weather_code&forecast_days=1&timezone=auto"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext WeatherInfo.UNAVAILABLE
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val root = JSONObject(body)
            val current = root.optJSONObject("current") ?: return@withContext WeatherInfo.UNAVAILABLE
            val daily = root.optJSONObject("daily")
            val hourlyObj = root.optJSONObject("hourly")
            val hourly = mutableListOf<HourlyForecast>()
            if (hourlyObj != null) {
                val times = hourlyObj.optJSONArray("time")
                val temps = hourlyObj.optJSONArray("temperature_2m")
                val codes = hourlyObj.optJSONArray("weather_code")
                if (times != null && temps != null && codes != null) {
                    val nowHour = java.time.LocalTime.now().hour
                    var start = 0
                    for (i in 0 until times.length()) {
                        val t = times.optString(i)
                        val h = t.substringAfter("T").substringBefore(":").toIntOrNull() ?: 0
                        if (h >= nowHour) { start = i; break }
                    }
                    for (i in start until minOf(start + 8, times.length())) {
                        val t = hourlyObj.optJSONArray("time")?.optString(i) ?: ""
                        val h = t.substringAfter("T").substringBefore(":").toIntOrNull() ?: 0
                        hourly.add(HourlyForecast(h, temps.optDouble(i), codes.optInt(i)))
                    }
                }
            }
            val sunriseRaw = daily?.optJSONArray("sunrise")?.optString(0)
            val sunsetRaw = daily?.optJSONArray("sunset")?.optString(0)
            WeatherInfo(
                temperatureC = if (current.has("temperature_2m")) current.getDouble("temperature_2m") else null,
                condition = weatherCodeToText(current.optInt("weather_code", -1)),
                isDay = current.optInt("is_day", 1) == 1,
                feelsLikeC = if (current.has("apparent_temperature")) current.getDouble("apparent_temperature") else null,
                humidityPct = if (current.has("relative_humidity_2m")) current.getInt("relative_humidity_2m") else null,
                windKph = if (current.has("wind_speed_10m")) current.getDouble("wind_speed_10m") else null,
                highC = daily?.optJSONArray("temperature_2m_max")?.optDouble(0),
                lowC = daily?.optJSONArray("temperature_2m_min")?.optDouble(0),
                sunrise = sunriseRaw?.substringAfter("T"),
                sunset = sunsetRaw?.substringAfter("T"),
                hourly = hourly,
                cityName = if (usingFallback) "Phnom Penh" else "Current location"
            )
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

/** Fetches the full forecast once and remembers it for the current composition. */
@Composable
fun rememberWeather(context: Context): WeatherInfo? {
    var weather by remember { mutableStateOf<WeatherInfo?>(null) }
    LaunchedEffect(Unit) { weather = WeatherRepository.fetchFull(context) }
    return weather
}
