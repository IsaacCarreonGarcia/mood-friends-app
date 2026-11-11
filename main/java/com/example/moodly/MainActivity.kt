package com.example.moodly

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Shows the first screen of the app, where the user registers their mood
class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        // setContent tells Compose what to show on the screen
        setContent{
            MaterialTheme{
                Surface(modifier = Modifier.fillMaxSize()){
                    HomeScreen()
                }
            }
        }
    }
}

// First Layout: Home Screen (a column with a row of mood boxes inside)
@Composable
fun HomeScreen() {
    // context is required to start other activities
    val context = LocalContext.current

    /* State: when the values changes, Compose redraws the screen
    selectedIndex is the position of the chosen mood in moodOptions (-1 means none chosen) */
    var selectedIndex by remember { mutableStateOf(-1) }
    var note by remember { mutableStateOf("") }

    // The following two lines tells us if the Save button is being pressed
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()  //it keeps the content away from the status bar
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // the screen can scroll if it is too small
    ) {
        Text(text = "Moodly", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(text = "How are you feeling today?")

        Spacer(modifier = Modifier.height(16.dp))

        // A row puts its children side by side: one box for each mood
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            //for in range, like in the Kotlin slides
            for (i in 0..moodOptions.size - 1) {
                val mood = moodOptions[i]

                // The chosen box has a different color, so the user sees the feedback
                val boxColor = if (i == selectedIndex) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }

                // A box with a clickable modifier, once tap it changes its state
                Box(
                    modifier = Modifier
                        .weight(1f) //all boxes have the same width
                        .background(boxColor)
                        .clickable { selectedIndex = i }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = mood.emoji, fontSize = 28.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedIndex == -1) {
            Text(text = "Tap an emoji to choose your mood")
        } else {
            Text(
                text = "You chose: ${moodOptions[selectedIndex].emoji} ${moodOptions[selectedIndex].label}"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        //Text box for an optional note
        OutlinedTextField(
            value = note,
            onValueChange = { note = it }, // saves the new text in the state
            label = { Text("Add a note") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Save button. Its text changed while user is pressing it (UI feedback)
        Button(
            onClick = {
                if (selectedIndex != -1) {
                    val mood = moodOptions[selectedIndex]
                    // Add the new mood at the start of the history
                    moodHistory.add(0, MoodEntry(mood.emoji, mood.label, note, 0))
                    myMood = mood.emoji + " " + mood.label
                    note = ""
                    Toast.makeText(context, "Mood saved", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = selectedIndex != -1, // disabled until a mood is chosen
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (isPressed) "Saving..." else "Save my mood")
        }

        Spacer(modifier = Modifier.height(8.dp))

        /* we only say "send this text" and Android lets the user
        choose an app that can do it (WhatsApp, messages, email...)
         */
        OutlinedButton(
            onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "I'm feeling $myMood today! (sent from Moodly)"
                )
                startSafely(context, Intent.createChooser(shareIntent, "Share my mood"))
            },
            enabled = myMood != "", //only works after a mood was saved
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Share my mood")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "My mood history", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        // Three buttons side by side: Week, Month, Year
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (period in listOf("Week", "Month", "Year")) {
                OutlinedButton(
                    onClick = {
                        // we tell Android exactly which activity to open
                        val intent = Intent(context, HistoryActivity::class.java)
                        intent.putExtra("PERIOD", period)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                )  {
                    Text(text = period)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        //card that opens the circle screen
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // EXPLICIT INTENT again, this time without extra data
                    context.startActivity(Intent(context, CircleActivity::class.java))
                }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "👨‍👩‍👧 My Circle", fontWeight = FontWeight.Bold)
                Text(text = "See the mood of your friends and family")
            }
        }
    }
}