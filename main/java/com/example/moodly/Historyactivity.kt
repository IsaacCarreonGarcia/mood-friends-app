package com.example.moodly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Second screen: the history of moods for the last week, month or year.
class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        Read the extra data that MainActivity sent with putExtra
         The ?: means "if nothing was sent, use Week" (null safety).
         */


        val startPeriod = intent.getStringExtra("PERIOD") ?: "Week"

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // finish() closes this screen and goes back to the previous one
                    HistoryScreen(startPeriod = startPeriod, onBack = { finish() })
                }
            }
        }
    }
}

// LAYOUT 2: History screen (a Row of filter buttons on top and a scrolling LazyColumn below)
@Composable
fun HistoryScreen(startPeriod: String, onBack: () -> Unit) {
    // State: the period the user is looking at
    var period by remember { mutableStateOf(startPeriod) }

    // "when" turns the period into a number of days
    val maxDays = when (period) {
        "Week" -> 7
        "Month" -> 30
        else -> 365
    }

    // Build a new list with only the moods that are inside the chosen period
    val shown = mutableListOf<MoodEntry>()
    for (entry in moodHistory) {
        if (entry.daysAgo < maxDays) {
            shown.add(entry)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        Text(text = "My mood history", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))

        // Three buttons side by side. The chosen one is filled, the others only have a border.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (p in listOf("Week", "Month", "Year")) {
                if (p == period) {
                    Button(onClick = { period = p }, modifier = Modifier.weight(1f)) {
                        Text(text = p)
                    }
                } else {
                    OutlinedButton(onClick = { period = p }, modifier = Modifier.weight(1f)) {
                        Text(text = p)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Moods saved (last $period): ${shown.size}")

        Spacer(modifier = Modifier.height(8.dp))

        if (shown.size == 0) {
            Text(text = "No moods saved in this period yet")
        }

        // LazyColumn: a scrolling list that only draws the items we can see
        LazyColumn(
            modifier = Modifier.weight(1f),   // takes all the free space above the Back button
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shown) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = entry.emoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = entry.label, fontWeight = FontWeight.Bold)
                            if (entry.note != "") {
                                Text(text = entry.note)
                            }
                        }
                        Text(text = daysText(entry.daysAgo))
                    }
                }
            }
        }

        TextButton(onClick = onBack) {
            Text(text = "Back")
        }
    }
}