package com.naomi.standby.ui.faces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.BatteryInfo
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.data.rememberWeather
import com.naomi.standby.model.BigCropTheme
import com.naomi.standby.model.RetroflipStyle
import com.naomi.standby.model.SegmentedFont
import com.naomi.standby.util.PIXEL_FONT
import com.naomi.standby.util.SEVEN_SEGMENTS
import com.naomi.standby.util.dayPart
import com.naomi.standby.util.formatDateLong
import com.naomi.standby.util.formatDateShort
import java.time.LocalDateTime

/** Shared small helpers for the digital faces. */

@Composable
fun DateLine(now: LocalDateTime, color: Color, scale: Float) {
    Text(
        text = formatDateShort(now),
        color = color.copy(alpha = 0.85f),
        fontSize = (24 * scale).sp
    )
}

@Composable
fun GreetingLine(now: LocalDateTime, color: Color, scale: Float) {
    Text(
        text = "Good ${dayPart(now.hour).lowercase()}",
        color = color.copy(alpha = 0.7f),
        fontSize = (20 * scale).sp
    )
}

/** Big Crop Clock — one huge bold time filling the area, each digit coloured
 *  from the theme palette in turn (the per-digit pastel look of the
 *  Play Store screenshots), theme background behind it. */
@Composable
fun BigCropFace(now: LocalDateTime, theme: BigCropTheme, scale: Float, showSeconds: Boolean) {
    val digits = "%02d%02d".format(now.hour, now.minute)
    val palette = theme.palette
    Box(
        modifier = Modifier.fillMaxSize().background(theme.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                for ((index, ch) in digits.withIndex()) {
                    if (index == 2) {
                        Text(
                            text = ":",
                            color = palette[2 % palette.size],
                            fontSize = (120 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = (2 * scale).dp)
                        )
                    }
                    Text(
                        text = ch.toString(),
                        color = palette[index % palette.size],
                        fontSize = (150 * scale).sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
            if (showSeconds) {
                Text(
                    text = "%02d".format(now.second),
                    color = palette[0].copy(alpha = 0.7f),
                    fontSize = (30 * scale).sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height((10 * scale).dp))
            Text(
                text = formatDateLong(now),
                color = palette[1].copy(alpha = 0.85f),
                fontSize = (24 * scale).sp
            )
        }
    }
}

/** Retroflip Clock — split-flap digit cards on the style's background
 *  (solid or gradient), hours/minutes/seconds each in their own card.
 *  The flip animation is simplified to static split cards with a centre
 *  seam; the digit changes every minute like the original. */
@Composable
fun RetroflipFace(now: LocalDateTime, style: RetroflipStyle, scale: Float, showSeconds: Boolean) {
    val bgModifier = if (style.gradient.size >= 2) {
        Modifier.fillMaxSize().background(Brush.linearGradient(style.gradient))
    } else {
        Modifier.fillMaxSize().background(style.bg)
    }
    Box(modifier = bgModifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                FlipCard("%02d".format(now.hour), style, scale)
                Text(
                    text = ":",
                    color = style.text,
                    fontSize = (80 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
                FlipCard("%02d".format(now.minute), style, scale)
                if (showSeconds) {
                    Text(
                        text = ":",
                        color = style.text.copy(alpha = 0.7f),
                        fontSize = (80 * scale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    FlipCard("%02d".format(now.second), style, scale)
                }
            }
            Spacer(modifier = Modifier.height((14 * scale).dp))
            Text(text = formatDateLong(now), color = style.text.copy(alpha = 0.85f), fontSize = (22 * scale).sp)
        }
    }
}

@Composable
private fun FlipCard(text: String, style: RetroflipStyle, scale: Float) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape((10 * scale).dp))
            .background(style.card)
            .padding(horizontal = (14 * scale).dp, vertical = (8 * scale).dp),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = style.text,
                fontSize = (92 * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            // Split-flap seam across the middle of the card.
            Box(
                modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.Black.copy(alpha = 0.35f))
            )
        }
    }
}

/** Segmented LED clock — 7-segment digits drawn on Canvas, with a dimmed
 *  ghost of the unlit segments behind the lit ones, a blinking separator
 *  option and a date/alarm info line underneath. */
@Composable
fun SegmentedFace(
    now: LocalDateTime,
    font: SegmentedFont,
    scale: Float,
    horizontal: Boolean,
    blinkSeparator: Boolean,
    showLeadingZero: Boolean
) {
    val hourText = if (showLeadingZero) "%02d".format(now.hour) else now.hour.toString()
    val digits = "$hourText:${"%02d".format(now.minute)}"
    val colonOn = !blinkSeparator || now.second % 2 == 0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (horizontal) (10 * scale).dp else (2 * scale).dp)
        ) {
            for (ch in digits) {
                if (ch == ':') {
                    Column(
                        modifier = Modifier.padding(horizontal = (6 * scale).dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size((12 * scale).dp)
                                .background(if (colonOn) Color(0xFFFF3B30) else Color.Transparent)
                        )
                        Spacer(modifier = Modifier.height((22 * scale).dp))
                        Box(
                            modifier = Modifier.size((12 * scale).dp)
                                .background(if (colonOn) Color(0xFFFF3B30) else Color.Transparent)
                        )
                    }
                } else {
                    SevenSegmentDigit(ch, Color(0xFFFF3B30), font, scale)
                }
            }
        }
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Text(text = formatDateShort(now), color = Color.White.copy(alpha = 0.8f), fontSize = (22 * scale).sp)
    }
}

