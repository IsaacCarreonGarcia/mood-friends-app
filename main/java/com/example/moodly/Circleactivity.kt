package com.example.moodly

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
Third screen: The circle of friends and family and how they feel,
The people are saved on the phone. We have no server yet, so the mood of each
person is set by hand with the "Change mood" button.
 */
class CircleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        loadData(this)
        setContent{
            MaterialTheme{
                Surface(modifier = Modifier.fillMaxSize()){
                    CircleScreen(onBack = {finish() })
                }
            }
        }
    }
}

// Third layout: Circle screen
@Composable
fun CircleScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    //State for the small "add person" form
    var showForm by remember {mutableStateOf(false)}
    var newName by remember {mutableStateOf("")}
    var newPhone by remember { mutableStateOf("") }
    var newGroup by remember {mutableStateOf("Family")}

    //A box puts its children on top of each other
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ){
        //First child: everything that's on the screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            Text(text = "My Circle", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(text = "Saved on this phone. Set the mood of your people by hand")

            Spacer(modifier = Modifier.height(12.dp))

            // the form only shows when showForm is true
            if (showForm) {
                Card(modifier = Modifier.fillMaxWidth()){
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = {newName = it},
                            label = { Text ("Name")},
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newPhone,
                            onValueChange = { newPhone = it },
                            label = {Text ("Phone (optional, for the Call button)")},
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        //Tap this button to change between Family and Friend
                        OutlinedButton(
                            onClick = {
                                newGroup = if (newGroup == "Family") "Friend" else "Family"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ){
                            Text(text = "Group: $newGroup (Tap to change)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                if (newName.isBlank()){
                                    Toast.makeText(context, "Please write a name",
                                        Toast.LENGTH_SHORT).show()
                                } else {
                                    // New people start with a neutral mood
                                    circleMembers.add(
                                        Member(cleanText(newName), newGroup, "😐", "Okay", cleanText(newPhone))
                                    )
                                    saveData(context) // save the change on the phone
                                    newName = ""
                                    newPhone = ""
                                    showForm = false
                                }
                            }) {
                                Text(text = "Add")
                            }
                            OutlinedButton(onClick = {showForm = false}) {
                                Text(text = "Cancel")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // The user's mood
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(text = "You", fontWeight = FontWeight.Bold)
                    Text(text = if (myMood == "") "No mood saved yet" else myMood)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (circleMembers.size ==0) {
                Text(text = "your circle is empty. Tap + Add to add a person.")
            }

            //The list of friends and family
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                items(circleMembers) { member ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = member.emoji, fontSize = 32.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column{
                                    Text(text = member.name, fontWeight = FontWeight.Bold)
                                    Text(text = "${member.group} - ${member.label}")
                                }
                            }

                            Row{
                                //Moves this person to the next mood in the list
                                TextButton(onClick = {
                                    var nextIndex = 0
                                    for (i in 0..moodOptions.size -1) {
                                        if (moodOptions[i].label == member.label) {
                                            nextIndex = i + 1
                                        }
                                    }
                                    if (nextIndex >= moodOptions.size) {
                                        nextIndex = 0 // after the last mood we go back to the first
                                    }
                                    val next = moodOptions[nextIndex]
                                    val position = circleMembers.indexOf(member)
                                    circleMembers[position] =
                                        Member(member.name, member.group, next.emoji, next.label, member.phone)
                                    saveData(context)
                                }) {
                                    Text(text = "Change mood")
                                }

                                /*
                                IMPLICIT INTENT: open the phone dialer with this person's number.
                                it will b disable if there is no number
                                 */
                                TextButton(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + member.phone))
                                        startSafely(context, dialIntent)
                                    },
                                    enabled = member.phone !=""
                                ) {
                                    Text(text = "Call")
                                }

                                // Removes this person and save the change on the phone
                                TextButton(onClick = {
                                    circleMembers.remove(member)
                                    saveData(context)
                                }) {
                                    Text(text = "Remove")
                                }
                            }
                        }
                    }
                }
            }

            TextButton(onClick = onBack) {
                Text(text = "Back")
            }
        }

        // Second Child: drawn on the top of the first one, in the bottom right corner
        Button(
            onClick = { showForm = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ){
            Text(text = "+ Add")
        }
    }

}