package com.example.runnity.ui.screens.challenge

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 챌린지 화면
 * - 진행 중인 챌린지 목록 (전체 챌린지)
 * - 참여 가능한 챌린지 목록
 * - 챌린지 클릭 시 세부 화면으로 이동
 *
 * @param navController 세부 화면으로 이동하기 위한 NavController (탭 내부 이동)
 * @param parentNavController 앱 전체 이동용 NavController
 * @param viewModel 챌린지 화면의 ViewModel (데이터 관리)
 */
@Composable
fun ChallengeScreen(
    navController: NavController? = null,        // 세부 화면 이동용
    parentNavController: NavController? = null,  // 앱 전체 이동용
    viewModel: ChallengeViewModel = viewModel()
) {
    // TODO: ViewModel에서 실제 데이터 가져오기
    // 현재는 임시 데이터 사용
    val allChallenges = listOf(
        "챌린지 A: 5km 완주",
        "챌린지 B: 주 5회 달리기",
        "챌린지 C: 하프 마라톤",
        "챌린지 D: 1시간 달리기",
        "챌린지 E: 100km 달성"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 제목
        Text(
            text = "전체 챌린지",
            style = Typography.Title,
            color = ColorPalette.Light.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 챌린지 리스트
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // items(): LazyColumn에서 리스트를 반복 렌더링
            items(allChallenges.size) { index ->
                AllChallengeListItem(
                    title = allChallenges[index],
                    challengeId = "ch_all_00$index",
                    onClick = { id ->
                        // 세부 화면으로 이동
                        // 홈 화면과 같은 "challenge_detail/{id}" 경로 사용
                        navController?.navigate("challenge_detail/$id")
                    }
                )
            }
        }

        // 리스트가 비어있을 때
        if (allChallenges.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "참여 가능한 챌린지가 없습니다",
                    style = Typography.Body,
                    color = ColorPalette.Light.component
                )
            }
        }
    }
}

/**
 * 전체 챌린지 리스트 아이템
 * (HomeScreen의 ChallengeListItem과 유사하지만 독립적으로 관리)
 *
 * @param title 챌린지 제목
 * @param challengeId 챌린지 ID
 * @param onClick 클릭 시 실행할 람다 함수
 */
@Composable
fun AllChallengeListItem(
    title: String,
    challengeId: String,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(challengeId) },
        colors = CardDefaults.cardColors(
            containerColor = ColorPalette.Light.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = Typography.Body,
                    color = ColorPalette.Light.primary  // 주 텍스트 색상 (검정)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: $challengeId",
                    style = Typography.Caption,
                    color = ColorPalette.Light.component  // 보조 텍스트 색상 (회색)
                )
            }
        }
    }
}
