package com.naomi.standby.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.naomi.standby.R

/**
 * Foreground Pomodoro timer. Counts down in the background, shows the
 * remaining time in a notification, and beeps when the session ends.
 * No audio files are bundled — the beep comes from ToneGenerator.
 */
class PomodoroService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var remainingSeconds = 0
    private var running = false

    private val ticker = object : Runnable {
        override fun run() {
            if (!running) return
            remainingSeconds--
            if (remainingSeconds <= 0) {
                running = false
                beep()
                updateNotification("Pomodoro done — take a break", 0)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return
            }
            updateNotification("Pomodoro — ${remainingSeconds / 60}:${"%02d".format(remainingSeconds % 60)} left", remainingSeconds)
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                running = false
                handler.removeCallbacks(ticker)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                remainingSeconds = intent.getIntExtra(EXTRA_SECONDS, 25 * 60)
                running = true
                createChannel()
                startForeground(NOTIFICATION_ID, buildNotification("Pomodoro started", remainingSeconds))
                handler.removeCallbacks(ticker)
                handler.postDelayed(ticker, 1000L)
                return START_STICKY
            }
        }
        return START_NOT_STICKY
    }

    private fun createChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.pomodoro_channel),
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = getString(R.string.pomodoro_channel_desc) }
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String, secondsLeft: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("StandBy Clock Pomodoro")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setProgress(25 * 60, secondsLeft, false)
            .build()
    }

    private fun updateNotification(text: String, secondsLeft: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(text, secondsLeft))
    }

    private fun beep() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_ALARM, 80)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP, 600)
            Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, 900)
        } catch (_: Exception) {
            // Sound is best-effort; the notification still shows.
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        running = false
        handler.removeCallbacks(ticker)
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "pomodoro"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.naomi.standby.POMODORO_START"
        const val ACTION_STOP = "com.naomi.standby.POMODORO_STOP"
        const val EXTRA_SECONDS = "seconds"

        fun start(context: Context, seconds: Int) {
            val intent = Intent(context, PomodoroService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SECONDS, seconds)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, PomodoroService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }
}