@Composable
fun SevenSegmentDigit(ch: Char, color: Color, font: SegmentedFont, scale: Float) {
    val lit = SEVEN_SEGMENTS[ch] ?: emptySet()
    val w = (52 * scale).dp
    val h = (104 * scale).dp
    val thick = (9 * font.thickness * scale).dp
    Canvas(modifier = Modifier.size(w, h).padding((3 * scale).dp)) {
        val segW = size.width
        val segH = size.height
        val t = thick.toPx()
        val ghost = color.copy(alpha = font.ghostAlpha)
        fun hSeg(y: Float, seg: Char) {
            val c = if (seg in lit) color else ghost
            if (font.rounded) {
                drawRoundRect(color = c, topLeft = Offset(t / 2f, y), size = Size(segW - t, t), cornerRadius = androidx.compose.ui.geometry.CornerRadius(t / 2f, t / 2f))
            } else {
                drawRect(color = c, topLeft = Offset(t / 2f, y), size = Size(segW - t, t))
            }
        }
        fun vSeg(x: Float, y: Float, len: Float, seg: Char) {
            val c = if (seg in lit) color else ghost
            if (font.rounded) {
                drawRoundRect(color = c, topLeft = Offset(x, y), size = Size(t, len), cornerRadius = androidx.compose.ui.geometry.CornerRadius(t / 2f, t / 2f))
            } else {
                drawRect(color = c, topLeft = Offset(x, y), size = Size(t, len))
            }
        }
        val mid = segH / 2f
        hSeg(0f, 'a')
        vSeg(0f, t / 2f, mid - t, 'f')
        vSeg(segW - t, t / 2f, mid - t, 'b')
        hSeg(mid - t / 2f, 'g')
        vSeg(0f, mid + t / 2f, mid - t, 'e')
        vSeg(segW - t, mid + t / 2f, mid - t, 'c')
        hSeg(segH - t, 'd')
    }
}

/** Neon Clock — digits with a layered glow. */
@Composable
fun NeonFace(now: LocalDateTime, color: Color, scale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = color,
            fontSize = (110 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            style = TextStyle(shadow = Shadow(color = color, blurRadius = 32f))
        )
        Text(
            text = formatDateShort(now),
            color = color,
            fontSize = (24 * scale).sp,
            style = TextStyle(shadow = Shadow(color = color, blurRadius = 18f))
        )
    }
}

