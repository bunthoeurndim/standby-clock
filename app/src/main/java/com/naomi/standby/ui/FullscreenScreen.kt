package com.naomi.standby.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.model.ScreenId
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * Fullscreen host for a single screen (opened by tapping a Duo widget).
 * Back button at the top; double-tap closes the fullscreen view — and
 * closes the app entirely when "Auto Close on Double Tap" is on.
 * Night-mode red tint and screen brightness apply here too.
 */
@Composable
fun FullscreenScreen(
    screen: ScreenId,
    settings: SettingsStore,
    onBack: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    var darkBySensor by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = LocalDateTime.now()
        }
    }
    val night = settings.isNightNow(now.hour, now.minute, darkBySensor)
    val brightness = if (night) settings.nightBrightness else settings.dayBrightness
    LaunchedEffect(brightness) {
        val activity = context as? Activity
        activity?.window?.attributes = activity.window.attributes.apply {
            screenBrightness = (brightness.coerceIn(5, 100) / 100f)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
            .pointerInput(settings.autoCloseDoubleTap) {
                detectTapGestures(
                    onDoubleTap = {
                        if (settings.autoCloseDoubleTap) onExit() else onBack()
                    }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack) {
                    Text("‹ Back", color = Color.White, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = screen.displayName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (night) {
                    Text(text = "NIGHT", color = Color(0xFFFF2A1A), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                ScreenContent(
                    screen = screen,
                    now = now,
                    settings = settings,
                    scale = 1.0f,
                    onOpenScreen = { /* fullscreen screens do not nest */ }
                )
            }
            Text(
                text = "Double-tap to close",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        if (night) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFF2A1A).copy(alpha = 0.28f)))
        }
    }
}
