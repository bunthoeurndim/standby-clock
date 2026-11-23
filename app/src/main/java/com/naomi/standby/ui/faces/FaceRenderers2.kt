package com.naomi.standby.ui.faces

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.model.AnalogVariant
import com.naomi.standby.model.SolarStyle
import com.naomi.standby.util.WORLD_ZONES
import com.naomi.standby.util.formatDateShort
import com.naomi.standby.util.timeInZone
import com.naomi.standby.util.toRoman
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Analog family — classic round dial, clean dials, square dial, Roman
 *  numerals, and the analog+digital combo. Thin white hands, minimal
 *  tick marks, red second hand (the Play Store screenshot look). */
@Composable
fun AnalogFace(
    now: LocalDateTime,
    scale: Float,
    variant: AnalogVariant,
    showDigital: Boolean = false,
    roman: Boolean = false
) {
    val square = variant == AnalogVariant.SQUARE || variant == AnalogVariant.SQUARE_MINI
    val noMarkers = variant == AnalogVariant.CLEAN_NO_MARKERS
    val dialSize = (300 * scale).dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(dialSize)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension / 2f - 10f
            if (square) {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(10f, 10f),
                    size = androidx.compose.ui.geometry.Size(size.width - 20f, size.height - 20f),
                    style = Stroke(width = 6f)
                )
            } else {
                drawCircle(color = Color.White, radius = radius, style = Stroke(width = 6f))
            }
            if (!noMarkers) {
                if (roman) {
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 34f * scale
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.SERIF
                        }
                        for (h in 1..12) {
                            val angle = h * 30.0 * PI / 180.0 - PI / 2.0
                            val r = radius - 46f * scale
                            canvas.nativeCanvas.drawText(
                                toRoman(h),
                                (cx + cos(angle) * r).toFloat(),
                                (cy + sin(angle) * r + 12f * scale).toFloat(),
                                paint
                            )
                        }
                    }
                } else {
                    for (h in 0 until 12) {
                        val angle = h * 30.0 * PI / 180.0 - PI / 2.0
                        val inner = radius - if (h % 3 == 0) 36f else 22f
                        val outer = radius - 6f
                        drawLine(
                            color = Color.White,
                            start = Offset((cx + cos(angle) * inner).toFloat(), (cy + sin(angle) * inner).toFloat()),
                            end = Offset((cx + cos(angle) * outer).toFloat(), (cy + sin(angle) * outer).toFloat()),
                            strokeWidth = if (h % 3 == 0) 8f else 4f
                        )
                    }
                }
            }
            val hourAngle = ((now.hour % 12) * 30.0 + now.minute * 0.5) * PI / 180.0 - PI / 2.0
            val minuteAngle = (now.minute * 6.0 + now.second * 0.1) * PI / 180.0 - PI / 2.0
            val secondAngle = now.second * 6.0 * PI / 180.0 - PI / 2.0
            drawLine(
                color = Color.White,
                start = Offset(cx, cy),
                end = Offset((cx + cos(hourAngle) * radius * 0.5).toFloat(), (cy + sin(hourAngle) * radius * 0.5).toFloat()),
                strokeWidth = 12f
            )
            drawLine(
                color = Color.White,
                start = Offset(cx, cy),
                end = Offset((cx + cos(minuteAngle) * radius * 0.75).toFloat(), (cy + sin(minuteAngle) * radius * 0.75).toFloat()),
                strokeWidth = 8f
            )
            drawLine(
                color = Color(0xFFFF2A1A),
                start = Offset(cx, cy),
                end = Offset((cx + cos(secondAngle) * radius * 0.85).toFloat(), (cy + sin(secondAngle) * radius * 0.85).toFloat()),
                strokeWidth = 3f
            )
            drawCircle(color = Color.White, radius = 10f, center = Offset(cx, cy))
        }
        if (showDigital || roman) {
            Spacer(modifier = Modifier.height((8 * scale).dp))
            Text(
                text = "%02d:%02d".format(now.hour, now.minute),
                color = Color.White,
                fontSize = (36 * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height((6 * scale).dp))
        Text(text = formatDateShort(now), color = Color.White.copy(alpha = 0.8f), fontSize = (22 * scale).sp)
    }
}

/** Radial Invert Clock — a white inverted dial on black: black hands and
 *  markers inside a white circle, digital readout underneath. */
