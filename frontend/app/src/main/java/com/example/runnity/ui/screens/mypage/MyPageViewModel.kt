package com.example.runnity.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * 마이페이지 화면 ViewModel
 * - 사용자 프로필 조회
 * - 러닝 통계 조회
 * - 로그아웃 처리
 */
class MyPageViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MyPageUiState>(MyPageUiState.Loading)
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    // 선택된 기간 타입 (주/월/연/전체)
    private val _selectedPeriodType = MutableStateFlow(PeriodType.WEEK)
    val selectedPeriodType: StateFlow<PeriodType> = _selectedPeriodType.asStateFlow()

    // 선택된 세부 기간 인덱스
    // 주: 3 (이번 주), 월: 59 (이번 달), 연: 4 (이번 년도)
    private val _selectedPeriodIndex = MutableStateFlow(3)
    val selectedPeriodIndex: StateFlow<Int> = _selectedPeriodIndex.asStateFlow()

    // 러닝 기록 탭 (개인/챌린지)
    private val _selectedRecordTab = MutableStateFlow(0)
    val selectedRecordTab: StateFlow<Int> = _selectedRecordTab.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                // TODO: Repository에서 사용자 프로필 및 통계 로드
                _uiState.value = MyPageUiState.Success(
                    userProfile = UserProfile(
                        nickname = "역삼 우사인볼트",
                        profileImageUrl = null
                    ),
                    stats = generateMockStats(),
                    graphData = generateMockGraphData(),
                    personalRecords = generateMockPersonalRecords(),
                    challengeRecords = generateMockChallengeRecords()
                )
            } catch (e: Exception) {
                _uiState.value = MyPageUiState.Error(e.message ?: "프로필 로드 실패")
            }
        }
    }

    fun selectPeriodType(type: PeriodType) {
        _selectedPeriodType.value = type
        // 기본값을 이번 주/월/연으로 설정
        _selectedPeriodIndex.value = when (type) {
            PeriodType.WEEK -> 3  // 이번 주 (4개 중 마지막)
            PeriodType.MONTH -> 59  // 이번 달 (60개 중 마지막)
            PeriodType.YEAR -> 4  // 이번 년도 (5개 중 마지막)
            PeriodType.ALL -> 0  // 전체 (1개만 있음)
        }
        // 기간 타입 변경 시 그래프 데이터 업데이트
        updateGraphData()
    }

    private fun getPeriodOptionsForType(type: PeriodType): List<String> {
        val today = LocalDate.now()
        return when (type) {
            PeriodType.WEEK -> {
                (0..3).map { weekOffset ->
                    val targetWeek = today.minusWeeks(weekOffset.toLong())
                    when (weekOffset) {
                        0 -> "이번 주"
                        1 -> "저번 주"
                        else -> {
                            val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
                            val startOfWeek = targetWeek.with(weekFields.dayOfWeek(), 1)
                            val endOfWeek = startOfWeek.plusDays(6)
                            val formatter = DateTimeFormatter.ofPattern("MM.dd")
                            "${startOfWeek.format(formatter)} - ${endOfWeek.format(formatter)}"
                        }
                    }
                }.reversed()
            }
            PeriodType.MONTH -> {
                // 최근 5년치 월 옵션 생성 (60개월)
                (0..59).map { monthOffset ->
                    val targetMonth = today.minusMonths(monthOffset.toLong())
                    targetMonth.format(DateTimeFormatter.ofPattern("yyyy.MM"))
                }.reversed()
            }
            PeriodType.YEAR -> {
                (0..4).map { yearOffset ->
                    (today.year - yearOffset).toString()
                }.reversed()
            }
            PeriodType.ALL -> {
                val yearsRange = getRunningYearsRange()
                listOf(yearsRange)
            }
        }
    }

    fun selectPeriodIndex(index: Int) {
        _selectedPeriodIndex.value = index
        // 세부 기간 변경 시 그래프 데이터 업데이트
        updateGraphData()
    }

    /**
     * 특정 인덱스에 대한 그래프 데이터 생성
     */
    fun getGraphDataForIndex(index: Int): List<GraphPoint> {
        val today = LocalDate.now()

        return when (_selectedPeriodType.value) {
            PeriodType.WEEK -> {
                // reversed 리스트이므로: index 3 = 이번 주, index 0 = 3주 전
                val weeksBack = 3 - index

                listOf(
                    GraphPoint("월", 0.1),
                    GraphPoint("화", 0.0),
                    GraphPoint("수", 0.0),
                    GraphPoint("목", 0.0),
                    GraphPoint("금", 2.36),
                    GraphPoint("토", 5.3),
                    GraphPoint("일", 0.0)
                )
            }
            PeriodType.MONTH -> {
                // reversed 리스트이므로: index 59 = 이번 달, index 0 = 59개월 전
                val monthsBack = 59 - index
                val targetMonth = today.minusMonths(monthsBack.toLong())
                val daysInMonth = targetMonth.lengthOfMonth()

                (1..daysInMonth).map { day ->
                    GraphPoint(
                        label = day.toString(),
                        distance = if (day % 7 == 0) (1..5).random().toDouble() else 0.0
                    )
                }
            }
            PeriodType.YEAR -> {
                // reversed 리스트이므로: index 4 = 이번 년도, index 0 = 4년 전
                val yearsBack = 4 - index
                val targetYear = today.year - yearsBack

                (1..12).map { month ->
                    GraphPoint(
                        label = "${month}월",
                        distance = if (month % 3 == 0) (2..8).random().toDouble() else 0.0
                    )
                }
            }
            PeriodType.ALL -> {
                // 데이터가 있는 연도 (최근 5년)
                (0..4).map { yearOffset ->
                    val year = today.year - yearOffset
                    GraphPoint(
                        label = year.toString(),
                        distance = (10..50).random().toDouble()
                    )
                }.reversed()
            }
        }
    }

    private fun updateGraphData() {
        val currentState = _uiState.value
        if (currentState is MyPageUiState.Success) {
            _uiState.value = currentState.copy(
                graphData = generateMockGraphData()
            )
        }
    }

    fun selectRecordTab(index: Int) {
        _selectedRecordTab.value = index
    }

    /**
     * 로그아웃
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // TODO: 로그아웃 처리
            } catch (e: Exception) {
                // TODO: 에러 처리
            }
        }
    }

    // Mock 데이터 생성 함수들
    private fun generateMockStats() = RunningStats(
        totalDistance = 13.0,
        runningDays = 13,
        averagePace = "6'33\"",
        totalTime = "23:07"
    )

    private fun generateMockGraphData(): List<GraphPoint> {
        val today = LocalDate.now()

        return when (_selectedPeriodType.value) {
            PeriodType.WEEK -> {
                // 선택된 주의 일주일 데이터 (월, 화, 수, 목, 금, 토, 일)
                // reversed 리스트이므로: index 3 = 이번 주, index 0 = 3주 전
                val weeksBack = 3 - _selectedPeriodIndex.value
                // TODO: 실제로는 weeksBack을 사용하여 해당 주의 실제 데이터를 가져와야 함

                listOf(
                    GraphPoint("월", 0.1),
                    GraphPoint("화", 0.0),
                    GraphPoint("수", 0.0),
                    GraphPoint("목", 0.0),
                    GraphPoint("금", 2.36),
                    GraphPoint("토", 5.3),
                    GraphPoint("일", 0.0)
                )
            }
            PeriodType.MONTH -> {
                // 선택된 월의 날짜 (1-31일)
                // reversed 리스트이므로: index 59 = 이번 달, index 0 = 59개월 전
                val monthsBack = 59 - _selectedPeriodIndex.value
                val targetMonth = today.minusMonths(monthsBack.toLong())
                val daysInMonth = targetMonth.lengthOfMonth()

                (1..daysInMonth).map { day ->
                    GraphPoint(
                        label = day.toString(),
                        distance = if (day % 7 == 0) (1..5).random().toDouble() else 0.0
                    )
                }
            }
            PeriodType.YEAR -> {
                // 선택된 년도의 12개월 (1-12월)
                // reversed 리스트이므로: index 4 = 이번 년도, index 0 = 4년 전
                val yearsBack = 4 - _selectedPeriodIndex.value
                val targetYear = today.year - yearsBack

                (1..12).map { month ->
                    GraphPoint(
                        label = "${month}월",
                        distance = if (month % 3 == 0) (2..8).random().toDouble() else 0.0
                    )
                }
            }
            PeriodType.ALL -> {
                // 데이터가 있는 연도 (최근 5년)
                (0..4).map { yearOffset ->
                    val year = today.year - yearOffset
                    GraphPoint(
                        label = year.toString(),
                        distance = (10..50).random().toDouble()
                    )
                }.reversed()
            }
        }
    }

    private fun generateMockPersonalRecords() = listOf(
        RunningRecord(
            date = "2025.11.02",
            distance = 5.30,
            pace = "6'03\"",
            time = "0:36"
        ),
        RunningRecord(
            date = "2025.11.01",
            distance = 2.36,
            pace = "5'02\"",
            time = "0:12"
        )
    )

    private fun generateMockChallengeRecords() = emptyList<RunningRecord>()

    /**
     * 기간 타입에 따른 드롭다운 옵션 생성
     */
    fun getPeriodOptions(): List<String> {
        return getPeriodOptionsForType(_selectedPeriodType.value)
    }

    /**
     * 러닝 데이터가 있는 년도 범위 반환
     * 예: "2025년" 또는 "2024년~2025년"
     */
    private fun getRunningYearsRange(): String {
        val currentState = _uiState.value
        if (currentState is MyPageUiState.Success) {
            val graphData = currentState.graphData
            val years = graphData.mapNotNull { it.label.toIntOrNull() }.sorted()

            return when {
                years.isEmpty() -> LocalDate.now().year.toString() + "년"
                years.size == 1 -> "${years.first()}년"
                else -> "${years.first()}년~${years.last()}년"
            }
        }
        return LocalDate.now().year.toString() + "년"
    }
}

