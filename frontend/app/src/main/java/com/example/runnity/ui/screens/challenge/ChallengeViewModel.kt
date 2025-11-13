package com.example.runnity.ui.screens.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.repository.ChallengeRepository
import com.example.runnity.data.model.response.ChallengeListItem
import com.example.runnity.data.model.response.ChallengeDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 챌린지 화면 ViewModel
 * - 챌린지 목록 조회 및 관리
 * - 챌린지 검색 및 필터링
 * - 챌린지 정렬 (인기순, 최신순)
 * - 챌린지 참여/취소
 */
class ChallengeViewModel(
    private val repository: ChallengeRepository = ChallengeRepository()
) : ViewModel() {

    // UI 상태
    private val _uiState = MutableStateFlow<ChallengeUiState>(ChallengeUiState.Loading)
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    // 챌린지 목록
    private val _challenges = MutableStateFlow<List<ChallengeListItem>>(emptyList())
    val challenges: StateFlow<List<ChallengeListItem>> = _challenges.asStateFlow()

    // 챌린지 상세 정보
    private val _challengeDetail = MutableStateFlow<ChallengeDetailResponse?>(null)
    val challengeDetail: StateFlow<ChallengeDetailResponse?> = _challengeDetail.asStateFlow()

    // 검색어
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 정렬 기준 (LATEST, POPULAR 등)
    private val _sortType = MutableStateFlow("LATEST")
    val sortType: StateFlow<String> = _sortType.asStateFlow()

    // 필터 옵션
    private val _distanceFilter = MutableStateFlow<List<String>?>(null)
    val distanceFilter: StateFlow<List<String>?> = _distanceFilter.asStateFlow()

    private val _startDateFilter = MutableStateFlow<String?>(null)
    val startDateFilter: StateFlow<String?> = _startDateFilter.asStateFlow()

    private val _endDateFilter = MutableStateFlow<String?>(null)
    val endDateFilter: StateFlow<String?> = _endDateFilter.asStateFlow()

    private val _startTimeFilter = MutableStateFlow<String?>(null)
    val startTimeFilter: StateFlow<String?> = _startTimeFilter.asStateFlow()

    private val _endTimeFilter = MutableStateFlow<String?>(null)
    val endTimeFilter: StateFlow<String?> = _endTimeFilter.asStateFlow()

    private val _visibilityFilter = MutableStateFlow<String?>(null)
    val visibilityFilter: StateFlow<String?> = _visibilityFilter.asStateFlow()

    init {
        loadChallenges()
    }

    /**
     * 챌린지 목록 로드
     */
    fun loadChallenges(
        keyword: String? = _searchQuery.value.takeIf { it.isNotBlank() },
        distances: List<String>? = _distanceFilter.value,
        startDate: String? = _startDateFilter.value,
        endDate: String? = _endDateFilter.value,
        startTime: String? = _startTimeFilter.value,
        endTime: String? = _endTimeFilter.value,
        visibility: String? = _visibilityFilter.value,
        sort: String? = _sortType.value,
        page: Int = 0,
        size: Int = 20
    ) {
        viewModelScope.launch {
            _uiState.value = ChallengeUiState.Loading

            // 실제 사용할 값 (파라미터가 명시적으로 전달되면 그 값을 사용)
            val actualKeyword = keyword ?: _searchQuery.value.takeIf { it.isNotBlank() }
            val actualDistances = distances ?: _distanceFilter.value
            val actualVisibility = visibility ?: _visibilityFilter.value
            val actualSort = sort ?: _sortType.value

            val actualStartDate = startDate ?: _startDateFilter.value ?: java.time.LocalDate.now().toString()
            val actualStartTime = startTime ?: _startTimeFilter.value
            val actualEndDate = endDate ?: _endDateFilter.value
            val actualEndTime = endTime ?: _endTimeFilter.value

            when (val response = repository.getChallenges(
                keyword = actualKeyword,
                distances = actualDistances,
                startDate = actualStartDate,
                endDate = actualEndDate,
                startTime = actualStartTime,
                endTime = actualEndTime,
                visibility = actualVisibility,
                sort = actualSort,
                page = page,
                size = size
            )) {
                is ApiResponse.Success -> {
                    _challenges.value = response.data.content
                    _uiState.value = ChallengeUiState.Success(response.data.content)
                    Timber.d("챌린지 목록 로드 성공: ${response.data.content.size}개")
                }
                is ApiResponse.Error -> {
                    _uiState.value = ChallengeUiState.Error(response.message)
                    Timber.e("챌린지 목록 로드 실패: ${response.message}")
                }
                is ApiResponse.NetworkError -> {
                    _uiState.value = ChallengeUiState.Error("네트워크 연결을 확인해주세요")
                    Timber.e("챌린지 목록 로드 실패: 네트워크 오류")
                }
            }
        }
    }

    /**
     * 챌린지 상세 조회
     */
    fun loadChallengeDetail(challengeId: Long) {
        viewModelScope.launch {
            when (val response = repository.getChallengeDetail(challengeId)) {
                is ApiResponse.Success -> {
                    _challengeDetail.value = response.data
                    Timber.d("챌린지 상세 조회 성공: ${response.data.title}")
                }
                is ApiResponse.Error -> {
                    Timber.e("챌린지 상세 조회 실패: ${response.message}")
                }
                is ApiResponse.NetworkError -> {
                    Timber.e("챌린지 상세 조회 실패: 네트워크 오류")
                }
            }
        }
    }

    /**
     * 챌린지 새로고침
     */
    fun refreshChallenges() {
        loadChallenges()
    }

    /**
     * 검색어 변경
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchChallenges() {
        loadChallenges(keyword = _searchQuery.value)
    }

    /**
     * 정렬 기준 변경
     */
    fun updateSortType(sort: String) {
        _sortType.value = sort
        loadChallenges(sort = sort)
    }

    /**
     * 거리 필터 변경
     */
    fun updateDistanceFilter(distances: List<String>?) {
        _distanceFilter.value = distances
        loadChallenges(distances = distances)
    }

    /**
     * 모든 필터 한 번에 적용 (필터 화면에서 사용)
     */
    fun applyFilters(
        distances: List<String>? = null,
        startDate: String? = null,
        endDate: String? = null,
        startTime: String? = null,
        endTime: String? = null,
        visibility: String? = null
    ) {
        Timber.d("applyFilters 호출: distances=$distances, startDate=$startDate, endDate=$endDate, startTime=$startTime, endTime=$endTime, visibility=$visibility")
        _distanceFilter.value = distances
        _startDateFilter.value = startDate
        _endDateFilter.value = endDate
        _startTimeFilter.value = startTime
        _endTimeFilter.value = endTime
        _visibilityFilter.value = visibility
        loadChallenges(
            distances = distances,
            startDate = startDate,
            endDate = endDate,
            startTime = startTime,
            endTime = endTime,
            visibility = visibility
        )
    }

    /**
     * 챌린지 참가
     */
    fun joinChallenge(challengeId: Long, password: String? = null) {
        viewModelScope.launch {
            when (val response = repository.joinChallenge(challengeId, password)) {
                is ApiResponse.Success -> {
                    Timber.d("챌린지 참가 성공: $challengeId")
                    // 목록 새로고침
                    refreshChallenges()
                    // 상세 정보 새로고침
                    loadChallengeDetail(challengeId)
                }
                is ApiResponse.Error -> {
                    Timber.e("챌린지 참가 실패: ${response.message}")
                }
                is ApiResponse.NetworkError -> {
                    Timber.e("챌린지 참가 실패: 네트워크 오류")
                }
            }
        }
    }

    /**
     * 챌린지 참가 취소
     */
    fun cancelChallenge(challengeId: Long) {
        viewModelScope.launch {
            when (val response = repository.cancelChallenge(challengeId)) {
                is ApiResponse.Success -> {
                    Timber.d("챌린지 참가 취소 성공: $challengeId")
                    // 목록 새로고침
                    refreshChallenges()
                    // 상세 정보 새로고침
                    loadChallengeDetail(challengeId)
                }
                is ApiResponse.Error -> {
                    Timber.e("챌린지 참가 취소 실패: ${response.message}")
                }
                is ApiResponse.NetworkError -> {
                    Timber.e("챌린지 참가 취소 실패: 네트워크 오류")
                }
            }
        }
    }

    /**
     * 챌린지 생성
     */
    fun createChallenge(
        title: String,
        description: String,
        maxParticipants: Int,
        startAt: String,
        distance: String,
        isPrivate: Boolean,
        password: String?,
        isBroadcast: Boolean,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val request = com.example.runnity.data.model.request.CreateChallengeRequest(
                title = title,
                description = description,
                maxParticipants = maxParticipants,
                startAt = startAt,
                distance = distance,
                isPrivate = isPrivate,
                password = password,
                isBroadcast = isBroadcast
            )

            when (val response = repository.createChallenge(request)) {
                is ApiResponse.Success -> {
                    Timber.d("챌린지 생성 성공: ${response.data.challengeId}")
                    // 목록 새로고침
                    refreshChallenges()
                    // 성공 콜백 호출
                    onSuccess(response.data.challengeId)
                }
                is ApiResponse.Error -> {
                    Timber.e("챌린지 생성 실패: ${response.message}")
                    onError(response.message)
                }
                is ApiResponse.NetworkError -> {
                    Timber.e("챌린지 생성 실패: 네트워크 오류")
                    onError("네트워크 연결을 확인해주세요")
                }
            }
        }
    }
}

/**
 * 챌린지 화면 UI 상태
 */
sealed class ChallengeUiState {
    object Loading : ChallengeUiState()
    data class Success(val challenges: List<ChallengeListItem>) : ChallengeUiState()
    data class Error(val message: String) : ChallengeUiState()
}
