package com.naomi.standby.ui

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.BatteryInfo
import com.naomi.standby.data.DuoSlot
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.model.ScreenCatalog
import com.naomi.standby.model.ScreenId
import com.naomi.standby.util.formatDateShort
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * The Duo home screen — the original app's home model: two side-by-side
 * widget slots in landscape. Each slot holds one screen.
 * - Tap a slot: opens its fullscreen counterpart.
 * - Long-press a slot: replaces its widget (widget picker).
 * - Empty slot: "Customize this space" — tap to add a widget.
 * - Lock: swipe down with 3 fingers to unlock (an on-screen Unlock
 *   button is the fallback). Double-tap closes the app when enabled.
 * - Night mode: a red tint overlay + dimming, with pixel-shift burn-in
 *   protection moving the content a few px every minute.
 */
@Composable
fun StandbyScreen(
    settings: SettingsStore,
    onOpenSettings: () -> Unit,
    onExit: () -> Unit,
    onPickSlot: (DuoSlot) -> Unit = {},
    onOpenFullscreen: (ScreenId) -> Unit = {}
) {
    val context = LocalContext.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var darkBySensor by remember { mutableStateOf(false) }
    var shiftIndex by remember { mutableStateOf(0) }
    var locked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = LocalDateTime.now()
        }
    }
    // Pixel-shift burn-in protection: move the content a few px every minute.
    LaunchedEffect(settings.burnInProtection) {
        while (settings.burnInProtection) {
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
    val brightness = if (night) settings.nightBrightness else settings.dayBrightness

    LaunchedEffect(brightness) {
        val activity = context as? Activity
        activity?.window?.attributes = activity.window.attributes.apply {
            screenBrightness = (brightness.coerceIn(5, 100) / 100f)
        }
    }

    val battery = remember(now.minute) { BatteryInfo.read(context) }
    val shiftX = when (shiftIndex) { 0 -> 0.dp; 1 -> 4.dp; 2 -> 0.dp; else -> (-4).dp }
    val shiftY = when (shiftIndex) { 0 -> 0.dp; 1 -> 0.dp; 2 -> 4.dp; else -> (-4).dp }

    val leftScreen = ScreenId.byId(settings.leftScreenId)
    val rightScreen = ScreenId.byId(settings.rightScreenId)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        val landscape = maxWidth > maxHeight
        val slotScale = if (landscape) 0.55f else 0.75f
        Column(
            modifier = Modifier.fillMaxSize().offset(shiftX, shiftY).padding(12.dp)
        ) {
            // Top status line: date, battery, night indicator, lock, settings.
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = formatDateShort(now), color = Color.White.copy(alpha = 0.85f), fontSize = 18.sp)
                Spacer(modifier = Modifier.weight(1f))
                if (night) {
                    Text(text = "NIGHT", color = Color(0xFFFF2A1A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Text(
                    text = "${battery.levelPercent}%${if (battery.charging) " ⚡" else ""}",
                    color = Color.White,
                    fontSize = 18.sp
                )
                if (battery.charging && battery.timeToFullMinutes != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "full in ${battery.timeToFullMinutes} min", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(onClick = { locked = !locked }) {
                    Text(if (locked) "Unlock" else "Lock", color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onOpenSettings) {
                    Text("Settings", color = Color.White, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (landscape) {
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    DuoSlotBox(
                        screen = leftScreen,
                        slot = DuoSlot.LEFT,
                        now = now,
                        settings = settings,
                        scale = slotScale,
                        locked = locked,
                        onPickSlot = onPickSlot,
                        onOpenFullscreen = onOpenFullscreen,
                        onExit = onExit,
                        modifier = Modifier.weight(1f)
                    )
                    DuoSlotBox(
                        screen = rightScreen,
                        slot = DuoSlot.RIGHT,
                        now = now,
                        settings = settings,
                        scale = slotScale,
                        locked = locked,
                        onPickSlot = onPickSlot,
                        onOpenFullscreen = onOpenFullscreen,
                        onExit = onExit,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    DuoSlotBox(
                        screen = leftScreen,
                        slot = DuoSlot.LEFT,
                        now = now,
                        settings = settings,
                        scale = slotScale,
                        locked = locked,
                        onPickSlot = onPickSlot,
                        onOpenFullscreen = onOpenFullscreen,
                        onExit = onExit,
                        modifier = Modifier.weight(1f)
                    )
                    DuoSlotBox(
                        screen = rightScreen,
                        slot = DuoSlot.RIGHT,
                        now = now,
                        settings = settings,
                        scale = slotScale,
                        locked = locked,
                        onPickSlot = onPickSlot,
                        onOpenFullscreen = onOpenFullscreen,
                        onExit = onExit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Text(
                text = "Tap a widget to open it · Long-press to replace it · Double-tap to close",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        // Night-mode red tint overlay.
        if (night) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFFF2A1A).copy(alpha = 0.28f))
            )
        }
        // Lock overlay: 3-finger swipe-down to unlock, plus a button fallback.
        if (locked) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            var startY: Float? = null
                            while (true) {
                                val event = awaitPointerEvent()
                                val pressed = event.changes.filter { it.pressed }
                                if (pressed.size >= 3) {
                                    val y = pressed.first().position.y
                                    if (startY == null) startY = y
                                    if (y - (startY ?: y) > 120f) {
                                        locked = false
                                        startY = null
                                    }
                                } else {
                                    startY = null
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Swipe down with 3 fingers to unlock",
                        color = Color.White,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { locked = false }) {
                        Text("Unlock", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DuoSlotBox(
    screen: ScreenId?,
    slot: DuoSlot,
    now: LocalDateTime,
    settings: SettingsStore,
    scale: Float,
    locked: Boolean,
    onPickSlot: (DuoSlot) -> Unit,
    onOpenFullscreen: (ScreenId) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(6.dp)
            .background(Color(0xFF0A0A0A))
            .pointerInput(screen, locked, settings.autoCloseDoubleTap) {
                detectTapGestures(
                    onTap = {
                        if (locked) return@detectTapGestures
                        val s = screen
                        if (s == null) {
                            onPickSlot(slot)
                        } else {
                            onOpenFullscreen(ScreenCatalog.fullscreenCounterpart(s) ?: s)
                        }
                    },
                    onDoubleTap = {
                        if (!locked && settings.autoCloseDoubleTap) onExit()
                    },
                    onLongPress = {
                        if (!locked) onPickSlot(slot)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (screen == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = "Customize this space",
                    color = Color.White,
                    fontSize = minOf(24f * scale / 0.55f, 26f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add your favorite widgets to make this slot yours. You can also long press to select.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { onPickSlot(slot) }) {
                    Text("Add a widget", color = Color.White)
                }
            }
        } else {
            ScreenContent(
                screen = screen,
                now = now,
                settings = settings,
                scale = scale,
                onOpenScreen = onOpenFullscreen
            )
        }
    }
}
