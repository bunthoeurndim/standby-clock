package com.naomi.standby.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Morning / Afternoon / Evening / Night, for the Day Clock face. */
fun dayPart(hour: Int): String = when (hour) {
    in 5..11 -> "Morning"
    in 12..16 -> "Afternoon"
    in 17..20 -> "Evening"
    else -> "Night"
}

fun formatDateLong(now: LocalDateTime): String =
    now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH))

fun formatDateShort(now: LocalDateTime): String =
    now.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH))

private val ROMAN_TABLE = listOf(
    10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I"
)

fun toRoman(value: Int): String {
    if (value <= 0) return "XII"
    var n = value
    val out = StringBuilder()
    // Hours 1..12 only need X, IX, V, IV, I.
    for ((v, s) in ROMAN_TABLE) {
        while (n >= v) {
            out.append(s)
            n -= v
        }
    }
    return out.toString()
}

private val NUMBER_WORDS = listOf(
    "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
    "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
    "seventeen", "eighteen", "nineteen", "twenty", "twenty-one", "twenty-two",
    "twenty-three", "twenty-four", "twenty-five", "twenty-six", "twenty-seven",
    "twenty-eight", "twenty-nine", "thirty", "thirty-one", "thirty-two",
    "thirty-three", "thirty-four", "thirty-five", "thirty-six", "thirty-seven",
    "thirty-eight", "thirty-nine", "forty", "forty-one", "forty-two", "forty-three",
    "forty-four", "forty-five", "forty-six", "forty-seven", "forty-eight",
    "forty-nine", "fifty", "fifty-one", "fifty-two", "fifty-three", "fifty-four",
    "fifty-five", "fifty-six", "fifty-seven", "fifty-eight", "fifty-nine"
)

/** "It is ten past three" style wording for the Word Clock face. */
fun timeInWords(now: LocalDateTime): String {
    val hour12 = now.hour % 12
    val displayHour = if (hour12 == 0) 12 else hour12
    val minute = now.minute
    return when {
        minute == 0 -> "It is ${NUMBER_WORDS[displayHour]} o'clock"
        minute == 15 -> "It is quarter past ${NUMBER_WORDS[displayHour]}"
        minute == 30 -> "It is half past ${NUMBER_WORDS[displayHour]}"
        minute == 45 -> {
            val next = if (displayHour == 12) 1 else displayHour + 1
            "It is quarter to ${NUMBER_WORDS[next]}"
        }
        minute < 30 -> "It is ${NUMBER_WORDS[minute]} past ${NUMBER_WORDS[displayHour]}"
        else -> {
            val next = if (displayHour == 12) 1 else displayHour + 1
            "It is ${NUMBER_WORDS[60 - minute]} to ${NUMBER_WORDS[next]}"
        }
    }
}

/**
 * Seven-segment display: for each digit 0-9, which of the 7 segments
 * (a top, b top-right, c bottom-right, d bottom, e bottom-left, f top-left, g middle) is lit.
 */
val SEVEN_SEGMENTS: Map<Char, Set<Char>> = mapOf(
    '0' to setOf('a', 'b', 'c', 'd', 'e', 'f'),
    '1' to setOf('b', 'c'),
    '2' to setOf('a', 'b', 'g', 'e', 'd'),
    '3' to setOf('a', 'b', 'g', 'c', 'd'),
    '4' to setOf('f', 'g', 'b', 'c'),
    '5' to setOf('a', 'f', 'g', 'c', 'd'),
    '6' to setOf('a', 'f', 'g', 'e', 'c', 'd'),
    '7' to setOf('a', 'b', 'c'),
    '8' to setOf('a', 'b', 'c', 'd', 'e', 'f', 'g'),
    '9' to setOf('a', 'b', 'c', 'd', 'f', 'g')
)

/** 5x7 pixel font for digits 0-9 and ':'. Each row is a 5-bit string, '#' = lit. */
val PIXEL_FONT: Map<Char, List<String>> = mapOf(
    '0' to listOf("01110", "10001", "10011", "10101", "11001", "10001", "01110"),
    '1' to listOf("00100", "01100", "00100", "00100", "00100", "00100", "01110"),
    '2' to listOf("01110", "10001", "00001", "00110", "01000", "10000", "11111"),
    '3' to listOf("11111", "00010", "00100", "00010", "00001", "10001", "01110"),
    '4' to listOf("00010", "00110", "01010", "10010", "11111", "00010", "00010"),
    '5' to listOf("11111", "10000", "11110", "00001", "00001", "10001", "01110"),
    '6' to listOf("00110", "01000", "10000", "11110", "10001", "10001", "01110"),
    '7' to listOf("11111", "00001", "00010", "00100", "01000", "01000", "01000"),
    '8' to listOf("01110", "10001", "10001", "01110", "10001", "10001", "01110"),
    '9' to listOf("01110", "10001", "10001", "01111", "00001", "00010", "01100"),
    ':' to listOf("00000", "00100", "00000", "00000", "00000", "00100", "00000")
)

/** Five world-clock zones, including Naomi's home zone. */
data class WorldZone(val label: String, val zoneId: String)

val WORLD_ZONES: List<WorldZone> = listOf(
    WorldZone("Phnom Penh", "Asia/Phnom_Penh"),
    WorldZone("London", "Europe/London"),
    WorldZone("New York", "America/New_York"),
    WorldZone("Tokyo", "Asia/Tokyo"),
    WorldZone("Sydney", "Australia/Sydney")
)

fun timeInZone(zoneId: String, now: LocalDateTime): String {
    val zoned = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of(zoneId))
    return zoned.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH))
}
