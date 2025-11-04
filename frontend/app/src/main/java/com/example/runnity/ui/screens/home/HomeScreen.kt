package com.example.runnity.ui.screens.home

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
 * 홈 화면
 * - 메인 대시보드
 * - 최근 러닝 기록, 추천 챌린지 등 표시
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun HomeScreen(
    parentNavController: NavController? = null, // 상세 페이지 이동 시 사용 예정
    viewModel: HomeViewModel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "홈 화면",
            style = Typography.Title,
            color = ColorPalette.Light.primary
        )
    }
}
