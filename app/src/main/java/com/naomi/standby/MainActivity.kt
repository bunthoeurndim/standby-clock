package com.naomi.standby

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.naomi.standby.data.DuoSlot
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.model.ScreenId
import com.naomi.standby.ui.FullscreenScreen
import com.naomi.standby.ui.OnboardingScreen
import com.naomi.standby.ui.SettingsScreen
import com.naomi.standby.ui.StandbyScreen
import com.naomi.standby.ui.WidgetPickerScreen
import com.naomi.standby.ui.widgets.AlarmsWidget

private sealed interface Screen {
    data object Standby : Screen
    data object Settings : Screen
    data class Picker(val slot: DuoSlot) : Screen
    data class Fullscreen(val screen: ScreenId) : Screen
    data object Alarms : Screen
}

/**
 * Launcher activity. Shows onboarding on first run, then the Duo home.
 * Tap a Duo widget to open it fullscreen; long-press to replace it;
 * Settings holds the original's General / Night Mode / Auto Close /
 * App Launch / Labs / News / Support sections.
 * The charging receiver can close this activity via [ACTION_CLOSE_STANDBY].
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
                var screen by remember { mutableStateOf<Screen>(Screen.Standby) }
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black)
                ) {
                    if (!settings.onboarded) {
                        OnboardingScreen(
                            onDone = { settings.setOnboarded(true) }
                        )
                    } else when (val current = screen) {
                        Screen.Standby -> StandbyScreen(
                            settings = settings,
                            onOpenSettings = { screen = Screen.Settings },
                            onExit = { finish() },
                            onPickSlot = { slot -> screen = Screen.Picker(slot) },
                            onOpenFullscreen = { id -> screen = Screen.Fullscreen(id) }
                        )
                        Screen.Settings -> SettingsScreen(
                            settings = settings,
                            onBack = { screen = Screen.Standby },
                            onOpenAlarms = { screen = Screen.Alarms }
                        )
                        is Screen.Picker -> WidgetPickerScreen(
                            settings = settings,
                            slot = current.slot,
                            onBack = { screen = Screen.Standby }
                        )
                        is Screen.Fullscreen -> FullscreenScreen(
                            screen = current.screen,
                            settings = settings,
                            onBack = { screen = Screen.Standby },
                            onExit = { finish() }
                        )
                        Screen.Alarms -> androidx.compose.foundation.layout.Column(
                            modifier = Modifier.fillMaxSize().background(Color.Black).padding(8.dp)
                        ) {
                            androidx.compose.material3.Button(onClick = { screen = Screen.Settings }) {
                                androidx.compose.material3.Text("‹ Back to settings", color = Color.White)
                            }
                            AlarmsWidget(scale = 1.0f)
                        }
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
