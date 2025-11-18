package com.example.runnity.ui.screens.challenge

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.runnity.theme.Typography

/**
 * 챌린지용 카운트다운 화면 (5 → 1)
 * - 뒤로가기 비활성화
 * - 카운트가 끝나면 챌린지 운동 화면으로 자동 이동
 */
@Composable
fun ChallengeCountdownScreen(
    navController: NavController,
    challengeId: String
) {
    var current by remember { mutableStateOf(5) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        val values = listOf(5, 4, 3, 2, 1)
        for (v in values) {
            current = v
            // 1초 동안 펄스 애니메이션
            scale.animateTo(1.25f, animationSpec = tween(durationMillis = 500))
            scale.animateTo(1.0f, animationSpec = tween(durationMillis = 500))
        }
        // 카운트 종료 후 챌린지 운동 화면으로 이동 (운동 화면은 이후 구현 예정)
        navController.navigate("challenge_workout/$challengeId") {
            // 카운트다운을 백스택에서 제거
            popUpTo("challenge_countdown/$challengeId") { inclusive = true }
        }
    }

    // 뒤로가기 비활성화
    BackHandler(enabled = true) { /* consume back press during countdown */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = current.toString(),
            style = Typography.LargeTitle.copy(fontSize = 108.sp),
            color = Color.Black,
            modifier = Modifier.graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value
            )
        )
    }
}
