package com.example.runnity.ui.screens.challenge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.ui.components.*

/**
 * 챌린지 화면
 * - 전체 챌린지 목록 표시
 * - 검색 및 필터링 기능
 * - 정렬 기능 (인기순, 최신순)
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
    // 검색어 상태
    var searchQuery by remember { mutableStateOf("") }

    // 정렬 상태
    var selectedSort by remember { mutableStateOf("인기순") }

    // TODO: ViewModel에서 실제 데이터 가져오기
    // 현재는 샘플 데이터 사용 (HomeScreen 참고)
    val challenges = listOf(
        ChallengeListItem(
            id = "ch_1",
            distance = "3km",
            title = "3km 달릴 사람 구한다",
            startDateTime = "2025.11.02 21:00 시작",
            participants = "15/100명",
            buttonState = ChallengeButtonState.None
        ),
        ChallengeListItem(
            id = "ch_2",
            distance = "3km",
            title = "3km 달릴 사람 구한다",
            startDateTime = "2025.11.02 21:00 시작",
            participants = "15/100명",
            buttonState = ChallengeButtonState.None
        ),
        ChallengeListItem(
            id = "ch_3",
            distance = "3km",
            title = "3km 달릴 사람 구한다",
            startDateTime = "2025.11.02 21:00 시작",
            participants = "15/100명",
            buttonState = ChallengeButtonState.None
        ),
        ChallengeListItem(
            id = "ch_4",
            distance = "3km",
            title = "3km 달릴 사람 구한다",
            startDateTime = "2025.11.02 21:00 시작",
            participants = "15/100명",
            buttonState = ChallengeButtonState.None
        ),
        ChallengeListItem(
            id = "ch_5",
            distance = "3km",
            title = "3km 달릴 사람 구한다",
            startDateTime = "2025.11.02 21:00 시작",
            participants = "15/100명",
            buttonState = ChallengeButtonState.None
        )
    )

    // 전체 레이아웃 (Box로 FAB 배치)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. 상단 헤더 (챌린지 타이틀)
            PageHeader(title = "챌린지")

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 검색바 + 필터 버튼
            SearchBarWithFilter(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterClick = {
                    // 필터 페이지로 이동
                    navController?.navigate("challenge_filter")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 새로고침 + 정렬 드롭다운
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 새로고침 버튼
                IconButton(
                    onClick = {
                        // TODO: 챌린지 목록 새로고침
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "새로고침",
                        tint = ColorPalette.Light.component
                    )
                }

                // 정렬 드롭다운 (오른쪽 정렬)
                SortDropdown(
                    selectedSort = selectedSort,
                    onSortSelected = { selectedSort = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. 챌린지 리스트 (LazyColumn)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)  // FAB 공간 확보
            ) {
                items(challenges.size) { index ->
                    ChallengeCard(
                        distance = challenges[index].distance,
                        title = challenges[index].title,
                        startDateTime = challenges[index].startDateTime,
                        participants = challenges[index].participants,
                        buttonState = challenges[index].buttonState,
                        onCardClick = {
                            // 세부 화면으로 이동
                            navController?.navigate("challenge_detail/${challenges[index].id}")
                        },
                        onButtonClick = {
                            // TODO: 예약하기/참가하기 버튼 클릭 처리
                        }
                    )
                }
            }
        }

        // 5. FloatingActionButton (챌린지 생성)
        FloatingActionButton(
            onClick = {
                // 챌린지 생성 페이지로 이동
                navController?.navigate("challenge_create")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = ColorPalette.Common.accent,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "챌린지 생성",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
