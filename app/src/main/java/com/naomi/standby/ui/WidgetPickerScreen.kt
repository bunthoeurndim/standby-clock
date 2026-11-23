package com.naomi.standby.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.DuoSlot
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.model.ScreenCatalog
import com.naomi.standby.model.ScreenCategory
import com.naomi.standby.model.ScreenId

/**
 * Widget picker (v2): "Add a widget" / "Replace a widget".
 * Search by name, category or keywords; category tabs + "All";
 * result count; each screen shows its name and explanation.
 */
@Composable
fun WidgetPickerScreen(
    settings: SettingsStore,
    slot: DuoSlot,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<ScreenCategory?>(null) }
    val results = remember(query, category) {
        val found = ScreenCatalog.search(query)
        if (category == null) found else found.filter { it.category == category }
    }
    val categories = ScreenCategory.entries.filter {
        it != ScreenCategory.UNDEFINED && ScreenCatalog.pickerScreens.any { s -> s.category == it }
    }
    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        Text(
            text = if (slot == DuoSlot.LEFT) "Add a widget — left" else "Add a widget — right",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose a category or search by name.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search widgets or categories", color = Color.White.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { category = null }) {
                Text("All", color = if (category == null) Color(0xFF4CAF50) else Color.White)
            }
            Spacer(modifier = Modifier.width(6.dp))
            for (cat in categories) {
                OutlinedButton(onClick = { category = cat }) {
                    Text(
                        cat.displayName,
                        color = if (category == cat) Color(0xFF4CAF50) else Color.White,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${results.size} widgets",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                if (slot == DuoSlot.LEFT) settings.setLeftScreenId("") else settings.setRightScreenId("")
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Remove widget from this slot", color = Color.White) }
        Spacer(modifier = Modifier.height(8.dp))
        if (results.isEmpty()) {
            Text("No widgets found", color = Color.White, fontSize = 18.sp)
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(results) { screen ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF111111))
                        .clickable {
                            if (slot == DuoSlot.LEFT) settings.setLeftScreenId(screen.id)
                            else settings.setRightScreenId(screen.id)
                            onBack()
                        }
                        .padding(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = screen.displayName,
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = screen.category.displayName,
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = screen.explanation,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 15.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel", color = Color.White)
        }
    }
}
