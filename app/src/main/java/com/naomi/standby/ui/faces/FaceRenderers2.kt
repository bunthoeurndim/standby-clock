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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.util.PIXEL_FONT
import com.naomi.standby.util.WORLD_ZONES
import com.naomi.standby.util.dayPart
import com.naomi.standby.util.formatDateLong
import com.naomi.standby.util.formatDateShort
import com.naomi.standby.util.timeInZone
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun MatrixFace(now: LocalDateTime, color: Color, scale: Float) {
    // Deterministic "rain" for this second: columns of characters, faded by row.
    val seed = now.second * 1000L + now.minute
    val rng = Random(seed)
    val chars = "01<>/\\|=+*#abcdefghijklmnopqrstuvwxyz"
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
                        val alpha = 0.15f + 0.08f * ((row + col) % 6)
                        Text(
                            text = ch.toString(),
                            color = color.copy(alpha = alpha.coerceIn(0f, 1f)),
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
                color = color,
                fontSize = (96 * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 16.dp)
            )
            Text(
                text = formatDateShort(now),
                color = color,
                fontSize = (24 * scale).sp,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun PixelArtFace(now: LocalDateTime, color: Color, scale: Float) {
    val digits = "%02d:%02d".format(now.hour, now.minute)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            for (ch in digits) {
                PixelChar(ch, color, scale)
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = formatDateShort(now), color = color.copy(alpha = 0.8f), fontSize = (22 * scale).sp)
    }
}

@Composable
private fun PixelChar(ch: Char, color: Color, scale: Float) {
    val rows = PIXEL_FONT[ch] ?: PIXEL_FONT['0']!!
    val cell = (9 * scale).dp
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

@Composable
fun SolarFace(now: LocalDateTime, color: Color, scale: Float) {
    // Sun travels an arc from 06:00 (horizon left) to 18:00 (horizon right).
    val dayFraction = ((now.hour * 60 + now.minute) - 360).coerceIn(0, 720) / 720f
    val angle = PI * (1f - dayFraction).toDouble()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size((320 * scale).dp, (180 * scale).dp)) {
            val cx = size.width / 2f
            val horizonY = size.height * 0.85f
            val radiusX = size.width * 0.45f
            val radiusY = size.height * 0.75f
            drawLine(color = color.copy(alpha = 0.7f), start = Offset(0f, horizonY), end = Offset(size.width, horizonY), strokeWidth = 4f)
            // Arc path (approximated with a polyline).
            var prev: Offset? = null
            for (i in 0..40) {
                val a = PI * i / 40.0
                val p = Offset((cx + cos(a) * radiusX).toFloat(), (horizonY - sin(a) * radiusY).toFloat())
                if (prev != null) drawLine(color = color.copy(alpha = 0.4f), start = prev, end = p, strokeWidth = 3f)
                prev = p
            }
            val sunX = (cx + cos(angle) * radiusX).toFloat()
            val sunY = (horizonY - sin(angle) * radiusY).toFloat()
            drawCircle(color = Color(0xFFFFB300), radius = 26f, center = Offset(sunX, sunY))
            drawCircle(color = color, radius = 34f, center = Offset(sunX, sunY), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
        }
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = color,
            fontSize = (72 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(text = "Sunrise 06:00  ·  Sunset 18:00", color = color.copy(alpha = 0.8f), fontSize = (20 * scale).sp)
    }
}

@Composable
fun WorldClockFace(now: LocalDateTime, color: Color, scale: Float) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = color,
            fontSize = (84 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(text = "Local · " + formatDateShort(now), color = color.copy(alpha = 0.8f), fontSize = (22 * scale).sp)
        Spacer(modifier = Modifier.height(20.dp))
        for (zone in WORLD_ZONES) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = zone.label, color = color, fontSize = (28 * scale).sp)
                Text(
                    text = timeInZone(zone.zoneId, now),
                    color = color,
                    fontSize = (28 * scale).sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun DementiaFace(now: LocalDateTime, color: Color, scale: Float) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayPart(now.hour),
            color = color,
            fontSize = (64 * scale).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatDateLong(now),
            color = color,
            fontSize = (36 * scale).sp,
            textAlign = TextAlign.Center,
            lineHeight = (44 * scale).sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = color,
            fontSize = (96 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
