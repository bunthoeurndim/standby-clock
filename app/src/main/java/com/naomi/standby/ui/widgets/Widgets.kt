package com.naomi.standby.ui.widgets

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.BatteryInfo
import com.naomi.standby.data.CalendarRepository
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.data.rememberWeather
import com.naomi.standby.model.QUOTES
import com.naomi.standby.service.MusicListenerService
import com.naomi.standby.service.PomodoroService
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun beep() {
    try {
        val tone = ToneGenerator(AudioManager.STREAM_ALARM, 70)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ tone.release() }, 800)
    } catch (_: Exception) {
        // Sound is best-effort.
    }
}

private fun sendMediaKey(context: Context, keyCode: Int) {
    val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
    audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
}

private fun openNotificationSettings(context: Context) {
    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

/** Month calendar grid. Mini shows the next event underneath. */
@Composable
fun CalendarWidget(settings: SettingsStore, scale: Float, mini: Boolean = false) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val month = YearMonth.from(today)
    val firstDow = (month.atDay(1).dayOfWeek.value % 7) // 0 = Sunday
    val startOffset = if (settings.startOfWeek == 1) (firstDow + 6) % 7 else firstDow
    val daysInMonth = month.lengthOfMonth()
    val events = remember { CalendarRepository.todayEvents(context) }
    Column(
        modifier = Modifier.fillMaxSize().padding((8 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)),
            color = Color.White,
            fontSize = (24 * scale).sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        val weekDays = if (settings.startOfWeek == 1)
            listOf("M", "T", "W", "T", "F", "S", "S") else listOf("S", "M", "T", "W", "T", "F", "S")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (d in weekDays) Text(text = d, color = Color.White.copy(alpha = 0.6f), fontSize = (16 * scale).sp)
        }
        val cells = (0 until 42).map { i ->
            val day = i - startOffset + 1
            if (day in 1..daysInMonth) day else null
        }
        for (week in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (col in 0 until 7) {
                    val day = cells[week * 7 + col]
                    val isToday = day == today.dayOfMonth
                    Box(
                        modifier = Modifier.size((34 * scale).dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isToday) Color.White else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            Text(
                                text = day.toString(),
                                color = if (isToday) Color.Black else Color.White,
                                fontSize = (18 * scale).sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
        if (mini) {
            Spacer(modifier = Modifier.height((8 * scale).dp))
            val next = events.firstOrNull()
            Text(
                text = if (next == null) "No events today" else "Next: ${next.title} · ${next.timeText()}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = (18 * scale).sp
            )
        }
    }
}

/** Schedule / agenda list for the day. */
@Composable
fun ScheduleWidget(scale: Float, mini: Boolean = false) {
    val context = LocalContext.current
    val events = remember { CalendarRepository.todayEvents(context) }
    Column(modifier = Modifier.fillMaxSize().padding((12 * scale).dp).verticalScroll(rememberScrollState())) {
        Text(
            text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)),
            color = Color.White,
            fontSize = (24 * scale).sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height((10 * scale).dp))
        if (events.isEmpty()) {
            Text("No events", color = Color.White, fontSize = (20 * scale).sp)
            Text(
                "Grant calendar access to see upcoming events.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (16 * scale).sp
            )
        } else {
            for (event in events) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = (6 * scale).dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = event.title, color = Color.White, fontSize = (20 * scale).sp, modifier = Modifier.weight(1f))
                    Text(text = event.timeText(), color = Color.White.copy(alpha = 0.8f), fontSize = (20 * scale).sp)
                }
            }
        }
    }
}

