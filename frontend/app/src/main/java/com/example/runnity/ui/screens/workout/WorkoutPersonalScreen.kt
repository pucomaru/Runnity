package com.example.runnity.ui.screens.workout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// 개인 러닝 화면
@Composable
fun WorkoutPersonalScreen(
    type: String?,
    km: String?,
    min: String?,
) {
    val summary = when (type) {
        "distance" -> "개인 러닝 시작 - 거리 목표: ${km ?: "-"} km"
        "time" -> "개인 러닝 시작 - 시간 목표: ${min ?: "-"} 분"
        else -> "개인 러닝 시작 - 자유 달리기"
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = summary)
    }
}