/**
 * 마이페이지 화면 UI 상태
 */
sealed class MyPageUiState {
    object Loading : MyPageUiState()
    data class Success(
        val userProfile: UserProfile,
        val stats: RunningStats,
        val graphData: List<GraphPoint>,
        val personalRecords: List<RunningRecord>,
        val challengeRecords: List<RunningRecord>
    ) : MyPageUiState()
    data class Error(val message: String) : MyPageUiState()
}

/**
 * 기간 타입
 */
enum class PeriodType {
    WEEK, MONTH, YEAR, ALL
}

/**
 * 사용자 프로필
 */
data class UserProfile(
    val nickname: String,
    val profileImageUrl: String?
)

/**
 * 러닝 통계
 */
data class RunningStats(
    val totalDistance: Double, // km
    val runningDays: Int,
    val averagePace: String, // "6'33\""
    val totalTime: String // "23:07"
)

/**
 * 그래프 포인트
 */
data class GraphPoint(
    val label: String, // "월", "1", "1월", "2024"
    val distance: Double, // km
    val time: String = "0:00",
    val pace: String = "0'00\"",
    val count: Int = 0
)

/**
 * 러닝 기록
 */
data class RunningRecord(
    val date: String, // "2025.11.02"
    val distance: Double, // km
    val pace: String, // "6'03\""
    val time: String // "0:36"
)
