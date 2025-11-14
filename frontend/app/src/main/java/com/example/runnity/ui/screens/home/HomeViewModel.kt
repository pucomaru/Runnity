package com.example.runnity.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.runnity.data.repository.RunHistoryRepository
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.ChallengeSimpleInfo
import com.example.runnity.ui.components.ChallengeListItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 홈 화면 ViewModel
 * - 최근 러닝 기록 조회
 * - 추천 챌린지 조회
 * - 통계 데이터 관리
 * - 예약한 챌린지의 시작 시간 체크 및 buttonState 자동 변경
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // 예약한 챌린지 리스트 State (enterableChallenge가 최상단)
    private val _reservedChallenges = MutableStateFlow<List<ChallengeListItem>>(emptyList())
    val reservedChallenges: StateFlow<List<ChallengeListItem>> = _reservedChallenges.asStateFlow()

    private val runHistoryRepository = RunHistoryRepository()

    init {
        loadHomeData()
        // TODO: 주기적으로 시간 체크하는 타이머 시작
        // startChallengeTimeChecker()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                // 예약한 챌린지 불러오기 (enterable → joined 순)
                fetchReservedChallenges()
                _uiState.value = HomeUiState.Success
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    // 예약한 챌린지 조회: enterableChallenge를 최상단에 배치
    fun fetchReservedChallenges() {
        viewModelScope.launch {
            when (val resp = runHistoryRepository.getMyChallenges()) {
                is ApiResponse.Success -> {
                    val data = resp.data
                    val list = buildList {
                        data.enterableChallenge?.let { add(mapToListItem(it)) }
                        data.joinedChallenges.forEach { add(mapToListItem(it)) }
                    }
                    _reservedChallenges.value = list
                }
                is ApiResponse.Error -> {
                    // 에러 시 비워두고 상태만 갱신 (UI는 변경하지 않음)
                    _reservedChallenges.value = emptyList()
                }
                ApiResponse.NetworkError -> {
                    _reservedChallenges.value = emptyList()
                }
            }
        }
    }

    // 서버의 간단 챌린지 정보를 홈 리스트 아이템으로 매핑
    private fun mapToListItem(info: ChallengeSimpleInfo): ChallengeListItem {
        val distanceText = formatDistance(info.distance)
        val participants = "${info.currentParticipants}/${info.maxParticipants}명"
        val startText = formatIsoToLocal(info.startAt)
        return ChallengeListItem(
            id = info.challengeId.toString(),
            distance = distanceText,
            title = info.title,
            startDateTime = startText,
            participants = participants
        )
    }

    private fun formatDistance(raw: String): String {
        val v = raw.toDoubleOrNull() ?: return raw
        val iv = v.toInt() // 소수점 버림
        return "${iv}km"
    }

    private fun formatIsoToLocal(iso: String): String {
        return try {
            val instant = Instant.parse(iso)
            val zoned = instant.atZone(ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
            zoned.format(formatter)
        } catch (e: Exception) {
            iso
        }
    }

    // TODO: 챌린지 시작 시간 체크 로직 구현
    // - 예약한 챌린지 리스트를 주기적으로 체크 (예: 1분마다)
    // - 각 챌린지의 startDateTime을 파싱하여 현재 시간과 비교
    // - 시작 5분 전부터는 buttonState를 ChallengeButtonState.Join으로 변경
    // - 시작 시간이 지나면 buttonState를 None으로 변경
    //
    // private fun startChallengeTimeChecker() {
    //     viewModelScope.launch {
    //         while (true) {
    //             delay(60000L) // 1분마다 체크
    //             updateChallengeButtonStates()
    //         }
    //     }
    // }
    //
    // private fun updateChallengeButtonStates() {
    //     val currentTime = System.currentTimeMillis()
    //     val updatedList = _reservedChallenges.value.map { challenge ->
    //         // startDateTime 파싱 (예: "2025.11.05 16:09")
    //         val startTime = parseDateTime(challenge.startDateTime)
    //         val fiveMinutesBefore = startTime - (5 * 60 * 1000)
    //
    //         // 시작 5분 전부터 시작 시간까지는 Join 버튼 표시
    //         val newButtonState = when {
    //             currentTime >= fiveMinutesBefore && currentTime < startTime -> ChallengeButtonState.Join
    //             else -> ChallengeButtonState.None
    //         }
    //
    //         challenge.copy(buttonState = newButtonState)
    //     }
    //     _reservedChallenges.value = updatedList
    // }
    //
    // private fun parseDateTime(dateTimeStr: String): Long {
    //     // "2025.11.05 16:09" 형식을 파싱하여 timestamp로 변환
    //     // SimpleDateFormat 또는 LocalDateTime 사용
    //     return 0L
    // }
}

/**
 * 홈 화면 UI 상태
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    object Success : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