/** AMOLED Clock — plain professional digital clock on pure black. */
@Composable
fun AmoledFace(now: LocalDateTime, scale: Float, showSeconds: Boolean, blinkSeparator: Boolean) {
    val colon = if (blinkSeparator && now.second % 2 == 1) " " else ":"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%02d$colon%02d".format(now.hour, now.minute),
            color = Color.White,
            fontSize = (120 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        if (showSeconds) {
            Text(
                text = "%02d".format(now.second),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = (28 * scale).sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height((8 * scale).dp))
        DateLine(now, Color.White, scale)
    }
}

/** Digital Clocks Fullscreen — big customizable digital clock. */
@Composable
fun DigitalFullscreenFace(now: LocalDateTime, scale: Float, showSeconds: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = Color.White,
            fontSize = (130 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        if (showSeconds) {
            Text(
                text = "%02d".format(now.second),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = (30 * scale).sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height((10 * scale).dp))
        DateLine(now, Color.White, scale)
        Spacer(modifier = Modifier.height((6 * scale).dp))
        GreetingLine(now, Color.White, scale)
    }
}

/** Digital Clock With Date — time, date, greeting and a weather line. */
@Composable
fun DigitalWithDateFace(now: LocalDateTime, scale: Float) {
    val context = LocalContext.current
    val weather = rememberWeather(context)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GreetingLine(now, Color.White, scale)
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = Color.White,
            fontSize = (110 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Text(
            text = formatDateLong(now),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = (26 * scale).sp
        )
        val w = weather
        if (w != null && w.temperatureC != null) {
            Spacer(modifier = Modifier.height((8 * scale).dp))
            Text(
                text = "%.0f° · ${w.condition}".format(w.temperatureC),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = (22 * scale).sp
            )
        }
    }
}

/** Dementia Clock — very large text, full date and time spelled out clearly. */
@Composable
fun DementiaFace(now: LocalDateTime, scale: Float) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = (24 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayPart(now.hour),
            color = Color.White,
            fontSize = (64 * scale).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Text(
            text = formatDateLong(now),
            color = Color.White,
            fontSize = (38 * scale).sp,
            textAlign = TextAlign.Center,
            lineHeight = (46 * scale).sp
        )
        Spacer(modifier = Modifier.height((18 * scale).dp))
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = Color.White,
            fontSize = (110 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "It is ${"%02d".format(now.hour)}:${"%02d".format(now.minute)}",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = (22 * scale).sp
        )
    }
}

/** Sereno — quiet, sober Duo clock: time, date and a subtle status line. */
@Composable
fun SerenoFace(now: LocalDateTime, scale: Float, settings: SettingsStore) {
    val context = LocalContext.current
    val battery = BatteryInfo.read(context)
    val weather = rememberWeather(context)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = Color.White,
            fontSize = (110 * scale).sp,
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Text(
            text = formatDateLong(now),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = (24 * scale).sp
        )
        Spacer(modifier = Modifier.height((14 * scale).dp))
        val status = buildString {
            append("${battery.levelPercent}%")
            if (battery.charging) append(" · Charging")
            val w = weather
            if (w != null && w.temperatureC != null) append(" · %.0f°".format(w.temperatureC))
        }
        Text(
            text = status,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = (20 * scale).sp
        )
    }
}

/** Clock Hero Duo — hero clock with a sun arc. */
@Composable
fun ClockHeroFace(now: LocalDateTime, scale: Float) {
    val dayFraction = ((now.hour * 60 + now.minute) - 360).coerceIn(0, 720) / 720f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = Color.White,
            fontSize = (120 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height((6 * scale).dp))
        Canvas(modifier = Modifier.size((320 * scale).dp, (60 * scale).dp)) {
            val horizonY = size.height * 0.8f
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(0f, horizonY),
                end = Offset(size.width, horizonY),
                strokeWidth = 3f
            )
            val cx = size.width / 2f
            val rx = size.width * 0.45f
            val ry = size.height * 0.7f
            var prev: Offset? = null
            for (i in 0..40) {
                val a = Math.PI * i / 40.0
                val p = Offset(
                    (cx + Math.cos(a) * rx).toFloat(),
                    (horizonY - Math.sin(a) * ry).toFloat()
                )
                if (prev != null) drawLine(Color(0xFFFFB300).copy(alpha = 0.8f), prev, p, 3f)
                prev = p
            }
            val angle = Math.PI * (1f - dayFraction)
            drawCircle(
                color = Color(0xFFFFB300),
                radius = 14f,
                center = Offset(
                    (cx + Math.cos(angle) * rx).toFloat(),
                    (horizonY - Math.sin(angle) * ry).toFloat()
                )
            )
        }
        Spacer(modifier = Modifier.height((6 * scale).dp))
        Text(
            text = formatDateShort(now) + "  ·  Sunrise 06:00  ·  Sunset 18:00",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = (20 * scale).sp
        )
    }
}

/** Word Clock — an 11×10 letter grid; the words forming the current time
 *  light up, the rest stays dim. Time is rounded to the nearest 5 minutes. */
