package com.naomi.standby.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.AlarmStore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Full-screen alarm ring screen, shown over the lock screen.
 * "I'm up" dismisses; "Snooze 5 min" re-rings in five minutes.
 * If nobody dismisses or snoozes, it goes quiet after 5 minutes.
 */
class AlarmRingActivity : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val index = intent.getIntExtra(AlarmStore.EXTRA_ALARM_INDEX, -1)
        val store = AlarmStore(this)
        val alarm = store.getAlarms().getOrNull(index)
        // Auto-silence after 5 minutes.
        handler.postDelayed({ finish() }, 5 * 60_000L)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var snoozed by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)),
                        color = Color.White, fontSize = 96.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (alarm?.message.isNullOrBlank()) "Rise and shine, the day is yours."
                        else "“${alarm.message}”",
                        color = Color.White.copy(alpha = 0.85f), fontSize = 22.sp, textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    if (snoozed) {
                        Text(
                            text = "Snoozed for 5 min",
                            color = Color(0xFF4CAF50), fontSize = 20.sp
                        )
                    } else {
                        Button(
                            onClick = { finish() },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("I'm up", color = Color.White, fontSize = 20.sp) }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                AlarmStore.scheduleSnooze(this@AlarmRingActivity, index, 5)
                                snoozed = true
                                handler.postDelayed({ finish() }, 1500)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Snooze 5 min", color = Color.White, fontSize = 20.sp) }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
