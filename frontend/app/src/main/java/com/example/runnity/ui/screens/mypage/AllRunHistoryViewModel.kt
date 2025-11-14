package com.example.runnity.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.MonthlyRunsResponse
import com.example.runnity.data.repository.RunHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 운동 기록 화면 ViewModel
 */
class AllRunHistoryViewModel(
    private val runHistoryRepository: RunHistoryRepository = RunHistoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<RunHistoryUiState>(RunHistoryUiState.Loading)
    val uiState: StateFlow<RunHistoryUiState> = _uiState.asStateFlow()

    /**
     * 월간 운동 기록 조회
     */
    fun loadMonthlyRuns(year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.value = RunHistoryUiState.Loading

            try {
                Timber.d("월간 운동 기록 조회 시작: $year-$month")

                when (val response = runHistoryRepository.getMonthlyRuns(year, month)) {
                    is ApiResponse.Success -> {
                        val data = response.data
                        Timber.d("월간 운동 기록 조회 성공: 개인=${data.personals.size}, 챌린지=${data.challenges.size}")

                        // 날짜별로 그룹화
                        val runDateMap = createRunDateMap(data)

                        _uiState.value = RunHistoryUiState.Success(
                            year = year,
                            month = month,
                            runDates = runDateMap,
                            allRecords = convertToRunningRecords(data)
                        )
                    }
                    is ApiResponse.Error -> {
                        Timber.e("월간 운동 기록 조회 실패: ${response.message}")
                        _uiState.value = RunHistoryUiState.Error(response.message)
                    }
                    is ApiResponse.NetworkError -> {
                        Timber.e("월간 운동 기록 조회 실패: 네트워크 오류")
                        _uiState.value = RunHistoryUiState.Error("네트워크 오류가 발생했습니다")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "월간 운동 기록 조회 중 예외 발생")
                _uiState.value = RunHistoryUiState.Error("알 수 없는 오류가 발생했습니다")
            }
        }
    }

    /**
     * 날짜별로 운동 타입 그룹화
     * Map<LocalDate, List<String>> 형태로 변환
     * 예: 2025-11-14 -> ["personal", "challenge"]
     */
    private fun createRunDateMap(data: MonthlyRunsResponse): Map<LocalDate, List<String>> {
        val dateMap = mutableMapOf<LocalDate, MutableSet<String>>()

        // 개인 운동 기록
        data.personals.forEach { record ->
            val date = parseDate(record.startAt)
            if (date != null) {
                dateMap.getOrPut(date) { mutableSetOf() }.add("personal")
            }
        }

        // 챌린지 운동 기록
        data.challenges.forEach { record ->
            val date = parseDate(record.startAt)
            if (date != null) {
                dateMap.getOrPut(date) { mutableSetOf() }.add("challenge")
            }
        }

        return dateMap.mapValues { it.value.toList() }
    }

    /**
     * MonthlyRunsResponse를 RunningRecord 리스트로 변환
     */
    private fun convertToRunningRecords(data: MonthlyRunsResponse): Map<LocalDate, List<RunningRecord>> {
        val recordsByDate = mutableMapOf<LocalDate, MutableList<RunningRecord>>()

        // 개인 운동 기록 변환
        data.personals.forEach { record ->
            val date = parseDate(record.startAt)
            if (date != null) {
                val runningRecord = RunningRecord(
                    id = record.runRecordId.toString(),
                    type = "personal",
                    distance = record.distance,
                    pace = formatPace(record.pace),
                    time = formatTime(record.durationSec),
                    date = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                )
                recordsByDate.getOrPut(date) { mutableListOf() }.add(runningRecord)
            }
        }

        // 챌린지 운동 기록 변환
        data.challenges.forEach { record ->
            val date = parseDate(record.startAt)
            if (date != null) {
                val runningRecord = RunningRecord(
                    id = record.runRecordId.toString(),
                    type = "challenge",
                    distance = record.distance,
                    pace = formatPace(record.pace),
                    time = formatTime(record.durationSec),
                    date = date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                )
                recordsByDate.getOrPut(date) { mutableListOf() }.add(runningRecord)
            }
        }

        return recordsByDate
    }

    /**
     * ISO 8601 날짜 문자열을 LocalDate로 파싱
     */
    private fun parseDate(dateString: String): LocalDate? {
        return try {
            if (dateString.endsWith("Z")) {
                java.time.ZonedDateTime.parse(dateString).toLocalDate()
            } else {
                java.time.LocalDateTime.parse(dateString).toLocalDate()
            }
        } catch (e: Exception) {
            Timber.w(e, "날짜 파싱 실패: $dateString")
            null
        }
    }

    /**
     * 페이스 (초/km) -> "M'SS\"" 형식으로 변환
     */
    private fun formatPace(paceSeconds: Int): String {
        val minutes = paceSeconds / 60
        val seconds = paceSeconds % 60
        return String.format("%d'%02d\"", minutes, seconds)
    }

    /**
     * 시간 (초) -> "H:MM" 또는 "MM:SS" 형식으로 변환
     */
    private fun formatTime(durationSec: Int): String {
        val hours = durationSec / 3600
        val minutes = (durationSec % 3600) / 60
        val seconds = durationSec % 60

        return if (hours > 0) {
            String.format("%d:%02d", hours, minutes)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}

/**
 * 운동 기록 화면 UI 상태
 */
sealed class RunHistoryUiState {
    object Loading : RunHistoryUiState()

    data class Success(
        val year: Int,
        val month: Int,
        val runDates: Map<LocalDate, List<String>>,  // 날짜별 운동 타입 목록
        val allRecords: Map<LocalDate, List<RunningRecord>>  // 날짜별 전체 운동 기록
    ) : RunHistoryUiState()

    data class Error(val message: String) : RunHistoryUiState()
}
