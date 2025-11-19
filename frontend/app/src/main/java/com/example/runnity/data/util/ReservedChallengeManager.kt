package com.example.runnity.data.util

import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.ChallengeSimpleInfo
import com.example.runnity.data.repository.RunHistoryRepository
import com.example.runnity.ui.components.ChallengeButtonState
import com.example.runnity.ui.components.ChallengeListItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.format.DateTimeFormatter

/**
 * 예약한 챌린지 목록 전역 관리 매니저
 * - 예약한 챌린지 목록을 앱 전역에서 공유
 * - 주기적으로 API 호출하여 enterableChallenge 상태 자동 갱신 (1분마다)
 * - 챌린지 시작 5분 전 참여하기 버튼 활성화 상태 자동 반영
 */
object ReservedChallengeManager {

    private val repository = RunHistoryRepository()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var periodicUpdateJob: Job? = null

    // 예약한 챌린지 목록 (enterableChallenge가 최상단)
    private val _reservedChallenges = MutableStateFlow<List<ChallengeListItem>>(emptyList())
    val reservedChallenges: StateFlow<List<ChallengeListItem>> = _reservedChallenges.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 마지막 갱신 시간
    private val _lastRefreshTime = MutableStateFlow(0L)
    val lastRefreshTime: StateFlow<Long> = _lastRefreshTime.asStateFlow()

    /**
     * 주기적 업데이트 시작
     * GlobalApplication onCreate에서 호출
     */
    fun startPeriodicUpdates() {
        Timber.d("ReservedChallengeManager: 주기적 업데이트 시작 (1분 간격)")

        // 즉시 한 번 갱신
        scope.launch {
            refresh()
        }

        // 주기적 업데이트 시작
        periodicUpdateJob?.cancel()
        periodicUpdateJob = scope.launch {
            while (isActive) {
                delay(60_000L) // 1분
                refresh()
            }
        }
    }

    /**
     * 주기적 업데이트 중지
     * 필요 시 호출 (일반적으로는 중지하지 않음)
     */
    fun stopPeriodicUpdates() {
        Timber.d("ReservedChallengeManager: 주기적 업데이트 중지")
        periodicUpdateJob?.cancel()
        periodicUpdateJob = null
    }

    /**
     * 예약한 챌린지 목록 수동 갱신
     * 챌린지 참여/취소 후 호출
     */
    suspend fun refresh() {
        if (_isLoading.value) {
            Timber.d("ReservedChallengeManager: 이미 로딩 중, 스킵")
            return
        }

        _isLoading.value = true
        Timber.d("ReservedChallengeManager: 예약한 챌린지 목록 갱신 시작")

        try {
            when (val resp = repository.getMyChallenges()) {
                is ApiResponse.Success -> {
                    val data = resp.data
                    val list = buildList {
                        // enterableChallenge를 최상단에 배치 (참여하기 버튼 활성화)
                        data.enterableChallenge?.let {
                            val item = mapToListItem(it).copy(
                                buttonState = ChallengeButtonState.Join
                            )
                            add(item)
                        }
                        // 나머지 참가 신청한 챌린지
                        data.joinedChallenges.forEach { add(mapToListItem(it)) }
                    }
                    _reservedChallenges.value = list
                    _lastRefreshTime.value = System.currentTimeMillis()
                    Timber.d("ReservedChallengeManager: 갱신 완료 (${list.size}개)")
                }
                is ApiResponse.Error -> {
                    Timber.e("ReservedChallengeManager: API 에러 - ${resp.message}")
                    // 에러 시 기존 목록 유지
                }
                is ApiResponse.NetworkError -> {
                    Timber.e("ReservedChallengeManager: 네트워크 에러")
                    // 네트워크 에러 시 기존 목록 유지
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "ReservedChallengeManager: 예외 발생")
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * 서버의 간단 챌린지 정보를 홈 리스트 아이템으로 매핑
     */
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

    /**
     * 거리 포맷 변환
     */
    private fun formatDistance(raw: String): String {
        return when (raw) {
            "ONE" -> "1km"
            "TWO" -> "2km"
            "THREE" -> "3km"
            "FOUR" -> "4km"
            "FIVE" -> "5km"
            "SIX" -> "6km"
            "SEVEN" -> "7km"
            "EIGHT" -> "8km"
            "NINE" -> "9km"
            "TEN" -> "10km"
            "FIFTEEN" -> "15km"
            "HALF" -> "하프"
            "M100", "0.1" -> "100m"
            "M500", "0.5" -> "500m"
            else -> {
                val v = raw.toDoubleOrNull()
                when {
                    v == 0.1 -> "100m"
                    v == 0.5 -> "500m"
                    v != null && v >= 1.0 -> "${v.toInt()}km"
                    else -> "${raw}km"
                }
            }
        }
    }

    /**
     * ISO 시간을 로컬 시간으로 변환
     * 서버 시간이 한국 시간 기준이지만 Z(UTC)로 표시되는 상황 고려
     */
    private fun formatIsoToLocal(iso: String): String {
        return try {
            val trimmed = iso.removeSuffix("Z")
            val localDt = java.time.LocalDateTime.parse(trimmed)
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
            localDt.format(formatter)
        } catch (e: Exception) {
            iso
        }
    }
}
