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
The data is saved on the phone, so it is still there after closing the app
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

// One saved mood in the history
// savedAt is the exact time it was saved (in milliseconds, from System.currentTimmeMillis())
class MoodEntry(val emoji: String, val label: String, val note: String, val savedAt: Long){

    // Members function: how many calendar days ago the mood was saved, 0 = today
    fun daysAgo(): Int{
        val now = System.currentTimeMillis()
        //the offset moves the time to the phone's time zone, so "today" remains correct
        val offset = java.util.TimeZone.getDefault().getOffset(now)
        val dayLength = 86400000L // milliseconds in one day
        return ((now + offset) / dayLength - (savedAt + offset) / dayLength).toInt()
    }
}

// One person in the circle (friends and family)
class Member(val name: String, val group: String, val emoji: String, val label: String, val phone: String)

/*
These lists work like mutableListOf, but Compose watches them,
the screen updates by itself when we add or remove anything.
  */
val moodHistory = mutableStateListOf<MoodEntry>()
val circleMembers = mutableStateListOf<Member>()

// The last mood the user saved, for example "Happy". Empty text means nothing saved yet.
var myMood by mutableStateOf("")
// true after the saved data was read from the phone
var dataLoaded = false

// Turns the number of days into a nice text. Uses "when" like in the slides.
fun daysText(daysAgo: Int): String {
    return when (daysAgo) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> "$daysAgo days ago"
    }
}

// removes the character used to separate data, so the saved text is never broken
fun cleanText(text: String): String{
    return text.replace("|", " ").replace("\n", " ").trim()
}

// Example data for the very first time the app runs, so history and circle are not empty
fun addSampleData() {
    val now = System.currentTimeMillis()
    val day = 86400000L

    moodHistory.clear()
    moodHistory.add(MoodEntry("🙂", "Good", "Coffee with friends", now - 1 * day))
    moodHistory.add(MoodEntry("😐", "Okay", "", now - 2 * day))
    moodHistory.add(MoodEntry("😄", "Happy", "Finished my project!", now - 3 * day))
    moodHistory.add(MoodEntry("😔", "Sad", "Tired after exams", now - 5 * day))
    moodHistory.add(MoodEntry("🙂", "Good", "", now - 9 * day))
    moodHistory.add(MoodEntry("😡", "Angry", "Missed the bus", now - 20 * day))
    moodHistory.add(MoodEntry("😄", "Happy", "Weekend trip", now - 45 * day))
    moodHistory.add(MoodEntry("😐", "Okay", "", now - 120 * day))
    moodHistory.add(MoodEntry("🙂", "Good", "", now - 300 * day))

    circleMembers.clear()
    circleMembers.add(Member("Mum", "Family", "😄", "Happy", ""))
    circleMembers.add(Member("Dad", "Family", "😐", "Okay", ""))
    circleMembers.add(Member("Kazzi", "Friend", "🙂", "Good", ""))
    circleMembers.add(Member("Osvaldo", "Friend", "😔", "Sad", ""))

}

/*
SharedPreferences saves everything on the phone.
Turning each list into one log text, and item per line and "|" between its parts
*/
fun saveData(context: Context) {
    var historyText = ""
    for (entry in moodHistory) {
    historyText = historyText + entry.emoji + "|" + entry.label + "|" +
        cleanText(entry.note) + "|" + entry.savedAt + "\n"
    }

    var circleText = ""
    for (member in circleMembers) {
        circleText = circleText + cleanText(member.name) + "|" + member.group + "|" +
        member.emoji + "|" + member.label + "|" + cleanText(member.phone) + "\n"
    }

    val prefs = context.getSharedPreferences("moodly", Context.MODE_PRIVATE)
    prefs.edit()
        .putString("history", historyText)
        .putString("circle", circleText)
        .putString("myMood", myMood)
        .apply()

}

/*
Reads the save date from the phone, Evry screen calls this in OnCreate
Data is only read the first time
*/
fun loadData(context : Context) {
    if (dataLoaded) {
        return
    }
    dataLoaded = true

    val prefs = context.getSharedPreferences("moodly", Context.MODE_PRIVATE)
    val historyText = prefs.getString("history", null)

    // nulls means that nothing was saved, for the first time tha app runs
    if (historyText == null) {
        addSampleData()
        saveData(context)
        return
    }

    myMood = prefs.getString("myMood", "") ?: ""

    moodHistory.clear()
    for(line in historyText.split("\n")) {
        val parts = line.split("|")
        if (parts.size == 4) {
            moodHistory.add(MoodEntry(parts[0], parts[1], parts[2], parts[3].toLongOrNull() ?:0L))
        }
    }

    circleMembers.clear()
    val circleText = prefs.getString("circle" , "") ?: ""
    for (line in circleText.split("\n")) {
        val parts = line.split("|")
        if (parts.size == 5){
            circleMembers.add(Member(parts[0], parts[1], parts[2], parts[3], parts[4]))
        }
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