package com.naomi.standby.ui.faces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.BatteryInfo
import com.naomi.standby.data.BatteryStatus
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.model.BatteryVariant
import com.naomi.standby.model.ChargingTheme
import com.naomi.standby.util.PIXEL_FONT
import com.naomi.standby.util.formatDateShort
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Ants Clock — the time is drawn by marching ants: tiny dark dots walk
 *  along the digit outlines. The dots shift position every second, so
 *  the trail appears to march. Approximation of the original's
 *  ant-trail rendering. */
@Composable
fun AntsFace(now: LocalDateTime, scale: Float, antSize: Int = 1) {
    val digits = "%02d:%02d".format(now.hour, now.minute)
    val march = now.second % 3
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row {
            for (ch in digits) {
                AntChar(ch, scale, march, antSize)
                Spacer(modifier = Modifier.width((8 * scale).dp))
            }
        }
        Spacer(modifier = Modifier.height((14 * scale).dp))
        Text(text = formatDateShort(now), color = Color.White.copy(alpha = 0.8f), fontSize = (22 * scale).sp)
    }
}

@Composable
private fun AntChar(ch: Char, scale: Float, march: Int, antSize: Int) {
    val rows = PIXEL_FONT[ch] ?: PIXEL_FONT['0']!!
    val cell = (11 * scale).dp
    val dot = ((3 + antSize) * scale).dp
    Column {
        for ((rowIndex, row) in rows.withIndex()) {
            Row {
                for ((colIndex, pixel) in row.withIndex()) {
                    Box(modifier = Modifier.size(cell), contentAlignment = Alignment.Center) {
                        if (pixel == '1') {
                            // The ant "walks": its dot is offset along the row by the march step.
                            val dx = (((rowIndex + colIndex + march) % 3) - 1) * 1.5f
                            Box(
                                modifier = Modifier.size(dot)
                                    .padding(start = dx.dp.coerceAtLeast(0.dp))
                                    .background(Color.White, shape = RoundedCornerShape(50))
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Clock Of Clocks — a grid of many small analog dials whose hands
 *  collectively form the digital time. Each cell of the 5×7 pixel
 *  digits is one mini dial: "on" cells show hands at 10:10, "off"
 *  cells show both hands pointing down. */
@Composable
fun ClockOfClocksFace(now: LocalDateTime, scale: Float) {
    val digits = "%02d:%02d".format(now.hour, now.minute)
    val cell = (34 * scale).dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 7 rows; each digit contributes 5 columns, colon contributes 1.
        for (row in 0 until 7) {
            Row {
                for (ch in digits) {
                    val glyph = PIXEL_FONT[ch] ?: PIXEL_FONT['0']!!
                    if (ch == ':') {
                        // Colon: one narrow column, lit on rows 1 and 5.
                        MiniDial(on = row == 1 || row == 5, size = cell)
                        Spacer(modifier = Modifier.width((4 * scale).dp))
                    } else {
                        for (col in 0 until 5) {
                            MiniDial(on = glyph[row][col] == '1', size = cell)
                        }
                        Spacer(modifier = Modifier.width((8 * scale).dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height((14 * scale).dp))
        Text(
            text = "Tap anywhere to see the animation",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = (18 * scale).sp
        )
    }
}

@Composable
private fun MiniDial(on: Boolean, size: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.size(size).padding(2.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2f - 2f
        drawCircle(color = Color.White.copy(alpha = if (on) 0.9f else 0.35f), radius = radius, style = Stroke(width = 2f))
        if (on) {
            // 10:10 pose.
            val a1 = (10 * 30.0) * PI / 180.0 - PI / 2.0
            val a2 = (2 * 30.0) * PI / 180.0 - PI / 2.0
            drawLine(Color.White, Offset(cx, cy), Offset((cx + cos(a1) * radius * 0.7).toFloat(), (cy + sin(a1) * radius * 0.7).toFloat()), 3f)
            drawLine(Color.White, Offset(cx, cy), Offset((cx + cos(a2) * radius * 0.7).toFloat(), (cy + sin(a2) * radius * 0.7).toFloat()), 3f)
        } else {
            // Both hands pointing down.
            drawLine(Color.White.copy(alpha = 0.5f), Offset(cx, cy), Offset(cx, cy + radius * 0.7f), 3f)
        }
        drawCircle(color = Color.White, radius = 2f, center = Offset(cx, cy))
    }
}

/** Battery family — all variants show charge % and charging state. */
@Composable
fun BatteryFace(variant: BatteryVariant, scale: Float, settings: SettingsStore) {
    val context = LocalContext.current
    val battery = BatteryInfo.read(context)
    when (variant) {
        BatteryVariant.PLAIN -> BatteryPlain(battery, scale, glow = false)
        BatteryVariant.NEON -> BatteryPlain(battery, scale, glow = true)
        BatteryVariant.CUTE -> BatteryCute(battery, scale, filled = false)
        BatteryVariant.CUTE_FILLED -> BatteryCute(battery, scale, filled = true)
        BatteryVariant.OVERFILL -> BatteryOverfill(battery, scale)
        BatteryVariant.DOTS -> BatteryDots(battery, scale)
        BatteryVariant.LIQUID_WAVE -> BatteryLiquidWave(battery, scale)
        BatteryVariant.RETRO_LCD -> BatteryRetroLcd(battery, scale)
        BatteryVariant.SPEEDOMETER -> BatterySpeedometer(battery, scale)
        BatteryVariant.HERO -> BatteryHero(battery, scale)
    }
}

@Composable
private fun BatteryStateLine(battery: BatteryStatus, scale: Float) {
    Text(
        text = when {
            battery.levelPercent >= 100 -> "Fully charged"
            battery.charging -> "Charging…"
            battery.levelPercent <= 20 -> "Low battery"
            else -> "Not charging"
        },
        color = Color.White.copy(alpha = 0.8f),
        fontSize = (22 * scale).sp
    )
}

@Composable
private fun BatteryPlain(battery: BatteryStatus, scale: Float, glow: Boolean) {
    val color = if (battery.charging) Color(0xFF4CAF50) else Color.White
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${battery.levelPercent}%",
            color = color,
            fontSize = (96 * scale).sp,
            fontWeight = FontWeight.Bold,
            style = if (glow) TextStyle(shadow = Shadow(color, blurRadius = 28f)) else TextStyle.Default
        )
        BatteryStateLine(battery, scale)
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Box(
            modifier = Modifier.width((280 * scale).dp).height((24 * scale).dp)
                .clip(RoundedCornerShape(12.dp)).background(Color(0xFF222222))
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(color)
                    .fillMaxWidth(battery.levelPercent / 100f)
            )
        }
    }
}

@Composable
private fun BatteryCute(battery: BatteryStatus, scale: Float, filled: Boolean) {
    val face = when {
        battery.levelPercent >= 80 -> "(^_^)"
        battery.levelPercent >= 40 -> "(•‿•)"
        else -> "(>_<)"
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = face, color = Color(0xFFF48FB1), fontSize = (64 * scale).sp, fontWeight = FontWeight.Bold)
        Text(
            text = "${battery.levelPercent}%",
            color = Color.White,
            fontSize = (72 * scale).sp,
            fontWeight = FontWeight.Bold
        )
        if (filled) {
            Box(
                modifier = Modifier.width((240 * scale).dp).height((20 * scale).dp)
                    .clip(RoundedCornerShape(10.dp)).background(Color(0xFF222222))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFF48FB1))
                        .fillMaxWidth(battery.levelPercent / 100f)
                )
            }
            Spacer(modifier = Modifier.height((8 * scale).dp))
        }
        BatteryStateLine(battery, scale)
    }
}

@Composable
private fun BatteryOverfill(battery: BatteryStatus, scale: Float) {
    // The bar overflows past 100%: it keeps growing past the frame.
    val shown = (battery.levelPercent / 100f).coerceAtMost(1.25f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${battery.levelPercent}%",
            color = Color(0xFFFF9800),
            fontSize = (88 * scale).sp,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier.width((260 * scale).dp).height((28 * scale).dp)
                .clip(RoundedCornerShape(14.dp)).background(Color(0xFF222222))
        ) {
            Box(
                modifier = Modifier.width((260 * shown * scale).dp).height((28 * scale).dp)
                    .background(Color(0xFFFF9800))
            )
        }
        Spacer(modifier = Modifier.height((8 * scale).dp))
        BatteryStateLine(battery, scale)
    }
}

@Composable
private fun BatteryDots(battery: BatteryStatus, scale: Float) {
    val lit = (battery.levelPercent / 10f).toInt()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy((10 * scale).dp)) {
            for (i in 0 until 10) {
                Box(
                    modifier = Modifier.size((22 * scale).dp)
                        .background(
                            if (i < lit) Color(0xFF4CAF50) else Color(0xFF222222),
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height((14 * scale).dp))
        Text(
            text = "${battery.levelPercent}%",
            color = Color.White,
            fontSize = (72 * scale).sp,
            fontWeight = FontWeight.Bold
        )
        BatteryStateLine(battery, scale)
    }
}

@Composable
private fun BatteryLiquidWave(battery: BatteryStatus, scale: Float) {
    // Animated liquid fill: a wave line whose height follows the charge level.
    val level = battery.levelPercent / 100f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.size((220 * scale).dp, (160 * scale).dp)) {
            val w = size.width
            val h = size.height
            val fillTop = h * (1f - level)
            drawRoundRect(
                color = Color(0xFF222222),
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(w, h)
            )
            drawRoundRect(
                color = Color(0xFF00BCD4),
                topLeft = Offset(0f, fillTop),
                size = androidx.compose.ui.geometry.Size(w, h - fillTop)
            )
            // Wave crest.
            var prev: Offset? = null
            for (i in 0..40) {
                val x = w * i / 40f
                val y = fillTop + (sin(i * 0.6f + battery.levelPercent) * 6f)
                val p = Offset(x, y)
                if (prev != null) drawLine(Color(0xFFB2EBF2), prev, p, 3f)
                prev = p
            }
        }
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Text(
            text = "${battery.levelPercent}%",
            color = Color.White,
            fontSize = (56 * scale).sp,
            fontWeight = FontWeight.Bold
        )
        BatteryStateLine(battery, scale)
    }
}

@Composable
private fun BatteryRetroLcd(battery: BatteryStatus, scale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF9E9D7A))
                .padding(horizontal = (20 * scale).dp, vertical = (10 * scale).dp)
        ) {
            Text(
                text = "${battery.levelPercent}%",
                color = Color(0xFF2E2E1F),
                fontSize = (72 * scale).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height((10 * scale).dp))
        BatteryStateLine(battery, scale)
    }
}

