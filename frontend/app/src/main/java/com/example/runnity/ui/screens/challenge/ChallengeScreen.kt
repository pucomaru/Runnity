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
    // ViewModel 상태 관찰
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // 정렬 상태 (기본값: 최신순)
    var selectedSort by remember { mutableStateOf("최신순") }

    // API 응답을 UI 모델로 변환
    val challenges = when (val state = uiState) {
        is ChallengeUiState.Success -> {
            state.challenges.map { apiItem ->
                ChallengeMapper.toUiModel(apiItem)
            }
        }
        else -> emptyList()
    }

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
                onSearchChange = { viewModel.updateSearchQuery(it) },
                onSearchSubmit = {
                    // 키보드 검색 버튼 또는 검색 아이콘 클릭 시 검색 실행
                    viewModel.searchChallenges()
                },
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
                        viewModel.refreshChallenges()
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
                    onSortSelected = {
                        selectedSort = it
                        // "인기순" -> "POPULAR", "최신순" -> "LATEST" 변환
                        val sortType = when (it) {
                            "인기순" -> "POPULAR"
                            "최신순" -> "LATEST"
                            else -> "LATEST"
                        }
                        viewModel.updateSortType(sortType)
                    }
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
                    val challenge = challenges[index]
                    ChallengeCard(
                        distance = challenge.distance,
                        title = challenge.title,
                        startDateTime = challenge.startDateTime,
                        participants = challenge.participants,
                        buttonState = challenge.buttonState,
                        onCardClick = {
                            // 세부 화면으로 이동
                            navController?.navigate("challenge_detail/${challenge.id}")
                        },
                        onButtonClick = {
                            // 참가하기 버튼 (시작 5분 전, 웹소켓 참가용)
                            val challengeId = challenge.id.toLongOrNull()
                            if (challengeId != null && challenge.buttonState == ChallengeButtonState.Join) {
                                // TODO: 웹소켓 참가 로직 추가
                                viewModel.joinChallenge(challengeId)
                            }
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
