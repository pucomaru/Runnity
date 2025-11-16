package com.example.runnity.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.runnity.data.repository.RunHistoryRepository
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.ChallengeSimpleInfo
import com.example.runnity.ui.components.ChallengeListItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.runnity.data.repository.ChallengeRepository
import com.example.runnity.socket.WebSocketManager
import com.example.runnity.data.util.TokenManager
import com.example.runnity.data.repository.WeatherRepository
import com.example.runnity.data.model.response.WeatherUiModel
import com.example.runnity.data.model.response.toUiModel
import timber.log.Timber

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

    // 일회성 에러 메시지 이벤트 (토스트 등)
    private val _errorEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorEvents: SharedFlow<String> = _errorEvents

    private val runHistoryRepository = RunHistoryRepository()
    private val challengeRepository = ChallengeRepository()
    private val weatherRepository = WeatherRepository()
    private val joinInFlight = mutableSetOf<String>()

    // 날씨 상태
    private val _weather = MutableStateFlow<WeatherUiModel?>(null)
    val weather: StateFlow<WeatherUiModel?> = _weather.asStateFlow()

    private val _weatherLoading = MutableStateFlow(false)
    val weatherLoading: StateFlow<Boolean> = _weatherLoading.asStateFlow()

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

    // 참여하기 버튼 처리: enter API 호출 후 티켓으로 WebSocket 연결
    // onJoined는 API + 소켓 연결까지 성공했을 때만 호출됨
    fun joinChallengeAndConnect(challengeId: String, onJoined: () -> Unit) {
        if (challengeId.isBlank() || joinInFlight.contains(challengeId)) return
        joinInFlight.add(challengeId)
        viewModelScope.launch {
            try {
                val idLong = challengeId.toLongOrNull()
                if (idLong == null) return@launch
                when (val resp = challengeRepository.enterChallenge(idLong)) {
                    is ApiResponse.Success -> {
                        val ticket = resp.data.ticket
                        val wsUrl = resp.data.wsUrl
                        val url = "$wsUrl?ticket=$ticket"
                        WebSocketManager.connect(
                            url = url,
                            tokenProvider = { TokenManager.getAccessToken() }
                        )

                        // WebSocket 연결 완료될 때까지 대기 후 콜백 호출
                        val state = WebSocketManager.state.first { it is WebSocketManager.WsState.Open || it is WebSocketManager.WsState.Failed }
                        when (state) {
                            is WebSocketManager.WsState.Open -> onJoined()
                            is WebSocketManager.WsState.Failed -> {
                                _errorEvents.tryEmit("챌린지 대기방 연결에 실패했어요. 네트워크 상태를 확인한 후 다시 시도해 주세요.")
                            }
                            else -> Unit
                        }
                    }
                    is ApiResponse.Error -> {
                        val message = when (resp.code) {
                            400 -> "아직 입장 가능한 시간이 아니에요. 챌린지 시작 전에는 입장할 수 없어요."
                            401 -> "인증이 만료되었어요. 다시 로그인한 후 시도해 주세요."
                            404 -> "해당 챌린지를 찾을 수 없거나 참가 중이 아닌 챌린지예요."
                            500 -> "서버 오류로 챌린지 입장에 실패했어요. 잠시 후 다시 시도해 주세요."
                            else -> resp.message.ifBlank { "챌린지 입장 중 오류가 발생했어요." }
                        }
                        _errorEvents.tryEmit(message)
                    }
                    is ApiResponse.NetworkError -> {
                        _errorEvents.tryEmit("네트워크 연결 상태를 확인한 후 다시 시도해 주세요.")
                    }
                }
            } finally {
                joinInFlight.remove(challengeId)
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
                        data.enterableChallenge?.let {
                            val item = mapToListItem(it).copy(
                                buttonState = com.example.runnity.ui.components.ChallengeButtonState.Join
                            )
                            add(item)
                        }
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
            // 서버 시간이 이미 한국 시간 기준인데 Z(UTC)로 표시되는 상황을 고려하여
            // Z를 제거하고 로컬 DateTime으로 그대로 해석
            val trimmed = iso.removeSuffix("Z")
            val localDt = java.time.LocalDateTime.parse(trimmed)
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
            localDt.format(formatter)
        } catch (e: Exception) {
            iso
        }
    }

    /**
     * 위치 기반 날씨 정보 조회
     */
    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherLoading.value = true

            when (val response = weatherRepository.getCurrentWeather(lat, lon)) {
                is ApiResponse.Success -> {
                    _weather.value = response.data.toUiModel()
                    Timber.d("날씨 정보 업데이트: ${_weather.value?.cityName}")
                }
                is ApiResponse.Error -> {
                    Timber.e("날씨 조회 실패: ${response.message}")
                    _weather.value = null
                }
                is ApiResponse.NetworkError -> {
                    Timber.e("날씨 네트워크 오류")
                    _weather.value = null
                }
            }

            _weatherLoading.value = false
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
