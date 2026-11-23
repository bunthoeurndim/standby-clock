package com.naomi.standby.ui.widgets

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.BatteryInfo
import com.naomi.standby.data.CalendarRepository
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.data.WeatherInfo
import com.naomi.standby.data.WeatherRepository
import com.naomi.standby.data.WidgetType
import com.naomi.standby.service.MusicListenerService
import com.naomi.standby.service.PomodoroService
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The second half of the Duo layout: header with prev/next, then the active widget. */
@Composable
fun DuoWidgetPanel(
    settings: SettingsStore,
    color: Color,
    modifier: Modifier = Modifier
) {
    val types = WidgetType.entries
    val current = types[settings.widgetIndex % types.size]
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                settings.setWidgetIndex((settings.widgetIndex - 1 + types.size) % types.size)
            }) { Text("‹", color = color, fontSize = 28.sp) }
            Text(text = current.displayName, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = {
                settings.setWidgetIndex((settings.widgetIndex + 1) % types.size)
            }) { Text("›", color = color, fontSize = 28.sp) }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (current) {
                WidgetType.CALENDAR -> CalendarWidget(context, color)
                WidgetType.BATTERY -> BatteryWidget(context, color)
                WidgetType.NEXT_ALARM -> NextAlarmWidget(context, color)
                WidgetType.WEATHER -> WeatherWidget(context, color)
                WidgetType.MUSIC -> MusicWidget(context, color)
                WidgetType.PHOTOS -> PhotoWidget(settings, color)
                WidgetType.TIMER -> TimerWidget(color)
                WidgetType.POMODORO -> PomodoroWidget(context, color)
            }
        }
    }
}

