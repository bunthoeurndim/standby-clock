package com.naomi.standby.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.naomi.standby.model.FaceTheme

/** Night-mode behaviour. */
enum class NightMode { OFF, AUTO, MANUAL }

/**
 * App settings, backed by SharedPreferences so the charging receiver
 * (which runs with no UI) can read the auto-start flags synchronously.
 * Compose screens observe the [mutableStateOf] fields.
 */
class SettingsStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("standby_settings", Context.MODE_PRIVATE)

    var onboarded by mutableStateOf(prefs.getBoolean("onboarded", false))
        private set
    var faceId by mutableStateOf(prefs.getString("face_id", null) ?: "big_digital_white_large")
        private set
    var autoStartCharging by mutableStateOf(prefs.getBoolean("auto_start_charging", true))
        private set
    var closeOnUnplug by mutableStateOf(prefs.getBoolean("close_on_unplug", true))
        private set
    var nightMode by mutableStateOf(
        NightMode.valueOf(prefs.getString("night_mode", NightMode.AUTO.name) ?: NightMode.AUTO.name)
    )
        private set
    var nightStartMinutes by mutableStateOf(prefs.getInt("night_start_minutes", 22 * 60))
        private set
    var nightEndMinutes by mutableStateOf(prefs.getInt("night_end_minutes", 7 * 60))
        private set
    var useLightSensor by mutableStateOf(prefs.getBoolean("use_light_sensor", false))
        private set
    var sleepTimerMinutes by mutableStateOf(prefs.getInt("sleep_timer_minutes", 0))
        private set
    var dayBrightness by mutableStateOf(prefs.getInt("day_brightness", 100))
        private set
    var nightBrightness by mutableStateOf(prefs.getInt("night_brightness", 30))
        private set
    var fontScale by mutableStateOf(prefs.getFloat("font_scale", 1.0f))
        private set
    var widgetIndex by mutableStateOf(prefs.getInt("widget_index", 0))
        private set
    var photoUris by mutableStateOf(
        prefs.getStringSet("photo_uris", emptySet())?.toList() ?: emptyList()
    )
        private set

    private fun edit(block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
    }

    fun setOnboarded(value: Boolean) { onboarded = value; edit { putBoolean("onboarded", value) } }
    fun setFaceId(value: String) { faceId = value; edit { putString("face_id", value) } }
    fun setAutoStartCharging(value: Boolean) { autoStartCharging = value; edit { putBoolean("auto_start_charging", value) } }
    fun setCloseOnUnplug(value: Boolean) { closeOnUnplug = value; edit { putBoolean("close_on_unplug", value) } }
    fun setNightMode(value: NightMode) { nightMode = value; edit { putString("night_mode", value.name) } }
    fun setNightStartMinutes(value: Int) { nightStartMinutes = value; edit { putInt("night_start_minutes", value) } }
    fun setNightEndMinutes(value: Int) { nightEndMinutes = value; edit { putInt("night_end_minutes", value) } }
    fun setUseLightSensor(value: Boolean) { useLightSensor = value; edit { putBoolean("use_light_sensor", value) } }
    fun setSleepTimerMinutes(value: Int) { sleepTimerMinutes = value; edit { putInt("sleep_timer_minutes", value) } }
    fun setDayBrightness(value: Int) { dayBrightness = value; edit { putInt("day_brightness", value) } }
    fun setNightBrightness(value: Int) { nightBrightness = value; edit { putInt("night_brightness", value) } }
    fun setFontScale(value: Float) { fontScale = value; edit { putFloat("font_scale", value) } }
    fun setWidgetIndex(value: Int) { widgetIndex = value; edit { putInt("widget_index", value) } }
    fun setPhotoUris(value: List<String>) {
        photoUris = value
        edit { putStringSet("photo_uris", value.toSet()) }
    }

    /** Whether night mode is active right now, given the clock and sensor hint. */
    fun isNightNow(hour: Int, minute: Int, darkBySensor: Boolean): Boolean {
        return when (nightMode) {
            NightMode.OFF -> false
            NightMode.MANUAL -> true
            NightMode.AUTO -> {
                if (useLightSensor && darkBySensor) return true
                val now = hour * 60 + minute
                if (nightStartMinutes <= nightEndMinutes) {
                    now in nightStartMinutes until nightEndMinutes
                } else {
                    now >= nightStartMinutes || now < nightEndMinutes
                }
            }
        }
    }

    companion object {
        fun themeForFaceId(faceId: String): FaceTheme {
            return FaceTheme.entries.firstOrNull { faceId.contains(it.idPart) } ?: FaceTheme.WHITE
        }
    }
}

/** Widget shown on the second half of the Duo layout. */
enum class WidgetType(val displayName: String) {
    CALENDAR("Calendar"),
    BATTERY("Battery"),
    NEXT_ALARM("Next alarm"),
    WEATHER("Weather"),
    MUSIC("Music"),
    PHOTOS("Photo frame"),
    TIMER("Timer"),
    POMODORO("Pomodoro")
}

fun formatMinutesOfDay(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return "%02d:%02d".format(h, m)
}

fun Color.toHex(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return "#%02X%02X%02X".format(r, g, b)
}