@Composable
fun WordClockFace(now: LocalDateTime, scale: Float) {
    val grid = listOf(
        "ITLISASTIME",
        "ACQUARTERDC",
        "TWENTYFIVEX",
        "HALFBTENFTO",
        "PASTERUNINE",
        "ONESIXTHREE",
        "FOURFIVETWO",
        "EIGHTELEVEN",
        "SEVENTWELVE",
        "TENSEOCLOCK"
    )
    // (row, col, length) of each word in the grid.
    val words = mapOf(
        "IT" to Triple(0, 0, 2), "IS" to Triple(0, 3, 2),
        "QUARTER" to Triple(1, 2, 7),
        "TWENTY" to Triple(2, 0, 6), "FIVE" to Triple(2, 7, 4),
        "HALF" to Triple(3, 0, 4), "TEN" to Triple(3, 6, 3),
        "TO" to Triple(3, 9, 2),
        "PAST" to Triple(4, 0, 4), "NINE" to Triple(4, 7, 4),
        "ONE" to Triple(5, 0, 3), "SIX" to Triple(5, 4, 3), "THREE" to Triple(5, 7, 5),
        "FOUR" to Triple(6, 0, 4), "FIVE2" to Triple(6, 5, 4), "TWO" to Triple(6, 8, 3),
        "EIGHT" to Triple(7, 0, 5), "ELEVEN" to Triple(7, 6, 5),
        "SEVEN" to Triple(8, 0, 5), "TWELVE" to Triple(8, 6, 6),
        "TEN2" to Triple(9, 0, 3), "OCLOCK" to Triple(9, 5, 6)
    )
    val hourNames = listOf(
        "TWELVE", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX",
        "SEVEN", "EIGHT", "NINE", "TEN", "ELEVEN"
    )
    val lit = mutableSetOf<Pair<Int, Int>>()
    fun light(word: String) {
        val (r, c, len) = words[word] ?: return
        for (i in 0 until len) lit.add(r to c + i)
    }
    val roundedMinutes = ((now.minute + 2) / 5) * 5
    val hour12 = now.hour % 12
    val displayHour = if (roundedMinutes == 60) (hour12 + 1) % 12 else hour12
    val hourWord = hourNames[displayHour]
    light("IT"); light("IS")
    when (roundedMinutes) {
        0 -> { light(hourWord); light("OCLOCK") }
        5 -> { light("FIVE"); light("PAST"); light(hourWord) }
        10 -> { light("TEN"); light("PAST"); light(hourWord) }
        15 -> { light("QUARTER"); light("PAST"); light(hourWord) }
        20 -> { light("TWENTY"); light("PAST"); light(hourWord) }
        25 -> { light("TWENTY"); light("FIVE"); light("PAST"); light(hourWord) }
        30 -> { light("HALF"); light("PAST"); light(hourWord) }
        35 -> { light("TWENTY"); light("FIVE"); light("TO"); light(hourNames[(displayHour + 1) % 12]) }
        40 -> { light("TWENTY"); light("TO"); light(hourNames[(displayHour + 1) % 12]) }
        45 -> { light("QUARTER"); light("TO"); light(hourNames[(displayHour + 1) % 12]) }
        50 -> { light("TEN"); light("TO"); light(hourNames[(displayHour + 1) % 12]) }
        55 -> { light("FIVE"); light("TO"); light(hourNames[(displayHour + 1) % 12]) }
        else -> { light(hourWord); light("OCLOCK") }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for ((rowIndex, row) in grid.withIndex()) {
            Row {
                for ((colIndex, ch) in row.withIndex()) {
                    val on = rowIndex to colIndex in lit
                    Text(
                        text = ch.toString(),
                        color = if (on) Color.White else Color.White.copy(alpha = 0.18f),
                        fontSize = (30 * scale).sp,
                        fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width((26 * scale).dp),
                        textAlign = TextAlign.Center,
                        style = if (on) TextStyle(shadow = Shadow(Color.White, blurRadius = 14f)) else TextStyle.Default
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = (24 * scale).sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/** Pixel Art Clock — blocky 5×7 pixel digits. */
@Composable
fun PixelArtFace(now: LocalDateTime, scale: Float) {
    val digits = "%02d:%02d".format(now.hour, now.minute)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            for (ch in digits) {
                PixelChar(ch, Color(0xFF76FF03), scale)
                Spacer(modifier = Modifier.width((6 * scale).dp))
            }
        }
        Spacer(modifier = Modifier.height((12 * scale).dp))
        DateLine(now, Color(0xFF76FF03), scale)
    }
}

@Composable
private fun PixelChar(ch: Char, color: Color, scale: Float) {
    val rows = PIXEL_FONT[ch] ?: PIXEL_FONT['0']!!
    val cell = (10 * scale).dp
    Column {
        for (row in rows) {
            Row {
                for (pixel in row) {
                    Box(
                        modifier = Modifier.size(cell)
                            .background(if (pixel == '1') color else Color.Transparent)
                    )
                }
            }
        }
    }
}
