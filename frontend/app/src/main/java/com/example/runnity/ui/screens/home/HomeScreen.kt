package com.example.runnity.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.R
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.*

/**
 * 홈 화면
 * - 메인 대시보드
 * - 날씨 카드, 추천 챌린지, 예약한 챌린지 표시
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

    // 추천 챌린지 샘플 데이터
    val recommendedChallenges = listOf(
        RecommendedChallengeItem(
            id = "rec_1",
            imageUrl = null,
            title = "러니티 추천 챌린지",
            description = "대규모 러닝 실시간 경쟁"
        ),
        RecommendedChallengeItem(
            id = "rec_2",
            imageUrl = null,
            title = "주말 마라톤",
            description = "함께 달리는 즐거움"
        ),
        RecommendedChallengeItem(
            id = "rec_3",
            imageUrl = null,
            title = "초보 러너 환영",
            description = "천천히 함께 달려요"
        )
    )

    // 예약한 챌린지 샘플 데이터
    // TODO: ViewModel에서 실제 데이터 가져오기
    // TODO: ViewModel에서 실제 챌린지 시작 시간을 확인하여
    //       시작 5분 전부터 buttonState를 Join으로 변경해야 함
    //       (HomeViewModel의 startChallengeTimeChecker 구현 필요)
    val reservedChallenges = listOf(
        ChallengeListItem(
            id = "res_1",
            distance = "3km",
            title = "아침 러닝 챌린지",
            startDateTime = "2025.11.05 16:09",
            participants = "12/20",
            buttonState = ChallengeButtonState.None  // 기본: 버튼 없음 (이미 예약됨)
        ),
        ChallengeListItem(
            id = "res_2",
            distance = "5km",
            title = "주말 마라톤 대회",
            startDateTime = "2025.11.09 10:00",
            participants = "45/50",
            buttonState = ChallengeButtonState.None
        ),
        ChallengeListItem(
            id = "res_3",
            distance = "10km",
            title = "야간 러닝 챌린지",
            startDateTime = "2025.11.10 19:00",
            participants = "8/15",
            buttonState = ChallengeButtonState.None
        )
    )

    // 전체 레이아웃
    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 96.dp) // FAB 여백
        ) {
            // 1. 상단 앱바
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorPalette.Common.accent)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.runnity_logo),
                        contentDescription = "Runnity Logo",
                        modifier = Modifier.height(32.dp),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "알림",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            // 2-1. 날씨 카드
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ColorPalette.Common.accent)
                        .padding(16.dp)
                ) {
                    WeatherCard(
                        country = "Korea",
                        city = "Seoul",
                        weather = "Cloudy",
                        temperature = "10°",
                        time = "9:41 AM",
                        backgroundImageUrl = null
                    )
                }
            }

            // 2-2. 운영진 추천 섹션 헤더
            item {
                SectionHeader(
                    subtitle = "운영진 추천 챌린지",
                    caption = "이번 주 인기 챌린지"
                )
            }

            // 2-2. 추천 캐러셀 (가로 스크롤은 OK)
            item {
                RecommendedChallengeCarousel(
                    challenges = recommendedChallenges,
                    onChallengeClick = { id ->
                        navController?.navigate("challenge_detail/$id")
                    }
                )
            }

            // 2-3. 예약한 챌린지 섹션 헤더
            item {
                SectionHeader(
                    subtitle = "예약한 챌린지",
                    caption = "내가 예약한 챌린지를 확인하세요"
                )
            }

            // 2-3. 예약한 챌린지 리스트
            items(reservedChallenges.size) { index ->
                val c = reservedChallenges[index]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    ChallengeCard(
                        distance = c.distance,
                        title = c.title,
                        startDateTime = c.startDateTime,
                        participants = c.participants,
                        buttonState = c.buttonState,
                        onCardClick = { navController?.navigate("challenge_detail/${c.id}") }
                    )
                }
            }
        }


        FloatingActionButton(
            onClick = { navController?.navigate("broadcast_view") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = ColorPalette.Common.accent,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Filled.LiveTv,
                contentDescription = "중계방 보기"
            )
        }
    }
}
