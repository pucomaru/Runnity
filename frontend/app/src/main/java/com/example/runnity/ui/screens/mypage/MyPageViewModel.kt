package com.example.runnity.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.StatsResponse
import com.example.runnity.data.repository.AuthRepository
import com.example.runnity.data.repository.StatsRepository
import com.example.runnity.data.util.UserProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * 마이페이지 화면 ViewModel
 * - 사용자 프로필 조회
 * - 러닝 통계 조회
 * - 로그아웃 처리
 */
class MyPageViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val statsRepository: StatsRepository = StatsRepository()
) : ViewModel() {

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

    // 그래프 데이터 캐시 (인덱스별로 저장)
    private val graphDataCache = mutableMapOf<String, List<GraphPoint>>()

    init {
        loadUserProfile()
        loadStats()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                // UserProfileManager의 StateFlow 구독
                Timber.d("MyPage: 프로필 StateFlow 구독 시작")

                UserProfileManager.profile.collect { profile ->
                    Timber.d("MyPage: 프로필 갱신 - nickname=${profile?.nickname}, email=${profile?.email}")

                    val currentState = _uiState.value
                    _uiState.value = when (currentState) {
                        is MyPageUiState.Success -> currentState.copy(
                            userProfile = UserProfile(
                                nickname = profile?.nickname ?: "닉네임 없음",
                                profileImageUrl = profile?.profileImageUrl
                            )
                        )
                        else -> MyPageUiState.Success(
                            userProfile = UserProfile(
                                nickname = profile?.nickname ?: "닉네임 없음",
                                profileImageUrl = profile?.profileImageUrl
                            ),
                            stats = RunningStats(0.0, 0, "0'00\"", "0:00"),
                            graphData = emptyList(),
                            personalRecords = emptyList(),
                            challengeRecords = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "MyPage: 프로필 로드 실패")
                _uiState.value = MyPageUiState.Error(e.message ?: "프로필 로드 실패")
            }
        }
    }

    /**
     * 통계 데이터 로드
     * 선택된 기간 타입과 인덱스에 따라 API 호출
     */
    private fun loadStats() {
        loadStatsForIndex(_selectedPeriodIndex.value)
    }

    /**
     * 특정 인덱스의 통계 데이터 로드
     */
    private fun loadStatsForIndex(targetIndex: Int) {
        viewModelScope.launch {
            try {
                val (startDate, endDate, period) = calculateDateRangeForIndex(targetIndex)
                val cacheKey = "${_selectedPeriodType.value}_${targetIndex}"

                Timber.d("MyPage: 통계 로드 - targetIndex=$targetIndex, startDate=$startDate, endDate=$endDate, period=$period, cacheKey=$cacheKey")

                when (val response = statsRepository.getStatsSummary(startDate, endDate, period)) {
                    is ApiResponse.Success -> {
                        val statsData = response.data
                        Timber.d("MyPage: 통계 로드 성공 - targetIndex=$targetIndex, totalDistance=${statsData.totalDistance}")

                        val graphData = convertToGraphData(statsData)
                        // 캐시에 저장 (targetIndex 사용)
                        graphDataCache[cacheKey] = graphData

                        // 현재 선택된 인덱스만 UI 상태 업데이트
                        if (targetIndex == _selectedPeriodIndex.value) {
                            val currentState = _uiState.value
                            if (currentState is MyPageUiState.Success) {
                                _uiState.value = currentState.copy(
                                    stats = convertToRunningStats(statsData),
                                    graphData = graphData,
                                    personalRecords = convertPersonalRecordsToRunningRecords(statsData.personals),
                                    challengeRecords = convertChallengeRecordsToRunningRecords(statsData.challenges)
                                )
                            }
                        }
                    }
                    is ApiResponse.Error -> {
                        Timber.e("MyPage: 통계 로드 실패 - ${response.message}")
                    }
                    is ApiResponse.NetworkError -> {
                        Timber.e("MyPage: 통계 로드 실패 - 네트워크 오류")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "MyPage: 통계 로드 중 예외 발생")
            }
        }
    }

    /**
     * 선택된 기간 타입과 인덱스에 따라 날짜 범위 계산
     * @return Triple(startDate, endDate, period)
     */
    private fun calculateDateRange(): Triple<String, String, String> {
        return calculateDateRangeForIndex(_selectedPeriodIndex.value)
    }

    /**
     * 특정 인덱스에 대한 날짜 범위 계산
     */
    private fun calculateDateRangeForIndex(periodIndex: Int): Triple<String, String, String> {
        val today = LocalDate.now()
        val periodType = _selectedPeriodType.value

        return when (periodType) {
            PeriodType.WEEK -> {
                // reversed 리스트이므로: index 3 = 이번 주, index 0 = 3주 전
                val weeksBack = 3 - periodIndex
                val targetWeek = today.minusWeeks(weeksBack.toLong())
                val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
                val startOfWeek = targetWeek.with(weekFields.dayOfWeek(), 1)
                val endOfWeek = startOfWeek.plusDays(6)

                Timber.d("MyPage: WEEK - index=$periodIndex, weeksBack=$weeksBack, range=${startOfWeek} ~ ${endOfWeek}")

                Triple(
                    "${startOfWeek}T00:00:00",
                    "${endOfWeek}T23:59:59",
                    "week"
                )
            }
            PeriodType.MONTH -> {
                // reversed 리스트이므로: index 59 = 이번 달, index 0 = 59개월 전
                val monthsBack = 59 - periodIndex
                val targetMonth = today.minusMonths(monthsBack.toLong())
                val startOfMonth = targetMonth.with(TemporalAdjusters.firstDayOfMonth())
                val endOfMonth = targetMonth.with(TemporalAdjusters.lastDayOfMonth())

                Timber.d("MyPage: MONTH - index=$periodIndex, monthsBack=$monthsBack, targetMonth=${targetMonth.format(DateTimeFormatter.ofPattern("yyyy.MM"))}")

                Triple(
                    "${startOfMonth}T00:00:00",
                    "${endOfMonth}T23:59:59",
                    "month"
                )
            }
            PeriodType.YEAR -> {
                // reversed 리스트이므로: index 4 = 이번 년도, index 0 = 4년 전
                val yearsBack = 4 - periodIndex
                val targetYear = today.year - yearsBack
                val startOfYear = LocalDate.of(targetYear, 1, 1)
                val endOfYear = LocalDate.of(targetYear, 12, 31)

                Timber.d("MyPage: YEAR - index=$periodIndex, yearsBack=$yearsBack, targetYear=$targetYear")

                Triple(
                    "${startOfYear}T00:00:00",
                    "${endOfYear}T23:59:59",
                    "year"
                )
            }
            PeriodType.ALL -> {
                // 전체: 2024년 1월 1일 ~ 오늘
                Triple(
                    "2024-01-01T00:00:00",
                    "${today}T23:59:59",
                    "all"
                )
            }
        }
    }

    fun selectPeriodType(type: PeriodType) {
        _selectedPeriodType.value = type
        // 기간 타입 변경 시 캐시 초기화
        graphDataCache.clear()
        // 기본값을 이번 주/월/연으로 설정
        _selectedPeriodIndex.value = when (type) {
            PeriodType.WEEK -> 3  // 이번 주 (4개 중 마지막)
            PeriodType.MONTH -> 59  // 이번 달 (60개 중 마지막)
            PeriodType.YEAR -> 4  // 이번 년도 (5개 중 마지막)
            PeriodType.ALL -> 0  // 전체 (1개만 있음)
        }
        // 기간 타입 변경 시 통계 재조회
        loadStats()
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
        Timber.d("MyPage: selectPeriodIndex - index=$index, periodType=${_selectedPeriodType.value}")
        _selectedPeriodIndex.value = index
        // 세부 기간 변경 시 통계 재조회
        loadStats()
    }

    /**
     * StatsResponse를 RunningStats로 변환
     */
    private fun convertToRunningStats(statsResponse: StatsResponse): RunningStats {
        // 총 시간 (초) -> "H:MM:SS" 형식
        val hours = statsResponse.totalTime / 3600
        val minutes = (statsResponse.totalTime % 3600) / 60
        val seconds = statsResponse.totalTime % 60
        val totalTimeString = String.format("%d:%02d:%02d", hours, minutes, seconds)

        // 평균 페이스 (초/km) -> "M'SS\"" 형식
        val paceMinutes = statsResponse.avgPace / 60
        val paceSeconds = statsResponse.avgPace % 60
        val avgPaceString = String.format("%d'%02d\"", paceMinutes, paceSeconds)

        return RunningStats(
            totalDistance = statsResponse.totalDistance,
            runningDays = statsResponse.totalRunDays,
            averagePace = avgPaceString,
            totalTime = totalTimeString
        )
    }

    /**
     * StatsResponse의 periodStats를 GraphData로 변환
     */
    private fun convertToGraphData(statsResponse: StatsResponse): List<GraphPoint> {
        Timber.d("MyPage: periodStats 개수=${statsResponse.periodStats.size}")
        if (statsResponse.periodStats.isNotEmpty()) {
            Timber.d("MyPage: 첫 번째 label=${statsResponse.periodStats.first().label}, 마지막 label=${statsResponse.periodStats.last().label}")
        }

        return statsResponse.periodStats.map { periodStat ->
            // 시간 (초) -> "HH:MM" 형식
            val hours = periodStat.time / 3600
            val minutes = (periodStat.time % 3600) / 60
            val timeString = String.format("%d:%02d", hours, minutes)

            // 페이스 (초/km) -> "M'SS\"" 형식
            val paceMinutes = periodStat.pace / 60
            val paceSeconds = periodStat.pace % 60
            val paceString = String.format("%d'%02d\"", paceMinutes, paceSeconds)

            GraphPoint(
                label = formatGraphLabel(periodStat.label),
                distance = periodStat.distance,
                time = timeString,
                pace = paceString,
                count = periodStat.count,
                timeSeconds = periodStat.time,
                paceSeconds = periodStat.pace
            )
        }
    }

    /**
     * 그래프 레이블 포맷팅
     * "2025-W46" -> "W46", "2025-11" -> "11월", "2025-11-14" -> "14일"
     */
    private fun formatGraphLabel(label: String): String {
        return when {
            // 주별: "2025-W46" -> "W46"
            label.contains("-W") -> label.substringAfter("-")
            // 월별: "2025-11" -> "11월"
            label.matches(Regex("\\d{4}-\\d{2}")) -> "${label.substringAfter("-").toInt()}월"
            // 일별: "2025-11-14" -> "14일" (일단 모두 "일" 붙이기)
            label.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> "${label.substringAfterLast("-").toInt()}일"
            // 년별: "2025" -> "2025"
            else -> label
        }
    }

    /**
     * RunRecordResponse 리스트를 RunningRecord 리스트로 변환
     */
    private fun convertPersonalRecordsToRunningRecords(
        records: List<com.example.runnity.data.model.response.RunRecordResponse>
    ): List<RunningRecord> {
        return records.map { record ->
            // ISO 8601 날짜 파싱: "2025-11-13T09:00:00Z" -> "2025.11.13"
            val dateString = try {
                val localDateTime = if (record.startAt.endsWith("Z")) {
                    java.time.ZonedDateTime.parse(record.startAt).toLocalDateTime()
                } else {
                    java.time.LocalDateTime.parse(record.startAt)
                }
                localDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            } catch (e: Exception) {
                Timber.w(e, "날짜 파싱 실패: ${record.startAt}")
                record.startAt.substringBefore("T")
            }

            // 페이스 (초/km) -> "M'SS\"" 형식
            val paceMinutes = record.pace / 60
            val paceSeconds = record.pace % 60
            val paceString = String.format("%d'%02d\"", paceMinutes, paceSeconds)

            // 시간 (초) -> "H:MM" 형식
            val hours = record.durationSec / 3600
            val minutes = (record.durationSec % 3600) / 60
            val timeString = if (hours > 0) {
                String.format("%d:%02d", hours, minutes)
            } else {
                String.format("0:%02d", minutes)
            }

            RunningRecord(
                id = record.runRecordId.toString(),
                type = "personal",
                date = dateString,
                distance = record.distance,
                pace = paceString,
                time = timeString
            )
        }
    }

    /**
     * ChallengeRunRecordResponse 리스트를 RunningRecord 리스트로 변환
     */
    private fun convertChallengeRecordsToRunningRecords(
        records: List<com.example.runnity.data.model.response.ChallengeRunRecordResponse>
    ): List<RunningRecord> {
        return records.map { record ->
            // ISO 8601 날짜 파싱: "2025-11-13T09:00:00Z" -> "2025.11.13"
            val dateString = try {
                val localDateTime = if (record.startAt.endsWith("Z")) {
                    java.time.ZonedDateTime.parse(record.startAt).toLocalDateTime()
                } else {
                    java.time.LocalDateTime.parse(record.startAt)
                }
                localDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            } catch (e: Exception) {
                Timber.w(e, "날짜 파싱 실패: ${record.startAt}")
                record.startAt.substringBefore("T")
            }

            // 페이스 (초/km) -> "M'SS\"" 형식
            val paceMinutes = record.pace / 60
            val paceSeconds = record.pace % 60
            val paceString = String.format("%d'%02d\"", paceMinutes, paceSeconds)

            // 시간 (초) -> "H:MM" 형식
            val hours = record.durationSec / 3600
            val minutes = (record.durationSec % 3600) / 60
            val timeString = if (hours > 0) {
                String.format("%d:%02d", hours, minutes)
            } else {
                String.format("0:%02d", minutes)
            }

            RunningRecord(
                id = record.runRecordId.toString(),
                type = "challenge",
                date = dateString,
                distance = record.distance,
                pace = paceString,
                time = timeString
            )
        }
    }

    /**
     * 특정 인덱스에 대한 그래프 데이터 반환
     * 캐시에서 가져오거나, 없으면 API 호출하여 로드
     */
    fun getGraphDataForIndex(index: Int): List<GraphPoint> {
        val cacheKey = "${_selectedPeriodType.value}_${index}"

        Timber.d("MyPage: getGraphDataForIndex - index=$index, selectedIndex=${_selectedPeriodIndex.value}, cacheKey=$cacheKey")

        // 캐시에 있으면 반환
        graphDataCache[cacheKey]?.let {
            Timber.d("MyPage: 캐시에서 데이터 반환 - $cacheKey, size=${it.size}")
            return it
        }

        // 캐시에 없으면 API 호출
        Timber.d("MyPage: 캐시에 없음 - API 호출 - $cacheKey")
        loadStatsForIndex(index)

        // API 응답 대기 중이므로 빈 리스트 반환
        return emptyList()
    }

    fun selectRecordTab(index: Int) {
        _selectedRecordTab.value = index
    }

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
    val count: Int = 0,
    val timeSeconds: Int = 0, // 캡션용 원본 데이터 (초)
    val paceSeconds: Int = 0  // 캡션용 원본 데이터 (초)
)

/**
 * 러닝 기록
 */
data class RunningRecord(
    val id: String, // 운동 기록 ID
    val type: String, // "personal" 또는 "challenge"
    val date: String, // "2025.11.02"
    val distance: Double, // km
    val pace: String, // "6'03\""
    val time: String // "0:36"
)