@Composable
private fun BatterySpeedometer(battery: BatteryStatus, scale: Float) {
    val sweep = battery.levelPercent / 100f * 270f
    Canvas(modifier = Modifier.size((280 * scale).dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2f - 16f
        drawArc(
            color = Color(0xFF333333),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = 18f)
        )
        drawArc(
            color = if (battery.charging) Color(0xFF4CAF50) else Color.White,
            startAngle = 135f,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(width = 18f)
        )
        val angle = (135f + sweep) * PI / 180.0
        drawLine(
            color = Color(0xFFFF2A1A),
            start = Offset(cx, cy),
            end = Offset((cx + cos(angle) * radius * 0.8).toFloat(), (cy + sin(angle) * radius * 0.8).toFloat()),
            strokeWidth = 6f
        )
        drawCircle(color = Color.White, radius = 10f, center = Offset(cx, cy))
    }
    Text(
        text = "${battery.levelPercent}%",
        color = Color.White,
        fontSize = (56 * scale).sp,
        fontWeight = FontWeight.Bold
    )
    BatteryStateLine(battery, scale)
}

@Composable
private fun BatteryHero(battery: BatteryStatus, scale: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${battery.levelPercent}%",
            color = Color.White,
            fontSize = (130 * scale).sp,
            fontWeight = FontWeight.Black
        )
        BatteryStateLine(battery, scale)
        battery.timeToFullMinutes?.let {
            Text(
                text = "Full in about $it min",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (22 * scale).sp
            )
        }
        battery.temperatureC?.let {
            Text(
                text = "%.1f °C".format(it),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (20 * scale).sp
            )
        }
    }
}

