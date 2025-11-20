package com.example.runnity.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.runnity.data.datalayer.sendSessionControlFromWatch
import com.example.runnity.health.ExerciseFgService
import com.example.runnity.theme.RunnityTheme
import kotlinx.coroutines.delay
import com.example.runnity.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

class WatchWorkoutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RunnityTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) { inner ->
                    WorkoutScreen(modifier = Modifier.padding(inner))
                }
            }
        }
    }
}

@Composable
fun WorkoutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = (context as? ComponentActivity)
    var isPaused by remember { mutableStateOf(false) }
    var elapsedSec by remember { mutableStateOf(0) }
    var distanceKm by remember { mutableStateOf(0.0) }
    var paceSecPerKm by remember { mutableStateOf<Int?>(null) }
    var hrBpm by remember { mutableStateOf<Int?>(null) }
    var rank by remember { mutableStateOf<Int?>(null) }

    // Receive metrics/rank/finish signals from ExerciseFgService local broadcast
    LaunchedEffect(Unit) {
        val filter = IntentFilter().apply {
            addAction("com.example.runnity.action.METRICS")
            addAction("com.example.runnity.action.RANK")
            addAction("com.example.runnity.action.FINISH_UI")
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent ?: return
                when (intent.action) {
                    "com.example.runnity.action.METRICS" -> {
                        val elapsed = intent.getLongExtra("elapsed_ms", -1L)
                        if (elapsed >= 0) elapsedSec = (elapsed / 1000L).toInt()
                        if (intent.hasExtra("distance_m")) {
                            val dm = intent.getDoubleExtra("distance_m", 0.0)
                            distanceKm = dm / 1000.0
                        }
                        if (intent.hasExtra("pace_spkm")) {
                            val p = intent.getDoubleExtra("pace_spkm", Double.NaN)
                            if (p.isFinite() && p > 0) paceSecPerKm = kotlin.math.round(p).toInt() else paceSecPerKm = null
                        }
                        if (intent.hasExtra("hr_bpm")) {
                            hrBpm = intent.getIntExtra("hr_bpm", 0)
                        }
                    }
                    "com.example.runnity.action.RANK" -> {
                        val r = intent.getIntExtra("rank", -1)
                        rank = r.takeIf { it > 0 }
                    }
                    "com.example.runnity.action.FINISH_UI" -> {
                        // 서비스에서 운동 종료를 알리면 워치 운동 화면을 닫고 홈/런처로 복귀
                        activity?.finish()
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        try {
            while (true) {
                if (!isPaused) elapsedSec += 0 // keep composition alive; metrics will override
                delay(1000)
            }
        } finally {
            context.unregisterReceiver(receiver)
        }
    }

    // FINISH_UI 브로드캐스트는 현재 사용하지 않음 (서비스에서도 송신하지 않음)

    // 시간은 서비스 브로드캐스트 값을 우선 사용

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        val pretendard = remember {
            FontFamily(
                Font(R.font.pretendard_bold, weight = FontWeight.Bold),
                Font(R.font.pretendard_medium, weight = FontWeight.Medium)
            )
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val h = elapsedSec / 3600
            val m = (elapsedSec % 3600) / 60
            val s = elapsedSec % 60
            val timeText = if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
            val p = paceSecPerKm
            val paceText = if (p != null && p > 0) String.format("%d'%02d\"/km", p / 60, p % 60) else "--:--/km"

            if (rank == null) {
                // 개인 운동 모드: 시간 크게 + 아래에 페이스/거리/심박 (기존 레이아웃)
                Text(
                    text = timeText,
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = pretendard
                )

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("페이스", color = Color.White, fontSize = 12.sp, fontFamily = pretendard, fontWeight = FontWeight.Medium)
                        Text(paceText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium, fontFamily = pretendard)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("거리", color = Color.White, fontSize = 12.sp, fontFamily = pretendard, fontWeight = FontWeight.Medium)
                        Text(String.format("%.2f km", distanceKm), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium, fontFamily = pretendard)
                    }
                }

                Spacer(Modifier.height(10.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("심박", color = Color.White, fontSize = 12.sp, fontFamily = pretendard, fontWeight = FontWeight.Medium)
                    Text(hrBpm?.let { "$it bpm" } ?: "-- bpm", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium, fontFamily = pretendard)
                }
            } else {
                // 챌린지 모드: 순위 크게 + 아래에 거리/페이스/시간/심박 (컴팩트 레이아웃)
                val rankText = "${rank}위"
                Text(
                    text = rankText,
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = pretendard
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("거리", color = Color.White, fontSize = 10.sp, fontFamily = pretendard, fontWeight = FontWeight.Medium)
                        Text(String.format("%.2f km", distanceKm), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, fontFamily = pretendard)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("페이스", color = Color.White, fontSize = 10.sp, fontFamily = pretendard, fontWeight = FontWeight.Medium)
                        Text(paceSecPerKm?.let { String.format("%d'%02d\"", it / 60, it % 60) } ?: "--'--\"", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, fontFamily = pretendard)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("시간", color = Color.White, fontSize = 10.sp, fontFamily = pretendard, fontWeight = FontWeight.Medium)
                        Text(timeText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, fontFamily = pretendard)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("심박", color = Color.White, fontSize = 10.sp, fontFamily = pretendard, fontWeight = FontWeight.Medium)
                    Text(hrBpm?.let { "$it bpm" } ?: "-- bpm", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, fontFamily = pretendard)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    ContextCompat.startForegroundService(
                        context,
                        android.content.Intent(context, ExerciseFgService::class.java).apply {
                            action = if (!isPaused) "com.example.runnity.action.PAUSE" else "com.example.runnity.action.RESUME"
                        }
                    )
                    sendSessionControlFromWatch(context, if (!isPaused) "pause" else "resume")
                    isPaused = !isPaused
                }) { Text(if (!isPaused) "일시정지" else "재개") }

                Spacer(Modifier.width(12.dp))
                Button(onClick = {
                    context.startService(
                        android.content.Intent(context, ExerciseFgService::class.java).apply {
                            action = "com.example.runnity.action.STOP"
                        }
                    )
                    sendSessionControlFromWatch(context, "stop")
                }) { Text("종료") }
            }
        }
    }
}
