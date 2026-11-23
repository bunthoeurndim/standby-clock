package com.naomi.standby.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.FileProvider
import com.naomi.standby.data.NightMode
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.data.formatMinutesOfDay
import com.naomi.standby.model.BigCropTheme
import com.naomi.standby.model.ChargingTheme
import com.naomi.standby.model.RetroflipStyle
import com.naomi.standby.model.ScreenCatalog
import com.naomi.standby.model.SegmentedFont
import com.naomi.standby.model.SolarStyle
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Settings, in the original's section order:
 * General, Night Mode, Auto Close, App Launch, Labs, News, Support.
 * Labels follow the spec's wording.
 */
@Composable
fun SettingsScreen(
    settings: SettingsStore,
    onBack: () -> Unit,
    onOpenAlarms: () -> Unit = {}
) {
    val context = LocalContext.current
    val leftScreen = ScreenCatalog.all.firstOrNull { it.id == settings.leftScreenId }
    val rightScreen = ScreenCatalog.all.firstOrNull { it.id == settings.rightScreenId }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { picked: List<Uri> ->
        if (picked.isNotEmpty()) settings.setPhotoUris(picked.map { it.toString() })
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: return@rememberLauncherForActivityResult
            val json = JSONObject(text)
            val values = mutableMapOf<String, Any?>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val raw = json.opt(key)
                when (raw) {
                    is Boolean -> values[key] = raw
                    is Int -> values[key] = raw
                    is Long -> values[key] = raw
                    is Double -> values[key] = if (key == "font_scale") raw.toFloat() else raw.toInt()
                    is String -> values[key] = raw
                    is JSONArray -> values[key] = (0 until raw.length()).mapNotNull { raw.optString(it) }.toSet()
                }
            }
            settings.restore(values)
        } catch (_: Exception) {
            // A bad backup file is ignored; settings stay as they are.
        }
    }

    fun exportSettings() {
        try {
            val json = JSONObject(settings.snapshot())
            val file = File(context.cacheDir, "standby-settings-backup.json")
            file.writeText(json.toString(2))
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export settings"))
        } catch (_: Exception) {
            // Export is best-effort.
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState())
    ) {
        Text(text = "Settings", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // ---- General ----
        Section("General")
        Text(text = "Left widget: ${leftScreen?.displayName ?: "Empty"}", color = Color.White, fontSize = 17.sp)
        Text(text = "Right widget: ${rightScreen?.displayName ?: "Empty"}", color = Color.White, fontSize = 17.sp)
        Text(
            text = "Long-press a Duo slot to change its widget, or tap it to open it full screen.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        CycleRow(
            label = "Retroflip style",
            value = RetroflipStyle.byName(settings.retroflipStyle).displayName,
            onPrev = {
                val list = RetroflipStyle.entries
                val i = list.indexOf(RetroflipStyle.byName(settings.retroflipStyle))
                settings.setRetroflipStyle(list[(i - 1 + list.size) % list.size].name)
            },
            onNext = {
                val list = RetroflipStyle.entries
                val i = list.indexOf(RetroflipStyle.byName(settings.retroflipStyle))
                settings.setRetroflipStyle(list[(i + 1) % list.size].name)
            }
        )
        CycleRow(
            label = "Big Crop theme",
            value = BigCropTheme.byName(settings.bigCropTheme).displayName,
            onPrev = {
                val list = BigCropTheme.entries
                val i = list.indexOf(BigCropTheme.byName(settings.bigCropTheme))
                settings.setBigCropTheme(list[(i - 1 + list.size) % list.size].name)
            },
            onNext = {
                val list = BigCropTheme.entries
                val i = list.indexOf(BigCropTheme.byName(settings.bigCropTheme))
                settings.setBigCropTheme(list[(i + 1) % list.size].name)
            }
        )
        CycleRow(
            label = "Solar style",
            value = SolarStyle.byName(settings.solarStyle).displayName,
            onPrev = {
                val list = SolarStyle.entries
                val i = list.indexOf(SolarStyle.byName(settings.solarStyle))
                settings.setSolarStyle(list[(i - 1 + list.size) % list.size].name)
            },
            onNext = {
                val list = SolarStyle.entries
                val i = list.indexOf(SolarStyle.byName(settings.solarStyle))
                settings.setSolarStyle(list[(i + 1) % list.size].name)
            }
        )
        CycleRow(
            label = "Segmented font",
            value = SegmentedFont.byName(settings.segmentedFont).displayName,
            onPrev = {
                val list = SegmentedFont.entries
                val i = list.indexOf(SegmentedFont.byName(settings.segmentedFont))
                settings.setSegmentedFont(list[(i - 1 + list.size) % list.size].name)
            },
            onNext = {
                val list = SegmentedFont.entries
                val i = list.indexOf(SegmentedFont.byName(settings.segmentedFont))
                settings.setSegmentedFont(list[(i + 1) % list.size].name)
            }
        )
        CycleRow(
            label = "Charging theme",
            value = (ChargingTheme.entries.firstOrNull { it.name == settings.chargingTheme } ?: ChargingTheme.MATERIAL).displayName,
            onPrev = {
                val list = ChargingTheme.entries
                val cur = ChargingTheme.entries.firstOrNull { it.name == settings.chargingTheme } ?: ChargingTheme.MATERIAL
                val i = list.indexOf(cur)
                settings.setChargingTheme(list[(i - 1 + list.size) % list.size].name)
            },
            onNext = {
                val list = ChargingTheme.entries
                val cur = ChargingTheme.entries.firstOrNull { it.name == settings.chargingTheme } ?: ChargingTheme.MATERIAL
                val i = list.indexOf(cur)
                settings.setChargingTheme(list[(i + 1) % list.size].name)
            }
        )
        ToggleRow("Show seconds", settings.showSeconds) { settings.setShowSeconds(it) }
        ToggleRow("Blinking separator", settings.blinkSeparator) { settings.setBlinkSeparator(it) }
        ToggleRow("Show clock in Duo", settings.showClockOnCharging) { settings.setShowClockOnCharging(it) }
        ToggleRow("Haptic feedback", settings.hapticFeedback) { settings.setHapticFeedback(it) }
        Text(text = "Text size", color = Color.White, fontSize = 17.sp)
        Slider(
            value = settings.fontScale,
            onValueChange = { settings.setFontScale(it) },
            valueRange = 0.7f..1.4f,
            steps = 6
        )
        Text(text = "%.0f%%".format(settings.fontScale * 100), color = Color.White.copy(alpha = 0.7f))
        Text(text = "Start of the Week", color = Color.White, fontSize = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { settings.setStartOfWeek(0) }) {
                Text("Sunday", color = if (settings.startOfWeek == 0) Color(0xFF4CAF50) else Color.White)
            }
            OutlinedButton(onClick = { settings.setStartOfWeek(1) }) {
                Text("Monday", color = if (settings.startOfWeek == 1) Color(0xFF4CAF50) else Color.White)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Photos (${settings.photoUris.size} selected)", color = Color.White, fontSize = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Add Pictures", color = Color.White) }
            OutlinedButton(onClick = { settings.setPhotoUris(emptyList()) }) {
                Text("Clear Pictures", color = Color.White)
            }
        }
        Text(text = "Change time between photos", color = Color.White, fontSize = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (seconds in listOf(5, 8, 15, 30)) {
                OutlinedButton(onClick = { settings.setPhotoIntervalSeconds(seconds) }) {
                    Text(
                        text = "${seconds}s",
                        color = if (settings.photoIntervalSeconds == seconds) Color(0xFF4CAF50) else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Section("Alarms")
        Button(onClick = onOpenAlarms, modifier = Modifier.fillMaxWidth()) {
            Text("Manage alarms", color = Color.White)
        }
        Text(
            text = "Allow exact alarms so this alarm rings on time, even in Doze.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )

        // ---- Night Mode ----
        Spacer(modifier = Modifier.height(16.dp))
        Section("Night Mode")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (mode in NightMode.entries) {
                OutlinedButton(onClick = { settings.setNightMode(mode) }) {
                    Text(
                        text = when (mode) {
                            NightMode.OFF -> "Off"
                            NightMode.AUTO -> "Scheduled"
                            NightMode.MANUAL -> "Manual"
                        },
                        color = if (settings.nightMode == mode) Color(0xFF4CAF50) else Color.White
                    )
                }
            }
        }
        Text(
            text = "Manual Night Mode applies a red tint overlay to make the UI easier on the eyes in dark environments. " +
                "Scheduled Night Mode automatically enables Night Mode based on your schedule.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        ToggleRow("Auto Night Mode in Dark Environments", settings.useLightSensor) { settings.setUseLightSensor(it) }
        Text(
            text = "Uses your phone's light sensor to enable Night Mode in the dark.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Text(text = "Night starts: ${formatMinutesOfDay(settings.nightStartMinutes)}", color = Color.White, fontSize = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { settings.setNightStartMinutes((settings.nightStartMinutes - 60 + 1440) % 1440) }) { Text("−1h", color = Color.White) }
            Button(onClick = { settings.setNightStartMinutes((settings.nightStartMinutes + 60) % 1440) }) { Text("+1h", color = Color.White) }
        }
        Text(text = "Night ends: ${formatMinutesOfDay(settings.nightEndMinutes)}", color = Color.White, fontSize = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { settings.setNightEndMinutes((settings.nightEndMinutes - 60 + 1440) % 1440) }) { Text("−1h", color = Color.White) }
            Button(onClick = { settings.setNightEndMinutes((settings.nightEndMinutes + 60) % 1440) }) { Text("+1h", color = Color.White) }
        }
        ToggleRow("Burn-In Protection", settings.burnInProtection) { settings.setBurnInProtection(it) }
        Text(
            text = "Moves the screen a few pixels every minute to protect the screen.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Text(text = "Day brightness: ${settings.dayBrightness}%", color = Color.White, fontSize = 17.sp)
        Slider(
            value = settings.dayBrightness.toFloat(),
            onValueChange = { settings.setDayBrightness(it.toInt()) },
            valueRange = 20f..100f
        )
        Text(text = "Night brightness: ${settings.nightBrightness}%", color = Color.White, fontSize = 17.sp)
        Slider(
            value = settings.nightBrightness.toFloat(),
            onValueChange = { settings.setNightBrightness(it.toInt()) },
            valueRange = 5f..60f
        )

        // ---- Auto Close ----
        Spacer(modifier = Modifier.height(16.dp))
        Section("Auto Close")
        ToggleRow("Auto Close on Unplug", settings.closeOnUnplug) { settings.setCloseOnUnplug(it) }
        Text(
            text = "Closes automatically when unplugged from the charger.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        ToggleRow("Auto Close on Double Tap", settings.autoCloseDoubleTap) { settings.setAutoCloseDoubleTap(it) }
        Text(
            text = "Double-tap anywhere to close the app.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Text(
            text = "Auto Close on Portrait: closes the app after your phone is picked up in portrait mode. Not available in Portrait or Automatic orientation.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )

        // ---- App Launch ----
        Spacer(modifier = Modifier.height(16.dp))
        Section("App Launch")
        ToggleRow("Launch on charging", settings.autoStartCharging) { settings.setAutoStartCharging(it) }
        Text(
            text = "Choose when to launch the app automatically. Disable this if you want to open the app manually. " +
                "App will auto launch when all conditions are met: charger plugged in.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Text(
            text = "If StandBy didn't open while charging, Quick Launch needs permission to display over other apps. " +
                "Allow it once and StandBy opens every time you plug in.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        OutlinedButton(onClick = {
            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }) { Text("Display over other apps settings", color = Color.White) }
        Text(
            text = "Samsung: use Samsung Modes & Routines to launch the app — if you use Samsung's feature, disable Quick Launch. " +
                "Huawei: add StandBy to Protected Apps. Vivo: allow background running. " +
                "Xiaomi: allow \"Display pop-up windows while running in the background\" and \"Show on Lock screen\".",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Text(
            text = "Also works as the Android screensaver: Settings → Display → Screen saver → StandBy.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )

        // ---- Sleep timer ----
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

        // ---- Labs ----
        Spacer(modifier = Modifier.height(16.dp))
        Section("Labs")
        Text(text = "Labs — try features first.", color = Color.White.copy(alpha = 0.7f), fontSize = 15.sp)
        ToggleRow("Focus Mode", settings.focusMode) { settings.setFocusMode(it) }
        Text(
            text = "Need to stay focused? Select which notifications are allowed while StandBy Mode is active.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        ToggleRow("Full-Screen Notifications (Beta)", settings.fullscreenNotifications) { settings.setFullscreenNotifications(it) }
        Text(
            text = "Experimental feature. Incoming notifications will appear full-screen with smooth animations. Requires Notification Access permission.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        OutlinedButton(onClick = {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }) { Text("Notification access settings", color = Color.White) }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Backup & Restore", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { exportSettings() }) { Text("Export Settings", color = Color.White) }
            OutlinedButton(onClick = { importLauncher.launch("application/json") }) {
                Text("Import Settings", color = Color.White)
            }
        }
        Text(
            text = "Save your current settings to a backup file, or restore your settings from a backup file.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )

        // ---- News ----
        Spacer(modifier = Modifier.height(16.dp))
        Section("News")
        Text(
            text = "Tips and guides on our blog — Desk setups, battery tips and step-by-step guides.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Text(
            text = "What's next — vote for what we build next. The ideas board needs an account in the original app; " +
                "this build has no sign-in, so voting is not available here.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )

        // ---- Support ----
        Spacer(modifier = Modifier.height(16.dp))
        Section("Support")
        Text(
            text = "Issues, Feature Requests, or Collaborations? Contact us with issues, suggestions, or feedback. " +
                "Include details about the screen or feature you were using.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bunthoeurndim/standby-clock")))
            }) { Text("Project page", color = Color.White) }
            OutlinedButton(onClick = {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "StandBy — turn your phone into a smart display while charging. https://github.com/bunthoeurndim/standby-clock")
                }
                context.startActivity(Intent.createChooser(share, "Share StandBy"))
            }) { Text("Share", color = Color.White) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Section("About")
        Text(
            text = "StandBy 1.1.0 — a faithful replica of StandBy Mode, rebuilt from scratch. " +
                "No code, art or assets were copied from any other app. All features are free in this build — there is no Premium. " +
                "Weather data: Open-Meteo.",
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
        Text(text = label, color = Color.White, fontSize = 17.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun CycleRow(label: String, value: String, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.White, fontSize = 17.sp, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = onPrev) { Text("‹", color = Color.White) }
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = onNext) { Text("›", color = Color.White) }
    }
}
