package com.example.runnity.ui.screens.challenge

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 챌린지 세부 정보 화면
 * - 하단 네비게이션 바 없음 (MainTabScreen에서 조건부로 숨김)
 * - 상단에 뒤로가기 버튼 있음
 * - 홈 화면과 챌린지 화면 모두에서 접근 가능
 *
 * @param challengeId 챌린지 고유 ID (route 파라미터로 전달됨)
 * @param navController 뒤로가기를 위한 NavController
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDetailScreen(
    challengeId: String,                    // URL 파라미터로 받은 챌린지 ID
    navController: NavController            // 뒤로가기용
) {
    // Scaffold: 상단바, 컨텐츠 영역을 가진 기본 레이아웃
    Scaffold(
        topBar = {
            // TopAppBar: 상단 앱 바 (제목 + 뒤로가기 버튼)
            TopAppBar(
                title = {
                    Text(
                        text = "챌린지 상세",
                        style = Typography.Title
                    )
                },
                navigationIcon = {
                    // 뒤로가기 버튼
                    IconButton(onClick = {
                        // popBackStack(): 이전 화면으로 돌아가기
                        // 홈에서 왔으면 → 홈으로
                        // 챌린지 리스트에서 왔으면 → 챌린지 리스트로
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = ColorPalette.Light.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorPalette.Light.background,
                    titleContentColor = ColorPalette.Light.primary
                )
            )
        }
    ) { innerPadding ->
        // 화면 컨텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),  // 내부 여백
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // TODO: 실제 챌린지 데이터를 ViewModel에서 가져와서 표시
            // 현재는 임시로 ID만 표시
            Text(
                text = "챌린지 세부 화면",
                style = Typography.Title,
                color = ColorPalette.Light.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "챌린지 ID: $challengeId",
                style = Typography.Body,
                color = ColorPalette.Light.secondary  // 보조 텍스트 색상
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 임시 설명
            Text(
                text = "여기에 챌린지 상세 정보가 표시됩니다:\n\n" +
                        "- 챌린지 제목\n" +
                        "- 설명\n" +
                        "- 기간\n" +
                        "- 참여 인원\n" +
                        "- 목표 거리/시간\n" +
                        "- 참여하기 버튼",
                style = Typography.Body,
                color = ColorPalette.Light.component
            )
        }
    }
}
