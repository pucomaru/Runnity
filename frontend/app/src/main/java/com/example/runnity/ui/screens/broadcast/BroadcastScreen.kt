package com.example.runnity.ui.screens.broadcast

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.ui.components.*
import com.example.runnity.ui.screens.challenge.ChallengeMapper
import com.example.runnity.ui.screens.challenge.ChallengeUiState

/**
 * 중계 화면
 * - 지금 중계중인 방 목록 표시
 * - 검색 및 필터링 기능
 * - 정렬 기능 (인기순, 최신순)
 * - 중계방 클릭 시 세부 화면으로 이동
 *
 * @param navController 세부 화면으로 이동하기 위한 NavController (탭 내부 이동)
 * @param parentNavController 앱 전체 이동용 NavController
 * @param viewModel 중계방 화면의 ViewModel (데이터 관리)
 */
@Composable
fun BroadcastScreen(
    navController: NavController? = null,        // 세부 화면 이동용
    parentNavController: NavController? = null,  // 앱 전체 이동용
    viewModel: BroadcastViewModel = viewModel(),
    innerPadding: PaddingValues = PaddingValues()
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

    var showJoinDialog by remember { mutableStateOf(false) }
    var selectedChallengeId by remember { mutableStateOf<Long?>(null) }


    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LiveBadge(text = "LIVE")

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
                    navController?.navigate("broadcast_filter")
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

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
                        else -> "POPULAR"
                    }
                    viewModel.updateSortType(sortType)
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. 중계목록
        when (val state = uiState) {
            is BroadcastUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is BroadcastUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message)
                }
            }

            is BroadcastUiState.Success -> {
                val items = state.broadcasts
                if (items.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("진행 중인 중계가 없습니다.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        items(items.size) { index ->
                            val item = items[index]
                            BroadcastCard(
                                title = item.title,
                                viewerCount = item.viewerCount,
                                participantCount = item.participantCount,
                                onCardClick = {
                                    selectedChallengeId = item.challengeId
                                    showJoinDialog = true
//                                    // 중계방 입장
//                                    navController?.navigate("broadcast_live/${item.challengeId}")
                                },
                                distance = item.distance
                            )
                        }
                    }
                }
            }
        }

        // 예약하기 확인 다이얼로그
        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                title = { Text("중계방 입장") },
                text = { Text("이 중계방에 입장하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        selectedChallengeId?.let { id ->
                            viewModel.joinBroadcast(id.toString())
                            navController?.navigate("broadcast_live/$id")
                        }
                        showJoinDialog = false
                    }) { Text("입장하기") }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinDialog = false }) {
                        Text("취소")
                    }
                }
            )
        }
    }
}
