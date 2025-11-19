package com.example.runnity

import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.runnity.theme.RunnityTheme
import com.example.runnity.health.ExerciseFgService
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 권한 체크
        val permissions = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

        // 권한 요청
        val requestPermissionsLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val allGranted = permissions.all { perm -> result[perm] == true }
            if (allGranted) {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, ExerciseFgService::class.java)
                )
            }
        }

        val hasAll = permissions.all { perm ->
            ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        }
        if (hasAll) {
            ContextCompat.startForegroundService(
                this,
                Intent(this, ExerciseFgService::class.java)
            )
        } else {
            requestPermissionsLauncher.launch(permissions)
        }

        setContent {
            RunnityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WatchHome(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WatchHome(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Runnity 앱을 켜주세요!",
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ControlPanelPreview() {
    RunnityTheme {
        WatchHome()
    }
}