/** Next Event Duo — a single next-event card. */
@Composable
fun NextEventWidget(scale: Float) {
    val context = LocalContext.current
    val events = remember { CalendarRepository.todayEvents(context) }
    val next = events.firstOrNull()
    Column(
        modifier = Modifier.fillMaxSize().padding((16 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Next event", color = Color.White.copy(alpha = 0.6f), fontSize = (20 * scale).sp)
        Spacer(modifier = Modifier.height((10 * scale).dp))
        if (next == null) {
            Text(text = "Clear day", color = Color.White, fontSize = (36 * scale).sp, fontWeight = FontWeight.Bold)
            Text(text = "Nothing scheduled", color = Color.White.copy(alpha = 0.7f), fontSize = (18 * scale).sp)
        } else {
            Text(
                text = next.timeText(),
                color = Color.White,
                fontSize = (64 * scale).sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = next.title, color = Color.White, fontSize = (24 * scale).sp, textAlign = TextAlign.Center)
        }
    }
}

/** Striking Calendar — huge date number over the month grid. */
@Composable
fun StrikingCalendarWidget(settings: SettingsStore, scale: Float) {
    val today = LocalDate.now()
    Column(
        modifier = Modifier.fillMaxSize().padding((12 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = today.dayOfMonth.toString(),
            color = Color.White,
            fontSize = (110 * scale).sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM yyyy", Locale.ENGLISH)),
            color = Color.White.copy(alpha = 0.85f),
            fontSize = (22 * scale).sp
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        CalendarWidget(settings, scale * 0.7f)
    }
}

/** Weather Duo / Fullscreen — current conditions, H/L, feels-like,
 *  humidity, wind, sunrise/sunset and an hourly strip. */
@Composable
fun WeatherWidget(scale: Float, fullscreen: Boolean = false) {
    val context = LocalContext.current
    val weather = rememberWeather(context)
    Column(
        modifier = Modifier.fillMaxSize().padding((14 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val w = weather
        if (w == null) {
            Text("Loading weather…", color = Color.White, fontSize = (20 * scale).sp)
        } else if (w.temperatureC == null) {
            Text("Weather unavailable", color = Color.White, fontSize = (20 * scale).sp)
            Text(
                "Grant location access to see weather for your area.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (16 * scale).sp,
                textAlign = TextAlign.Center
            )
        } else {
            Text(text = w.cityName, color = Color.White.copy(alpha = 0.7f), fontSize = (20 * scale).sp)
            Text(
                text = "%.0f°".format(w.temperatureC),
                color = Color.White,
                fontSize = ((if (fullscreen) 130 else 84) * scale).sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = w.condition, color = Color.White, fontSize = (22 * scale).sp)
            if (w.highC != null && w.lowC != null) {
                Text(
                    text = "H: %.0f°  L: %.0f°".format(w.highC, w.lowC),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = (18 * scale).sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy((16 * scale).dp)) {
                w.feelsLikeC?.let { Text(text = "Feels like %.0f°".format(it), color = Color.White.copy(alpha = 0.8f), fontSize = (16 * scale).sp) }
                w.humidityPct?.let { Text(text = "Humidity $it%", color = Color.White.copy(alpha = 0.8f), fontSize = (16 * scale).sp) }
                w.windKph?.let { Text(text = "Wind %.0f km/h".format(it), color = Color.White.copy(alpha = 0.8f), fontSize = (16 * scale).sp) }
            }
            if (w.sunrise != null && w.sunset != null) {
                Text(
                    text = "Sunrise ${w.sunrise}  ·  Sunset ${w.sunset}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = (16 * scale).sp
                )
            }
            if (w.hourly.isNotEmpty()) {
                Spacer(modifier = Modifier.height((12 * scale).dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for ((i, h) in w.hourly.withIndex()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (i == 0) "Now" else "%02d".format(h.hour),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = (15 * scale).sp
                            )
                            Text(
                                text = "%.0f°".format(h.temperatureC),
                                color = Color.White,
                                fontSize = (18 * scale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Powered by Open-Meteo",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = (13 * scale).sp
            )
        }
    }
}

/** Weather Hero Duo — the same data as a large hero card. */
@Composable
fun WeatherHeroWidget(scale: Float) {
    WeatherWidget(scale * 1.15f)
}

/** Weather Bottom Layout — forecast strip along the bottom. */
@Composable
fun WeatherBottomLayoutWidget(scale: Float, fullscreen: Boolean = false) {
    val context = LocalContext.current
    val weather = rememberWeather(context)
    Column(modifier = Modifier.fillMaxSize().padding((14 * scale).dp)) {
        val w = weather
        if (w == null || w.temperatureC == null) {
            Text("Loading weather…", color = Color.White, fontSize = (20 * scale).sp)
        } else {
            Text(
                text = "%.0f°  ·  ${w.condition}".format(w.temperatureC),
                color = Color.White,
                fontSize = (56 * scale).sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = w.cityName, color = Color.White.copy(alpha = 0.7f), fontSize = (18 * scale).sp)
            Spacer(modifier = Modifier.weight(1f))
            if (w.hourly.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for ((i, h) in w.hourly.withIndex()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (i == 0) "Now" else "%02d".format(h.hour),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = (15 * scale).sp
                            )
                            Text(
                                text = "%.0f°".format(h.temperatureC),
                                color = Color.White,
                                fontSize = (20 * scale).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height((6 * scale).dp))
            Text(
                text = "Powered by Open-Meteo",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = (13 * scale).sp
            )
        }
    }
}

/** Weather And Clock — time + weather in one widget. */
@Composable
fun WeatherAndClockWidget(now: LocalDateTime, scale: Float, fullscreen: Boolean = false) {
    val context = LocalContext.current
    val weather = rememberWeather(context)
    Column(
        modifier = Modifier.fillMaxSize().padding((14 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = Color.White,
            fontSize = (84 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        val w = weather
        if (w != null && w.temperatureC != null) {
            Text(
                text = "%.0f°  ·  ${w.condition}".format(w.temperatureC),
                color = Color.White,
                fontSize = (24 * scale).sp
            )
            if (w.highC != null && w.lowC != null) {
                Text(
                    text = "High %.0f° · Low %.0f°".format(w.highC, w.lowC),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = (18 * scale).sp
                )
            }
        } else {
            Text("Loading weather…", color = Color.White.copy(alpha = 0.7f), fontSize = (18 * scale).sp)
        }
        Spacer(modifier = Modifier.height((6 * scale).dp))
        Text(text = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)), color = Color.White.copy(alpha = 0.8f), fontSize = (20 * scale).sp)
    }
}

/** Player — Duo, Fullscreen, Blur and Hero variants. Reads now-playing
 *  via NotificationListenerService (transport only). */
@Composable
fun PlayerWidget(context: Context, scale: Float, hero: Boolean = false, blur: Boolean = false, fullscreen: Boolean = false) {
    val track by MusicListenerService.currentTrack.collectAsState()
    val bg = if (blur) Color(0xFF1A1A2E) else Color.Transparent
    Column(
        modifier = Modifier.fillMaxSize().background(bg).padding((16 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val t = track
        if (t == null) {
            Text(text = "Audio on standby", color = Color.White, fontSize = (24 * scale).sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Nothing playing. Play music in Spotify, YouTube Music or 200+ other apps.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (16 * scale).sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height((12 * scale).dp))
            OutlinedButton(onClick = { openNotificationSettings(context) }) {
                Text("Grant notification access", color = Color.White)
            }
        } else {
            if (hero || fullscreen) {
                Box(
                    modifier = Modifier.size((110 * scale).dp).clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF333333)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "♪", color = Color.White, fontSize = (48 * scale).sp)
                }
                Spacer(modifier = Modifier.height((12 * scale).dp))
            }
            Text(
                text = t.title,
                color = Color.White,
                fontSize = (26 * scale).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (t.artist.isNotEmpty()) {
                Text(text = t.artist, color = Color.White.copy(alpha = 0.8f), fontSize = (20 * scale).sp, textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height((16 * scale).dp))
        Row(horizontalArrangement = Arrangement.spacedBy((14 * scale).dp)) {
            Button(onClick = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS) }) {
                Text("⏮", color = Color.White, fontSize = (20 * scale).sp)
            }
            Button(onClick = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }) {
                Text("⏯", color = Color.White, fontSize = (20 * scale).sp)
            }
            Button(onClick = { sendMediaKey(context, KeyEvent.KEYCODE_MEDIA_NEXT) }) {
                Text("⏭", color = Color.White, fontSize = (20 * scale).sp)
            }
        }
    }
}

/** Meme Player Duo — a meme/GIF player combined with music. GIFs are not
 *  bundled in this build; it shows a rotating set of meme captions over
 *  the player. */
@Composable
fun MemePlayerWidget(context: Context, scale: Float) {
    val memes = listOf(
        "When the alarm goes off…",
        "Me: one more episode. Also me at 3am:",
        "My code works and I have no idea why.",
        "Monday has entered the chat.",
        "That feeling when the Wi-Fi comes back."
    )
    var index by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(8000)
            index = (index + 1) % memes.size
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding((16 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF222222))
                .padding((16 * scale).dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = memes[index],
                color = Color.White,
                fontSize = (22 * scale).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height((12 * scale).dp))
        PlayerWidget(context, scale * 0.8f)
    }
}

/** User Photo Slide / Mini — slideshow of the user's own photos.
 *  Modes: Fullscreen (crop to fill), Fit (letterboxed), Adaptive.
 *  Photos stay on the device and are never shared. */
@Composable
fun PhotoSlideWidget(settings: SettingsStore, scale: Float, mini: Boolean = false) {
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
    val interval = (settings.photoIntervalSeconds.coerceIn(3, 60) * 1000L)
    LaunchedEffect(uris.size, interval) {
        while (uris.size > 1) {
            delay(interval)
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
    Column(modifier = Modifier.fillMaxSize().padding((8 * scale).dp)) {
        if (uris.isEmpty()) {
            Text("Add your photos to get started.", color = Color.White, fontSize = (20 * scale).sp)
            Text(
                "Tap the screen to add new photos. Swipe to browse your photos. " +
                    "We keep your photos private and never share them.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (16 * scale).sp
            )
            Spacer(modifier = Modifier.height((12 * scale).dp))
            Button(onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Choose photos", color = Color.White) }
        } else {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF111111))
                    .pointerInput(uris.size) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (totalDrag < -60f) index = (index + 1) % uris.size
                                else if (totalDrag > 60f) index = (index - 1 + uris.size) % uris.size
                                totalDrag = 0f
                            },
                            onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                val bmp = bitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = "Photo frame",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Loading photo…", color = Color.White, fontSize = (18 * scale).sp)
                }
            }
            Spacer(modifier = Modifier.height((8 * scale).dp))
            Row(horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
                OutlinedButton(onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Change photos", color = Color.White) }
                TextButton(onClick = { if (uris.isNotEmpty()) index = (index + 1) % uris.size }) {
                    Text("Next ›", color = Color.White)
                }
            }
        }
    }
}

/** Photo Stack — polaroid-style stack; photos spawn, drift and rotate. */
@Composable
fun PhotoStackWidget(settings: SettingsStore, scale: Float) {
    val context = LocalContext.current
    val uris = settings.photoUris
    var shown by remember { mutableStateOf(0) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { picked: List<Uri> ->
        if (picked.isNotEmpty()) settings.setPhotoUris(picked.map { it.toString() })
    }
    LaunchedEffect(uris.size) {
        while (uris.size > 1) {
            delay(6000)
            shown = (shown + 1) % uris.size
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding((12 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uris.isEmpty()) {
            Text("Tap the screen to add new photos.", color = Color.White, fontSize = (20 * scale).sp)
            Spacer(modifier = Modifier.height((12 * scale).dp))
            Button(onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Choose photos", color = Color.White) }
        } else {
            Text(
                text = "Photo ${shown + 1} of ${uris.size}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (18 * scale).sp
            )
            Spacer(modifier = Modifier.height((8 * scale).dp))
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth()
                    .clickable { shown = (shown + 1) % uris.size },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▣  Photo stack",
                    color = Color.White,
                    fontSize = (28 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Photos spawn, drift and rotate. Adjust spawn rate in the Animation tab.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = (15 * scale).sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Quotes — swipe for the next quote; auto-rotates every hour. */
@Composable
fun QuotesWidget(settings: SettingsStore, scale: Float, fullscreen: Boolean = false) {
    val index = settings.quoteIndex % QUOTES.size
    val (quote, category) = QUOTES[index]
    var totalDrag by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier.fillMaxSize().padding((20 * scale).dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (totalDrag < -60f) settings.setQuoteIndex((index + 1) % QUOTES.size)
                        else if (totalDrag > 60f) settings.setQuoteIndex((index - 1 + QUOTES.size) % QUOTES.size)
                        totalDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "“$quote”",
            color = Color.White,
            fontSize = ((if (fullscreen) 40 else 28) * scale).sp,
            textAlign = TextAlign.Center,
            lineHeight = ((if (fullscreen) 52 else 36) * scale).sp
        )
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Text(text = category, color = Color.White.copy(alpha = 0.6f), fontSize = (18 * scale).sp)
        Spacer(modifier = Modifier.height((16 * scale).dp))
        Row(horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
            OutlinedButton(onClick = { settings.setQuoteIndex((index - 1 + QUOTES.size) % QUOTES.size) }) {
                Text("‹ Prev", color = Color.White)
            }
            OutlinedButton(onClick = { settings.setQuoteIndex((index + 1) % QUOTES.size) }) {
                Text("Next ›", color = Color.White)
            }
        }
    }
    // Auto-rotate every hour.
    LaunchedEffect(Unit) {
        while (true) {
            delay(3_600_000L)
            settings.setQuoteIndex((settings.quoteIndex + 1) % QUOTES.size)
        }
    }
}

/** Timer — set a duration, counts down, "Time's up!" on finish. */
@Composable
fun TimerWidget(scale: Float, fullscreen: Boolean = false) {
    var totalSeconds by remember { mutableStateOf(5 * 60) }
    var remaining by remember { mutableStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining--
        }
        if (running && remaining <= 0) {
            running = false
            done = true
            beep()
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding((16 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (done) {
            Text(text = "Time's up!", color = Color(0xFF4CAF50), fontSize = (48 * scale).sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height((12 * scale).dp))
            Button(onClick = { done = false; remaining = totalSeconds }) { Text("Reset", color = Color.White) }
            return@Column
        }
        Text(
            text = "%02d:%02d".format(remaining / 60, remaining % 60),
            color = Color.White,
            fontSize = (84 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
            for (minutes in listOf(1, 3, 5, 10, 15, 25)) {
                OutlinedButton(onClick = { totalSeconds = minutes * 60; remaining = minutes * 60; running = false; done = false }) {
                    Text(
                        text = "$minutes",
                        color = if (totalSeconds == minutes * 60) Color(0xFF4CAF50) else Color.White,
                        fontSize = (16 * scale).sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Row(horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
            Button(onClick = {
                if (remaining <= 0) remaining = totalSeconds
                running = !running
            }) { Text(if (running) "Pause" else "Start", color = Color.White) }
            OutlinedButton(onClick = { running = false; remaining = totalSeconds }) {
                Text("Reset", color = Color.White)
            }
        }
        Text(
            text = "Tap to pause or resume.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = (15 * scale).sp
        )
    }
}

/** Stopwatch — tap to start, tap again to pause; laps list. */
@Composable
fun StopwatchWidget(scale: Float, fullscreen: Boolean = false) {
    var elapsedMs by remember { mutableStateOf(0L) }
    var running by remember { mutableStateOf(false) }
    var laps by remember { mutableStateOf<List<Long>>(emptyList()) }
    var lastTick by remember { mutableStateOf(0L) }
    LaunchedEffect(running) {
        lastTick = System.currentTimeMillis()
        while (running) {
            delay(50)
            val nowMs = System.currentTimeMillis()
            elapsedMs += nowMs - lastTick
            lastTick = nowMs
        }
    }
    fun fmt(ms: Long): String {
        val totalSeconds = ms / 1000
        return "%02d:%02d.%d".format(totalSeconds / 60, totalSeconds % 60, (ms % 1000) / 100)
    }
    Column(
        modifier = Modifier.fillMaxSize().padding((16 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = fmt(elapsedMs),
            color = Color.White,
            fontSize = (72 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Row(horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
            Button(onClick = { running = !running }) {
                Text(if (running) "Pause" else "Start", color = Color.White)
            }
            OutlinedButton(onClick = {
                if (running) laps = laps + elapsedMs else { running = false; elapsedMs = 0; laps = emptyList() }
            }) { Text(if (running) "Lap" else "Reset", color = Color.White) }
        }
        if (laps.isNotEmpty()) {
            Spacer(modifier = Modifier.height((12 * scale).dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = "All Laps", color = Color.White.copy(alpha = 0.7f), fontSize = (16 * scale).sp)
                for ((i, lap) in laps.withIndex()) {
                    Text(text = "Lap ${i + 1} — ${fmt(lap)}", color = Color.White, fontSize = (18 * scale).sp)
                }
            }
        }
        Text(
            text = "Tap to start, tap again to pause.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = (15 * scale).sp
        )
    }
}

/** Pomodoro — 25-minute focus blocks with a foreground service. */
@Composable
fun PomodoroWidget(context: Context, scale: Float) {
    var modeSeconds by remember { mutableStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    var remaining by remember { mutableStateOf(25 * 60) }
    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining--
        }
        if (running && remaining <= 0) {
            running = false
            beep()
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding((16 * scale).dp)) {
        Text(text = "Pomodoro", color = Color.White, fontSize = (26 * scale).sp, fontWeight = FontWeight.Bold)
        Text(
            text = "%02d:%02d".format(remaining / 60, remaining % 60),
            color = Color.White,
            fontSize = (64 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Work in 25-min blocks to stay focused. Tap the timer to pause or resume.",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = (16 * scale).sp
        )
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Row(horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
            Button(onClick = { modeSeconds = 25 * 60; remaining = 25 * 60; running = false }) {
                Text("25 min focus", color = Color.White)
            }
            OutlinedButton(onClick = { modeSeconds = 5 * 60; remaining = 5 * 60; running = false }) {
                Text("5 min break", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height((12 * scale).dp))
        if (running) {
            OutlinedButton(onClick = { PomodoroService.stop(context); running = false }) {
                Text("Stop timer", color = Color.White)
            }
        } else {
            Button(onClick = {
                PomodoroService.start(context, remaining)
                running = true
            }) { Text("Start timer", color = Color.White) }
        }
        Text(
            text = "The countdown keeps running in the background and beeps when it ends.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = (15 * scale).sp
        )
    }
}

/** System Dashboard — battery, storage and memory at a glance. */
@Composable
fun SystemDashboardWidget(context: Context, scale: Float) {
    val battery = remember { BatteryInfo.read(context) }
    val storage = remember { BatteryInfo.storageText() }
    val memory = remember {
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val info = android.app.ActivityManager.MemoryInfo()
            am.getMemoryInfo(info)
            val totalGb = info.totalMem / 1e9
            val availGb = info.availMem / 1e9
            "%.1f GB free of %.1f GB".format(availGb, totalGb)
        } catch (_: Exception) {
            "Memory unavailable"
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding((16 * scale).dp)) {
        Text(text = "System Dashboard", color = Color.White, fontSize = (26 * scale).sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Text(text = "Battery: ${battery.levelPercent}% ${if (battery.charging) "(charging)" else ""}", color = Color.White, fontSize = (20 * scale).sp)
        Text(text = "Health: ${battery.healthText}", color = Color.White, fontSize = (20 * scale).sp)
        battery.temperatureC?.let { Text(text = "Temperature: %.1f °C".format(it), color = Color.White, fontSize = (20 * scale).sp) }
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Text(text = "Storage: $storage", color = Color.White, fontSize = (20 * scale).sp)
        Text(text = "Memory: $memory", color = Color.White, fontSize = (20 * scale).sp)
        Spacer(modifier = Modifier.height((8 * scale).dp))
        val nextAlarm = (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).nextAlarmClock
        if (nextAlarm != null) {
            val dt = Instant.ofEpochMilli(nextAlarm.triggerTime).atZone(ZoneId.systemDefault())
            Text(
                text = "Next alarm: ${dt.format(DateTimeFormatter.ofPattern("HH:mm, EEEE", Locale.ENGLISH))}",
                color = Color.White,
                fontSize = (20 * scale).sp
            )
        }
    }
}

/** Day Centric Dashboard — date, agenda and day progress. */
@Composable
fun DayCentricWidget(now: LocalDateTime, scale: Float) {
    val context = LocalContext.current
    val events = remember { CalendarRepository.todayEvents(context) }
    val dayFraction = (now.hour * 60 + now.minute) / 1440f
    Column(modifier = Modifier.fillMaxSize().padding((16 * scale).dp)) {
        Text(
            text = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)),
            color = Color.White,
            fontSize = (26 * scale).sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Text(text = "Day progress", color = Color.White.copy(alpha = 0.7f), fontSize = (18 * scale).sp)
        LinearProgressIndicator(
            progress = { dayFraction },
            modifier = Modifier.fillMaxWidth().height((10 * scale).dp),
            color = Color(0xFF4CAF50),
            trackColor = Color(0xFF222222)
        )
        Text(
            text = "${(dayFraction * 100).toInt()}% of the day gone",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = (18 * scale).sp
        )
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Text(text = "Today", color = Color.White, fontSize = (20 * scale).sp, fontWeight = FontWeight.Bold)
        if (events.isEmpty()) {
            Text("No events today.", color = Color.White.copy(alpha = 0.8f), fontSize = (18 * scale).sp)
        } else {
            for (event in events) {
                Text(text = "${event.timeText()}  ${event.title}", color = Color.White, fontSize = (18 * scale).sp)
            }
        }
    }
}

/** Vibes Clocks — a mood-driven clock that changes its look based on the music. */
@Composable
fun VibesWidget(now: LocalDateTime, scale: Float) {
    val track by MusicListenerService.currentTrack.collectAsState()
    val moods = listOf(
        Color(0xFF7C4DFF) to "Electric",
        Color(0xFFFF4081) to "Warm",
        Color(0xFF00E5FF) to "Cool",
        Color(0xFF76FF03) to "Fresh",
        Color(0xFFFFB300) to "Golden"
    )
    val (color, mood) = if (track == null) {
        Color.White to "Quiet"
    } else {
        moods[(track!!.title.hashCode().let { if (it < 0) -it else it }) % moods.size]
    }
    Column(
        modifier = Modifier.fillMaxSize().padding((16 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "%02d:%02d".format(now.hour, now.minute),
            color = color,
            fontSize = (96 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            style = TextStyle(shadow = Shadow(color, blurRadius = 24f))
        )
        Text(text = "Mood: $mood", color = color.copy(alpha = 0.85f), fontSize = (22 * scale).sp)
        val t = track
        Text(
            text = if (t == null) "Play music to change the mood" else "${t.title} — ${t.artist}",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = (18 * scale).sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Third-party widget slots — AppWidget hosting is not in this build. */
@Composable
fun ThirdPartyWidget(slot: Int, scale: Float) {
    Column(
        modifier = Modifier.fillMaxSize().padding((16 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "3rd Party Widget – Slot $slot",
            color = Color.White,
            fontSize = (24 * scale).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Text(
            text = "Hosting widgets from other apps is not included in this build. " +
                "Pick any built-in widget instead.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = (17 * scale).sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Notification Mini — latest notifications (never leaves the device). */
@Composable
fun NotificationMiniWidget(context: Context, scale: Float) {
    val items by MusicListenerService.recentNotifications.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding((14 * scale).dp)) {
        Text(text = "Notifications", color = Color.White, fontSize = (24 * scale).sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height((8 * scale).dp))
        if (items.isEmpty()) {
            Text("No notifications yet", color = Color.White, fontSize = (20 * scale).sp)
            Text(
                "Notifications from your apps will appear here. This information never leaves your device.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (16 * scale).sp
            )
            Spacer(modifier = Modifier.height((12 * scale).dp))
            OutlinedButton(onClick = { openNotificationSettings(context) }) {
                Text("Grant notification access", color = Color.White)
            }
        } else {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                for (item in items) {
                    Text(text = item.title, color = Color.White, fontSize = (19 * scale).sp, fontWeight = FontWeight.Bold)
                    if (item.text.isNotEmpty()) {
                        Text(text = item.text, color = Color.White.copy(alpha = 0.8f), fontSize = (16 * scale).sp)
                    }
                    Text(
                        text = item.packageName,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = (14 * scale).sp
                    )
                    Spacer(modifier = Modifier.height((10 * scale).dp))
                }
            }
        }
    }
}
