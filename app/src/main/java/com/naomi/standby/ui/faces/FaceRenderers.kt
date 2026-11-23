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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.model.FaceBase
import com.naomi.standby.model.FaceEntry
import com.naomi.standby.util.SEVEN_SEGMENTS
import com.naomi.standby.util.formatDateLong
import com.naomi.standby.util.formatDateShort
import com.naomi.standby.util.timeInWords
import com.naomi.standby.util.toRoman
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Dispatches to the right base renderer for a face entry. */
@Composable
fun ClockFace(entry: FaceEntry, now: LocalDateTime, fontScale: Float, modifier: Modifier = Modifier) {
    val color = entry.theme.color
    val scale = entry.layout.sizeFactor * fontScale
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (entry.base) {
            FaceBase.BIG_DIGITAL -> BigDigitalFace(now, color, scale)
            FaceBase.FLIP -> FlipFace(now, color, scale)
            FaceBase.ANALOG -> AnalogFace(now, color, scale, roman = false)
            FaceBase.SEGMENTED_LCD -> LcdFace(now, color, scale)
            FaceBase.NEON -> NeonFace(now, color, scale)
            FaceBase.WORD_CLOCK -> WordClockFace(now, color, scale)
            FaceBase.ROMAN -> AnalogFace(now, color, scale, roman = true)
            FaceBase.MATRIX -> MatrixFace(now, color, scale)
            FaceBase.PIXEL_ART -> PixelArtFace(now, color, scale)
            FaceBase.SOLAR -> SolarFace(now, color, scale)
            FaceBase.WORLD_CLOCK -> WorldClockFace(now, color, scale)
            FaceBase.DEMENTIA -> DementiaFace(now, color, scale)
        }
    }
}

@Composable
private fun BigDigitalFace(now: LocalDateTime, color: Color, scale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = color,
            fontSize = (110 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        Text(
            text = "%02d".format(now.second),
            color = color.copy(alpha = 0.6f),
            fontSize = (28 * scale).sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = formatDateShort(now),
            color = color.copy(alpha = 0.8f),
            fontSize = (26 * scale).sp
        )
    }
}

@Composable
private fun FlipFace(now: LocalDateTime, color: Color, scale: Float) {
    val digits = "%02d%02d".format(now.hour, now.minute)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for ((index, ch) in digits.withIndex()) {
                if (index == 2) {
                    Text(
                        text = ":",
                        color = color,
                        fontSize = (90 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1A1A))
                        .padding(horizontal = (10 * scale).dp, vertical = (6 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ch.toString(),
                        color = color,
                        fontSize = (88 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = formatDateLong(now), color = color.copy(alpha = 0.8f), fontSize = (22 * scale).sp)
    }
}

@Composable
private fun AnalogFace(now: LocalDateTime, color: Color, scale: Float, roman: Boolean) {
    val dialSize = (300 * scale).dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size(dialSize)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val radius = size.minDimension / 2f - 8f
            drawCircle(color = color, radius = radius, style = Stroke(width = 6f))
            for (h in 0 until 12) {
                val angle = h * 30.0 * PI / 180.0 - PI / 2.0
                val inner = radius - if (h % 3 == 0) 34f else 20f
                val outer = radius - 6f
                drawLine(
                    color = color,
                    start = Offset((cx + cos(angle) * inner).toFloat(), (cy + sin(angle) * inner).toFloat()),
                    end = Offset((cx + cos(angle) * outer).toFloat(), (cy + sin(angle) * outer).toFloat()),
                    strokeWidth = if (h % 3 == 0) 8f else 4f
                )
            }
            val hourAngle = ((now.hour % 12) * 30.0 + now.minute * 0.5) * PI / 180.0 - PI / 2.0
            val minuteAngle = (now.minute * 6.0 + now.second * 0.1) * PI / 180.0 - PI / 2.0
            val secondAngle = now.second * 6.0 * PI / 180.0 - PI / 2.0
            drawLine(
                color = color,
                start = Offset(cx, cy),
                end = Offset((cx + cos(hourAngle) * radius * 0.5).toFloat(), (cy + sin(hourAngle) * radius * 0.5).toFloat()),
                strokeWidth = 12f
            )
            drawLine(
                color = color,
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
            drawCircle(color = color, radius = 10f, center = Offset(cx, cy))
        }
        if (roman) {
            Text(
                text = "${toRoman(if (now.hour % 12 == 0) 12 else now.hour % 12)} : ${"%02d".format(now.minute)}",
                color = color,
                fontSize = (30 * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        }
        Text(text = formatDateShort(now), color = color.copy(alpha = 0.8f), fontSize = (22 * scale).sp)
    }
}

@Composable
private fun LcdFace(now: LocalDateTime, color: Color, scale: Float) {
    val digits = "%02d:%02d".format(now.hour, now.minute)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            for (ch in digits) {
                if (ch == ':') {
                    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                        Box(modifier = Modifier.size((10 * scale).dp).background(color))
                        Spacer(modifier = Modifier.height((18 * scale).dp))
                        Box(modifier = Modifier.size((10 * scale).dp).background(color))
                    }
                } else {
                    SevenSegmentDigit(ch, color, scale)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = formatDateShort(now), color = color.copy(alpha = 0.8f), fontSize = (22 * scale).sp)
    }
}

@Composable
private fun SevenSegmentDigit(ch: Char, color: Color, scale: Float) {
    val lit = SEVEN_SEGMENTS[ch] ?: emptySet()
    val w = (44 * scale).dp
    val h = (88 * scale).dp
    val thick = (8 * scale).dp
    Canvas(modifier = Modifier.size(w, h).padding(2.dp)) {
        val segW = size.width
        val segH = size.height
        val t = thick.toPx()
        fun hSeg(y: Float, on: Boolean) {
            if (on) drawRoundRect(color = color, topLeft = Offset(t / 2f, y), size = androidx.compose.ui.geometry.Size(segW - t, t))
        }
        fun vSeg(x: Float, y: Float, len: Float, on: Boolean) {
            if (on) drawRoundRect(color = color, topLeft = Offset(x, y), size = androidx.compose.ui.geometry.Size(t, len))
        }
        val mid = segH / 2f
        hSeg(0f, 'a' in lit)
        vSeg(0f, t / 2f, mid - t, 'f' in lit)
        vSeg(segW - t, t / 2f, mid - t, 'b' in lit)
        hSeg(mid - t / 2f, 'g' in lit)
        vSeg(0f, mid + t / 2f, mid - t, 'e' in lit)
        vSeg(segW - t, mid + t / 2f, mid - t, 'c' in lit)
        hSeg(segH - t, 'd' in lit)
    }
}

@Composable
private fun NeonFace(now: LocalDateTime, color: Color, scale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = color,
            fontSize = (100 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            style = TextStyle(shadow = Shadow(color = color, blurRadius = 28f))
        )
        Text(
            text = formatDateShort(now),
            color = color,
            fontSize = (24 * scale).sp,
            style = TextStyle(shadow = Shadow(color = color, blurRadius = 16f))
        )
    }
}

@Composable
private fun WordClockFace(now: LocalDateTime, color: Color, scale: Float) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timeInWords(now),
            color = color,
            fontSize = (44 * scale).sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = (56 * scale).sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = color.copy(alpha = 0.6f),
            fontSize = (26 * scale).sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
