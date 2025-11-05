package com.example.runnity.ui.screens.startrun

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 개인 러닝 시작 화면
 * - 러닝 시작 준비 화면
 * - 목표 설정, 경로 선택 등
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun StartRunScreen(
    parentNavController: NavController? = null, // 러닝 화면 이동 시 사용 예정
    viewModel: StartRunViewModel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "개인 러닝 화면",
            style = Typography.Title,
            color = ColorPalette.Light.primary
        )
    }
}
