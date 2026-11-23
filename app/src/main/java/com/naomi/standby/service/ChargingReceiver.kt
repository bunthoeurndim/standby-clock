package com.naomi.standby.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.naomi.standby.MainActivity
import com.naomi.standby.data.SettingsStore

/**
 * Auto-start on charging, close on unplug, re-arm after boot.
 * Reads the user's toggles from SharedPreferences (via SettingsStore),
 * which is safe to do synchronously from a receiver.
 */
class ChargingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val settings = SettingsStore(context)
        when (intent?.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                if (!settings.autoStartCharging) return
                val open = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(MainActivity.EXTRA_AUTOSTART, true)
                }
                context.startActivity(open)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                if (!settings.closeOnUnplug) return
                val close = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    action = MainActivity.ACTION_CLOSE_STANDBY
                }
                context.startActivity(close)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                // Nothing to do immediately: this receiver stays registered,
                // so future power events will be delivered.
            }
        }
    }
}
