package com.naomi.standby.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs

data class BatteryStatus(
    val levelPercent: Int,
    val charging: Boolean,
    val temperatureC: Float?,
    val healthText: String,
    val timeToFullMinutes: Long?
)

/** Reads battery + storage from the system; no permissions needed. */
object BatteryInfo {
    fun read(context: Context): BatteryStatus {
        val intent = context.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val temp = if (tempTenths > 0) tempTenths / 10f else null
        val health = when (intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val timeToFull = try {
            val seconds = bm.computeChargeTimeRemaining()
            if (seconds > 0) seconds / 60 else null
        } catch (_: Exception) {
            null
        }
        return BatteryStatus(percent, charging, temp, health, timeToFull)
    }

    /** Free / total internal storage in GB, one decimal. */
    fun storageText(): String {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val freeGb = stat.availableBytes / 1e9
            val totalGb = stat.totalBytes / 1e9
            "%.1f GB free of %.1f GB".format(freeGb, totalGb)
        } catch (_: Exception) {
            "Storage unavailable"
        }
    }
}
