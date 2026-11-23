package com.naomi.standby.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TrackInfo(val title: String, val artist: String, val packageName: String)

data class NotificationItem(
    val title: String,
    val text: String,
    val packageName: String,
    val timeMillis: Long
)

/**
 * Listens for media notifications and exposes the currently playing
 * track. Only transport/media notifications are treated as music, so
 * ordinary messages never appear in the music widget. The user must
 * enable notification access for StandBy Clock in system settings;
 * the widget explains this when nothing is playing.
 */
class MusicListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification ?: return
        val isMedia = notification.category == Notification.CATEGORY_TRANSPORT ||
            notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION)
        if (isMedia) {
            val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: return
            val artist = notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                ?: notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
                ?: ""
            _currentTrack.value = TrackInfo(title, artist, sbn.packageName)
            return
        }
        // Non-media notifications feed Notification Mini (never leaves the device).
        val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: return
        val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val item = NotificationItem(title, text, sbn.packageName, sbn.postTime)
        val updated = (listOf(item) + _recentNotifications.value)
            .distinctBy { it.title to it.text to it.packageName }
            .take(20)
        _recentNotifications.value = updated
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn?.packageName == _currentTrack.value?.packageName) {
            _currentTrack.value = null
        }
    }

    companion object {
        private val _currentTrack = MutableStateFlow<TrackInfo?>(null)
        val currentTrack: StateFlow<TrackInfo?> = _currentTrack.asStateFlow()

        private val _recentNotifications = MutableStateFlow<List<NotificationItem>>(emptyList())
        val recentNotifications: StateFlow<List<NotificationItem>> = _recentNotifications.asStateFlow()
    }
}
