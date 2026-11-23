package com.naomi.standby.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * First-run explanation: what the app does, which permissions it wants
 * and why. Nothing is forced — every permission can be granted later
 * from system settings, and the app still works without it.
 */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* results are best-effort; missing permissions degrade gracefully */ }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "StandBy Clock", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Turn your phone into a bedside clock while it charges. " +
                "A big clock and a widget — calendar, battery, weather, music, photos, timers — " +
                "in landscape, large enough to read across the room.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "How it works", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "• Plug in the charger and StandBy Clock opens by itself (you can turn this off).\n" +
                "• Swipe left or right to change the clock face — there are 144 to choose from.\n" +
                "• Tap the screen for settings. Long-press for the face picker.\n" +
                "• At night the screen turns red and dims so it will not light up the room.\n" +
                "• It also works as the Android screensaver (Daydream) when docked.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Permissions", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Calendar (agenda widget), location (weather), photos (photo frame) and " +
                "notifications (music track + Pomodoro) are all optional. The clock works without any of them.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            val permissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissions.add(Manifest.permission.READ_CALENDAR)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            permissionLauncher.launch(permissions.toTypedArray())
            onDone()
        }) {
            Text("Allow & start", color = Color.White, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { onDone() }) {
            Text("Skip for now", color = Color.White, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Inspired by the iOS StandBy screen. Original implementation — no code, art or assets copied from any other app.",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp
        )
    }
}

/** Standalone activity hosting the same onboarding (used by the Daydream settings entry). */
class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                OnboardingScreen(onDone = { finish() })
            }
        }
    }
}
