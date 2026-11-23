package com.naomi.standby.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** Night-mode behaviour. */
enum class NightMode { OFF, AUTO, MANUAL }

/** Which Duo slot the widget picker is editing. */
enum class DuoSlot { LEFT, RIGHT }

/**
 * App settings, backed by SharedPreferences so the charging receiver
 * (which runs with no UI) can read the auto-start flags synchronously.
 * Compose screens observe the [mutableStateOf] fields.
 *
 * Pattern (do not change): each setting is a private mutable state plus a
 * public read-only property; the explicit setXxx() functions are the only
 * setters. Declaring `var x by mutableStateOf(...) private set` AND a
 * `fun setX()` generates the same JVM setter twice — "Platform
 * declaration clash" — which failed build run #2 in v1.
 */
class SettingsStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("standby_settings", Context.MODE_PRIVATE)

    private var onboardedState by mutableStateOf(prefs.getBoolean("onboarded", false))
    val onboarded: Boolean get() = onboardedState

    // Duo slots. Empty string = an empty slot ("Customize this space").
    private var leftScreenIdState by mutableStateOf(prefs.getString("left_screen_id", null) ?: "big_crop_clock_duo")
    val leftScreenId: String get() = leftScreenIdState
    private var rightScreenIdState by mutableStateOf(prefs.getString("right_screen_id", null) ?: "calendar")
    val rightScreenId: String get() = rightScreenIdState

    // Face styles, by enum name.
    private var retroflipStyleState by mutableStateOf(prefs.getString("retroflip_style", null) ?: "PURPLE_AND_WHITE")
    val retroflipStyle: String get() = retroflipStyleState
    private var bigCropThemeState by mutableStateOf(prefs.getString("big_crop_theme", null) ?: "NEON_LIGHTS")
    val bigCropTheme: String get() = bigCropThemeState
    private var solarStyleState by mutableStateOf(prefs.getString("solar_style", null) ?: "STYLE1")
    val solarStyle: String get() = solarStyleState
    private var segmentedFontState by mutableStateOf(prefs.getString("segmented_font", null) ?: "SEGMENTS_7_CLASSIC_REGULAR")
    val segmentedFont: String get() = segmentedFontState
    private var chargingThemeState by mutableStateOf(prefs.getString("charging_theme", null) ?: "MATERIAL")
    val chargingTheme: String get() = chargingThemeState

    private var showSecondsState by mutableStateOf(prefs.getBoolean("show_seconds", true))
    val showSeconds: Boolean get() = showSecondsState
    private var blinkSeparatorState by mutableStateOf(prefs.getBoolean("blink_separator", false))
    val blinkSeparator: Boolean get() = blinkSeparatorState
    private var burnInProtectionState by mutableStateOf(prefs.getBoolean("burn_in_protection", true))
    val burnInProtection: Boolean get() = burnInProtectionState
    private var hapticFeedbackState by mutableStateOf(prefs.getBoolean("haptic_feedback", true))
    val hapticFeedback: Boolean get() = hapticFeedbackState
    private var showClockOnChargingState by mutableStateOf(prefs.getBoolean("show_clock_on_charging", true))
    val showClockOnCharging: Boolean get() = showClockOnChargingState
    private var focusModeState by mutableStateOf(prefs.getBoolean("focus_mode", false))
    val focusMode: Boolean get() = focusModeState
    private var fullscreenNotificationsState by mutableStateOf(prefs.getBoolean("fullscreen_notifications", false))
    val fullscreenNotifications: Boolean get() = fullscreenNotificationsState

    private var milestoneTargetState by mutableStateOf(prefs.getInt("milestone_target", 0))
    /** 0 = off, otherwise the charge % to celebrate (e.g. 80). */
    val milestoneTarget: Int get() = milestoneTargetState
    private var photoIntervalSecondsState by mutableStateOf(prefs.getInt("photo_interval_seconds", 8))
    val photoIntervalSeconds: Int get() = photoIntervalSecondsState
    private var startOfWeekState by mutableStateOf(prefs.getInt("start_of_week", 0))
    /** 0 = Sunday, 1 = Monday. */
    val startOfWeek: Int get() = startOfWeekState
    private var quoteIndexState by mutableStateOf(prefs.getInt("quote_index", 0))
    val quoteIndex: Int get() = quoteIndexState

    private var autoStartChargingState by mutableStateOf(prefs.getBoolean("auto_start_charging", true))
    val autoStartCharging: Boolean get() = autoStartChargingState
    private var closeOnUnplugState by mutableStateOf(prefs.getBoolean("close_on_unplug", true))
    val closeOnUnplug: Boolean get() = closeOnUnplugState
    private var autoCloseDoubleTapState by mutableStateOf(prefs.getBoolean("auto_close_double_tap", false))
    val autoCloseDoubleTap: Boolean get() = autoCloseDoubleTapState

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
    private var photoUrisState by mutableStateOf(
        prefs.getStringSet("photo_uris", emptySet())?.toList() ?: emptyList()
    )
    val photoUris: List<String> get() = photoUrisState

    private fun edit(block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
    }

    fun setOnboarded(value: Boolean) { onboardedState = value; edit { putBoolean("onboarded", value) } }
    fun setLeftScreenId(value: String) { leftScreenIdState = value; edit { putString("left_screen_id", value) } }
    fun setRightScreenId(value: String) { rightScreenIdState = value; edit { putString("right_screen_id", value) } }
    fun setRetroflipStyle(value: String) { retroflipStyleState = value; edit { putString("retroflip_style", value) } }
    fun setBigCropTheme(value: String) { bigCropThemeState = value; edit { putString("big_crop_theme", value) } }
    fun setSolarStyle(value: String) { solarStyleState = value; edit { putString("solar_style", value) } }
    fun setSegmentedFont(value: String) { segmentedFontState = value; edit { putString("segmented_font", value) } }
    fun setChargingTheme(value: String) { chargingThemeState = value; edit { putString("charging_theme", value) } }
    fun setShowSeconds(value: Boolean) { showSecondsState = value; edit { putBoolean("show_seconds", value) } }
    fun setBlinkSeparator(value: Boolean) { blinkSeparatorState = value; edit { putBoolean("blink_separator", value) } }
    fun setBurnInProtection(value: Boolean) { burnInProtectionState = value; edit { putBoolean("burn_in_protection", value) } }
    fun setHapticFeedback(value: Boolean) { hapticFeedbackState = value; edit { putBoolean("haptic_feedback", value) } }
    fun setShowClockOnCharging(value: Boolean) { showClockOnChargingState = value; edit { putBoolean("show_clock_on_charging", value) } }
    fun setFocusMode(value: Boolean) { focusModeState = value; edit { putBoolean("focus_mode", value) } }
    fun setFullscreenNotifications(value: Boolean) { fullscreenNotificationsState = value; edit { putBoolean("fullscreen_notifications", value) } }
    fun setMilestoneTarget(value: Int) { milestoneTargetState = value; edit { putInt("milestone_target", value) } }
    fun setPhotoIntervalSeconds(value: Int) { photoIntervalSecondsState = value; edit { putInt("photo_interval_seconds", value) } }
    fun setStartOfWeek(value: Int) { startOfWeekState = value; edit { putInt("start_of_week", value) } }
    fun setQuoteIndex(value: Int) { quoteIndexState = value; edit { putInt("quote_index", value) } }
    fun setAutoStartCharging(value: Boolean) { autoStartChargingState = value; edit { putBoolean("auto_start_charging", value) } }
    fun setCloseOnUnplug(value: Boolean) { closeOnUnplugState = value; edit { putBoolean("close_on_unplug", value) } }
    fun setAutoCloseDoubleTap(value: Boolean) { autoCloseDoubleTapState = value; edit { putBoolean("auto_close_double_tap", value) } }
    fun setNightMode(value: NightMode) { nightModeState = value; edit { putString("night_mode", value.name) } }
    fun setNightStartMinutes(value: Int) { nightStartMinutesState = value; edit { putInt("night_start_minutes", value) } }
    fun setNightEndMinutes(value: Int) { nightEndMinutesState = value; edit { putInt("night_end_minutes", value) } }
    fun setUseLightSensor(value: Boolean) { useLightSensorState = value; edit { putBoolean("use_light_sensor", value) } }
    fun setSleepTimerMinutes(value: Int) { sleepTimerMinutesState = value; edit { putInt("sleep_timer_minutes", value) } }
    fun setDayBrightness(value: Int) { dayBrightnessState = value; edit { putInt("day_brightness", value) } }
    fun setNightBrightness(value: Int) { nightBrightnessState = value; edit { putInt("night_brightness", value) } }
    fun setFontScale(value: Float) { fontScaleState = value; edit { putFloat("font_scale", value) } }
    fun setPhotoUris(value: List<String>) {
        photoUrisState = value
        edit { putStringSet("photo_uris", value.toSet()) }
    }

    /** Raw access for backup/restore: all stored values. */
    fun snapshot(): Map<String, Any?> = prefs.all.toMap()
    fun restore(values: Map<String, Any?>) {
        val editor = prefs.edit()
        for ((key, value) in values) {
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is String -> editor.putString(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        editor.apply()
        // Re-read into state.
        onboardedState = prefs.getBoolean("onboarded", false)
        leftScreenIdState = prefs.getString("left_screen_id", null) ?: "big_crop_clock_duo"
        rightScreenIdState = prefs.getString("right_screen_id", null) ?: "calendar"
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
