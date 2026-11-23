package com.naomi.standby.ui

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.BatteryInfo
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.model.FaceRegistry
import com.naomi.standby.ui.faces.ClockFace
import com.naomi.standby.ui.widgets.DuoWidgetPanel
import com.naomi.standby.util.formatDateShort
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * The standby screen: big clock on one side, a widget on the other.
 * - Swipe left/right: change clock face.
 * - Tap: open settings. Long-press: open the face picker (handled by the host via onOpenSettings).
 * - Night mode tints everything red and dims; the whole screen shifts a few
 *   pixels every minute so static digits do not burn into OLED panels.
 */
@Composable
fun StandbyScreen(
    settings: SettingsStore,
    onOpenSettings: () -> Unit,
    onExit: () -> Unit,
    onLongPress: () -> Unit = onOpenSettings
) {
    val context = LocalContext.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var darkBySensor by remember { mutableStateOf(false) }
    var shiftIndex by remember { mutableStateOf(0) }

    // Tick once a second.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = LocalDateTime.now()
        }
    }
    // Pixel-shift: move the content a few px every minute.
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            shiftIndex = (shiftIndex + 1) % 4
        }
    }
    // Sleep timer: leave the standby screen when it expires.
    val sleepMinutes = settings.sleepTimerMinutes
    LaunchedEffect(sleepMinutes) {
        if (sleepMinutes > 0) {
            delay(sleepMinutes * 60_000L)
            onExit()
        }
    }
    // Light sensor for the optional sensor-based night mode.
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                darkBySensor = event.values.firstOrNull()?.let { it < 10f } ?: false
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (light != null) sensorManager.registerListener(listener, light, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    val night = settings.isNightNow(now.hour, now.minute, darkBySensor)
    val face = FaceRegistry.byId(settings.faceId)
    // Night mode forces the red theme regardless of the picked face theme.
    val displayColor = if (night) Color(0xFFFF2A1A) else face.theme.color
    val background = Color.Black
    val brightness = if (night) settings.nightBrightness else settings.dayBrightness

    // Apply the chosen screen brightness while standby is showing.
    LaunchedEffect(brightness) {
        val activity = context as? Activity
        activity?.window?.attributes = activity.window.attributes.apply {
            screenBrightness = (brightness.coerceIn(5, 100) / 100f)
        }
    }

    val battery = remember(now.minute) { BatteryInfo.read(context) }
    val shiftX = when (shiftIndex) { 0 -> 0.dp; 1 -> 4.dp; 2 -> 0.dp; else -> (-4).dp }
    val shiftY = when (shiftIndex) { 0 -> 0.dp; 1 -> 0.dp; 2 -> 4.dp; else -> (-4).dp }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onOpenSettings() },
                    onLongPress = { onLongPress() }
                )
            }
            .pointerInput(face.id) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val faces = FaceRegistry.allFaces
                        val idx = faces.indexOf(face)
                        if (totalDrag < -120f) {
                            settings.setFaceId(faces[(idx + 1) % faces.size].id)
                        } else if (totalDrag > 120f) {
                            settings.setFaceId(faces[(idx - 1 + faces.size) % faces.size].id)
                        }
                        totalDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount }
                )
            }
    ) {
        val landscape = maxWidth > maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(shiftX, shiftY)
                .padding(12.dp)
        ) {
            // Top status line: date, battery, night indicator.
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = formatDateShort(now), color = displayColor.copy(alpha = 0.85f), fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                if (night) Text(text = "NIGHT", color = displayColor, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${battery.levelPercent}%${if (battery.charging) " ⚡" else ""}",
                    color = displayColor,
                    fontSize = 18.sp
                )
                if (battery.charging && battery.timeToFullMinutes != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "full in ${battery.timeToFullMinutes} min", color = displayColor.copy(alpha = 0.7f), fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (landscape) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        ClockFace(entry = face, now = now, fontScale = settings.fontScale)
                    }
                    DuoWidgetPanel(settings = settings, color = displayColor, modifier = Modifier.weight(1f))
                }
            } else {
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        ClockFace(entry = face, now = now, fontScale = settings.fontScale)
                    }
                    DuoWidgetPanel(settings = settings, color = displayColor, modifier = Modifier.weight(1f))
                }
            }
            Text(
                text = "Swipe to change face · Tap for settings · Long-press for faces",
                color = displayColor.copy(alpha = 0.5f),
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
