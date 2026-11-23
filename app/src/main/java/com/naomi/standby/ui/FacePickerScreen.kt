package com.naomi.standby.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naomi.standby.data.SettingsStore
import com.naomi.standby.model.FaceRegistry
import com.naomi.standby.ui.faces.ClockFace
import java.time.LocalDateTime

/**
 * Face picker: every face in the catalogue with a live preview.
 * Previews use a fixed time (10:09) so 144 ticking clocks do not
 * all re-compose every second.
 */
@Composable
fun FacePickerScreen(
    settings: SettingsStore,
    onBack: () -> Unit,
    onFaceSelected: () -> Unit
) {
    val previewTime = LocalDateTime.of(2026, 11, 23, 10, 9, 30)
    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(12.dp)) {
        Text(
            text = "Clock faces — ${FaceRegistry.count}",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "12 designs × 6 colours × 2 sizes. Tap one to use it.",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = 12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(FaceRegistry.allFaces) { face ->
                val selected = face.id == settings.faceId
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) Color(0xFF222222) else Color(0xFF111111))
                        .clickable {
                            settings.setFaceId(face.id)
                            onFaceSelected()
                        }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Preview at half scale so it fits the card.
                        ClockFace(entry = face, now = previewTime, fontScale = 0.45f)
                    }
                    Text(
                        text = face.name,
                        color = if (selected) Color(0xFF4CAF50) else Color.White,
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back", color = Color.White)
        }
    }
}
