package com.naomi.standby.service

import android.service.dreams.DreamService
import androidx.compose.ui.platform.ComposeView
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.ui.StandbyScreen

/**
 * Android Daydream / screensaver entry point.
 * When the device is docked or idle and this app is the chosen screensaver,
 * the standby screen shows — the same screen as MainActivity.
 */
class StandbyDream : DreamService() {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = true
        isFullscreen = true
        val settings = SettingsStore(this)
        val view = ComposeView(this).apply {
            setContent {
                StandbyScreen(
                    settings = settings,
                    onOpenSettings = { /* settings are in MainActivity; dream is view-only */ },
                    onExit = { finish() }
                )
            }
        }
        setContentView(view)
    }
}
