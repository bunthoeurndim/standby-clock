package com.naomi.standby.ui.widgets

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.AlarmItem
import com.naomi.standby.data.AlarmStore
import com.naomi.standby.data.BatteryInfo
import com.naomi.standby.data.CalendarRepository
import com.naomi.standby.data.WidgetStore
import com.naomi.standby.data.rememberWeather
import com.naomi.standby.model.CONCEPT_TABS
import com.naomi.standby.model.PROTOCOL_SUGGESTIONS
import com.naomi.standby.model.ScreenId
import com.naomi.standby.service.MusicListenerService
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Calculator — basic ops; a Scientific toggle adds sin/cos/sqrt/log/π. */
@Composable
fun CalculatorWidget(scale: Float) {
    var display by remember { mutableStateOf("0") }
    var accumulator by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var fresh by remember { mutableStateOf(true) }
    var scientific by remember { mutableStateOf(false) }

    fun inputDigit(d: String) {
        display = if (fresh || display == "0") d else display + d
        fresh = false
    }

    fun applyOp(op: String) {
        val value = display.toDoubleOrNull() ?: 0.0
        val acc = accumulator
        if (acc != null && pendingOp != null && !fresh) {
            val result = when (pendingOp) {
                "+" -> acc + value
                "−" -> acc - value
                "×" -> acc * value
                "÷" -> if (value == 0.0) Double.NaN else acc / value
                else -> value
            }
            accumulator = result
            display = formatCalc(result)
        } else {
            accumulator = value
        }
        pendingOp = op
        fresh = true
    }

    fun equals() {
        val acc = accumulator
        val op = pendingOp
        if (acc != null && op != null) {
            val value = display.toDoubleOrNull() ?: 0.0
            val result = when (op) {
                "+" -> acc + value
                "−" -> acc - value
                "×" -> acc * value
                "÷" -> if (value == 0.0) Double.NaN else acc / value
                else -> value
            }
            display = formatCalc(result)
            accumulator = null
            pendingOp = null
            fresh = true
        }
    }

    fun applyFn(fn: String) {
        val value = display.toDoubleOrNull() ?: 0.0
        val result = when (fn) {
            "sin" -> sin(value * PI / 180.0)
            "cos" -> cos(value * PI / 180.0)
            "√" -> sqrt(value)
            "log" -> kotlin.math.log10(value)
            "π" -> PI
            "x²" -> value * value
            else -> value
        }
        display = formatCalc(result)
        fresh = true
    }

    Column(
        modifier = Modifier.fillMaxSize().padding((12 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = display,
            color = Color.White,
            fontSize = (48 * scale).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        val rows = listOf(
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "−"),
            listOf("0", ".", "C", "+")
        )
        for (row in rows) {
            Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                for (key in row) {
                    Button(
                        onClick = {
                            when (key) {
                                "C" -> { display = "0"; accumulator = null; pendingOp = null; fresh = true }
                                "+", "−", "×", "÷" -> applyOp(key)
                                else -> inputDigit(key)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(key, color = Color.White, fontSize = (22 * scale).sp) }
                }
            }
            Spacer(modifier = Modifier.height((8 * scale).dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
            Button(onClick = { equals() }, modifier = Modifier.weight(1f)) {
                Text("=", color = Color.White, fontSize = (22 * scale).sp)
            }
            OutlinedButton(onClick = { scientific = !scientific }, modifier = Modifier.weight(1f)) {
                Text(if (scientific) "Basic" else "Scientific", color = Color.White, fontSize = (18 * scale).sp)
            }
        }
        if (scientific) {
            Spacer(modifier = Modifier.height((8 * scale).dp))
            Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                for (fn in listOf("sin", "cos", "√", "log", "π", "x²")) {
                    OutlinedButton(onClick = { applyFn(fn) }, modifier = Modifier.weight(1f)) {
                        Text(fn, color = Color.White, fontSize = (16 * scale).sp)
                    }
                }
            }
        }
        Text(
            text = "Swipe up for scientific functions.",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = (14 * scale).sp
        )
    }
}

private fun formatCalc(value: Double): String {
    if (value.isNaN()) return "Error"
    if (value.isInfinite()) return "∞"
    val rounded = (value * 1e10).toLong() / 1e10
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}

/** Tally Counters — tap to count up, long-press to subtract. */
@Composable
fun TallyWidget(scale: Float) {
    val context = LocalContext.current
    val store = remember { WidgetStore(context) }
    var tallies by remember { mutableStateOf(store.getTallies()) }
    var newName by remember { mutableStateOf("") }
    fun save(next: List<Pair<String, Int>>) {
        tallies = next
        store.setTallies(next)
    }
    Column(modifier = Modifier.fillMaxSize().padding((14 * scale).dp).verticalScroll(rememberScrollState())) {
        Text(text = "Tally Counters", color = Color.White, fontSize = (24 * scale).sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Tap a counter to count up. Long-press to subtract.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = (15 * scale).sp
        )
        Spacer(modifier = Modifier.height((10 * scale).dp))
        if (tallies.isEmpty()) {
            Text("No counters yet. Add one below.", color = Color.White, fontSize = (18 * scale).sp)
        }
        for ((i, tally) in tallies.withIndex()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = (6 * scale).dp)
                    .clip(RoundedCornerShape(8.dp)).background(Color(0xFF1A1A1A)).padding((10 * scale).dp)
                    .pointerInput(i) {
                        detectTapGestures(
                            onTap = { save(tallies.mapIndexed { j, t -> if (j == i) t.first to t.second + 1 else t }) },
                            onLongPress = { save(tallies.mapIndexed { j, t -> if (j == i) t.first to (t.second - 1).coerceAtLeast(0) else t }) }
                        )
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = tally.first, color = Color.White, fontSize = (20 * scale).sp, modifier = Modifier.weight(1f))
                Text(text = tally.second.toString(), color = Color(0xFF4CAF50), fontSize = (26 * scale).sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { save(tallies.filterIndexed { j, _ -> j != i }) }) {
                    Text("✕", color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New counter (water, coffee…)", color = Color.White.copy(alpha = 0.6f)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width((8 * scale).dp))
            Button(onClick = {
                if (newName.isNotBlank()) {
                    save(tallies + (newName.trim() to 0))
                    newName = ""
                }
            }) { Text("Add", color = Color.White) }
        }
    }
}

/** Todo List — multiple tasks with checkboxes, hide-completed, count. */
@Composable
fun TodoWidget(scale: Float) {
    val context = LocalContext.current
    val store = remember { WidgetStore(context) }
    var todos by remember { mutableStateOf(store.getTodos()) }
    var newTask by remember { mutableStateOf("") }
    var hideCompleted by remember { mutableStateOf(false) }
    fun save(next: List<Pair<String, Boolean>>) {
        todos = next
        store.setTodos(next)
    }
    val doneCount = todos.count { it.second }
    Column(modifier = Modifier.fillMaxSize().padding((14 * scale).dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Todo List", color = Color.White, fontSize = (24 * scale).sp, fontWeight = FontWeight.Bold)
            Text(
                text = "$doneCount/${todos.size}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (18 * scale).sp
            )
        }
        if (todos.isEmpty()) {
            Text(
                text = "All done — Every task is checked off.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = (18 * scale).sp
            )
        }
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            for ((i, todo) in todos.withIndex()) {
                if (hideCompleted && todo.second) continue
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = (2 * scale).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = todo.second,
                        onCheckedChange = { checked ->
                            save(todos.mapIndexed { j, t -> if (j == i) t.first to checked else t })
                        }
                    )
                    Text(
                        text = todo.first,
                        color = if (todo.second) Color.White.copy(alpha = 0.5f) else Color.White,
                        fontSize = (19 * scale).sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { save(todos.filterIndexed { j, _ -> j != i }) }) {
                        Text("✕", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTask,
                onValueChange = { newTask = it },
                label = { Text("New task", color = Color.White.copy(alpha = 0.6f)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width((8 * scale).dp))
            Button(onClick = {
                if (newTask.isNotBlank()) {
                    save(todos + (newTask.trim() to false))
                    newTask = ""
                }
            }) { Text("Add", color = Color.White) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Hide completed", color = Color.White, fontSize = (16 * scale).sp)
            Switch(checked = hideCompleted, onCheckedChange = { hideCompleted = it })
        }
    }
}

/** Sober Counter — streak tracker with discreet mode and relapse flow. */
@Composable
fun SoberWidget(scale: Float) {
    val context = LocalContext.current
    val store = remember { WidgetStore(context) }
    var label by remember { mutableStateOf(store.getSoberLabel()) }
    var startMillis by remember { mutableStateOf(store.getSoberStartMillis()) }
    var timesPerWeek by remember { mutableStateOf(store.getSoberTimesPerWeek()) }
    var costEach by remember { mutableStateOf(store.getSoberCostEach()) }
    var discreet by remember { mutableStateOf(store.getSoberDiscreet()) }
    var confirmEnd by remember { mutableStateOf(false) }
    var setupMode by remember { mutableStateOf(startMillis < 0) }

    fun saveAll() {
        store.setSober(label, startMillis, timesPerWeek, costEach, discreet)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding((14 * scale).dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (setupMode || startMillis < 0) {
            Text(text = "What are you tracking?", color = Color.White, fontSize = (22 * scale).sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height((10 * scale).dp))
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("e.g. Alcohol free", color = Color.White.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height((8 * scale).dp))
            Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                OutlinedButton(onClick = { timesPerWeek = (timesPerWeek - 1).coerceAtLeast(0) }) { Text("−", color = Color.White) }
                Text(text = "Times per week: $timesPerWeek", color = Color.White, fontSize = (18 * scale).sp)
                OutlinedButton(onClick = { timesPerWeek = (timesPerWeek + 1).coerceAtMost(21) }) { Text("+", color = Color.White) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                OutlinedButton(onClick = { costEach = (costEach - 1).coerceAtLeast(0f) }) { Text("−", color = Color.White) }
                Text(text = "Cost each: $${"%.0f".format(costEach)}", color = Color.White, fontSize = (18 * scale).sp)
                OutlinedButton(onClick = { costEach = (costEach + 1).coerceAtMost(200f) }) { Text("+", color = Color.White) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Discreet mode", color = Color.White, fontSize = (17 * scale).sp)
                Switch(checked = discreet, onCheckedChange = { discreet = it })
            }
            Text(
                text = "Discreet mode hides what you are tracking and the money saved, so a glance at the screen gives nothing away.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = (14 * scale).sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height((10 * scale).dp))
            Button(onClick = {
                startMillis = System.currentTimeMillis()
                saveAll()
                setupMode = false
            }, modifier = Modifier.fillMaxWidth()) { Text("Start streak", color = Color.White) }
            return@Column
        }
        val days = ((System.currentTimeMillis() - startMillis) / 86_400_000L).toInt().coerceAtLeast(0)
        val weeks = days / 7.0
        val saved = if (timesPerWeek > 0 && costEach > 0) weeks * timesPerWeek * costEach else null
        Text(
            text = if (discreet) "Streak" else label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = (20 * scale).sp
        )
        Text(
            text = "$days",
            color = Color(0xFF4CAF50),
            fontSize = (96 * scale).sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = if (days == 1) "day" else "days",
            color = Color.White,
            fontSize = (24 * scale).sp
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Text(
            text = "Tracking since " + InstantToDate(startMillis),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = (16 * scale).sp
        )
        if (!discreet && saved != null) {
            Text(
                text = "Saved so far: $${"%.0f".format(saved)}",
                color = Color.White,
                fontSize = (18 * scale).sp
            )
            Text(
                text = "Commitment: $label · $timesPerWeek per week",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = (16 * scale).sp
            )
        }
        Spacer(modifier = Modifier.height((12 * scale).dp))
        if (confirmEnd) {
            Text(
                text = "End this streak?",
                color = Color.White,
                fontSize = (20 * scale).sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "This streak ends here, and day one starts again now. One hard day does not undo the decision.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = (16 * scale).sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height((8 * scale).dp))
            Row(horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
                Button(onClick = {
                    store.resetSober()
                    startMillis = System.currentTimeMillis()
                    confirmEnd = false
                }) { Text("Restart from now", color = Color.White) }
                OutlinedButton(onClick = { confirmEnd = false }) { Text("Not yet", color = Color.White) }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)) {
                OutlinedButton(onClick = { setupMode = true }) { Text("Edit", color = Color.White) }
                OutlinedButton(onClick = { confirmEnd = true }) { Text("End streak", color = Color.White) }
            }
        }
    }
}

private fun InstantToDate(millis: Long): String {
    val dt = java.time.Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    return dt.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH))
}

/** Protocols Duo — personal rules / manifesto. */
@Composable
fun ProtocolsWidget(scale: Float) {
    val context = LocalContext.current
    val store = remember { WidgetStore(context) }
    var rules by remember { mutableStateOf(store.getProtocols()) }
    var newRule by remember { mutableStateOf("") }
    var rotate by remember { mutableStateOf(false) }
    var rotateIndex by remember { mutableStateOf(0) }
    fun save(next: List<String>) {
        rules = next
        store.setProtocols(next)
    }
    LaunchedEffect(rotate, rules.size) {
        while (rotate && rules.isNotEmpty()) {
            delay(30_000)
            rotateIndex = (rotateIndex + 1) % rules.size
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding((14 * scale).dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Protocols", color = Color.White, fontSize = (24 * scale).sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { rotate = !rotate }) {
                Text(if (rotate) "Manifesto" else "Rotate", color = Color(0xFF4CAF50))
            }
        }
        if (rules.isEmpty()) {
            Text(
                text = "No protocols set for this time of day.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = (18 * scale).sp
            )
            Text(
                text = "Add a rule, e.g. No phone after 10pm.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = (16 * scale).sp
            )
        } else if (rotate) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = rules[rotateIndex % rules.size],
                    color = Color.White,
                    fontSize = (30 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                for ((i, rule) in rules.withIndex()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = (6 * scale).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "•  $rule", color = Color.White, fontSize = (20 * scale).sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { save(rules.filterIndexed { j, _ -> j != i }) }) {
                            Text("✕", color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Text(text = "Suggestions:", color = Color.White.copy(alpha = 0.6f), fontSize = (15 * scale).sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy((6 * scale).dp)
        ) {
            for (suggestion in PROTOCOL_SUGGESTIONS.take(3)) {
                OutlinedButton(onClick = {
                    if (suggestion !in rules) save(rules + suggestion)
                }) { Text(suggestion, color = Color.White, fontSize = (13 * scale).sp) }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newRule,
                onValueChange = { newRule = it },
                label = { Text("Add a rule", color = Color.White.copy(alpha = 0.6f)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width((8 * scale).dp))
            Button(onClick = {
                if (newRule.isNotBlank()) {
                    save(rules + newRule.trim())
                    newRule = ""
                }
            }) { Text("Add", color = Color.White) }
        }
    }
}

/** Countdown Duo — days/hours/minutes until a date. */
@Composable
fun CountdownWidget(scale: Float) {
    val context = LocalContext.current
    val store = remember { WidgetStore(context) }
    var items by remember { mutableStateOf(store.getCountdowns()) }
    var newLabel by remember { mutableStateOf("") }
    var newDays by remember { mutableStateOf(30) }
    fun save(next: List<Pair<String, Long>>) {
        items = next
        store.setCountdowns(next)
    }
    Column(modifier = Modifier.fillMaxSize().padding((14 * scale).dp)) {
        Text(text = "Countdown", color = Color.White, fontSize = (24 * scale).sp, fontWeight = FontWeight.Bold)
        if (items.isEmpty()) {
            Text("No countdowns yet.", color = Color.White.copy(alpha = 0.8f), fontSize = (18 * scale).sp)
        }
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            for ((i, item) in items.withIndex()) {
                val diffMs = item.second - System.currentTimeMillis()
                val text = when {
                    diffMs <= 0 -> "Now"
                    diffMs < 3_600_000L -> "in ${diffMs / 60_000} min"
                    diffMs < 86_400_000L -> "in ${diffMs / 3_600_000}h"
                    else -> "in ${diffMs / 86_400_000} d"
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = (6 * scale).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = item.first, color = Color.White, fontSize = (20 * scale).sp, modifier = Modifier.weight(1f))
                    Text(text = text, color = Color(0xFF4CAF50), fontSize = (22 * scale).sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { save(items.filterIndexed { j, _ -> j != i }) }) {
                        Text("✕", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newLabel,
                onValueChange = { newLabel = it },
                label = { Text("Event name", color = Color.White.copy(alpha = 0.6f)) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width((8 * scale).dp))
            OutlinedButton(onClick = { newDays = (newDays - 1).coerceAtLeast(1) }) { Text("−", color = Color.White) }
            Text(text = "${newDays}d", color = Color.White, fontSize = (18 * scale).sp)
            OutlinedButton(onClick = { newDays = (newDays + 1).coerceAtMost(365) }) { Text("+", color = Color.White) }
            Button(onClick = {
                if (newLabel.isNotBlank()) {
                    val target = System.currentTimeMillis() + newDays * 86_400_000L
                    save(items + (newLabel.trim() to target))
                    newLabel = ""
                }
            }) { Text("Add", color = Color.White) }
        }
    }
}

/** Command Center — focus dashboard: greeting, agenda, audio, battery, weather, countdown. */
@Composable
fun CommandCenterWidget(now: LocalDateTime, scale: Float) {
    val context = LocalContext.current
    val battery = remember { BatteryInfo.read(context) }
    val weather = rememberWeather(context)
    val track by MusicListenerService.currentTrack.collectAsState()
    val events = remember { CalendarRepository.todayEvents(context) }
    val hour = now.hour
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Good night"
    }
    var focus by remember { mutableStateOf<String?>(null) }
    Column(modifier = Modifier.fillMaxSize().padding((14 * scale).dp)) {
        Text(text = greeting, color = Color.White, fontSize = (30 * scale).sp, fontWeight = FontWeight.Bold)
        Text(
            text = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)),
            color = Color.White.copy(alpha = 0.75f),
            fontSize = (19 * scale).sp
        )
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Row(horizontalArrangement = Arrangement.spacedBy((10 * scale).dp)) {
            CommandTile("Agenda", if (events.isEmpty()) "Clear day" else events.first().title, focus == "Agenda", scale, Modifier.weight(1f)) { focus = "Agenda" }
            CommandTile("Audio", track?.title ?: "Audio on standby", focus == "Audio", scale, Modifier.weight(1f)) { focus = "Audio" }
            CommandTile("Battery", "${battery.levelPercent}%${if (battery.charging) " · Charging" else ""}", focus == "Battery", scale, Modifier.weight(1f)) { focus = "Battery" }
            CommandTile("Weather", weather?.let { "%.0f° · ${it.condition}".format(it.temperatureC) } ?: "Loading…", focus == "Weather", scale, Modifier.weight(1f)) { focus = "Weather" }
        }
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Text(
            text = when (focus) {
                "Agenda" -> if (events.isEmpty()) "Clear day — nothing scheduled." else events.joinToString("\n") { "${it.timeText()}  ${it.title}" }
                "Audio" -> track?.let { "Now playing: ${it.title} — ${it.artist}" } ?: "Nothing playing. The soundtrack to your focus."
                "Battery" -> if (battery.charging) "Charging — almost there. You too." else if (battery.levelPercent < 20) "Time to plug in." else "Ready for the day."
                "Weather" -> weather?.let { "High ${it.highC?.let { h -> "%.0f°".format(h) } ?: "–"} · Low ${it.lowC?.let { l -> "%.0f°".format(l) } ?: "–"}" } ?: "Grant location for local weather."
                else -> "Tap a small tile to make it the main focus."
            },
            color = Color.White.copy(alpha = 0.85f),
            fontSize = (18 * scale).sp
        )
    }
}

@Composable
private fun CommandTile(title: String, value: String, selected: Boolean, scale: Float, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFF2E7D32) else Color(0xFF1A1A1A))
            .clickable { onClick() }
            .padding((10 * scale).dp)
    ) {
        Text(text = title, color = Color.White.copy(alpha = 0.6f), fontSize = (15 * scale).sp)
        Text(text = value, color = Color.White, fontSize = (17 * scale).sp, fontWeight = FontWeight.Bold)
    }
}

/** Starship Console — LCARS-style master system display. */
@Composable
fun StarshipWidget(now: LocalDateTime, scale: Float) {
    val context = LocalContext.current
    val battery = remember { BatteryInfo.read(context) }
    val weather = rememberWeather(context)
    val track by MusicListenerService.currentTrack.collectAsState()
    val events = remember { CalendarRepository.todayEvents(context) }
    var mode by remember { mutableStateOf("Chrono") }
    var shipName by remember { mutableStateOf("USS StandBy") }
    val orange = Color(0xFFFF9F00)
    val peach = Color(0xFFCC99CC)
    Column(modifier = Modifier.fillMaxSize().padding((12 * scale).dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width((60 * scale).dp).height((24 * scale).dp).background(orange, RoundedCornerShape(12.dp)))
            Spacer(modifier = Modifier.width((8 * scale).dp))
            Text(text = shipName, color = orange, fontSize = (22 * scale).sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "LCARS", color = peach, fontSize = (18 * scale).sp)
        }
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
            for (m in listOf("Chrono", "Audio", "Power", "Events", "Weather")) {
                OutlinedButton(onClick = { mode = m }) {
                    Text(text = m, color = if (mode == m) orange else Color.White, fontSize = (15 * scale).sp)
                }
            }
        }
        Spacer(modifier = Modifier.height((12 * scale).dp))
        when (mode) {
            "Chrono" -> {
                Text(
                    text = "%02d:%02d:%02d".format(now.hour, now.minute, now.second),
                    color = Color.White,
                    fontSize = (64 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = (20 * scale).sp
                )
            }
            "Audio" -> {
                val t = track
                Text(
                    text = if (t == null) "Standby" else t.title,
                    color = Color.White,
                    fontSize = (24 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = t?.artist ?: "No media channel open",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = (18 * scale).sp
                )
            }
            "Power" -> {
                Text(text = "Cell ${battery.levelPercent}%", color = Color.White, fontSize = (28 * scale).sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (battery.charging) "Charge — reserves rising" else "Reserve — emergency power available",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = (18 * scale).sp
                )
                if (battery.levelPercent < 20) Text(text = "Low power alert — power reserves low", color = Color(0xFFFF5252), fontSize = (18 * scale).sp)
            }
            "Events" -> {
                if (events.isEmpty()) Text(text = "No events scheduled", color = Color.White, fontSize = (20 * scale).sp)
                else for (event in events) Text(text = "${event.timeText()}  ${event.title}", color = Color.White, fontSize = (19 * scale).sp)
            }
            "Weather" -> {
                val w = weather
                if (w != null && w.temperatureC != null) {
                    Text(text = "%.0f° · ${w.condition}".format(w.temperatureC), color = Color.White, fontSize = (26 * scale).sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Rise ${w.sunrise ?: "–"}  ·  Set ${w.sunset ?: "–"}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = (18 * scale).sp
                    )
                } else {
                    Text(text = "Weather offline", color = Color.White, fontSize = (20 * scale).sp)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedTextField(
            value = shipName,
            onValueChange = { shipName = it },
            label = { Text("Ship name", color = Color.White.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text(
            text = "Using $mode mode — tap lit panels to switch.",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = (14 * scale).sp
        )
    }
}

/** Concepts — a tabbed playground of 34 experimental widgets. */
@Composable
fun ConceptsWidget(now: LocalDateTime, scale: Float) {
    var tab by remember { mutableStateOf("Affirmations") }
    Column(modifier = Modifier.fillMaxSize().padding((12 * scale).dp)) {
        Text(text = "Concepts", color = Color.White, fontSize = (24 * scale).sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Experimental concept widgets currently in development.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = (15 * scale).sp
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy((6 * scale).dp)
        ) {
            for (t in CONCEPT_TABS) {
                OutlinedButton(onClick = { tab = t }) {
                    Text(text = t, color = if (tab == t) Color(0xFF4CAF50) else Color.White, fontSize = (14 * scale).sp)
                }
            }
        }
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            ConceptTabContent(tab, now, scale)
        }
    }
}

@Composable
private fun ConceptTabContent(tab: String, now: LocalDateTime, scale: Float) {
    when (tab) {
        "Affirmations" -> ConceptText(listOf(
            "I am capable of more than I know.",
            "I choose calm over chaos.",
            "Today I will be kind to myself."
        ), scale)
        "Binary" -> {
            val bits = "%02d:%02d".format(now.hour, now.minute).map {
                if (it == ':') " : " else it.toString().toIntOrNull()?.let { d -> d.toString(2).padStart(4, '0') } ?: ""
            }
            Text(text = bits.joinToString("  "), color = Color(0xFF76FF03), fontSize = (28 * scale).sp, fontFamily = FontFamily.Monospace)
        }
        "Birthday" -> ConceptText(listOf("Your next birthday is coming — set a countdown in the Countdown widget."), scale)
        "Breathing" -> BreathingConcept(scale)
        "Coin Flip" -> RandomButtonConcept("Flip a coin", { if (Random.nextBoolean()) "Heads" else "Tails" }, scale)
        "Boost" -> ConceptText(listOf(
            "You have a great eye for detail.",
            "Your focus today is impressive.",
            "You make hard things look easy."
        ), scale)
        "Decisions" -> RandomButtonConcept("Decide for me", { if (Random.nextBoolean()) "Yes — go for it" else "No — not this time" }, scale)
        "Dice" -> RandomButtonConcept("Roll the dice", { "You rolled a ${Random.nextInt(1, 7)}" }, scale)
        "Countdown" -> {
            val midnight = LocalDate.now().plusDays(1).atStartOfDay()
            val minutesLeft = java.time.Duration.between(now, midnight).toMinutes()
            Text(text = "${minutesLeft / 60}h ${minutesLeft % 60}m left today", color = Color.White, fontSize = (28 * scale).sp, fontWeight = FontWeight.Bold)
        }
        "Eye Care" -> ConceptText(listOf("20-20-20: every 20 minutes, look at something 20 feet away for 20 seconds."), scale)
        "Tasks" -> Text(text = "Open the Todo List widget for tasks.", color = Color.White, fontSize = (20 * scale).sp)
        "Sounds" -> ConceptText(listOf("Rain", "White noise", "Forest", "Ocean"), scale)
        "Fortune" -> RandomButtonConcept("Open a fortune cookie", {
            listOf(
                "A pleasant surprise is on its way.",
                "Your hard work will pay off soon.",
                "Good things come to those who wait — and work.",
                "Today is a good day to try something new."
            ).random()
        }, scale)
        "Gratitude" -> ConceptText(listOf("Name three good things about today. Say them out loud."), scale)
        "Habits" -> CounterConcept("Days in a row", scale)
        "Heartbeat" -> Text(text = "♥  72 bpm", color = Color(0xFFFF5252), fontSize = (36 * scale).sp, fontWeight = FontWeight.Bold)
        "Hydration" -> CounterConcept("Glasses of water today", scale)
        "Kindness" -> ConceptText(listOf(
            "Text someone you have not spoken to in a while.",
            "Hold the door for a stranger.",
            "Leave a kind note for someone."
        ), scale)
        "Kintsugi" -> ConceptText(listOf("Broken things, mended with gold, become more beautiful than before."), scale)
        "Life Stats" -> {
            val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.of(2000, 1, 1), LocalDate.now())
            Text(text = "$days days since the year 2000", color = Color.White, fontSize = (24 * scale).sp)
        }
        "Math" -> MathConcept(scale)
        "Metronome" -> MetronomeConcept(scale)
        "Mood" -> MoodConcept(scale)
        "Moon" -> {
            val knownNewMoon = LocalDate.of(2000, 1, 6)
            val days = java.time.temporal.ChronoUnit.DAYS.between(knownNewMoon, LocalDate.now())
            val phase = (days % 29.53) / 29.53
            val name = when {
                phase < 0.03 || phase > 0.97 -> "New Moon"
                phase < 0.22 -> "Waxing Crescent"
                phase < 0.28 -> "First Quarter"
                phase < 0.47 -> "Waxing Gibbous"
                phase < 0.53 -> "Full Moon"
                phase < 0.72 -> "Waning Gibbous"
                phase < 0.78 -> "Last Quarter"
                else -> "Waning Crescent"
            }
            Text(text = "Moon: $name", color = Color.White, fontSize = (26 * scale).sp)
        }
        "Now" -> Text(
            text = "%02d:%02d:%02d".format(now.hour, now.minute, now.second),
            color = Color.White,
            fontSize = (48 * scale).sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        "Posture" -> ConceptText(listOf("Sit tall. Shoulders back. Chin level. Breathe."), scale)
        "Time Left" -> {
            val midnight = LocalDate.now().plusDays(1).atStartOfDay()
            val minutesLeft = java.time.Duration.between(now, midnight).toMinutes()
            Text(text = "${minutesLeft / 60}h ${minutesLeft % 60}m left today", color = Color.White, fontSize = (28 * scale).sp, fontWeight = FontWeight.Bold)
        }
        "Stopwatch" -> StopwatchWidget(scale * 0.8f)
        "Calendar" -> {
            Text(
                text = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)),
                color = Color.White,
                fontSize = (24 * scale).sp
            )
        }
        "Tiles" -> TilesConcept(now, scale)
        "Xbox Music" -> {
            val track by MusicListenerService.currentTrack.collectAsState()
            Text(
                text = track?.let { "${it.title} — ${it.artist}" } ?: "Nothing playing",
                color = Color.White,
                fontSize = (22 * scale).sp,
                textAlign = TextAlign.Center
            )
        }
        "News" -> ConceptText(listOf("News is not available offline in this build."), scale)
        "Weather" -> {
            val context = LocalContext.current
            val weather = rememberWeather(context)
            Text(
                text = weather?.let { "%.0f° · ${it.condition}".format(it.temperatureC) } ?: "Loading weather…",
                color = Color.White,
                fontSize = (26 * scale).sp
            )
        }
        "World Clock" -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Phnom Penh " + com.naomi.standby.util.timeInZone("Asia/Phnom_Penh", now), color = Color.White, fontSize = (22 * scale).sp)
                Text(text = "London " + com.naomi.standby.util.timeInZone("Europe/London", now), color = Color.White, fontSize = (22 * scale).sp)
                Text(text = "New York " + com.naomi.standby.util.timeInZone("America/New_York", now), color = Color.White, fontSize = (22 * scale).sp)
            }
        }
        else -> Text(text = tab, color = Color.White, fontSize = (24 * scale).sp)
    }
}

@Composable
private fun ConceptText(options: List<String>, scale: Float) {
    var index by remember { mutableStateOf(0) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = options[index % options.size],
            color = Color.White,
            fontSize = (24 * scale).sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height((12 * scale).dp))
        OutlinedButton(onClick = { index = (index + 1) % options.size }) {
            Text("Next", color = Color.White)
        }
    }
}

@Composable
private fun RandomButtonConcept(label: String, roll: () -> String, scale: Float) {
    var result by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (result.isNotEmpty()) {
            Text(text = result, color = Color.White, fontSize = (28 * scale).sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height((12 * scale).dp))
        }
        Button(onClick = { result = roll() }) { Text(label, color = Color.White) }
    }
}

@Composable
private fun CounterConcept(label: String, scale: Float) {
    var count by remember { mutableStateOf(0) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), color = Color(0xFF4CAF50), fontSize = (64 * scale).sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = Color.White, fontSize = (20 * scale).sp)
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Button(onClick = { count++ }) { Text("+1", color = Color.White) }
    }
}

@Composable
private fun BreathingConcept(scale: Float) {
    var phase by remember { mutableStateOf("Breathe in…") }
    LaunchedEffect(Unit) {
        while (true) {
            phase = "Breathe in…"
            delay(4000)
            phase = "Hold…"
            delay(4000)
            phase = "Breathe out…"
            delay(4000)
        }
    }
    Text(text = phase, color = Color.White, fontSize = (32 * scale).sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun MathConcept(scale: Float) {
    var a by remember { mutableStateOf(Random.nextInt(2, 13)) }
    var b by remember { mutableStateOf(Random.nextInt(2, 13)) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$a × $b = ?", color = Color.White, fontSize = (32 * scale).sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height((8 * scale).dp))
        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it.filter { c -> c.isDigit() } },
            label = { Text("Answer", color = Color.White.copy(alpha = 0.6f)) },
            singleLine = true
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Button(onClick = {
            if (answer.toIntOrNull() == a * b) {
                feedback = "Correct!"
                a = Random.nextInt(2, 13)
                b = Random.nextInt(2, 13)
                answer = ""
            } else {
                feedback = "Try again."
            }
        }) { Text("Check", color = Color.White) }
        if (feedback.isNotEmpty()) {
            Text(text = feedback, color = Color(0xFF4CAF50), fontSize = (18 * scale).sp)
        }
    }
}

@Composable
private fun MetronomeConcept(scale: Float) {
    var bpm by remember { mutableStateOf(60) }
    var running by remember { mutableStateOf(false) }
    var beat by remember { mutableStateOf(false) }
    LaunchedEffect(running, bpm) {
        while (running) {
            beat = !beat
            delay(60_000L / bpm)
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$bpm bpm", color = if (beat) Color(0xFF4CAF50) else Color.White, fontSize = (40 * scale).sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
            OutlinedButton(onClick = { bpm = (bpm - 5).coerceAtLeast(30) }) { Text("−", color = Color.White) }
            Button(onClick = { running = !running }) { Text(if (running) "Stop" else "Start", color = Color.White) }
            OutlinedButton(onClick = { bpm = (bpm + 5).coerceAtMost(240) }) { Text("+", color = Color.White) }
        }
    }
}

@Composable
private fun MoodConcept(scale: Float) {
    var mood by remember { mutableStateOf<String?>(null) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "How are you feeling?", color = Color.White, fontSize = (22 * scale).sp)
        Spacer(modifier = Modifier.height((10 * scale).dp))
        Row(horizontalArrangement = Arrangement.spacedBy((10 * scale).dp)) {
            for (m in listOf("😀", "🙂", "😐", "😟", "😢")) {
                Text(
                    text = m,
                    fontSize = (36 * scale).sp,
                    modifier = Modifier.clickable { mood = m }
                )
            }
        }
        if (mood != null) {
            Text(text = "Noted: $mood", color = Color(0xFF4CAF50), fontSize = (18 * scale).sp)
        }
    }
}

@Composable
private fun TilesConcept(now: LocalDateTime, scale: Float) {
    val context = LocalContext.current
    val battery = remember { BatteryInfo.read(context) }
    Row(horizontalArrangement = Arrangement.spacedBy((10 * scale).dp)) {
        Box(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFF1A1A1A)).padding((12 * scale).dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "%02d:%02d".format(now.hour, now.minute), color = Color.White, fontSize = (28 * scale).sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFF1A1A1A)).padding((12 * scale).dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "${battery.levelPercent}%", color = Color.White, fontSize = (28 * scale).sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFF1A1A1A)).padding((12 * scale).dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = now.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)), color = Color.White, fontSize = (28 * scale).sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** Game Simon — 4 coloured pads; watch the sequence, then repeat it. */
@Composable
fun SimonWidget(scale: Float) {
    val colors = listOf(Color(0xFF4CAF50), Color(0xFFF44336), Color(0xFFFFEB3B), Color(0xFF2196F3))
    var sequence by remember { mutableStateOf<List<Int>>(emptyList()) }
    var showing by remember { mutableStateOf(false) }
    var litPad by remember { mutableStateOf(-1) }
    var inputIndex by remember { mutableStateOf(0) }
    var round by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("Watch Carefully") }
    var started by remember { mutableStateOf(false) }

    fun playSequence(seq: List<Int>) {
        showing = true
        message = "Watch Carefully"
    }

    LaunchedEffect(sequence, showing) {
        if (showing && sequence.isNotEmpty()) {
            delay(600)
            for (pad in sequence) {
                litPad = pad
                delay(500)
                litPad = -1
                delay(200)
            }
            showing = false
            inputIndex = 0
            message = "Your Turn"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding((14 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Simon", color = Color.White, fontSize = (26 * scale).sp, fontWeight = FontWeight.Bold)
        Text(text = message, color = Color.White.copy(alpha = 0.8f), fontSize = (20 * scale).sp)
        Text(text = "Round $round", color = Color.White.copy(alpha = 0.6f), fontSize = (17 * scale).sp)
        Spacer(modifier = Modifier.height((12 * scale).dp))
        Column(verticalArrangement = Arrangement.spacedBy((10 * scale).dp)) {
            for (row in 0 until 2) {
                Row(horizontalArrangement = Arrangement.spacedBy((10 * scale).dp)) {
                    for (col in 0 until 2) {
                        val pad = row * 2 + col
                        Box(
                            modifier = Modifier.size((90 * scale).dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (litPad == pad) colors[pad] else colors[pad].copy(alpha = 0.45f))
                                .clickable(enabled = started && !showing) {
                                    if (sequence.isEmpty()) return@clickable
                                    litPad = pad
                                    if (pad == sequence[inputIndex]) {
                                        inputIndex++
                                        if (inputIndex >= sequence.size) {
                                            inputIndex = 0
                                            round++
                                            sequence = sequence + Random.nextInt(4)
                                            playSequence(sequence)
                                        }
                                    } else {
                                        message = "Game over — Round $round"
                                        started = false
                                        sequence = emptyList()
                                        round = 0
                                    }
                                    litPad = -1
                                }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height((12 * scale).dp))
        if (!started) {
            Button(onClick = {
                started = true
                round = 1
                sequence = listOf(Random.nextInt(4))
                playSequence(sequence)
            }) { Text("Start game", color = Color.White) }
        }
    }
}

/** Quick App Launch — grid of installed apps; tap to open. */
@Composable
fun QuickAppLaunchWidget(context: Context, scale: Float) {
    val apps = remember {
        try {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            @Suppress("DEPRECATION")
            val infos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            infos.map { it.loadLabel(pm).toString() to it.activityInfo.packageName }
                .distinctBy { it.second }
                .sortedBy { it.first.lowercase() }
        } catch (_: Exception) {
            emptyList()
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding((12 * scale).dp)) {
        Text(text = "Quick App Launch", color = Color.White, fontSize = (24 * scale).sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Tap an app to open it.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = (16 * scale).sp
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            for ((label, pkg) in apps) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = (4 * scale).dp)
                        .clip(RoundedCornerShape(8.dp)).background(Color(0xFF1A1A1A)).padding((10 * scale).dp)
                        .clickable {
                            try {
                                context.packageManager.getLaunchIntentForPackage(pkg)?.let {
                                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(it)
                                }
                            } catch (_: Exception) {
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, color = Color.White, fontSize = (19 * scale).sp)
                }
            }
            if (apps.isEmpty()) {
                Text("No launchable apps found.", color = Color.White, fontSize = (18 * scale).sp)
            }
        }
    }
}

/** Widget Rotator — cycles through a list of widgets on an interval. */
@Composable
fun WidgetRotatorWidget(now: LocalDateTime, scale: Float, onOpenScreen: (ScreenId) -> Unit) {
    val screens = listOf(
        ScreenId.BIG_CROP_CLOCK_DUO,
        ScreenId.WEATHER_DUO,
        ScreenId.CALENDAR,
        ScreenId.PLAYER_DUO
    )
    var index by remember { mutableStateOf(0) }
    var intervalSeconds by remember { mutableStateOf(30) }
    LaunchedEffect(intervalSeconds) {
        while (true) {
            delay(intervalSeconds * 1000L)
            index = (index + 1) % screens.size
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().padding((12 * scale).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { index = (index - 1 + screens.size) % screens.size }) {
                Text("‹", color = Color.White, fontSize = (28 * scale).sp)
            }
            Text(
                text = screens[index].displayName,
                color = Color.White,
                fontSize = (20 * scale).sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            TextButton(onClick = { index = (index + 1) % screens.size }) {
                Text("›", color = Color.White, fontSize = (28 * scale).sp)
            }
        }
        Text(
            text = "Tap the left edge to switch manually. Rotates every ${intervalSeconds}s.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = (15 * scale).sp,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
            for (seconds in listOf(15, 30, 60)) {
                OutlinedButton(onClick = { intervalSeconds = seconds }) {
                    Text(
                        text = "${seconds}s",
                        color = if (intervalSeconds == seconds) Color(0xFF4CAF50) else Color.White,
                        fontSize = (15 * scale).sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height((8 * scale).dp))
        OutlinedButton(onClick = { onOpenScreen(screens[index]) }) {
            Text("Open ${screens[index].displayName}", color = Color.White)
        }
    }
}

/** Alarms — local alarm list + editor with snooze. */
@Composable
fun AlarmsWidget(scale: Float) {
    val context = LocalContext.current
    val store = remember { AlarmStore(context) }
    var alarms by remember { mutableStateOf(store.getAlarms()) }
    var newHour by remember { mutableStateOf(7) }
    var newMinute by remember { mutableStateOf(0) }
    var newMessage by remember { mutableStateOf("") }
    fun save(next: List<AlarmItem>) {
        alarms = next
        store.setAlarms(next)
        next.forEachIndexed { i, alarm ->
            if (alarm.enabled) AlarmStore.schedule(context, i, alarm) else AlarmStore.cancel(context, i)
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding((14 * scale).dp)) {
        Text(text = "Alarms", color = Color.White, fontSize = (24 * scale).sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Allow exact alarms so this alarm rings on time, even in Doze.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = (15 * scale).sp
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        if (alarms.isEmpty()) {
            Text("No alarms set.", color = Color.White, fontSize = (18 * scale).sp)
        }
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            for ((i, alarm) in alarms.withIndex()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = (6 * scale).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alarm.timeText(),
                            color = Color.White,
                            fontSize = (28 * scale).sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = alarm.daysText() + if (alarm.message.isNotBlank()) " · ${alarm.message}" else "",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = (16 * scale).sp
                        )
                    }
                    Switch(
                        checked = alarm.enabled,
                        onCheckedChange = { enabled ->
                            save(alarms.mapIndexed { j, a -> if (j == i) a.copy(enabled = enabled) else a })
                        }
                    )
                    TextButton(onClick = {
                        AlarmStore.cancel(context, i)
                        save(alarms.filterIndexed { j, _ -> j != i })
                    }) { Text("✕", color = Color.White.copy(alpha = 0.7f)) }
                }
            }
        }
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((6 * scale).dp)) {
            OutlinedButton(onClick = { newHour = (newHour - 1 + 24) % 24 }) { Text("−", color = Color.White) }
            Text(text = "%02d:%02d".format(newHour, newMinute), color = Color.White, fontSize = (24 * scale).sp, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { newHour = (newHour + 1) % 24 }) { Text("+", color = Color.White) }
            OutlinedButton(onClick = { newMinute = (newMinute - 15 + 60) % 60 }) { Text("−15", color = Color.White) }
            OutlinedButton(onClick = { newMinute = (newMinute + 15) % 60 }) { Text("+15", color = Color.White) }
        }
        OutlinedTextField(
            value = newMessage,
            onValueChange = { newMessage = it },
            label = { Text("A note for tomorrow morning (optional)", color = Color.White.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height((8 * scale).dp))
        Button(
            onClick = {
                save(alarms + AlarmItem(newHour, newMinute, 0, newMessage.trim(), true))
                newMessage = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Create alarm", color = Color.White) }
    }
}
