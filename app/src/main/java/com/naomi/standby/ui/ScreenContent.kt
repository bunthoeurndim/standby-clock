package com.naomi.standby.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.model.AnalogVariant
import com.naomi.standby.model.BatteryVariant
import com.naomi.standby.model.BigCropTheme
import com.naomi.standby.model.ChargingTheme
import com.naomi.standby.model.RetroflipStyle
import com.naomi.standby.model.ScreenId
import com.naomi.standby.model.SegmentedFont
import com.naomi.standby.model.SolarStyle
import com.naomi.standby.ui.faces.AmoledFace
import com.naomi.standby.ui.faces.AnalogFace
import com.naomi.standby.ui.faces.AntsFace
import com.naomi.standby.ui.faces.BatteryFace
import com.naomi.standby.ui.faces.BigCropFace
import com.naomi.standby.ui.faces.ChargingFace
import com.naomi.standby.ui.faces.ClockHeroFace
import com.naomi.standby.ui.faces.ClockOfClocksFace
import com.naomi.standby.ui.faces.DementiaFace
import com.naomi.standby.ui.faces.DigitalFullscreenFace
import com.naomi.standby.ui.faces.DigitalWithDateFace
import com.naomi.standby.ui.faces.MatrixFace
import com.naomi.standby.ui.faces.NeonFace
import com.naomi.standby.ui.faces.PixelArtFace
import com.naomi.standby.ui.faces.RadialInvertFace
import com.naomi.standby.ui.faces.RetroflipFace
import com.naomi.standby.ui.faces.SegmentedFace
import com.naomi.standby.ui.faces.SerenoFace
import com.naomi.standby.ui.faces.SolarFace
import com.naomi.standby.ui.faces.WordClockFace
import com.naomi.standby.ui.faces.WorldClockFace
import com.naomi.standby.ui.widgets.AlarmsWidget
import com.naomi.standby.ui.widgets.CalculatorWidget
import com.naomi.standby.ui.widgets.CalendarWidget
import com.naomi.standby.ui.widgets.CommandCenterWidget
import com.naomi.standby.ui.widgets.ConceptsWidget
import com.naomi.standby.ui.widgets.CountdownWidget
import com.naomi.standby.ui.widgets.DayCentricWidget
import com.naomi.standby.ui.widgets.MemePlayerWidget
import com.naomi.standby.ui.widgets.NextEventWidget
import com.naomi.standby.ui.widgets.NotificationMiniWidget
import com.naomi.standby.ui.widgets.PhotoSlideWidget
import com.naomi.standby.ui.widgets.PhotoStackWidget
import com.naomi.standby.ui.widgets.PlayerWidget
import com.naomi.standby.ui.widgets.PomodoroWidget
import com.naomi.standby.ui.widgets.ProtocolsWidget
import com.naomi.standby.ui.widgets.QuickAppLaunchWidget
import com.naomi.standby.ui.widgets.QuotesWidget
import com.naomi.standby.ui.widgets.ScheduleWidget
import com.naomi.standby.ui.widgets.SimonWidget
import com.naomi.standby.ui.widgets.SoberWidget
import com.naomi.standby.ui.widgets.StarshipWidget
import com.naomi.standby.ui.widgets.StopwatchWidget
import com.naomi.standby.ui.widgets.StrikingCalendarWidget
import com.naomi.standby.ui.widgets.SystemDashboardWidget
import com.naomi.standby.ui.widgets.TallyWidget
import com.naomi.standby.ui.widgets.ThirdPartyWidget
import com.naomi.standby.ui.widgets.TimerWidget
import com.naomi.standby.ui.widgets.TodoWidget
import com.naomi.standby.ui.widgets.VibesWidget
import com.naomi.standby.ui.widgets.WeatherAndClockWidget
import com.naomi.standby.ui.widgets.WeatherBottomLayoutWidget
import com.naomi.standby.ui.widgets.WeatherHeroWidget
import com.naomi.standby.ui.widgets.WeatherWidget
import com.naomi.standby.ui.widgets.WidgetRotatorWidget
import java.time.LocalDateTime

