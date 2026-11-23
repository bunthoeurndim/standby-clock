package com.naomi.standby

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.ui.FacePickerScreen
import com.naomi.standby.ui.OnboardingScreen
import com.naomi.standby.ui.SettingsScreen
import com.naomi.standby.ui.StandbyScreen

private enum class Screen { STANDBY, SETTINGS, FACES }

/**
 * Launcher activity. Shows onboarding on first run, then the standby screen.
 * Tap opens settings, long-press opens the face picker. The charging
 * receiver can close this activity again via [ACTION_CLOSE_STANDBY].
 */
class MainActivity : ComponentActivity() {
    private var closeRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        closeRequested = intent?.action == ACTION_CLOSE_STANDBY
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val settings = remember { SettingsStore(this) }
                var screen by remember { mutableStateOf(Screen.STANDBY) }
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black)
                ) {
                    if (!settings.onboarded) {
                        OnboardingScreen(
                            onDone = { settings.setOnboarded(true) }
                        )
                    } else when (screen) {
                        Screen.STANDBY -> StandbyScreen(
                            settings = settings,
                            onOpenSettings = { screen = Screen.SETTINGS },
                            onExit = { finish() },
                            onLongPress = { screen = Screen.FACES }
                        )
                        Screen.SETTINGS -> SettingsScreen(
                            settings = settings,
                            onBack = { screen = Screen.STANDBY },
                            onPickFace = { screen = Screen.FACES }
                        )
                        Screen.FACES -> FacePickerScreen(
                            settings = settings,
                            onBack = { screen = Screen.STANDBY },
                            onFaceSelected = { screen = Screen.STANDBY }
                        )
                    }
                }
            }
        }
        if (closeRequested) finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == ACTION_CLOSE_STANDBY) finish()
    }

    companion object {
        const val EXTRA_AUTOSTART = "autostart"
        const val ACTION_CLOSE_STANDBY = "com.naomi.standby.CLOSE"
    }
}
