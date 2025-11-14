package com.example.runnity.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runnity.theme.RunnityTheme
import kotlinx.coroutines.delay
import android.content.Intent

class WatchCountdownActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val seconds = intent?.getIntExtra("seconds", 3) ?: 3
        setContent {
            RunnityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { inner ->
                    CountdownScreen(seconds = seconds, modifier = Modifier.padding(inner)) {
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
private fun CountdownScreen(seconds: Int, modifier: Modifier = Modifier, onFinished: () -> Unit) {
    var current by remember { mutableStateOf(seconds) }

    LaunchedEffect(seconds) {
        for (i in seconds downTo 1) {
            current = i
            delay(1000)
        }
        onFinished()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$current",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp
        )
    }
}
