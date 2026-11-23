package com.naomi.standby.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persistence for the stateful utility widgets (Tally, Todo, Protocols,
 * Sober Counter, Countdown). Small lists are stored as one delimited
 * string per list; '|' and ';' are escaped in user text.
 * Alarms live in [AlarmStore].
 */
class WidgetStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("standby_widgets", Context.MODE_PRIVATE)

    private fun esc(s: String) = s.replace("|", "/").replace(";", ",").replace("\n", " ")
    private fun splitList(raw: String?): List<String> =
        raw?.split(";")?.filter { it.isNotEmpty() } ?: emptyList()

    // ---- Tally counters: "name|count;name|count" ----
    fun getTallies(): List<Pair<String, Int>> = splitList(prefs.getString("tallies", null)).mapNotNull {
        val parts = it.split("|")
        if (parts.size == 2) parts[0] to (parts[1].toIntOrNull() ?: 0) else null
    }

    fun setTallies(tallies: List<Pair<String, Int>>) {
        prefs.edit().putString("tallies", tallies.joinToString(";") { "${esc(it.first)}|${it.second}" }).apply()
    }

    // ---- Todo list: "title|done(0/1);..." ----
    fun getTodos(): List<Pair<String, Boolean>> = splitList(prefs.getString("todos", null)).mapNotNull {
        val parts = it.split("|")
        if (parts.size == 2) parts[0] to (parts[1] == "1") else null
    }

    fun setTodos(todos: List<Pair<String, Boolean>>) {
        prefs.edit().putString("todos", todos.joinToString(";") { "${esc(it.first)}|${if (it.second) 1 else 0}" }).apply()
    }

    // ---- Protocols: one rule per entry ----
    fun getProtocols(): List<String> = splitList(prefs.getString("protocols", null))
    fun setProtocols(rules: List<String>) {
        prefs.edit().putString("protocols", rules.joinToString(";") { esc(it) }).apply()
    }

    // ---- Sober counter ----
    fun getSoberLabel(): String = prefs.getString("sober_label", null) ?: "Alcohol free"
    fun getSoberStartMillis(): Long = prefs.getLong("sober_start", -1L)
    fun getSoberTimesPerWeek(): Int = prefs.getInt("sober_times_per_week", 0)
    fun getSoberCostEach(): Float = prefs.getFloat("sober_cost_each", 0f)
    fun getSoberDiscreet(): Boolean = prefs.getBoolean("sober_discreet", false)

    fun setSober(label: String, startMillis: Long, timesPerWeek: Int, costEach: Float, discreet: Boolean) {
        prefs.edit()
            .putString("sober_label", label)
            .putLong("sober_start", startMillis)
            .putInt("sober_times_per_week", timesPerWeek)
            .putFloat("sober_cost_each", costEach)
            .putBoolean("sober_discreet", discreet)
            .apply()
    }

    fun resetSober() {
        prefs.edit().putLong("sober_start", System.currentTimeMillis()).apply()
    }

    // ---- Countdowns: "label|epochMillis;..." ----
    fun getCountdowns(): List<Pair<String, Long>> = splitList(prefs.getString("countdowns", null)).mapNotNull {
        val parts = it.split("|")
        if (parts.size == 2) parts[0] to (parts[1].toLongOrNull() ?: 0L) else null
    }

    fun setCountdowns(items: List<Pair<String, Long>>) {
        prefs.edit().putString("countdowns", items.joinToString(";") { "${esc(it.first)}|${it.second}" }).apply()
    }
}
