package com.example.runnity.ui.screens.challenge

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
 * 챌린지 화면
 * - 진행 중인 챌린지 목록
 * - 참여 가능한 챌린지 목록
 * - 챌린지 검색 및 필터링
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun ChallengeScreen(
    parentNavController: NavController? = null, // 챌린지 상세 페이지 이동 시 사용 예정
    viewModel: ChallengeViewModel = viewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "챌린지 화면",
            style = Typography.Title,
            color = ColorPalette.Light.primary
        )
    }
}
