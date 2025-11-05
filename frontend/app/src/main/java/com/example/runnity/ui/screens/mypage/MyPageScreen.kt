package com.example.runnity.ui.screens.mypage

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
 * 마이페이지 화면
 * - 사용자 프로필
 * - 러닝 통계
 * - 설정 및 로그아웃
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun MyPageScreen(
    parentNavController: NavController? = null, // 설정, 프로필 수정 페이지 이동 시 사용 예정
    viewModel: MyPageViewModel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "마이페이지 화면",
            style = Typography.Title,
            color = ColorPalette.Light.primary
        )
    }
}
