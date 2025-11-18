package com.example.runnity.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography

/**
 * 챌린지 리스트 아이템 데이터
 *
 * @param id 챌린지 고유 ID
 * @param distance 거리 (예: "3km")
 * @param title 제목
 * @param startDateTime 시작 일시
 * @param participants 참여 인원 (예: "15/100명")
 * @param buttonState 버튼 상태 (None, Join) - 시작 5분 전부터 Join
 */
data class ChallengeListItem(
    val id: String,
    val distance: String,
    val title: String,
    val startDateTime: String,
    val participants: String,
    val buttonState: ChallengeButtonState = ChallengeButtonState.None
)

/**
 * 챌린지 리스트 섹션 컴포넌트
 * - 챌린지 카드 리스트를 표시
 * - 선택적으로 검색바, 필터/정렬 버튼 추가 가능
 *
 * @param challenges 챌린지 리스트
 * @param onChallengeClick 챌린지 카드 클릭 이벤트 (id 전달)
 * @param onButtonClick 참가하기 버튼 클릭 이벤트 (id 전달) - 시작 5분 전만 표시
 * @param showSearchBar 검색바 표시 여부 (기본: false)
 * @param showFilters 필터/정렬 버튼 표시 여부 (기본: false)
 * @param onSearchClick 검색 버튼 클릭 이벤트
 * @param onFilterClick 필터 버튼 클릭 이벤트
 * @param onSortClick 정렬 버튼 클릭 이벤트
 * @param modifier Modifier (선택사항)
 *
 * 사용 예시 1 (홈 화면 - 필터 없음):
 * ChallengeListSection(
 *     challenges = myJoinedChallenges,
 *     onChallengeClick = { id -> navController.navigate("detail/$id") },
 *     onButtonClick = { id -> viewModel.joinChallenge(id) }
 * )
 *
 * 사용 예시 2 (챌린지 화면 - 필터 있음):
 * ChallengeListSection(
 *     challenges = allChallenges,
 *     showSearchBar = true,
 *     showFilters = true,
 *     onChallengeClick = { id -> navController.navigate("detail/$id") },
 *     onButtonClick = { id -> viewModel.joinChallenge(id) },
 *     onSearchClick = { viewModel.toggleSearch() },
 *     onFilterClick = { viewModel.showFilterDialog() },
 *     onSortClick = { viewModel.showSortDialog() }
 * )
 */
@Composable
fun ChallengeListSection(
    challenges: List<ChallengeListItem>,      // 챌린지 리스트 데이터
    onChallengeClick: (String) -> Unit,       // 카드 클릭 (챌린지 ID)
    onButtonClick: (String) -> Unit,          // 버튼 클릭 (챌린지 ID)
    modifier: Modifier = Modifier,            // 추가 Modifier
    showSearchBar: Boolean = false,           // 검색바 표시 여부
    showFilters: Boolean = false,             // 필터/정렬 표시 여부
    onSearchClick: () -> Unit = {},           // 검색 버튼 클릭
    onFilterClick: () -> Unit = {},           // 필터 버튼 클릭
    onSortClick: () -> Unit = {}              // 정렬 버튼 클릭
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 검색바 (선택사항)
        if (showSearchBar) {
            // TODO: 검색바 컴포넌트 추가
            // SearchBar(onSearchClick = onSearchClick)
            Text(
                text = "[검색바 영역]",
                style = Typography.Caption,
                color = ColorPalette.Light.component,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 필터/정렬 버튼 (선택사항)
        if (showFilters) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // TODO: 필터/정렬 버튼 컴포넌트 추가
                SmallPillButton(
                    text = "필터",
                    onClick = onFilterClick
                )
                SmallPillButton(
                    text = "정렬",
                    onClick = onSortClick
                )
            }
        }

        // 챌린지 리스트
        if (challenges.isEmpty()) {
            // 빈 리스트 상태
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "참여 가능한 챌린지가 없습니다",
                    style = Typography.Body,
                    color = ColorPalette.Light.component
                )
            }
        } else {
            // LazyColumn: 스크롤 가능한 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),  // 좌우 여백
                verticalArrangement = Arrangement.spacedBy(12.dp)    // 카드 간 간격
            ) {
                items(
                    items = challenges,
                    key = { it.id }  // 각 아이템의 고유 키 (성능 최적화)
                ) { challenge ->
                    ChallengeCard(
                        distance = challenge.distance,
                        title = challenge.title,
                        startDateTime = challenge.startDateTime,
                        participants = challenge.participants,
                        buttonState = challenge.buttonState,
                        onCardClick = { onChallengeClick(challenge.id) },
                        onButtonClick = { onButtonClick(challenge.id) }
                    )
                }
            }
        }
    }
}

/**
 * 미리보기 (Preview)
 */
@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun ChallengeListSectionPreview() {
    val sampleChallenges = listOf(
        ChallengeListItem(
            id = "1",
            distance = "3km",
            title = "3km 달릴 사람 구한다",
            startDateTime = "2025.11.02 21:00 시작",
            participants = "15/100명",
            buttonState = ChallengeButtonState.None  // 기본 상태
        ),
        ChallengeListItem(
            id = "2",
            distance = "5km",
            title = "주말 아침 런닝",
            startDateTime = "2025.11.03 08:00 시작",
            participants = "8/20명",
            buttonState = ChallengeButtonState.Join  // 시작 5분 전
        ),
        ChallengeListItem(
            id = "3",
            distance = "10km",
            title = "한강 야간 러닝",
            startDateTime = "2025.11.05 20:00 시작",
            participants = "3/15명",
            buttonState = ChallengeButtonState.None
        )
    )

    Column {
        // 홈 화면 스타일 (필터 없음)
        Text(
            text = "홈 화면 (필터 없음)",
            style = Typography.Title,
            modifier = Modifier.padding(16.dp)
        )
        ChallengeListSection(
            challenges = sampleChallenges,
            onChallengeClick = {},
            onButtonClick = {},
            modifier = Modifier.height(400.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 챌린지 화면 스타일 (필터 있음)
        Text(
            text = "챌린지 화면 (필터 있음)",
            style = Typography.Title,
            modifier = Modifier.padding(16.dp)
        )
        ChallengeListSection(
            challenges = sampleChallenges,
            showSearchBar = true,
            showFilters = true,
            onChallengeClick = {},
            onButtonClick = {},
            onSearchClick = {},
            onFilterClick = {},
            onSortClick = {},
            modifier = Modifier.height(400.dp)
        )
    }
}