@Composable
fun RadialInvertFace(now: LocalDateTime, scale: Float) {
    val dialSize = (300 * scale).dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(dialSize)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension / 2f - 10f
            drawCircle(color = Color.White, radius = radius)
            drawCircle(color = Color.Black, radius = radius, style = Stroke(width = 4f))
            for (h in 0 until 12) {
                val angle = h * 30.0 * PI / 180.0 - PI / 2.0
                val inner = radius - if (h % 3 == 0) 34f else 20f
                val outer = radius - 6f
                drawLine(
                    color = Color.Black,
                    start = Offset((cx + cos(angle) * inner).toFloat(), (cy + sin(angle) * inner).toFloat()),
                    end = Offset((cx + cos(angle) * outer).toFloat(), (cy + sin(angle) * outer).toFloat()),
                    strokeWidth = if (h % 3 == 0) 8f else 4f
                )
            }
            val hourAngle = ((now.hour % 12) * 30.0 + now.minute * 0.5) * PI / 180.0 - PI / 2.0
            val minuteAngle = (now.minute * 6.0 + now.second * 0.1) * PI / 180.0 - PI / 2.0
            drawLine(
                color = Color.Black,
                start = Offset(cx, cy),
                end = Offset((cx + cos(hourAngle) * radius * 0.5).toFloat(), (cy + sin(hourAngle) * radius * 0.5).toFloat()),
                strokeWidth = 12f
            )
            drawLine(
                color = Color.Black,
                start = Offset(cx, cy),
                end = Offset((cx + cos(minuteAngle) * radius * 0.75).toFloat(), (cy + sin(minuteAngle) * radius * 0.75).toFloat()),
                strokeWidth = 8f
            )
            drawCircle(color = Color.Black, radius = 10f, center = Offset(cx, cy))
        }
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = Color.White,
            fontSize = (48 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

/** Solar Watch — time plus a sun arc showing daylight progress, in the
 *  selected SolarStyle colours. */
@Composable
fun SolarFace(now: LocalDateTime, style: SolarStyle, scale: Float) {
    val dayFraction = ((now.hour * 60 + now.minute) - 360).coerceIn(0, 720) / 720f
    val angle = PI * (1f - dayFraction).toDouble()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size((340 * scale).dp, (190 * scale).dp)) {
            val cx = size.width / 2f
            val horizonY = size.height * 0.85f
            val radiusX = size.width * 0.45f
            val radiusY = size.height * 0.75f
            drawLine(
                color = style.text.copy(alpha = 0.7f),
                start = Offset(0f, horizonY),
                end = Offset(size.width, horizonY),
                strokeWidth = 4f
            )
            var prev: Offset? = null
            for (i in 0..40) {
                val a = PI * i / 40.0
                val p = Offset((cx + cos(a) * radiusX).toFloat(), (horizonY - sin(a) * radiusY).toFloat())
                if (prev != null) drawLine(style.accent.copy(alpha = 0.6f), prev, p, 3f)
                prev = p
            }
            val sunX = (cx + cos(angle) * radiusX).toFloat()
            val sunY = (horizonY - sin(angle) * radiusY).toFloat()
            drawCircle(color = style.text, radius = 26f, center = Offset(sunX, sunY))
            drawCircle(color = style.accent, radius = 34f, center = Offset(sunX, sunY), style = Stroke(width = 3f))
        }
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = style.text,
            fontSize = (76 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Sunrise 06:00  ·  Sunset 18:00",
            color = style.text.copy(alpha = 0.8f),
            fontSize = (20 * scale).sp
        )
    }
}

/** Matrix Clock — falling green code columns behind the time.
 *  The rain is deterministic per second, so it changes every tick
 *  without a recomposition storm. */
@Composable
fun MatrixFace(now: LocalDateTime, scale: Float) {
    val seed = now.second * 1000L + now.minute
    val rng = Random(seed)
    val chars = "01<>/\\|=+*#abcdefghijklmnopqrstuvwxyz"
    val green = Color(0xFF00FF41)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (col in 0 until 14) {
                Column(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in 0 until 10) {
                        val ch = chars[rng.nextInt(chars.length)]
                        val alpha = 0.12f + 0.08f * ((row + col) % 6)
                        Text(
                            text = ch.toString(),
                            color = green.copy(alpha = alpha.coerceIn(0f, 1f)),
                            fontSize = (18 * scale).sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%02d:%02d".format(now.hour, now.minute),
                color = green,
                fontSize = (96 * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 16.dp)
            )
            Text(
                text = formatDateShort(now),
                color = green,
                fontSize = (24 * scale).sp,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 12.dp)
            )
        }
    }
}

/** World Clock — local time plus up to 5 cities with offsets. */
@Composable
fun WorldClockFace(now: LocalDateTime, scale: Float) {
    val systemZone = ZoneId.systemDefault()
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = (32 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = Color.White,
            fontSize = (84 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Local · " + formatDateShort(now),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = (22 * scale).sp
        )
        Spacer(modifier = Modifier.height((20 * scale).dp))
        for (zone in WORLD_ZONES) {
            val zoneId = ZoneId.of(zone.zoneId)
            val zoned = now.atZone(systemZone).withZoneSameInstant(zoneId)
            val offsetHours = (zoned.offset.totalSeconds - now.atZone(systemZone).offset.totalSeconds) / 3600
            val offsetText = when {
                offsetHours == 0 -> "same time"
                offsetHours > 0 -> "+${offsetHours}h ahead"
                else -> "${offsetHours}h behind"
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = zone.label, color = Color.White, fontSize = (28 * scale).sp)
                Text(
                    text = offsetText,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = (20 * scale).sp
                )
                Text(
                    text = zoned.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)),
                    color = Color.White,
                    fontSize = (30 * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height((10 * scale).dp))
        }
    }
}