@Composable
private fun CalendarWidget(context: Context, color: Color) {
    val events = remember { CalendarRepository.todayEvents(context) }
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        if (events.isEmpty()) {
            Text("No events today.", color = color, fontSize = 20.sp)
            Text(
                "Allow calendar access in system settings to see your agenda here.",
                color = color.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        } else {
            for (event in events) {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = event.title, color = color, fontSize = 20.sp, modifier = Modifier.weight(1f))
                    Text(text = event.timeText(), color = color.copy(alpha = 0.8f), fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun BatteryWidget(context: Context, color: Color) {
    val battery = remember { BatteryInfo.read(context) }
    val storage = remember { BatteryInfo.storageText() }
    Column {
        Text(text = "${battery.levelPercent}%", color = color, fontSize = 56.sp, fontWeight = FontWeight.Bold)
        Text(
            text = if (battery.charging) "Charging" else "Not charging",
            color = color.copy(alpha = 0.8f),
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Health: ${battery.healthText}", color = color, fontSize = 18.sp)
        battery.temperatureC?.let {
            Text(text = "Temperature: %.1f °C".format(it), color = color, fontSize = 18.sp)
        }
        battery.timeToFullMinutes?.let {
            Text(text = "Full in about $it min", color = color, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = storage, color = color.copy(alpha = 0.8f), fontSize = 18.sp)
    }
}

@Composable
private fun NextAlarmWidget(context: Context, color: Color) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val next = remember { alarmManager.nextAlarmClock }
    Column {
        if (next == null) {
            Text("No alarm set.", color = color, fontSize = 20.sp)
            Text("Set one in the Clock app and it will show here.", color = color.copy(alpha = 0.7f), fontSize = 16.sp)
        } else {
            val dt = Instant.ofEpochMilli(next.triggerTime).atZone(ZoneId.systemDefault())
            Text(
                text = dt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)),
                color = color,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dt.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)),
                color = color.copy(alpha = 0.8f),
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun WeatherWidget(context: Context, color: Color) {
    var weather by remember { mutableStateOf<WeatherInfo?>(null) }
    LaunchedEffect(Unit) { weather = WeatherRepository.fetchCurrent(context) }
    Column {
        val w = weather
        if (w == null) {
            Text("Loading weather…", color = color, fontSize = 20.sp)
        } else if (w.temperatureC == null) {
            Text("Weather unavailable", color = color, fontSize = 20.sp)
            Text("Check your connection and try again.", color = color.copy(alpha = 0.7f), fontSize = 16.sp)
        } else {
            Text(text = "%.0f°C".format(w.temperatureC), color = color, fontSize = 64.sp, fontWeight = FontWeight.Bold)
            Text(text = w.condition, color = color, fontSize = 22.sp)
            Text(text = if (w.isDay) "Daytime" else "Night-time", color = color.copy(alpha = 0.8f), fontSize = 18.sp)
        }
    }
}

private fun sendMediaKey(context: Context, keyCode: Int) {
    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val down = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
    val up = KeyEvent(KeyEvent.ACTION_UP, keyCode)
    audio.dispatchMediaKeyEvent(down)
    audio.dispatchMediaKeyEvent(up)
}

@Composable
private fun MusicWidget(context: Context, color: Color) {
    val track by MusicListenerService.currentTrack.collectAsState()
    Column {
        val t = track
        if (t == null) {
            Text("Nothing playing.", color = color, fontSize = 20.sp)
            Text(
                "Play music in any app. To show the track here, enable notification access for StandBy Clock in system settings.",
                color = color.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(onClick = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }) { Text("Open notification settings", color = color) }
        } else {
            Text(text = t.title, color = color, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            if (t.artist.isNotEmpty()) {
                Text(text = t.artist, color = color.copy(alpha = 0.8f), fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS) }) {
                Text("⏮", color = Color.White)
            }
            Button(onClick = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }) {
                Text("⏯", color = Color.White)
            }
            Button(onClick = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT) }) {
                Text("⏭", color = Color.White)
            }
        }
    }
}

@Composable
private fun PhotoWidget(settings: SettingsStore, color: Color) {
    val context = LocalContext.current
    val uris = settings.photoUris
    var index by remember { mutableStateOf(0) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { picked: List<Uri> ->
        if (picked.isNotEmpty()) {
            settings.setPhotoUris(picked.map { it.toString() })
            index = 0
        }
    }
    LaunchedEffect(uris.size) {
        while (uris.size > 1) {
            delay(8000)
            index = (index + 1) % uris.size
        }
    }
    var bitmap by remember(uris, index) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(uris, index) {
        bitmap = null
        val uriString = uris.getOrNull(index) ?: return@LaunchedEffect
        try {
            val bmp = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                context.contentResolver.openInputStream(Uri.parse(uriString))?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            if (bmp != null) bitmap = bmp.asImageBitmap()
        } catch (_: Exception) {
            bitmap = null
        }
    }
    Column {
        if (uris.isEmpty()) {
            Text("No photos yet.", color = color, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Choose photos", color = Color.White) }
        } else {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF111111)),
                contentAlignment = Alignment.Center
            ) {
                val bmp = bitmap
                if (bmp != null) {
                    Image(bitmap = bmp, contentDescription = "Photo frame", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Text("Loading photo…", color = color, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Change photos", color = color) }
                TextButton(onClick = { if (uris.isNotEmpty()) index = (index + 1) % uris.size }) {
                    Text("Next ›", color = color)
                }
            }
        }
    }
}

@Composable
private fun TimerWidget(color: Color) {
    var elapsedSeconds by remember { mutableStateOf(0) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running) {
        while (running) {
            delay(1000)
            elapsedSeconds++
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "%02d:%02d:%02d".format(elapsedSeconds / 3600, (elapsedSeconds / 60) % 60, elapsedSeconds % 60),
            color = color,
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { running = !running }) {
                Text(if (running) "Pause" else "Start", color = Color.White)
            }
            OutlinedButton(onClick = { running = false; elapsedSeconds = 0 }) {
                Text("Reset", color = color)
            }
        }
    }
}

@Composable
private fun PomodoroWidget(context: Context, color: Color) {
    var modeSeconds by remember { mutableStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    Column {
        Text(text = "Pomodoro", color = color, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Focus ${modeSeconds / 60} minutes, then take a break.",
            color = color.copy(alpha = 0.8f),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { modeSeconds = 25 * 60; running = false }) {
                Text("25 min", color = Color.White)
            }
            OutlinedButton(onClick = { modeSeconds = 5 * 60; running = false }) {
                Text("5 min break", color = color)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (running) {
            OutlinedButton(onClick = { PomodoroService.stop(context); running = false }) {
                Text("Stop timer", color = color)
            }
        } else {
            Button(onClick = { PomodoroService.start(context, modeSeconds); running = true }) {
                Text("Start timer", color = Color.White)
            }
        }
        Text(
            text = "The countdown keeps running in the background and beeps when it ends.",
            color = color.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
    }
}
