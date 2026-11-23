package com.naomi.standby.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.naomi.standby.data.AlarmStore
import com.naomi.standby.ui.AlarmRingActivity

/** Fires a local alarm (or a snooze) and opens the ring screen over the lock screen. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val index = intent?.getIntExtra(AlarmStore.EXTRA_ALARM_INDEX, -1) ?: -1
        if (index < 0) return
        val ring = Intent(context, AlarmRingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(AlarmStore.EXTRA_ALARM_INDEX, index)
        }
        context.startActivity(ring)
        // Re-schedule repeating alarms (non-snooze firings).
        if (intent?.getBooleanExtra(AlarmStore.EXTRA_SNOOZE, false) != true) {
            val alarms = AlarmStore(context).getAlarms()
            alarms.getOrNull(index)?.let { AlarmStore.schedule(context, index, it) }
        }
    }
}
