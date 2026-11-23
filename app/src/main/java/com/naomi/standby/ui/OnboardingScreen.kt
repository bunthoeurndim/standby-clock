package com.naomi.standby.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
 * First-run intro, following the original's onboarding copy:
 * "This is StandBy Mode Pro — An app that turns your Phone into a
 * Smart Display when Charging." Permissions are listed with the
 * original's titles and descriptions; every permission is optional
 * and can be granted later from system settings.
 *
 * There is no Premium in this build — everything is free.
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
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "This is StandBy Mode Pro", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "An app that turns your Phone into a Smart Display when Charging.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 19.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "How it works", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "• Duo is the home screen: pick a widget for the left and the right side.\n" +
                "• Tap a widget to open it full screen. Long-press a widget to replace it.\n" +
                "• Plug in the charger and StandBy opens by itself (you can turn this off).\n" +
                "• Swipe down with 3 fingers to unlock when the screen is locked.\n" +
                "• At night the screen turns red and dims so it will not light up the room.\n" +
                "• It also works as the Android screensaver (Daydream) when docked.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Some Widgets require your permission to work", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "• Calendar Access — View your upcoming events on your lock screen\n" +
                "• Display Over Other Apps — Launch StandBy automatically when you start charging\n" +
                "• Notification Access — Control your music without leaving the app\n" +
                "• Notifications — Keep StandBy running in the background\n" +
                "• Storage/photos — Allow access to your device storage to browse and select photos from your folders.\n" +
                "• Alarms — Allow exact alarms so this alarm rings on time, even in Doze.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 17.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Want to start with a Premium Experience? Everything is free in this build — there is no Premium, no paywall, no ads.",
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
            Text("Let's get started", color = Color.White, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { onDone() }) {
            Text("Skip for now", color = Color.White, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "A faithful replica of StandBy Mode, rebuilt from scratch. Original implementation — no code, art or assets copied from any other app.",
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
