package com.example.runnity.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import com.example.runnity.theme.Typography
import androidx.activity.compose.BackHandler
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.runnity.data.datalayer.sendSessionControl

@Composable
fun CountdownScreen(
    navController: NavController,
    type: String?,
    km: String?,
    min: String?,
) {
    // 숫자 상태: 3 -> 2 -> 1
    var current by remember { mutableStateOf(3) }
    val scale = remember { Animatable(1f) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val values = listOf(3, 2, 1)
        for (v in values) {
            current = v
            // 1초 동안 펄스(커졌다가 원상 복귀)
            scale.animateTo(1.25f, animationSpec = tween(durationMillis = 500))
            scale.animateTo(1.0f, animationSpec = tween(durationMillis = 500))
        }
        // 카운트 종료: 워치에 start 전송 후 개인 운동 화면으로 이동
        sendSessionControl(context, "start")
        val route = when (type) {
            "distance" -> "workout/personal?type=distance&km=${km ?: ""}"
            "time" -> "workout/personal?type=time&min=${min ?: ""}"
            else -> "workout/personal"
        }
        navController.navigate(route) {
            // 카운트다운을 백스택에서 제거해서 뒤로가기 시 StartRun으로 복귀
            popUpTo("countdown/personal?type={type}&km={km}&min={min}") { inclusive = true }
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
