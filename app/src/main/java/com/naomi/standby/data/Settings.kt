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

    // NOTE: each setting is a private mutable state plus a public read-only
    // property. The explicit setXxx() functions below are the only setters.
    // (Declaring `var x by mutableStateOf(...) private set` AND a fun setX()
    // generates the same JVM setter twice — "Platform declaration clash".)
    private var onboardedState by mutableStateOf(prefs.getBoolean("onboarded", false))
    val onboarded: Boolean get() = onboardedState
    private var faceIdState by mutableStateOf(prefs.getString("face_id", null) ?: "big_digital_white_large")
    val faceId: String get() = faceIdState
    private var autoStartChargingState by mutableStateOf(prefs.getBoolean("auto_start_charging", true))
    val autoStartCharging: Boolean get() = autoStartChargingState
    private var closeOnUnplugState by mutableStateOf(prefs.getBoolean("close_on_unplug", true))
    val closeOnUnplug: Boolean get() = closeOnUnplugState
    private var nightModeState by mutableStateOf(
        NightMode.valueOf(prefs.getString("night_mode", NightMode.AUTO.name) ?: NightMode.AUTO.name)
    )
    val nightMode: NightMode get() = nightModeState
    private var nightStartMinutesState by mutableStateOf(prefs.getInt("night_start_minutes", 22 * 60))
    val nightStartMinutes: Int get() = nightStartMinutesState
    private var nightEndMinutesState by mutableStateOf(prefs.getInt("night_end_minutes", 7 * 60))
    val nightEndMinutes: Int get() = nightEndMinutesState
    private var useLightSensorState by mutableStateOf(prefs.getBoolean("use_light_sensor", false))
    val useLightSensor: Boolean get() = useLightSensorState
    private var sleepTimerMinutesState by mutableStateOf(prefs.getInt("sleep_timer_minutes", 0))
    val sleepTimerMinutes: Int get() = sleepTimerMinutesState
    private var dayBrightnessState by mutableStateOf(prefs.getInt("day_brightness", 100))
    val dayBrightness: Int get() = dayBrightnessState
    private var nightBrightnessState by mutableStateOf(prefs.getInt("night_brightness", 30))
    val nightBrightness: Int get() = nightBrightnessState
    private var fontScaleState by mutableStateOf(prefs.getFloat("font_scale", 1.0f))
    val fontScale: Float get() = fontScaleState
    private var widgetIndexState by mutableStateOf(prefs.getInt("widget_index", 0))
    val widgetIndex: Int get() = widgetIndexState
    private var photoUrisState by mutableStateOf(
        prefs.getStringSet("photo_uris", emptySet())?.toList() ?: emptyList()
    )
    val photoUris: List<String> get() = photoUrisState

    private fun edit(block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
    }

    fun setOnboarded(value: Boolean) { onboardedState = value; edit { putBoolean("onboarded", value) } }
    fun setFaceId(value: String) { faceIdState = value; edit { putString("face_id", value) } }
    fun setAutoStartCharging(value: Boolean) { autoStartChargingState = value; edit { putBoolean("auto_start_charging", value) } }
    fun setCloseOnUnplug(value: Boolean) { closeOnUnplugState = value; edit { putBoolean("close_on_unplug", value) } }
    fun setNightMode(value: NightMode) { nightModeState = value; edit { putString("night_mode", value.name) } }
    fun setNightStartMinutes(value: Int) { nightStartMinutesState = value; edit { putInt("night_start_minutes", value) } }
    fun setNightEndMinutes(value: Int) { nightEndMinutesState = value; edit { putInt("night_end_minutes", value) } }
    fun setUseLightSensor(value: Boolean) { useLightSensorState = value; edit { putBoolean("use_light_sensor", value) } }
    fun setSleepTimerMinutes(value: Int) { sleepTimerMinutesState = value; edit { putInt("sleep_timer_minutes", value) } }
    fun setDayBrightness(value: Int) { dayBrightnessState = value; edit { putInt("day_brightness", value) } }
    fun setNightBrightness(value: Int) { nightBrightnessState = value; edit { putInt("night_brightness", value) } }
    fun setFontScale(value: Float) { fontScaleState = value; edit { putFloat("font_scale", value) } }
    fun setWidgetIndex(value: Int) { widgetIndexState = value; edit { putInt("widget_index", value) } }
    fun setPhotoUris(value: List<String>) {
        photoUrisState = value
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
