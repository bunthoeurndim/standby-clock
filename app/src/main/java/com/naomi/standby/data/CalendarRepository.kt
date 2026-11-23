package com.naomi.standby.data

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CalendarEvent(val title: String, val startMillis: Long, val allDay: Boolean) {
    fun timeText(): String {
        if (allDay) return "All day"
        val dt = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault())
        return dt.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH))
    }
}

/** Reads today's agenda from the system calendar (needs READ_CALENDAR). */
object CalendarRepository {
    fun todayEvents(context: Context): List<CalendarEvent> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED
        ) return emptyList()
        val resolver: ContentResolver = context.contentResolver
        val now = System.currentTimeMillis()
        val endOfDay = now + 24L * 60 * 60 * 1000
        val projection = arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.ALL_DAY
        )
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ?"
        val args = arrayOf(now.toString(), endOfDay.toString())
        val sort = "${CalendarContract.Events.DTSTART} ASC"
        val out = mutableListOf<CalendarEvent>()
        try {
            resolver.query(
                CalendarContract.Events.CONTENT_URI, projection, selection, args, sort
            )?.use { cursor ->
                val titleIdx = cursor.getColumnIndex(CalendarContract.Events.TITLE)
                val startIdx = cursor.getColumnIndex(CalendarContract.Events.DTSTART)
                val allDayIdx = cursor.getColumnIndex(CalendarContract.Events.ALL_DAY)
                while (cursor.moveToNext() && out.size < 8) {
                    val title = if (titleIdx >= 0) cursor.getString(titleIdx) ?: "(No title)" else "(No title)"
                    val start = if (startIdx >= 0) cursor.getLong(startIdx) else now
                    val allDay = if (allDayIdx >= 0) cursor.getInt(allDayIdx) == 1 else false
                    out.add(CalendarEvent(title, start, allDay))
                }
            }
        } catch (_: SecurityException) {
            return emptyList()
        } catch (_: Exception) {
            return emptyList()
        }
        return out
    }
}