/** Charging screen — large battery %, charging state, speed label and ETA.
 *  Themes: Material / Material Bloom / Neon / Pixel change the accent. */
@Composable
fun ChargingFace(theme: ChargingTheme, scale: Float, settings: SettingsStore, now: LocalDateTime) {
    val context = LocalContext.current
    val battery = BatteryInfo.read(context)
    val accent = when (theme) {
        ChargingTheme.MATERIAL -> Color(0xFF4CAF50)
        ChargingTheme.MATERIAL_BLOOM -> Color(0xFF81C784)
        ChargingTheme.NEON -> Color(0xFF00E5FF)
        ChargingTheme.PIXEL -> Color(0xFF76FF03)
    }
    val speedLabel = when {
        battery.levelPercent >= 100 -> "Fully charged"
        !battery.charging -> "Not charging"
        (battery.timeToFullMinutes ?: 9999) <= 30 -> "Fast charging"
        (battery.timeToFullMinutes ?: 9999) <= 90 -> "Charging"
        else -> "Slow charging"
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (settings.showClockOnCharging) {
            Text(
                text = "%02d:%02d".format(now.hour, now.minute),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = (48 * scale).sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = "${battery.levelPercent}%",
            color = accent,
            fontSize = (130 * scale).sp,
            fontWeight = FontWeight.Black,
            fontFamily = if (theme == ChargingTheme.PIXEL) FontFamily.Monospace else FontFamily.SansSerif,
            style = if (theme == ChargingTheme.NEON) TextStyle(shadow = Shadow(accent, blurRadius = 28f)) else TextStyle.Default
        )
        Text(text = speedLabel, color = Color.White, fontSize = (26 * scale).sp)
        battery.timeToFullMinutes?.let {
            val eta = if (it >= 60) "${it / 60}h ${it % 60}m" else "${it}m"
            Text(
                text = "$eta to 100%",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = (22 * scale).sp
            )
        }
        if (settings.milestoneTarget > 0) {
            Text(
                text = "Milestone: ${settings.milestoneTarget}%",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (20 * scale).sp
            )
        }
        Text(
            text = "StandBy Mode • Plug charger to launch",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = (16 * scale).sp
        )
    }
}