/**
 * Renders any of the 97 screens. `scale` is ~0.55 in a Duo slot and 1.0
 * fullscreen. `onOpenScreen` is used by screens that open another screen
 * (Widget Rotator). The caller (Duo slot / fullscreen host) owns gestures:
 * tap opens the fullscreen counterpart, long-press replaces the widget.
 */
@Composable
fun ScreenContent(
    screen: ScreenId,
    now: LocalDateTime,
    settings: SettingsStore,
    scale: Float,
    modifier: Modifier = Modifier,
    onOpenScreen: (ScreenId) -> Unit = {}
) {
    val context = LocalContext.current
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (screen) {
            ScreenId.CHARGING_SCREEN -> ChargingFace(
                ChargingTheme.entries.firstOrNull { it.name == settings.chargingTheme } ?: ChargingTheme.MATERIAL,
                scale, settings, now
            )
            ScreenId.SERENO_DUO -> SerenoFace(now, scale, settings)
            ScreenId.DUO -> BigCropFace(now, BigCropTheme.byName(settings.bigCropTheme), scale, settings.showSeconds)
            ScreenId.USER_PHOTO_SLIDE -> PhotoSlideWidget(settings, scale)
            ScreenId.PLAYER_DUO -> PlayerWidget(context, scale)
            ScreenId.USER_PHOTO_SLIDE_MINI -> PhotoSlideWidget(settings, scale * 0.9f, mini = true)
            ScreenId.DIGITAL_CLOCKS_FULLSCREEN -> DigitalFullscreenFace(now, scale, settings.showSeconds)
            ScreenId.SYSTEM_DASHBOARD -> SystemDashboardWidget(context, scale)
            ScreenId.DAY_CENTRIC_DASHBOARD -> DayCentricWidget(now, scale)
            ScreenId.QUOTES_DUO -> QuotesWidget(settings, scale)
            ScreenId.QUOTES_FULLSCREEN -> QuotesWidget(settings, scale, fullscreen = true)
            ScreenId.PIXEL_ART_CLOCK_DUO -> PixelArtFace(now, scale)
            ScreenId.WEATHER_BOTTOM_LAYOUT_DUO -> WeatherBottomLayoutWidget(scale)
            ScreenId.POMODORO -> PomodoroWidget(context, scale)
            ScreenId.TIMER_DUO -> TimerWidget(scale)
            ScreenId.ROMAN_CLOCK_DUO -> AnalogFace(now, scale, AnalogVariant.CLASSIC, showDigital = true, roman = true)
            ScreenId.WORD_CLOCK_DUO -> WordClockFace(now, scale)
            ScreenId.NEON_CLOCK_DUO -> NeonFace(now, Color(0xFF00E5FF), scale)
            ScreenId.AMOLED_CLOCK_DUO -> AmoledFace(now, scale, settings.showSeconds, settings.blinkSeparator)
            ScreenId.BIG_CROP_CLOCK_DUO -> BigCropFace(now, BigCropTheme.byName(settings.bigCropTheme), scale, settings.showSeconds)
            ScreenId.RETROFLIP_CLOCK_DUO_LANDSCAPE -> RetroflipFace(now, RetroflipStyle.byName(settings.retroflipStyle), scale, settings.showSeconds)
            ScreenId.SOLAR_WATCH_DUO -> SolarFace(now, SolarStyle.byName(settings.solarStyle), scale)
            ScreenId.SCHEDULE -> ScheduleWidget(scale)
            ScreenId.SCHEDULE_MINI -> ScheduleWidget(scale, mini = true)
            ScreenId.MATRIX_CLOCK_DUO -> MatrixFace(now, scale)
            ScreenId.ANALOG_CLOCK_WITH_DIGITAL_FULLSCREEN -> AnalogFace(now, scale, AnalogVariant.WITH_DIGITAL, showDigital = true)
            ScreenId.ANALOG_CLOCK_WITH_DIGITAL_DUO -> AnalogFace(now, scale, AnalogVariant.WITH_DIGITAL, showDigital = true)
            ScreenId.ANALOG_CLOCK -> AnalogFace(now, scale, AnalogVariant.CLASSIC)
            ScreenId.ANALOG_CLOCK_CLEAN -> AnalogFace(now, scale, AnalogVariant.CLEAN)
            ScreenId.ANALOG_CLOCK_CLEAN_NO_MARKERS -> AnalogFace(now, scale, AnalogVariant.CLEAN_NO_MARKERS)
            ScreenId.ANALOG_CLOCK_SQUARE -> AnalogFace(now, scale, AnalogVariant.SQUARE)
            ScreenId.DIGITAL_CLOCK_WITH_DATE -> DigitalWithDateFace(now, scale)
            ScreenId.ANALOG_CLOCK_SQUARE_MINI -> AnalogFace(now, scale, AnalogVariant.SQUARE_MINI)
            ScreenId.CALENDAR -> CalendarWidget(settings, scale)
            ScreenId.CALENDAR_MINI -> CalendarWidget(settings, scale, mini = true)
            ScreenId.SETTINGS -> Text("Open Settings from the top bar.", color = Color.White, fontSize = (20 * scale).sp)
            ScreenId.PLAYER_FULLSCREEN -> PlayerWidget(context, scale, fullscreen = true)
            ScreenId.PLAYER_BLUR -> PlayerWidget(context, scale, blur = true)
            ScreenId.BATTERY_SPEEDOMETER_SIMPLE -> BatteryFace(BatteryVariant.SPEEDOMETER, scale, settings)
            ScreenId.BATTERY_DUO -> BatteryFace(BatteryVariant.PLAIN, scale, settings)
            ScreenId.BATTERY_DUO_NEON -> BatteryFace(BatteryVariant.NEON, scale, settings)
            ScreenId.BATTERY_DUO_CUTE -> BatteryFace(BatteryVariant.CUTE, scale, settings)
            ScreenId.BATTERY_DUO_CUTE_FILLED -> BatteryFace(BatteryVariant.CUTE_FILLED, scale, settings)
            ScreenId.BATTERY_DUO_OVERFILL -> BatteryFace(BatteryVariant.OVERFILL, scale, settings)
            ScreenId.BATTERY_DOTS -> BatteryFace(BatteryVariant.DOTS, scale, settings)
            ScreenId.BATTERY_LIQUID_WAVE -> BatteryFace(BatteryVariant.LIQUID_WAVE, scale, settings)
            ScreenId.BATTERY_RETRO_LCD -> BatteryFace(BatteryVariant.RETRO_LCD, scale, settings)
            ScreenId.TIMER_FULLSCREEN -> TimerWidget(scale, fullscreen = true)
            ScreenId.SOLAR_WATCH_FULLSCREEN -> SolarFace(now, SolarStyle.byName(settings.solarStyle), scale)
            ScreenId.NEON_CLOCK_FULLSCREEN -> NeonFace(now, Color(0xFF00E5FF), scale)
            ScreenId.BIG_CROP_CLOCK -> BigCropFace(now, BigCropTheme.byName(settings.bigCropTheme), scale, settings.showSeconds)
            ScreenId.WEATHER_AND_CLOCK_FULLSCREEN -> WeatherAndClockWidget(now, scale, fullscreen = true)
            ScreenId.WEATHER_AND_CLOCK_DUO -> WeatherAndClockWidget(now, scale)
            ScreenId.WEATHER_FULLSCREEN -> WeatherWidget(scale, fullscreen = true)
            ScreenId.WEATHER_DUO -> WeatherWidget(scale)
            ScreenId.RETROFLIP_CLOCK -> RetroflipFace(now, RetroflipStyle.byName(settings.retroflipStyle), scale, settings.showSeconds)
            ScreenId.MATRIX_CLOCK_FULLSCREEN -> MatrixFace(now, scale)
            ScreenId.RADIAL_INVERT_CLOCK_FULLSCREEN -> RadialInvertFace(now, scale)
            ScreenId.RADIAL_INVERT_CLOCK_DUO -> RadialInvertFace(now, scale)
            ScreenId.VIBES_CLOCKS -> VibesWidget(now, scale)
            ScreenId.MEME_PLAYER_DUO -> MemePlayerWidget(context, scale)
            ScreenId.GAME_SIMON -> SimonWidget(scale)
            ScreenId.THIRD_PARTY_WIDGET_SLOT1 -> ThirdPartyWidget(1, scale)
            ScreenId.THIRD_PARTY_WIDGET_SLOT2 -> ThirdPartyWidget(2, scale)
            ScreenId.THIRD_PARTY_WIDGET_SLOT3 -> ThirdPartyWidget(3, scale)
            ScreenId.THIRD_PARTY_WIDGET_SLOT4 -> ThirdPartyWidget(4, scale)
            ScreenId.THIRD_PARTY_WIDGET_SLOT5 -> ThirdPartyWidget(5, scale)
            ScreenId.THIRD_PARTY_WIDGET_SLOT6 -> ThirdPartyWidget(6, scale)
            ScreenId.NOTIFICATION_MINI -> NotificationMiniWidget(context, scale)
            ScreenId.COUNTDOWN_DUO -> CountdownWidget(scale)
            ScreenId.SEGMENTED_CLOCK -> SegmentedFace(
                now, SegmentedFont.byName(settings.segmentedFont), scale,
                horizontal = false, blinkSeparator = settings.blinkSeparator, showLeadingZero = true
            )
            ScreenId.SEGMENTED_CLOCK_HORIZONTAL -> SegmentedFace(
                now, SegmentedFont.byName(settings.segmentedFont), scale,
                horizontal = true, blinkSeparator = settings.blinkSeparator, showLeadingZero = true
            )
            ScreenId.WEATHER_BOTTOM_LAYOUT_FULLSCREEN -> WeatherBottomLayoutWidget(scale, fullscreen = true)
            ScreenId.DEMENTIA_CLOCK_FULLSCREEN -> DementiaFace(now, scale)
            ScreenId.DEMENTIA_CLOCK_DUO -> DementiaFace(now, scale)
            ScreenId.ANTS_CLOCK_FULLSCREEN -> AntsFace(now, scale)
            ScreenId.CLOCK_OF_CLOCKS -> ClockOfClocksFace(now, scale)
            ScreenId.CALCULATOR -> CalculatorWidget(scale)
            ScreenId.QUICKAPPLAUNCH -> QuickAppLaunchWidget(context, scale)
            ScreenId.CONCEPTS -> ConceptsWidget(now, scale)
            ScreenId.PHOTO_STACK -> PhotoStackWidget(settings, scale)
            ScreenId.STOPWATCH -> StopwatchWidget(scale)
            ScreenId.STOPWATCH_FULLSCREEN -> StopwatchWidget(scale, fullscreen = true)
            ScreenId.WORLD_CLOCK_FULLSCREEN -> WorldClockFace(now, scale)
            ScreenId.WIDGET_ROTATOR -> WidgetRotatorWidget(now, scale, onOpenScreen)
            ScreenId.STARTREK -> StarshipWidget(now, scale)
            ScreenId.CLOCK_HERO_DUO -> ClockHeroFace(now, scale)
            ScreenId.WEATHER_HERO_DUO -> WeatherHeroWidget(scale)
            ScreenId.NEXT_EVENT_DUO -> NextEventWidget(scale)
            ScreenId.CALENDAR_STRIKING_DUO -> StrikingCalendarWidget(settings, scale)
            ScreenId.PLAYER_HERO_DUO -> PlayerWidget(context, scale, hero = true)
            ScreenId.BATTERY_HERO_DUO -> BatteryFace(BatteryVariant.HERO, scale, settings)
            ScreenId.PROTOCOLS_DUO -> ProtocolsWidget(scale)
            ScreenId.TALLY_COUNTERS -> TallyWidget(scale)
            ScreenId.TODO_LIST -> TodoWidget(scale)
            ScreenId.SOBER_COUNTER -> SoberWidget(scale)
            ScreenId.COMMAND_CENTER -> CommandCenterWidget(now, scale)
        }
    }
}
