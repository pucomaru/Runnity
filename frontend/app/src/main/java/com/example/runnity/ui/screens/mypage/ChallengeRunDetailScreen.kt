package com.example.runnity.ui.screens.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.ActionHeader

/**
 * 챌린지 운동 기록 상세 화면
 * - 마이페이지 -> 챌린지 탭 -> 리스트 항목 클릭 시 이동
 * - 챌린지 달리기 기록의 상세 정보 표시
 *
 * @param runId 운동 기록 ID
 * @param navController 뒤로가기용
 */
@Composable
fun ChallengeRunDetailScreen(
    runId: String,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단 헤더
        ActionHeader(
            title = "운동 기록 상세",
            onBack = { navController.navigateUp() },
            height = 56.dp
        )

        // 임시 내용
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "챌린지 운동 기록 상세 페이지\nID: $runId",
                style = Typography.Body
            )
        }
    }
}
