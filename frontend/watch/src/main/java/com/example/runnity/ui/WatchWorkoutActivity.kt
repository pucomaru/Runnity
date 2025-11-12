package com.example.runnity.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnity.theme.RunnityTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.Intent
import com.example.runnity.health.ExerciseFgService
import com.example.runnity.data.datalayer.sendSessionControlFromWatch

class WatchWorkoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RunnityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { inner ->
                    WorkoutScreen(modifier = Modifier.padding(inner))
                }
            }
        }
    }
}

@Composable
fun WorkoutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isPaused = remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Running", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = "Distance: -- km", fontSize = 14.sp)
        Text(text = "Pace: --'--\" /km", fontSize = 14.sp)
        Text(text = "HR: -- bpm", fontSize = 14.sp)
        Text(text = "Calories: -- kcal", fontSize = 14.sp)

        Spacer(Modifier.height(16.dp))
        Row {
            if (!isPaused.value) {
                Button(onClick = {
                    // local pause
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, ExerciseFgService::class.java).apply {
                            action = "com.example.runnity.action.PAUSE"
                        }
                    )
                    // notify phone
                    sendSessionControlFromWatch(context, "pause")
                    isPaused.value = true
                }) { Text("Pause") }
            } else {
                Button(onClick = {
                    ContextCompat.startForegroundService(
                        context,
                        Intent(context, ExerciseFgService::class.java).apply {
                            action = "com.example.runnity.action.RESUME"
                        }
                    )
                    sendSessionControlFromWatch(context, "resume")
                    isPaused.value = false
                }) { Text("Resume") }
            }

            Spacer(Modifier.width(12.dp))
            Button(onClick = {
                context.startService(
                    Intent(context, ExerciseFgService::class.java).apply {
                        action = "com.example.runnity.action.STOP"
                    }
                )
                sendSessionControlFromWatch(context, "stop")
            }) { Text("Stop") }
        }
    }
}
