package com.naomi.standby.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.NightMode
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.data.formatMinutesOfDay
import com.naomi.standby.model.FaceRegistry

/** Settings: face, night mode, brightness, auto-start, sleep timer, about. */
@Composable
fun SettingsScreen(
    settings: SettingsStore,
    onBack: () -> Unit,
    onPickFace: () -> Unit
) {
    val context = LocalContext.current
    val currentFace = FaceRegistry.byId(settings.faceId)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Settings", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Section("Clock face")
        Text(text = currentFace.name, color = Color.White, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onPickFace) { Text("Choose a face (${FaceRegistry.count} available)", color = Color.White) }

        Spacer(modifier = Modifier.height(16.dp))
        Section("Text size")
        Slider(
            value = settings.fontScale,
            onValueChange = { settings.setFontScale(it) },
            valueRange = 0.7f..1.4f,
            steps = 6
        )
        Text(text = "%.0f%%".format(settings.fontScale * 100), color = Color.White.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(16.dp))
        Section("Night mode")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (mode in NightMode.entries) {
                OutlinedButton(onClick = { settings.setNightMode(mode) }) {
                    Text(
                        text = mode.name,
                        color = if (settings.nightMode == mode) Color(0xFF4CAF50) else Color.White
                    )
                }
            }
        }
        Text(
            text = "AUTO uses the schedule below (default 22:00–07:00). MANUAL is always red. " +
                "The light-sensor option also turns night mode on in a dark room.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        ToggleRow("Use light sensor", settings.useLightSensor) { settings.setUseLightSensor(it) }
        Text(text = "Night starts: ${formatMinutesOfDay(settings.nightStartMinutes)}", color = Color.White, fontSize = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                settings.setNightStartMinutes((settings.nightStartMinutes - 60 + 1440) % 1440)
            }) { Text("−1h", color = Color.White) }
            Button(onClick = {
                settings.setNightStartMinutes((settings.nightStartMinutes + 60) % 1440)
            }) { Text("+1h", color = Color.White) }
        }
        Text(text = "Night ends: ${formatMinutesOfDay(settings.nightEndMinutes)}", color = Color.White, fontSize = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                settings.setNightEndMinutes((settings.nightEndMinutes - 60 + 1440) % 1440)
            }) { Text("−1h", color = Color.White) }
            Button(onClick = {
                settings.setNightEndMinutes((settings.nightEndMinutes + 60) % 1440)
            }) { Text("+1h", color = Color.White) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Section("Brightness")
        Text(text = "Day: ${settings.dayBrightness}%", color = Color.White, fontSize = 17.sp)
        Slider(
            value = settings.dayBrightness.toFloat(),
            onValueChange = { settings.setDayBrightness(it.toInt()) },
            valueRange = 20f..100f
        )
        Text(text = "Night: ${settings.nightBrightness}%", color = Color.White, fontSize = 17.sp)
        Slider(
            value = settings.nightBrightness.toFloat(),
            onValueChange = { settings.setNightBrightness(it.toInt()) },
            valueRange = 5f..60f
        )

        Spacer(modifier = Modifier.height(16.dp))
        Section("Charging")
        ToggleRow("Open on charger", settings.autoStartCharging) { settings.setAutoStartCharging(it) }
        ToggleRow("Close when unplugged", settings.closeOnUnplug) { settings.setCloseOnUnplug(it) }
        Text(
            text = "Also works as the Android screensaver: Settings → Display → Screen saver → StandBy Clock.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(16.dp))
        Section("Sleep timer")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (minutes in listOf(0, 15, 30, 60)) {
                OutlinedButton(onClick = { settings.setSleepTimerMinutes(minutes) }) {
                    Text(
                        text = if (minutes == 0) "Off" else "$minutes min",
                        color = if (settings.sleepTimerMinutes == minutes) Color(0xFF4CAF50) else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Section("Music & notifications")
        Text(
            text = "The music widget shows the track playing in any app. Enable notification access for StandBy Clock to use it.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        OutlinedButton(onClick = {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }) { Text("Notification access settings", color = Color.White) }

        Spacer(modifier = Modifier.height(16.dp))
        Section("About")
        Text(
            text = "StandBy Clock 1.0.0 — an original implementation inspired by the iOS StandBy screen. " +
                "No code, art or assets were copied from any other app. Weather data: Open-Meteo.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to standby", color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String) {
    Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 17.sp)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
