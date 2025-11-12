package com.example.runnity.ui.screens.mypage

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.runnity.theme.ColorPalette
import com.example.runnity.theme.Typography
import com.example.runnity.ui.components.BarChartData
import com.example.runnity.ui.components.MonthPeriodPicker
import com.example.runnity.ui.components.PageHeader
import com.example.runnity.ui.components.PrimaryButton
import com.example.runnity.ui.components.SimpleBarChart
import com.example.runnity.ui.components.SingleSpinnerPicker
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 마이페이지 화면
 * - 사용자 프로필
 * - 러닝 통계
 * - 설정 및 로그아웃
 */
@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    navController: NavController? = null,
    parentNavController: NavController? = null,
    viewModel: MyPageViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriodType by viewModel.selectedPeriodType.collectAsState()
    val selectedPeriodIndex by viewModel.selectedPeriodIndex.collectAsState()
    val selectedRecordTab by viewModel.selectedRecordTab.collectAsState()

    // 기간 선택 바텀 시트
    var showPeriodSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorPalette.Light.background)
    ) {
        // 헤더
        PageHeader(title = "마이페이지")

        when (val state = uiState) {
            is MyPageUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("로딩 중...", style = Typography.Body)
                }
            }
            is MyPageUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, style = Typography.Body, color = ColorPalette.Common.stopAccent)
                }
            }
            is MyPageUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 프로필 섹션
                    item {
                        ProfileSection(
                            userProfile = state.userProfile,
                            onEditClick = {
                                navController?.navigate("profile_setting")
                            }
                        )
                    }

                    // Scope Bar (주/월/연/전체)
                    item {
                        ScopeBar(
                            selectedPeriodType = selectedPeriodType,
                            onPeriodTypeSelected = { viewModel.selectPeriodType(it) }
                        )
                    }

                    // 기간 선택 버튼 (기간별 세부 설정)
                    item {
                        PeriodSelector(
                            selectedText = viewModel.getPeriodOptions().getOrNull(selectedPeriodIndex) ?: "",
                            onClick = {
                                // 전체가 아닐 때만 모달 표시
                                if (selectedPeriodType != PeriodType.ALL) {
                                    showPeriodSheet = true
                                }
                            },
                            showDropdownIcon = selectedPeriodType != PeriodType.ALL
                        )
                    }

                    // 통계 섹션
                    item {
                        StatsSection(stats = state.stats)
                    }

                    // 그래프 (스와이프 가능)
                    item {
                        SwipeableGraphSection(
                            periodType = selectedPeriodType,
                            selectedPeriodIndex = selectedPeriodIndex,
                            onPeriodIndexChanged = { viewModel.selectPeriodIndex(it) },
                            periodOptions = viewModel.getPeriodOptions()
                        )
                    }

                    // 러닝 기록 탭바
                    item {
                        RunningRecordTabSection(
                            selectedTab = selectedRecordTab,
                            onTabSelected = { viewModel.selectRecordTab(it) },
                            personalRecords = state.personalRecords,
                            challengeRecords = state.challengeRecords,
                            onViewAllClick = {
                                // TODO: 모든 운동 기록 페이지로 이동
                            }
                        )
                    }
                }
            }
        }
    }

    // 기간 선택 바텀 시트
    if (showPeriodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPeriodSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            PeriodBottomSheetContent(
                periodType = selectedPeriodType,
                options = viewModel.getPeriodOptions(),
                selectedIndex = selectedPeriodIndex,
                onSelected = { index ->
                    viewModel.selectPeriodIndex(index)
                },
                onConfirm = {
                    showPeriodSheet = false
                }
            )
        }
    }
}

/**
 * 스와이프 가능한 그래프 섹션
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeableGraphSection(
    periodType: PeriodType,
    selectedPeriodIndex: Int,
    onPeriodIndexChanged: (Int) -> Unit,
    periodOptions: List<String>,
    viewModel: MyPageViewModel = viewModel()
) {
    // 전체 타입은 스와이프 불가
    if (periodType == PeriodType.ALL) {
        val uiState by viewModel.uiState.collectAsState()
        if (uiState is MyPageUiState.Success) {
            GraphSection(
                graphData = (uiState as MyPageUiState.Success).graphData,
                periodType = periodType
            )
        }
        return
    }

    val pageCount = periodOptions.size
    val pagerState = rememberPagerState(
        initialPage = selectedPeriodIndex.coerceIn(0, maxOf(0, pageCount - 1)),
        pageCount = { pageCount }
    )

    var isUpdatingFromPager by remember { mutableStateOf(false) }

    // pagerState 변경 감지하여 ViewModel 업데이트 (스와이프 시)
    LaunchedEffect(pagerState, pageCount) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                isUpdatingFromPager = true
                onPeriodIndexChanged(page)
                isUpdatingFromPager = false
            }
    }

    // ViewModel에서 selectedPeriodIndex 변경되면 pagerState도 업데이트 (드롭다운 선택 시)
    LaunchedEffect(selectedPeriodIndex) {
        if (!isUpdatingFromPager && pagerState.currentPage != selectedPeriodIndex) {
            pagerState.animateScrollToPage(selectedPeriodIndex.coerceIn(0, maxOf(0, pageCount - 1)))
        }
    }

    // periodType이 변경되면 초기 페이지로 이동
    LaunchedEffect(periodType, pageCount) {
        val targetPage = selectedPeriodIndex.coerceIn(0, maxOf(0, pageCount - 1))
        pagerState.scrollToPage(targetPage)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        // 각 페이지에 대한 그래프 데이터 생성
        val graphData = viewModel.getGraphDataForIndex(page)
        GraphSection(
            graphData = graphData,
            periodType = periodType
        )
    }
}

/**
 * 그래프 섹션
 */
@Composable
private fun GraphSection(
    graphData: List<GraphPoint>,
    periodType: PeriodType
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 막대 그래프 (재사용 가능한 컴포넌트 사용)
        SimpleBarChart(
            data = graphData.map { BarChartData(it.label, it.distance) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            xAxisLabelFilter = { index, label ->
                // 월일 경우: 1일, 15일, 마지막 일만 표시
                if (periodType == PeriodType.MONTH) {
                    val day = label.toIntOrNull() ?: 0
                    day == 1 || day == 15 || index == graphData.size - 1
                } else {
                    true
                }
            }
        )
    }
}

/**
 * 기간 선택 바텀 시트 내용 (Picker 스타일)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PeriodBottomSheetContent(
    periodType: PeriodType,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // 헤더
        Text(
            text = "기간 선택",
            style = Typography.Heading,
            color = ColorPalette.Light.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )

        // 기간 타입에 따라 다른 스피너 표시
        when (periodType) {
            PeriodType.MONTH -> {
                // 월: 년도와 월을 각각 스피너로 표시
                MonthPeriodPicker(
                    selectedIndex = selectedIndex,
                    onSelected = onSelected,
                    maxMonths = 60
                )
            }
            else -> {
                // 주, 연: 단일 스피너
                SingleSpinnerPicker(
                    options = options,
                    selectedIndex = selectedIndex,
                    onSelected = onSelected
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 확인 버튼
        PrimaryButton(
            text = "확인",
            onClick = onConfirm
        )
    }
}
