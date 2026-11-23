package com.naomi.standby.model

import androidx.compose.ui.graphics.Color

/**
 * v2 catalogue, rebuilt from the replica spec (replica-spec.md).
 *
 * The original app has 97 screens (its AppScreen enum). Every screen is
 * listed here in the original picker order with its display name, picker
 * category and kind, so the Duo picker, the default-partner map and the
 * fullscreen-counterpart map all match the original.
 *
 * All code in this file is original: names, labels and hex colours come
 * from the spec's behavioural description; no code, strings or assets
 * were copied from the original app.
 */

enum class ScreenCategory(val displayName: String) {
    UTILITY("Utility"),
    MEDIA("Media"),
    DIGITAL_WATCH("Digital clocks"),
    ANALOG_WATCH("Analog clocks"),
    THIRD_PARTY("Third party"),
    WEATHER("Weather"),
    BATTERY("Battery"),
    FUN("Fun"),
    UNDEFINED("Other")
}

enum class ScreenKind { DUO, MINI, FULLSCREEN, PLAIN, SYSTEM }

enum class ScreenId(
    val displayName: String,
    val category: ScreenCategory,
    val kind: ScreenKind,
    val explanation: String,
    val keywords: String = ""
) {
    CHARGING_SCREEN("Charging", ScreenCategory.BATTERY, ScreenKind.SYSTEM, "Battery level, charging speed and time to full while charging."),
    SERENO_DUO("Sereno", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "A quiet, sober clock for a Duo slot: time, date and a subtle status line on pure black.", "sereno quiet minimal sober"),
    DUO("Duo", ScreenCategory.UNDEFINED, ScreenKind.SYSTEM, "Display two widgets side by side on your screen."),
    USER_PHOTO_SLIDE("User Photo Slide", ScreenCategory.UNDEFINED, ScreenKind.PLAIN, "Slideshow of your personal photos displayed full screen."),
    PLAYER_DUO("Player Duo", ScreenCategory.MEDIA, ScreenKind.DUO, "Compact music player with album art and playback controls.", "music player spotify youtube"),
    USER_PHOTO_SLIDE_MINI("User Photo Slide Mini", ScreenCategory.MEDIA, ScreenKind.MINI, "Compact photo slideshow for a Duo slot."),
    DIGITAL_CLOCKS_FULLSCREEN("Digital Clocks Fullscreen", ScreenCategory.DIGITAL_WATCH, ScreenKind.FULLSCREEN, "A customizable digital clock with multiple styles and fonts.", "digital clock fonts styles"),
    SYSTEM_DASHBOARD("System Dashboard", ScreenCategory.UTILITY, ScreenKind.PLAIN, "Shows battery, storage, and memory at a glance.", "system battery storage memory"),
    DAY_CENTRIC_DASHBOARD("Day Centric Dashboard", ScreenCategory.UTILITY, ScreenKind.PLAIN, "A day-focused dashboard: date, agenda and how far through the day you are."),
    QUOTES_DUO("Quotes Duo", ScreenCategory.UTILITY, ScreenKind.DUO, "Inspirational quotes. Swipe for the next one.", "quotes inspirational motivation"),
    QUOTES_FULLSCREEN("Quotes Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "Inspirational quotes, full screen. Swipe for the next one."),
    PIXEL_ART_CLOCK_DUO("Pixel Art Clock Duo", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "A compact pixel art digital clock with a retro aesthetic.", "pixel retro 8-bit game"),
    WEATHER_BOTTOM_LAYOUT_DUO("Weather Bottom Layout Duo", ScreenCategory.WEATHER, ScreenKind.DUO, "Weather with the forecast along the bottom.", "weather forecast bottom"),
    POMODORO("Pomodoro", ScreenCategory.UTILITY, ScreenKind.PLAIN, "25-minute focus timer. Tap the timer to pause or resume.", "pomodoro focus timer work"),
    TIMER_DUO("Timer Duo", ScreenCategory.UTILITY, ScreenKind.DUO, "Countdown timer. Tap to pause or resume.", "timer countdown"),
    ROMAN_CLOCK_DUO("Roman Clock Duo", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "An elegant clock that tells the time in Roman numerals.", "roman numerals elegant classic"),
    WORD_CLOCK_DUO("Word Clock Duo", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "Tells the time in words — IT IS HALF PAST TEN — on a glowing word grid.", "word clock words grid"),
    NEON_CLOCK_DUO("Neon Clock Duo", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "A neon-styled digital clock with a glowing effect.", "neon glow bright"),
    AMOLED_CLOCK_DUO("AMOLED Clock", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "A simple, professional digital clock on a pure black background — perfect for AMOLED screens.", "amoled black simple"),
    BIG_CROP_CLOCK_DUO("Big Crop Clock Duo", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "A bold large-digit clock that makes the time impossible to miss.", "big crop bold huge"),
    RETROFLIP_CLOCK_DUO_LANDSCAPE("Retroflip Clock Duo Landscape", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "A compact landscape retro flip clock with satisfying digit animations.", "flip retro split flap"),
    SOLAR_WATCH_DUO("Solar Watch Duo", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "A solar-inspired watch face showing time and sun position.", "solar sun arc daylight"),
    SCHEDULE("Schedule", ScreenCategory.UNDEFINED, ScreenKind.PLAIN, "Calendar agenda list for the day."),
    SCHEDULE_MINI("Schedule Mini", ScreenCategory.UTILITY, ScreenKind.MINI, "Compact agenda list for the day."),
    MATRIX_CLOCK_DUO("Matrix Clock Duo", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "A Matrix-inspired digital rain clock.", "matrix clock digital rain code hacker sci-fi"),
    ANALOG_CLOCK_WITH_DIGITAL_FULLSCREEN("Analog Clock With Digital Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "A full-screen analog clock with a digital time readout overlay."),
    ANALOG_CLOCK_WITH_DIGITAL_DUO("Analog Clock With Digital Duo", ScreenCategory.ANALOG_WATCH, ScreenKind.DUO, "A compact analog clock with a digital time readout side by side.", "analog digital combo"),
    ANALOG_CLOCK("Analog Clock", ScreenCategory.ANALOG_WATCH, ScreenKind.PLAIN, "A classic analog clock face with customizable styles.", "analog classic dial hands"),
    ANALOG_CLOCK_CLEAN("Analog Clock Clean", ScreenCategory.ANALOG_WATCH, ScreenKind.PLAIN, "A minimalist analog clock face with a clean, uncluttered design.", "analog minimal clean"),
    ANALOG_CLOCK_CLEAN_NO_MARKERS("Analog Clock Clean No Markers", ScreenCategory.ANALOG_WATCH, ScreenKind.PLAIN, "A sleek analog clock face with no hour markers for a pure minimal look.", "analog minimal no markers"),
    ANALOG_CLOCK_SQUARE("Analog Clock Square", ScreenCategory.UNDEFINED, ScreenKind.PLAIN, "A square analog clock face with a modern design."),
    DIGITAL_CLOCK_WITH_DATE("Digital Clock With Date", ScreenCategory.UNDEFINED, ScreenKind.PLAIN, "A digital clock that also displays the current date."),
    ANALOG_CLOCK_SQUARE_MINI("Analog Clock Square Mini", ScreenCategory.ANALOG_WATCH, ScreenKind.MINI, "A compact square analog clock that fits neatly alongside another widget.", "analog square mini"),
    CALENDAR("Calendar", ScreenCategory.UTILITY, ScreenKind.PLAIN, "Month calendar.", "calendar month date"),
    CALENDAR_MINI("Calendar Mini", ScreenCategory.UTILITY, ScreenKind.MINI, "Compact month calendar with the next event.", "calendar mini"),
    SETTINGS("Settings", ScreenCategory.UNDEFINED, ScreenKind.SYSTEM, "App settings."),
    PLAYER_FULLSCREEN("Player Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "Full-screen music player."),
    PLAYER_BLUR("Player Blur", ScreenCategory.MEDIA, ScreenKind.PLAIN, "Music player with blurred album-art background.", "music blur album art"),
    BATTERY_SPEEDOMETER_SIMPLE("Battery Speedometer Simple", ScreenCategory.BATTERY, ScreenKind.PLAIN, "Battery as a speedometer gauge.", "battery gauge speedometer"),
    BATTERY_DUO("Battery Duo", ScreenCategory.BATTERY, ScreenKind.DUO, "Battery percentage card.", "battery percent charge"),
    BATTERY_DUO_NEON("Battery Duo Neon", ScreenCategory.BATTERY, ScreenKind.DUO, "A compact neon-styled battery indicator with a glowing charge display.", "battery neon glow"),
    BATTERY_DUO_CUTE("Battery Duo Cute", ScreenCategory.BATTERY, ScreenKind.DUO, "A cute battery face.", "battery cute kawaii"),
    BATTERY_DUO_CUTE_FILLED("Battery Duo Cute Filled", ScreenCategory.BATTERY, ScreenKind.DUO, "A cute battery face, filled.", "battery cute kawaii filled"),
    BATTERY_DUO_OVERFILL("Battery Duo Overfill", ScreenCategory.BATTERY, ScreenKind.DUO, "Battery that overflows past 100%.", "battery overfill"),
    BATTERY_DOTS("Battery Dots", ScreenCategory.BATTERY, ScreenKind.PLAIN, "Dot-matrix battery.", "battery dots matrix"),
    BATTERY_LIQUID_WAVE("Battery Liquid Wave", ScreenCategory.BATTERY, ScreenKind.PLAIN, "Liquid-wave fill battery.", "battery liquid wave"),
    BATTERY_RETRO_LCD("Battery Retro LCD", ScreenCategory.BATTERY, ScreenKind.PLAIN, "Retro LCD battery.", "battery lcd retro"),
    TIMER_FULLSCREEN("Timer Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "Countdown timer, full screen."),
    SOLAR_WATCH_FULLSCREEN("Solar Watch Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "A solar-inspired watch face showing time and sun arc."),
    NEON_CLOCK_FULLSCREEN("Neon Clock Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "A neon-styled digital clock with a vivid glowing effect."),
    BIG_CROP_CLOCK("Big Crop Clock", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "A bold large-digit clock that makes the time impossible to miss."),
    WEATHER_AND_CLOCK_FULLSCREEN("Weather And Clock Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "Clock and weather combined, full screen."),
    WEATHER_AND_CLOCK_DUO("Weather And Clock Duo", ScreenCategory.WEATHER, ScreenKind.DUO, "Clock and weather in one widget.", "weather clock combo"),
    WEATHER_FULLSCREEN("Weather Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "Weather, full screen."),
    WEATHER_DUO("Weather Duo", ScreenCategory.WEATHER, ScreenKind.DUO, "Current weather in a Duo slot.", "weather temperature"),
    RETROFLIP_CLOCK("Retroflip Clock", ScreenCategory.UNDEFINED, ScreenKind.PLAIN, "Full-screen retro flip clock with satisfying digit animations."),
    MATRIX_CLOCK_FULLSCREEN("Matrix Clock Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "A Matrix-inspired digital rain clock, full screen."),
    RADIAL_INVERT_CLOCK_FULLSCREEN("Radial Invert Clock Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "A radial clock with an inverted, eye-catching visual style."),
    RADIAL_INVERT_CLOCK_DUO("Radial Invert Clock Duo", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "A radial clock with an inverted visual style.", "radial invert circle"),
    VIBES_CLOCKS("Vibes Clocks", ScreenCategory.MEDIA, ScreenKind.PLAIN, "A mood-driven clock that changes its look based on the music you play.", "vibes mood music"),
    MEME_PLAYER_DUO("Meme Player Duo", ScreenCategory.FUN, ScreenKind.DUO, "Meme player combined with music.", "meme memes gif gifs animated gif reaction funny music player"),
    GAME_SIMON("Game Simon", ScreenCategory.FUN, ScreenKind.PLAIN, "Simon memory game. Watch carefully, then repeat.", "simon game memory"),
    THIRD_PARTY_WIDGET_SLOT1("Third-Party Widget Slot 1", ScreenCategory.THIRD_PARTY, ScreenKind.PLAIN, "Host a widget from another app. 3rd Party Widget – Slot 1."),
    THIRD_PARTY_WIDGET_SLOT2("Third-Party Widget Slot 2", ScreenCategory.THIRD_PARTY, ScreenKind.PLAIN, "Host a widget from another app. 3rd Party Widget – Slot 2."),
    THIRD_PARTY_WIDGET_SLOT3("Third-Party Widget Slot 3", ScreenCategory.THIRD_PARTY, ScreenKind.PLAIN, "Host a widget from another app. 3rd Party Widget – Slot 3."),
    THIRD_PARTY_WIDGET_SLOT4("Third-Party Widget Slot 4", ScreenCategory.THIRD_PARTY, ScreenKind.PLAIN, "Host a widget from another app. 3rd Party Widget – Slot 4."),
    THIRD_PARTY_WIDGET_SLOT5("Third-Party Widget Slot 5", ScreenCategory.THIRD_PARTY, ScreenKind.PLAIN, "Host a widget from another app. 3rd Party Widget – Slot 5."),
    THIRD_PARTY_WIDGET_SLOT6("Third-Party Widget Slot 6", ScreenCategory.THIRD_PARTY, ScreenKind.PLAIN, "Host a widget from another app. 3rd Party Widget – Slot 6."),
    NOTIFICATION_MINI("Notification Mini", ScreenCategory.UTILITY, ScreenKind.MINI, "Latest notifications in a compact list.", "notifications recent alerts"),
    COUNTDOWN_DUO("Countdown Duo", ScreenCategory.UTILITY, ScreenKind.DUO, "Days until a date you choose.", "countdown days until"),
    SEGMENTED_CLOCK("Segmented Clock", ScreenCategory.DIGITAL_WATCH, ScreenKind.PLAIN, "A segmented LED-style digital clock with a classic display aesthetic.", "segmented led lcd seven segment"),
    SEGMENTED_CLOCK_HORIZONTAL("Segmented Clock Horizontal", ScreenCategory.DIGITAL_WATCH, ScreenKind.PLAIN, "A wide horizontal segmented LED clock that makes the time stand out.", "segmented led horizontal wide"),
    WEATHER_BOTTOM_LAYOUT_FULLSCREEN("Weather Bottom Layout Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "Weather with the forecast along the bottom, full screen."),
    DEMENTIA_CLOCK_FULLSCREEN("Dementia Clock Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "A clear, large-text clock showing the full date and time for easy reading."),
    DEMENTIA_CLOCK_DUO("Dementia Clock Duo", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "A clear, large-text clock showing the full date and time for easy reading.", "dementia clear large readable"),
    ANTS_CLOCK_FULLSCREEN("Ants Clock Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "The time drawn by marching ants."),
    CLOCK_OF_CLOCKS("Clock Of Clocks", ScreenCategory.UNDEFINED, ScreenKind.PLAIN, "A mesmerizing clock made up of many smaller analog clock faces."),
    CALCULATOR("Calculator", ScreenCategory.UTILITY, ScreenKind.PLAIN, "Calculator. Swipe up for scientific functions.", "calculator math"),
    QUICKAPPLAUNCH("Quick App Launch", ScreenCategory.UTILITY, ScreenKind.PLAIN, "Quick-launch shortcuts to your favorite apps from the lock screen.", "apps launch shortcuts"),
    CONCEPTS("Concepts", ScreenCategory.UTILITY, ScreenKind.PLAIN, "Experimental concept widgets currently in development.", "concepts experimental labs"),
    PHOTO_STACK("Photo Stack", ScreenCategory.MEDIA, ScreenKind.PLAIN, "Polaroid-style photo stack with drifting photos.", "photos stack polaroid"),
    STOPWATCH("Stopwatch", ScreenCategory.UTILITY, ScreenKind.PLAIN, "Tap to start, tap again to pause.", "stopwatch laps"),
    STOPWATCH_FULLSCREEN("Stopwatch Fullscreen", ScreenCategory.UNDEFINED, ScreenKind.FULLSCREEN, "Stopwatch, full screen. Tap to start, tap again to pause."),
    WORLD_CLOCK_FULLSCREEN("World Clock Fullscreen", ScreenCategory.DIGITAL_WATCH, ScreenKind.FULLSCREEN, "See the time across multiple cities around the world.", "world clock cities timezone"),
    WIDGET_ROTATOR("Widget Rotator", ScreenCategory.UTILITY, ScreenKind.PLAIN, "Cycles through a list of widgets on an interval.", "rotator cycle slideshow widgets"),
    STARTREK("Starship Console", ScreenCategory.UNDEFINED, ScreenKind.PLAIN, "Master system display."),
    CLOCK_HERO_DUO("Clock Hero Duo", ScreenCategory.DIGITAL_WATCH, ScreenKind.DUO, "Hero clock with sun arc.", "hero clock sun arc bold calm modern"),
    WEATHER_HERO_DUO("Weather Hero Duo", ScreenCategory.WEATHER, ScreenKind.DUO, "Large weather card with high/low, hourly and sunrise/sunset.", "weather hero large"),
    NEXT_EVENT_DUO("Next Event Duo", ScreenCategory.UTILITY, ScreenKind.DUO, "Your next calendar event, at a glance.", "next event calendar"),
    CALENDAR_STRIKING_DUO("Striking Calendar", ScreenCategory.UTILITY, ScreenKind.DUO, "Calendar with a huge date number.", "striking calendar big date"),
    PLAYER_HERO_DUO("Player Hero Duo", ScreenCategory.MEDIA, ScreenKind.DUO, "Large hero-style player card.", "music hero player large"),
    BATTERY_HERO_DUO("Battery Hero Duo", ScreenCategory.BATTERY, ScreenKind.DUO, "Large hero battery card.", "battery hero large"),
    PROTOCOLS_DUO("Protocols Duo", ScreenCategory.UTILITY, ScreenKind.DUO, "Your personal rules, shown as a manifesto.", "protocols rules manifesto habits"),
    TALLY_COUNTERS("Tally Counters", ScreenCategory.UTILITY, ScreenKind.PLAIN, "Tap a counter to count up. Long-press to subtract.", "tally counter count"),
    TODO_LIST("Todo List", ScreenCategory.UTILITY, ScreenKind.PLAIN, "Checklists with multiple lists and recurrence.", "todo tasks checklist"),
    SOBER_COUNTER("Sober Counter", ScreenCategory.UTILITY, ScreenKind.PLAIN, "Streak tracker with a discreet mode.", "sober streak clean days"),
    COMMAND_CENTER("Command Center", ScreenCategory.UNDEFINED, ScreenKind.PLAIN, "Focus dashboard: greeting, agenda, audio, battery, weather, countdown.");

    val id: String get() = name.lowercase()

    companion object {
        fun byId(id: String?): ScreenId? = entries.firstOrNull { it.id == id }
    }
}

/** The picker-eligible screens: grouped under a category and shown in the widget picker. */
object ScreenCatalog {
    val all: List<ScreenId> get() = ScreenId.entries

    /** Picker order = enum order, filtered to screens with a category that are not system screens. */
    val pickerScreens: List<ScreenId>
        get() = ScreenId.entries.filter { it.category != ScreenCategory.UNDEFINED && it.kind != ScreenKind.SYSTEM }

    fun search(query: String): List<ScreenId> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return pickerScreens
        return pickerScreens.filter {
            it.displayName.lowercase().contains(q) ||
                it.category.displayName.lowercase().contains(q) ||
                it.explanation.lowercase().contains(q) ||
                it.keywords.lowercase().contains(q)
        }
    }

    /** Default partner for the other Duo half (spec §3); anything not listed defaults to Calendar. */
    fun defaultPartner(screen: ScreenId): ScreenId = when (screen) {
        ScreenId.WEATHER_HERO_DUO -> ScreenId.DEMENTIA_CLOCK_DUO
        ScreenId.PLAYER_BLUR, ScreenId.PLAYER_HERO_DUO, ScreenId.NOTIFICATION_MINI -> ScreenId.SYSTEM_DASHBOARD
        ScreenId.CALENDAR_STRIKING_DUO -> ScreenId.ANALOG_CLOCK
        ScreenId.MATRIX_CLOCK_DUO, ScreenId.PIXEL_ART_CLOCK_DUO -> ScreenId.PLAYER_DUO
        else -> ScreenId.CALENDAR
    }

    /** Tapping a Duo widget opens its fullscreen counterpart (spec §3). Null = open the screen itself. */
    fun fullscreenCounterpart(screen: ScreenId): ScreenId? = when (screen) {
        ScreenId.QUOTES_DUO -> ScreenId.QUOTES_FULLSCREEN
        ScreenId.WEATHER_BOTTOM_LAYOUT_DUO -> ScreenId.WEATHER_BOTTOM_LAYOUT_FULLSCREEN
        ScreenId.TIMER_DUO -> ScreenId.TIMER_FULLSCREEN
        ScreenId.NEON_CLOCK_DUO -> ScreenId.NEON_CLOCK_FULLSCREEN
        ScreenId.BIG_CROP_CLOCK_DUO -> ScreenId.BIG_CROP_CLOCK
        ScreenId.SOLAR_WATCH_DUO -> ScreenId.SOLAR_WATCH_FULLSCREEN
        ScreenId.MATRIX_CLOCK_DUO -> ScreenId.MATRIX_CLOCK_FULLSCREEN
        ScreenId.ANALOG_CLOCK_WITH_DIGITAL_DUO -> ScreenId.ANALOG_CLOCK_WITH_DIGITAL_FULLSCREEN
        ScreenId.ANALOG_CLOCK_SQUARE_MINI -> ScreenId.ANALOG_CLOCK_SQUARE
        ScreenId.WEATHER_AND_CLOCK_DUO -> ScreenId.WEATHER_AND_CLOCK_FULLSCREEN
        ScreenId.WEATHER_DUO -> ScreenId.WEATHER_FULLSCREEN
        ScreenId.RADIAL_INVERT_CLOCK_DUO -> ScreenId.RADIAL_INVERT_CLOCK_FULLSCREEN
        ScreenId.DEMENTIA_CLOCK_DUO -> ScreenId.DEMENTIA_CLOCK_FULLSCREEN
        else -> null
    }
}

/** Retroflip styles (spec §5.1). Colours are the spec's decoded hex where given;
 *  styles whose colours lived in obfuscated code use close matches to their names. */
enum class RetroflipStyle(
    val displayName: String,
    val bg: Color,
    val card: Color,
    val text: Color,
    val gradient: List<Color> = emptyList()
) {
    GRADIENT1_COLORED("Gradient 1", Color.Black, Color(0xFF222222), Color.White,
        listOf(Color(0xFFFFA2DF), Color.Black, Color(0xFFFFF971))),
    GRADIENT5_COLORED("Gradient 5", Color(0xFF101018), Color(0xFF222230), Color.White,
        listOf(Color(0xFF7C6CFF), Color(0xFF101018), Color(0xFFFF8FD8))),
    LIGHT_CLASSIC1("Light Classic 1", Color(0xFFF9F9F9), Color(0xFF1B1B1B), Color.White),
    LIGHT_CLASSIC2("Light Classic 2", Color(0xFFEEEEEE), Color(0xFF333333), Color.White),
    COLORED_MODERN_DARK("Colored Modern Dark", Color(0xFF080C38), Color(0xFF0A0D38), Color(0xFF66CCFF)),
    GRADIENT3_COLORED("Gradient 3", Color(0xFFFEFFFE), Color(0xFF2E3B33), Color.White,
        listOf(Color(0xFF74E6C4), Color(0xFFFAFFFC), Color(0xFF6CF275))),
    SAN_FRANCISCO("San Francisco", Color(0xFFEFEBE9), Color(0xFFD1D1D1), Color.Black),
    WHITE_ON_BLUE("White on Blue", Color(0xFF3793EF), Color(0xFF0E49A3), Color.White),
    GRADIENT2_COLORED("Gradient 2", Color(0xFF153F32), Color(0xFF0F121A), Color(0xFFBFE8D2),
        listOf(Color(0xFF1D734D), Color(0xFF153F32), Color(0xFF0F121A))),
    VERY_BLUEISH_LIGHT("Very Blueish Light", Color(0xFFE6E7E8), Color(0xFF020434), Color.White),
    GRADIENT4_COLORED("Gradient 4", Color.Black, Color(0xFF2A2350), Color.White,
        listOf(Color(0xFFFD9D6A), Color(0xFF8352BB), Color(0xFF5A6CEC))),
    PASTEL_AND_ORANGE_DARK("Pastel & Orange Dark", Color(0xFF1A1210), Color(0xFFFB6D45), Color.White),
    PASTEL_CLASSIC_LIGHT("Pastel Classic Light", Color(0xFFF2E9DC), Color(0xFF3A2E26), Color(0xFFF2E9DC)),
    RETRO_COLORFUL("Retro Colorful", Color.Black, Color(0xFFFF3399), Color(0xFFFFD700)),
    GREEN_AND_WHITE("Green & White", Color(0xFF164319), Color(0xFF57B85C), Color(0xFFFADFFF)),
    PURPLE_AND_WHITE("Purple & White", Color(0xFF6C1C9B), Color(0xFF9A27AF), Color.White),
    ROARING_TWENTIES("Roaring Twenties", Color(0xFF141414), Color(0xFFD4AF37), Color(0xFF1A1A1A)),
    VERY_BLUEISH_DARK("Very Blueish Dark", Color(0xFF020434), Color(0xFF101238), Color(0xFFBFD0FF)),
    LIGHT_CLASSIC("Light Classic", Color(0xFFE0E0E0), Color(0xFF212121), Color.White),
    GRADIENT6_COLORED("Gradient 6", Color.Black, Color(0xFF2B2340), Color.White,
        listOf(Color(0xFFC7D461), Color(0xFFFFC692), Color(0xFFAD35FE))),
    ART_DECO("Art Deco", Color(0xFF141414), Color(0xFFFF4500), Color.White),
    KINDA_GREENISH("Kinda Greenish", Color(0xFF1C2B1E), Color(0xFF91C499), Color(0xFF0F1F11)),
    BLUE_AND_WHITE("Blue & White", Color(0xFF1367D4), Color(0xFF0B3D91), Color.White),
    WHITE_ON_RED("White on Red", Color(0xFFD71B60), Color(0xFF731037), Color.White),
    BLUE_GRADIENT_RETRO("Blue Gradient Retro", Color(0xFF0D47A1), Color(0xFF1976D2), Color.White,
        listOf(Color(0xFF64B5F6), Color(0xFF1976D2), Color(0xFF0D47A1))),
    MODERN_STYLE2("Modern 2", Color(0xFF1E1E1E), Color(0xFF292929), Color(0xFF00BFFF)),
    KINDA_GREENISH_LIGHT("Kinda Greenish Light", Color(0xFFE4F0E4), Color(0xFF91C499), Color(0xFF1C2B1E)),
    CELESTIAL_DREAMS("Celestial Dreams", Color(0xFF1B262C), Color(0xFF0F4C75), Color(0xFFE1B382)),
    MATERIAL_DARK("Material Dark", Color(0xFF212121), Color(0xFF2D2D2D), Color(0xFF03A9F4)),
    DERN("Dern", Color(0xFF1A1A1A), Color(0xFFEF5350), Color.White),
    XIAOMI_BLUE("Xiaomi Blue", Color(0xFF3F51B5), Color(0xFF212F58), Color.White),
    HUAWEI_PINK("Huawei Pink", Color(0xFFD81B60), Color(0xFFC2185B), Color.White),
    HUAWEI_BLUE("Huawei Blue", Color(0xFF009688), Color(0xFF00796B), Color.White);

    companion object {
        fun byName(name: String?): RetroflipStyle =
            entries.firstOrNull { it.name == name } ?: PURPLE_AND_WHITE
    }
}

/** Big Crop Clock themes (spec §5.2). bg + per-digit palette; colours are the
 *  spec's decoded hex where given, close matches to the theme names otherwise. */
enum class BigCropTheme(
    val displayName: String,
    val bg: Color,
    val palette: List<Color>,
    val isDark: Boolean
) {
    NEON_LIGHTS("Neon Lights", Color(0xFF111111), listOf(Color(0xFF00E676), Color(0xFF69F0AE), Color(0xFF00E5FF), Color(0xFF76FF03)), true),
    ELEGANT_RED("Elegant Red", Color(0xFFF5F5F5), listOf(Color(0xFF1A1A1A), Color(0xFFC0392B), Color(0xFFE74C3C), Color(0xFFD91E18)), false),
    MODERN_BLUE("Modern Blue", Color(0xFFF2F6FA), listOf(Color(0xFF0E1621), Color(0xFF2C3E50), Color(0xFF3498DB), Color(0xFF2980B9)), false),
    BLACK_AND_WHITE("Black & White", Color.Black, listOf(Color.White, Color(0xFFCCCCCC), Color(0xFF999999), Color.White), true),
    PRIMARY_LIGHT("Primary Light", Color(0xFFFFFFFF), listOf(Color(0xFF111111), Color(0xFF1976D2), Color(0xFF111111), Color(0xFF1976D2)), false),
    SAMURAI_SPIRIT("Samurai Spirit", Color(0xFFF7F3EC), listOf(Color(0xFFEF5350), Color(0xFF1A237E), Color(0xFFEF5350), Color(0xFF1A237E)), false),
    SECONDARY_LIGHT("Secondary Light", Color(0xFFFAFAFA), listOf(Color(0xFF5D4037), Color(0xFF8D6E63), Color(0xFF5D4037), Color(0xFF8D6E63)), false),
    OCEANIC_VIBES("Oceanic Vibes", Color(0xFF04222E), listOf(Color(0xFF4DD0E1), Color(0xFF00ACC1), Color(0xFF80DEEA), Color(0xFF26C6DA)), true),
    ELEGANT_GOLD("Elegant Gold", Color(0xFF1D1F20), listOf(Color(0xFFF5D061), Color(0xFFFFE082), Color(0xFFF5D061), Color(0xFFFFECB3)), true),
    OCEAN_SUNSET("Ocean Sunset", Color(0xFF06222E), listOf(Color(0xFF0099CC), Color(0xFFFFD600), Color(0xFF00B0FF), Color(0xFFFFEA00)), true),
    PURPLE_AND_PINK("Purple & Pink", Color(0xFF1B0B2B), listOf(Color(0xFFCE93D8), Color(0xFFF48FB1), Color(0xFFBA68C8), Color(0xFFF06292)), true),
    VIBRANT_GREEN("Vibrant Green", Color(0xFF0E1A12), listOf(Color(0xFF27AE60), Color(0xFF2ECC71), Color(0xFF1F8A13), Color(0xFF58D68D)), true),
    SUNSET_HUES("Sunset Hues", Color(0xFF2B0F1E), listOf(Color(0xFFFF7043), Color(0xFFFFAB91), Color(0xFFFF8A65), Color(0xFFFFCCBC)), true),
    CYBERPUNK("Cyberpunk", Color(0xFF0D0221), listOf(Color(0xFFF9F002), Color(0xFF00F0FF), Color(0xFFFF2A6D), Color(0xFF05FFA1)), true),
    ORANGE_AND_YELLOW("Orange & Yellow", Color(0xFF201400), listOf(Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFFFFB300), Color(0xFFFFF176)), true),
    SECONDARY_DARK("Secondary Dark", Color(0xFF121212), listOf(Color(0xFFB0BEC5), Color(0xFF90A4AE), Color(0xFFCFD8DC), Color(0xFF78909C)), true),
    TEAL_AND_CYAN("Teal & Cyan", Color(0xFF002424), listOf(Color(0xFF4DB6AC), Color(0xFF00BCD4), Color(0xFF80CBC4), Color(0xFF26C6DA)), true),
    TERTIARY_LIGHT("Tertiary Light", Color(0xFFFDF6EC), listOf(Color(0xFF6D4C41), Color(0xFFA1887F), Color(0xFF6D4C41), Color(0xFFD7CCC8)), false),
    NEON_NIGHT("Neon Night", Color.Black, listOf(Color(0xFF00E5FF), Color(0xFFFF4081), Color(0xFF76FF03), Color(0xFFFFEA00)), true),
    XIAOMI_STYLE2("Xiaomi Style 2", Color(0xFF2A2320), listOf(Color(0xFFE65A53), Color(0xFFEBB3A9), Color(0xFFB5A99E), Color(0xFFEFD9CE)), true),
    STARRY_SKY("Starry Sky", Color(0xFF050510), listOf(Color(0xFFE1F5FE), Color(0xFF90CAF9), Color(0xFFFFF9C4), Color(0xFFBBDEFB)), true),
    LUNAR_ECLIPSE("Lunar Eclipse", Color(0xFF0A0A12), listOf(Color(0xFFEF5350), Color(0xFFE8EAF6), Color(0xFFB71C1C), Color(0xFFC5CAE9)), true),
    MIDNIGHT_GLOW("Midnight Glow", Color(0xFF0C1220), listOf(Color(0xFF34495E), Color(0xFF5D6D7E), Color(0xFF85C1E9), Color(0xFFAED6F1)), true),
    NIGHT_CARNIVAL("Night Carnival", Color(0xFF14041F), listOf(Color(0xFFFF4081), Color(0xFFFFD54F), Color(0xFF7C4DFF), Color(0xFF69F0AE)), true),
    ETHEREAL_ELEGANCE("Ethereal Elegance", Color(0xFFF3EAF7), listOf(Color(0xFF7B5EA7), Color(0xFFB39DDB), Color(0xFF9575CD), Color(0xFFD1C4E9)), false),
    OCEAN_MIST("Ocean Mist", Color(0xFFE8F4F4), listOf(Color(0xFF00695C), Color(0xFF00897B), Color(0xFF4DB6AC), Color(0xFF26A69A)), false),
    ROYAL_NIGHT("Royal Night", Color(0xFF0D0B2A), listOf(Color(0xFF7C4DFF), Color(0xFFB388FF), Color(0xFF651FFF), Color(0xFFD1C4E9)), true),
    RUBY_NOIR("Ruby Noir", Color(0xFF1A0505), listOf(Color(0xFFE53935), Color(0xFFEF9A9A), Color(0xFFC62828), Color(0xFFFFCDD2)), true),
    DARK_AMBER("Dark Amber", Color(0xFF1C1206), listOf(Color(0xFFFFB300), Color(0xFFFF8F00), Color(0xFFFFD54F), Color(0xFFFFCA28)), true),
    TERTIARY_DARK("Tertiary Dark", Color(0xFF17130F), listOf(Color(0xFFBCAAA4), Color(0xFFA1887F), Color(0xFFD7CCC8), Color(0xFF8D6E63)), true),
    ENCHANTED_NIGHT("Enchanted Night", Color(0xFF0E1424), listOf(Color(0xFF82B1FF), Color(0xFFB388FF), Color(0xFF80D8FF), Color(0xFFEA80FC)), true),
    PERSIAN_MOSAIC("Persian Mosaic", Color(0xFF0F1C26), listOf(Color(0xFF273746), Color(0xFF2E86C1), Color(0xFF1ABC9C), Color(0xFFF1C40F)), true),
    MONOCHROME_MINIMAL("Monochrome Minimal", Color(0xFFFFFFFF), listOf(Color(0xFF333333), Color(0xFF666666), Color(0xFF999999), Color(0xFFCCCCCC)), false),
    GOLDEN_NIGHTS("Golden Nights", Color(0xFF191204), listOf(Color(0xFFFFC107), Color(0xFFFFD54F), Color(0xFFFFE082), Color(0xFFFFB300)), true),
    MYSTICAL_MOON("Mystical Moon", Color(0xFF101018), listOf(Color(0xFFE8EAF6), Color(0xFFB0BEC5), Color(0xFFFFF8E1), Color(0xFFCFD8DC)), true),
    BRAZILIAN_CARNIVAL("Brazilian Carnival", Color(0xFF12060A), listOf(Color(0xFFD32F2F), Color(0xFF388E3C), Color(0xFFFFD600), Color(0xFF1976D2)), true),
    GOLDEN_HOUR("Golden Hour", Color(0xFFFFF8DC), listOf(Color(0xFFDAA520), Color(0xFFFFA500), Color(0xFFFF8C00), Color(0xFFB8860B)), false),
    INDIAN_SILK("Indian Silk", Color(0xFF2A0A12), listOf(Color(0xFFFF6F61), Color(0xFFFFD166), Color(0xFFEF476F), Color(0xFF06D6A0)), true);

    companion object {
        fun byName(name: String?): BigCropTheme =
            entries.firstOrNull { it.name == name } ?: NEON_LIGHTS
    }
}

/** Solar Watch styles (spec §5.3): bg / text / gradient accent. */
enum class SolarStyle(val displayName: String, val bg: Color, val text: Color, val accent: Color) {
    STYLE1("Style 1", Color(0xFFA88056), Color(0xFFFFB54E), Color(0xFFDF98CD)),
    STYLE2("Style 2", Color(0xFF8FA4A8), Color(0xFFA4DDDD), Color(0xFFB5E4AB)),
    STYLE3("Style 3", Color(0xFFA1A870), Color(0xFFCEDE55), Color(0xFFE38C63)),
    STYLE4("Style 4", Color(0xFF9893A8), Color(0xFFD0C4F9), Color(0xFFB5DCE5)),
    STYLE5("Style 5", Color(0xFFCD8CD0), Color(0xFFFFA1FF), Color(0xFF3B283C)),
    STYLE6("Style 6", Color(0xFFA8396C), Color(0xFFFF67E6), Color(0xFFDC89D4)),
    STYLE7("Style 7", Color(0xFF94A87D), Color(0xFFB0E279), Color(0xFFE0D977)),
    STYLE8("Style 8", Color(0xFF5B6D8A), Color(0xFFC4E3FF), Color(0xFF384A6B)),
    STYLE10("Style 10", Color(0xFF625138), Color(0xFFFFE18C), Color(0xFF8D734D)),
    STYLE11("Style 11", Color(0xFF33A1B4), Color(0xFFF8D7A4), Color(0xFF66C3D8)),
    STYLE12("Style 12", Color(0xFF4C4C4C), Color(0xFFE0E0E0), Color(0xFF332727)),
    STYLE13("Style 13", Color(0xFF5E7E80), Color(0xFFDFE1E5), Color(0xFF9FBFBE));

    companion object {
        fun byName(name: String?): SolarStyle =
            entries.firstOrNull { it.name == name } ?: STYLE1
    }
}

/** Segmented LED fonts (spec §5.4). Rendered in code as 7-segment digits;
 *  each font varies segment thickness, rounding and ghost-digit brightness. */
enum class SegmentedFont(
    val displayName: String,
    val thickness: Float,
    val rounded: Boolean,
    val ghostAlpha: Float
) {
    SEGMENTS_7_CLASSIC_REGULAR("7 Classic", 1.0f, false, 0.07f),
    SEGMENTS_7_BOLD("7 Bold", 1.45f, false, 0.08f),
    SEGMENTS_7_ROUND("7 Round", 1.0f, true, 0.07f),
    SEGMENTS_8_REGULAR("8 Regular", 0.9f, false, 0.06f),
    SEGMENTS_9_REGULAR("9 Regular", 0.85f, true, 0.06f),
    SEGMENTS_13_REGULAR("13 Regular", 0.8f, false, 0.05f),
    SEGMENTS_13_ROUND("13 Round", 0.8f, true, 0.05f),
    SEGMENTS_15_CUBE("15 Cube", 1.2f, false, 0.09f),
    SEGMENTS_15_LINE("15 Line", 0.6f, false, 0.05f),
    SEGMENTS_15_ROUND("15 Round", 0.9f, true, 0.06f),
    SEGMENTS_17_REGULAR("17 Regular", 0.75f, false, 0.05f),
    SEGMENTS_23_PIXEL("23 Pixel", 1.1f, false, 0.10f);

    companion object {
        fun byName(name: String?): SegmentedFont =
            entries.firstOrNull { it.name == name } ?: SEGMENTS_7_CLASSIC_REGULAR
    }
}

/** Battery variants (spec §6.1). */
enum class BatteryVariant(val displayName: String) {
    PLAIN("Battery Duo"),
    NEON("Battery Duo Neon"),
    CUTE("Battery Duo Cute"),
    CUTE_FILLED("Battery Duo Cute Filled"),
    OVERFILL("Battery Duo Overfill"),
    DOTS("Battery Dots"),
    LIQUID_WAVE("Battery Liquid Wave"),
    RETRO_LCD("Battery Retro LCD"),
    SPEEDOMETER("Battery Speedometer Simple"),
    HERO("Battery Hero Duo")
}

/** Analog dial variants (spec §5.7). */
enum class AnalogVariant(val displayName: String) {
    CLASSIC("Analog Clock"),
    CLEAN("Analog Clock Clean"),
    CLEAN_NO_MARKERS("Analog Clock Clean No Markers"),
    SQUARE("Analog Clock Square"),
    SQUARE_MINI("Analog Clock Square Mini"),
    WITH_DIGITAL("Analog Clock With Digital")
}

/** Charging-screen themes (spec §7). */
enum class ChargingTheme(val displayName: String) {
    MATERIAL("Material"),
    MATERIAL_BLOOM("Material Bloom"),
    NEON("Neon"),
    PIXEL("Pixel")
}

/** Our own quote library — written for this app, in the same spirit as the
 *  original's categories. Never copied from the original's quote strings. */
val QUOTES: List<Pair<String, String>> = listOf(
    "Small steps every day add up to a life you are proud of." to "On progress",
    "The best time to start was yesterday. The next best time is now." to "On starting",
    "You do not have to be great to start, but you have to start to be great." to "On starting",
    "Rest is part of the work. Charge yourself like you charge your phone." to "On rest",
    "Focus is saying no to a hundred good ideas." to "On focus",
    "What you do today shapes every tomorrow." to "On today",
    "Calm is a superpower. Breathe in, breathe out, begin again." to "On calm",
    "A year from now you will wish you had started today." to "On starting",
    "Do the hard thing first and the day gets easier." to "On discipline",
    "Kindness costs nothing and changes everything." to "On kindness",
    "The sun rises every morning. So can you." to "On hope",
    "Slow down. You are not late for your own life." to "On calm",
    "Discipline is choosing what you want most over what you want now." to "On discipline",
    "Gratitude turns what we have into enough." to "On gratitude",
    "Every expert was once a beginner." to "On learning",
    "Your only limit is the story you keep telling yourself." to "On courage",
    "Make today so good that yesterday gets jealous." to "On today",
    "It always seems impossible until it is done." to "On courage",
    "The quieter you become, the more you can hear." to "On calm",
    "Happiness is not a destination. It is the way you travel." to "On happiness",
    "One kind word can change someone's whole day." to "On kindness",
    "Do not wait for the perfect moment. Take the moment and make it perfect." to "On today",
    "You are stronger than you think and braver than you believe." to "On courage",
    "A little progress each day adds up to big results." to "On progress",
    "The future depends on what you do today." to "On today",
    "Be the reason someone smiles today." to "On kindness",
    "Difficult roads often lead to beautiful destinations." to "On hope",
    "Your dreams do not have an expiry date. Take a deep breath and try again." to "On hope",
    "What lies behind us and what lies before us are small matters compared to what lies within us." to "On courage",
    "Stay close to people who feel like sunlight." to "On friendship",
    "The best way out is always through." to "On courage",
    "Nothing worth having comes easy — and that is what makes it worth having." to "On progress",
    "Today is a new page. Write something worth reading." to "On today",
    "Peace begins when expectation ends." to "On calm",
    "You cannot pour from an empty cup. Take care of yourself first." to "On rest",
    "Success is the sum of small efforts repeated day in and day out." to "On progress",
    "Believe you can and you are halfway there." to "On courage",
    "The only bad workout is the one that did not happen." to "On discipline",
    "Learn to rest, not to quit." to "On rest",
    "Where focus goes, energy flows." to "On focus",
    "Be patient with yourself. Nothing in nature blooms all year." to "On calm",
    "A journey of a thousand miles begins with a single step." to "On starting",
    "Do not count the days. Make the days count." to "On today",
    "The mind is everything. What you think, you become." to "On mindset",
    "Chase the vision, not the money. The money will end up following you." to "On purpose",
    "Work hard in silence. Let success make the noise." to "On discipline",
    "Keep your face always toward the sunshine, and shadows will fall behind you." to "On hope",
    "It is never too late to be what you might have been." to "On hope"
)

/** Concepts tabs (spec §5.20) — 34 experimental widgets. */
val CONCEPT_TABS: List<String> = listOf(
    "Affirmations", "Binary", "Birthday", "Breathing", "Coin Flip", "Boost",
    "Decisions", "Dice", "Countdown", "Eye Care", "Tasks", "Sounds",
    "Fortune", "Gratitude", "Habits", "Heartbeat", "Hydration", "Kindness",
    "Kintsugi", "Life Stats", "Math", "Metronome", "Mood", "Moon",
    "Now", "Posture", "Time Left", "Stopwatch", "Calendar", "Tiles",
    "Xbox Music", "News", "Weather", "World Clock"
)

/** Protocol suggestions (spec §6.10). */
val PROTOCOL_SUGGESTIONS: List<String> = listOf(
    "No phone after 10pm",
    "Move before you scroll",
    "Say no by default",
    "Journal before bed",
    "Stretch before touching your phone",
    "Walk for 20 minutes",
    "Drink water before coffee",
    "No screens during meals"
)
