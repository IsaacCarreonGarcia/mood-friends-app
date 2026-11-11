package com.example.moodly

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/*
This file keeps the data that all the screens share.
It is only temporary: the data is lost when the app is closed.
In Milestone 2 we will save it for real.
 */


// A simple class with 2 properties: one mood (emoji + word)
class Mood(val emoji: String, val label: String)

// The 5 moods the user can choose from
val moodOptions = listOf(
    Mood("😄", "Happy"),
    Mood("🙂", "Good"),
    Mood("😐", "Okay"),
    Mood("😔", "Sad"),
    Mood("😡", "Angry")
)

// One saved mood in the history. daysAgo = 0 means today.
class MoodEntry(val emoji: String, val label: String, val note: String, val daysAgo: Int)

// One person in the circle (friends and family)
class Member(val name: String, val group: String, val emoji: String, val label: String)

/*
The mood history. It works like mutableListOf, but Compose watches it,
 so the screen updates by itself when we add something.
 The first items are example (mock) data so the history is not empty
  */
val moodHistory = mutableStateListOf(
    MoodEntry("🙂", "Good", "Coffee with friends", 1),
    MoodEntry("😐", "Okay", "", 2),
    MoodEntry("😄", "Happy", "Finished my project!", 3),
    MoodEntry("😔", "Sad", "Tired after exams", 5),
    MoodEntry("🙂", "Good", "", 9),
    MoodEntry("😡", "Angry", "Missed the bus", 20),
    MoodEntry("😄", "Happy", "Weekend trip", 45),
    MoodEntry("😐", "Okay", "", 120),
    MoodEntry("🙂", "Good", "", 300)
)

// The circle of friends and family (mock people, not real)
val circleMembers = mutableStateListOf(
    Member("Mum", "Family", "😄", "Happy"),
    Member("Dad", "Family", "😐", "Okay"),
    Member("Kazzi", "Friend", "🙂", "Good"),
    Member("Osvaldo", "Friend", "😔", "Sad")
)

// The last mood the user saved, for example "😄 Happy". Empty text means nothing saved yet.
var myMood by mutableStateOf("")

// Turns the number of days into a nice text. Uses "when" like in the slides.
fun daysText(daysAgo: Int): String {
    return when (daysAgo) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> "$daysAgo days ago"
    }
}

// Starts an intent. If no app can handle it, we catch the exception and show a message
// instead of crashing. All the screens use this function.
fun startSafely(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to do this", Toast.LENGTH_SHORT).show()
    }
}