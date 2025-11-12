package com.example.runnity.ui.screens.broadcast

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
    viewModel: BroadcastViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    // 검색어 상태
    var searchQuery by remember { mutableStateOf("") }

    // 정렬 상태
    var selectedSort by remember { mutableStateOf("인기순") }

    // TODO: ViewModel에서 실제 데이터 가져오기
    // 현재는 샘플 데이터 사용 (HomeScreen 참고)
    val broadcasts = listOf(
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

            // 1. 상단 헤더 (중계 타이틀)
            PageHeader(title = "중계")

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 검색바 + 필터 버튼
            SearchBarWithFilter(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onFilterClick = {
                    // 필터 페이지로 이동
                    navController?.navigate("Broadcast_filter")
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
                        // TODO: 중계 목록 새로고침
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

            when (val state = uiState) {
                is BroadcastUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is BroadcastUiState.Success -> {
                    if (state.broadcasts.isEmpty()) {
                        // 2. 데이터가 없을 때 메시지 표시
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("진행 중인 중계가 없습니다.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                        ) {
                            items(state.broadcasts.size) { index ->
                                val broadcast = state.broadcasts[index]
                                // 3. BroadcastCard 사용
                                BroadcastCard(
                                    title = broadcast.title,
                                    viewerCount = broadcast.viewerCount,
                                    participantCount = broadcast.participantCount,
                                    onCardClick = {
                                        // TODO: 중계방 상세 화면으로 이동
                                    }
                                )
                            }
                        }
                    }
                }
                is BroadcastUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message)
                    }
                }
            }
        }
    }
}
