package com.example.runnity.ui.screens.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.ui.components.*
import com.example.runnity.utils.PermissionUtils
import com.example.runnity.utils.hasNotificationPermission
import com.example.runnity.utils.rememberLocationPermissionLauncher
import com.example.runnity.utils.rememberNotificationPermissionLauncher
import com.example.runnity.utils.requestLocationPermissions
import android.os.Build
import android.widget.Toast

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
    val context = LocalContext.current
    val locationLauncher = rememberLocationPermissionLauncher { }
    val notificationLauncher = rememberNotificationPermissionLauncher { }

    LaunchedEffect(Unit) {
        val needsLocation = !PermissionUtils.hasLocationPermission(context)
        val needsNotification = Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission(context)
        if (needsLocation || needsNotification) {
            Toast.makeText(
                context,
                "운동을 위해 위치 및 알림 권한을 허용해 주세요. 설정에서 권한을 켜고 다시 시도해 주세요.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // 대기 중인 새로고침 처리
    val pendingRefresh by viewModel.pendingRefresh.collectAsState()
    LaunchedEffect(pendingRefresh) {
        if (pendingRefresh) {
            viewModel.consumePendingRefresh()
        }
    }
    // ViewModel 상태 관찰
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // 정렬 상태 (기본값: 임박순)
    var selectedSort by remember { mutableStateOf("임박순") }

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
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Light.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorPalette.Light.background)
        ) {
            // 1. 상단 헤더 (챌린지 타이틀)
            PageHeader(title = "챌린지")

            Spacer(modifier = Modifier.height(8.dp))

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
                        // "인기순" -> "POPULAR", "임박순" -> "LATEST" 변환
                        val sortType = when (it) {
                            "인기순" -> "POPULAR"
                            "임박순" -> "LATEST"
                            else -> "LATEST"
                        }
                        viewModel.updateSortType(sortType)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. 챌린지 리스트 또는 상태 메시지
            when (val state = uiState) {
                is ChallengeUiState.Loading -> {
                    // 로딩 중
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ColorPalette.Common.accent
                        )
                    }
                }
                is ChallengeUiState.Success -> {
                    if (challenges.isEmpty()) {
                        // 챌린지가 없음
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "챌린지가 없습니다",
                                    style = com.example.runnity.theme.Typography.Subheading,
                                    color = ColorPalette.Light.secondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "새로운 챌린지를 생성해보세요!",
                                    style = com.example.runnity.theme.Typography.Caption,
                                    color = ColorPalette.Light.component,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // 챌린지 리스트 표시
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                        ) {
                            items(
                                count = challenges.size,
                                key = { index -> challenges[index].id }
                            ) { index ->
                                val challenge = challenges[index]
                                ChallengeCard(
                                    distance = challenge.distance,
                                    title = challenge.title,
                                    startDateTime = challenge.startDateTime,
                                    participants = challenge.participants,
                                    isPrivate = challenge.isPrivate,
                                    buttonState = challenge.buttonState,
                                    onCardClick = {
                                        navController?.navigate("challenge_detail/${challenge.id}")
                                    },
                                    onButtonClick = {
                                        val challengeId = challenge.id.toLongOrNull()
                                        if (challengeId != null && challenge.buttonState == ChallengeButtonState.Join) {
                                            val needsLocation = !PermissionUtils.hasLocationPermission(context)
                                            val needsNotification = Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission(context)
                                            if (needsLocation || needsNotification) {
                                                if (needsLocation) {
                                                    requestLocationPermissions(locationLauncher)
                                                } else if (needsNotification && Build.VERSION.SDK_INT >= 33) {
                                                    notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                                }
                                            } else {
                                                viewModel.joinChallenge(challengeId)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is ChallengeUiState.Error -> {
                    // 에러 메시지 표시
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (state.message.contains("네트워크")) {
                                    "네트워크 연결을 확인해주세요"
                                } else {
                                    state.message
                                },
                                style = com.example.runnity.theme.Typography.Subheading,
                                color = ColorPalette.Light.secondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            IconButton(
                                onClick = { viewModel.refreshChallenges() }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "다시 시도",
                                    tint = ColorPalette.Common.accent
                                )
                            }
                        }
                    }
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
