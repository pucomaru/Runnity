package com.example.runnity.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
 * 홈 화면
 * - 메인 대시보드
 * - 내가 예약(참여)한 챌린지 리스트 표시
 * - 챌린지 클릭 시 세부 화면으로 이동
 *
 * @param navController 세부 화면으로 이동하기 위한 NavController (탭 내부 이동)
 * @param parentNavController 앱 전체 이동용 NavController (로그인 화면 등으로 이동)
 * @param viewModel 홈 화면의 ViewModel (데이터 관리)
 */
@Composable
fun HomeScreen(
    navController: NavController? = null,        // 세부 화면 이동용 (MainTabScreen에서 전달)
    parentNavController: NavController? = null,  // 앱 전체 이동용
    viewModel: HomeViewModel = viewModel()       // viewModel(): ViewModel 자동 생성
) {
    // TODO: ViewModel에서 실제 데이터 가져오기
    // 현재는 임시 데이터 사용
    val myJoinedChallenges = listOf(
        "챌린지 1: 10km 완주하기",
        "챌린지 2: 주 3회 달리기",
        "챌린지 3: 30분 달리기"
    )

    // Column: 세로로 UI 요소 배치
    Column(
        modifier = Modifier
            .fillMaxSize()  // 화면 전체 크기
            .padding(16.dp) // 외부 여백
    ) {
        // 제목
        Text(
            text = "내가 참여한 챌린지",
            style = Typography.Title,
            color = ColorPalette.Light.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // LazyColumn: 리사이클러뷰처럼 스크롤 가능한 리스트
        // items(): 리스트 데이터를 하나씩 UI로 변환
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)  // 아이템 간격
        ) {
            items(myJoinedChallenges.size) { index ->
                // 각 챌린지 아이템
                ChallengeListItem(
                    title = myJoinedChallenges[index],
                    challengeId = "ch_00$index",  // 임시 ID
                    onClick = { id ->
                        // 챌린지 클릭 시 세부 화면으로 이동
                        // navController?.navigate("경로/파라미터")
                        navController?.navigate("challenge_detail/$id")
                    }
                )
            }
        }

        // 리스트가 비어있을 때 표시
        if (myJoinedChallenges.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "참여 중인 챌린지가 없습니다",
                    style = Typography.Body,
                    color = ColorPalette.Light.component
                )
            }
        }
    }
}

/**
 * 챌린지 리스트 아이템 (재사용 가능한 컴포넌트)
 *
 * @param title 챌린지 제목
 * @param challengeId 챌린지 ID
 * @param onClick 클릭 시 실행할 함수 (람다)
 */
@Composable
fun ChallengeListItem(
    title: String,
    challengeId: String,
    onClick: (String) -> Unit  // (String) -> Unit: 문자열을 받아서 리턴값 없는 함수
) {
    // Card: 그림자와 모서리가 둥근 카드 UI
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(challengeId) },  // 클릭 이벤트: onClick 함수 호출
        colors = CardDefaults.cardColors(
            containerColor = ColorPalette.Light.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp  // 그림자 높이
        )
    ) {
        // 카드 내부
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,  // 양 끝 정렬
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
