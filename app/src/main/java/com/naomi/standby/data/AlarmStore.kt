package com.naomi.standby.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.naomi.standby.service.AlarmReceiver
import java.util.Calendar

data class AlarmItem(
    val hour: Int,
    val minute: Int,
    val daysMask: Int, // bit 0 = Mon … bit 6 = Sun; 0 = every day
    val message: String,
    val enabled: Boolean
) {
    fun timeText(): String = "%02d:%02d".format(hour, minute)
    fun daysText(): String {
        if (daysMask == 0) return "Every day"
        val names = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return names.filterIndexed { i, _ -> daysMask and (1 shl i) != 0 }.joinToString(", ")
    }
}

/** Simple local alarms, persisted in SharedPreferences. No cloud, no video themes. */
class AlarmStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("standby_alarms", Context.MODE_PRIVATE)

    fun getAlarms(): List<AlarmItem> {
        val raw = prefs.getString("alarms", null) ?: return emptyList()
        return raw.split(";").filter { it.isNotEmpty() }.mapNotNull {
            val p = it.split("|")
            if (p.size == 5) AlarmItem(
                p[0].toIntOrNull() ?: 0,
                p[1].toIntOrNull() ?: 0,
                p[2].toIntOrNull() ?: 0,
                p[3].replace("/", "|"),
                p[4] == "1"
            ) else null
        }
    }

    fun setAlarms(alarms: List<AlarmItem>) {
        val raw = alarms.joinToString(";") {
            "${it.hour}|${it.minute}|${it.daysMask}|${it.message.replace("|", "/")}|${if (it.enabled) 1 else 0}"
        }
        prefs.edit().putString("alarms", raw).apply()
    }

    companion object {
        const val EXTRA_ALARM_INDEX = "alarm_index"
        const val EXTRA_SNOOZE = "snooze"

        /** Next trigger time for an alarm, honouring repeat days. */
        fun nextTriggerMillis(alarm: AlarmItem, fromMillis: Long = System.currentTimeMillis()): Long {
            val cal = Calendar.getInstance().apply { timeInMillis = fromMillis }
            for (dayOffset in 0..7) {
                val candidate = (cal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, dayOffset)
                    set(Calendar.HOUR_OF_DAY, alarm.hour)
                    set(Calendar.MINUTE, alarm.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (candidate.timeInMillis <= fromMillis) continue
                // Calendar.DAY_OF_WEEK: 1=Sun … 7=Sat. Our mask: 0=Mon … 6=Sun.
                val dow = candidate.get(Calendar.DAY_OF_WEEK)
                val maskBit = when (dow) {
                    Calendar.MONDAY -> 0
                    Calendar.TUESDAY -> 1
                    Calendar.WEDNESDAY -> 2
                    Calendar.THURSDAY -> 3
                    Calendar.FRIDAY -> 4
                    Calendar.SATURDAY -> 5
                    else -> 6
                }
                if (alarm.daysMask == 0 || alarm.daysMask and (1 shl maskBit) != 0) {
                    return candidate.timeInMillis
                }
            }
            return fromMillis + 24L * 60 * 60 * 1000
        }

        fun schedule(context: Context, index: Int, alarm: AlarmItem) {
            if (!alarm.enabled) return
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
                // Without exact-alarm permission, fall back to an inexact alarm.
                manager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, nextTriggerMillis(alarm), pendingIntent(context, index, false)
                )
                return
            }
            manager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, nextTriggerMillis(alarm), pendingIntent(context, index, false)
            )
        }

        fun scheduleSnooze(context: Context, index: Int, minutes: Int) {
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val trigger = System.currentTimeMillis() + minutes * 60_000L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && manager.canScheduleExactAlarms()) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pendingIntent(context, index, true))
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pendingIntent(context, index, true))
            }
        }

        fun cancel(context: Context, index: Int) {
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            manager.cancel(pendingIntent(context, index, false))
            manager.cancel(pendingIntent(context, index, true))
        }

        private fun pendingIntent(context: Context, index: Int, snooze: Boolean): PendingIntent {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(EXTRA_ALARM_INDEX, index)
                putExtra(EXTRA_SNOOZE, snooze)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return PendingIntent.getBroadcast(context, 5000 + index * 2 + if (snooze) 1 else 0, intent, flags)
        }
    }
}
