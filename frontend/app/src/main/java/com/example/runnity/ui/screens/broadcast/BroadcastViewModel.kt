package com.example.runnity.ui.screens.broadcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.BroadcastListItem
import com.example.runnity.data.repository.BroadcastRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 중계 화면 ViewModel
 * - 중계 목록 조회 및 관리
 * - 중계 검색 및 필터링
 * - 중계 정렬 (인기순, 최신순)
 * - 중계 참여
 */

class BroadcastViewModel (
    private val repository: BroadcastRepository = BroadcastRepository()
) : ViewModel() {

    // UI 상태
    private val _uiState = MutableStateFlow<BroadcastUiState>(BroadcastUiState.Loading)
    val uiState: StateFlow<BroadcastUiState> = _uiState.asStateFlow()

    // 중계 목록
     private val _broadcastsList = MutableStateFlow<List<BroadcastListItem>>(emptyList())
     val broadcasts: StateFlow<List<BroadcastListItem>> = _broadcastsList.asStateFlow()

    // 중계 라이브 화면
     private val _broadcastLive = MutableStateFlow("")
     val broadcastLive: StateFlow<String> = _broadcastLive.asStateFlow()

    // 검색어
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 정렬 기준 (LATEST, POPULAR 등)
    private val _sortTypeUi = MutableStateFlow("인기순")
    val sortTypeUi: StateFlow<String> = _sortTypeUi.asStateFlow()

    private val _sortType = MutableStateFlow("POPULAR")
    val sortType: StateFlow<String> = _sortType.asStateFlow()

    // 필터 옵션
    private val _distanceFilter = MutableStateFlow<List<String>?>(null)
    val distanceFilter: StateFlow<List<String>?> = _distanceFilter.asStateFlow()

    init {
        loadActiveBroadcasts()
    }

    /**
     * 중계방 목록 로드
     */
    fun loadActiveBroadcasts(
        keyword: String? = _searchQuery.value.takeIf { it.isNotBlank() },
        distance: List<String>? = _distanceFilter.value,
        sort: String? = _sortType.value,
        page: Int = 0,
        size: Int = 20
    ) {
        viewModelScope.launch {
            _uiState.value = BroadcastUiState.Loading

            // 현재 상태와 파라미터 병합
            val actualKeyword = (keyword ?: _searchQuery.value).takeIf { !it.isNullOrBlank() }
            val actualDistance = distance ?: _distanceFilter.value
            val actualSort = sort ?: _sortType.value

            Timber.d("로드 파라미터 q=%s, dist=%s, sort=%s, page=%d, size=%d",
                actualKeyword, actualDistance, actualSort, page, size)

            when (val response = repository.getActiveBroadcasts(
                keyword = actualKeyword,
                distance = actualDistance,
                sort = actualSort,
                page = page,
                size = size
            )) {
                is ApiResponse.Success -> {
                    val server = response.data
                    val mapped = server
                        .map(BroadcastMapper::toItem)
                        .distinctBy { it.challengeId }
                    _broadcastsList.value = mapped
                    _uiState.value = BroadcastUiState.Success(mapped)
                    Timber.d("중계방 목록 로드 성공: %d개", mapped.size)
                }
                is ApiResponse.Error -> {
                    val cached = _broadcastsList.value
                    if (cached.isNotEmpty()) _uiState.value = BroadcastUiState.Success(cached)
                    else _uiState.value = BroadcastUiState.Error(response.message)
                    Timber.e("중계방 목록 로드 실패: ${response.message}")
                }
                is ApiResponse.NetworkError -> {
                    val cached = _broadcastsList.value
                    if (cached.isNotEmpty()) _uiState.value = BroadcastUiState.Success(cached)
                    else _uiState.value = BroadcastUiState.Error("네트워크 연결을 확인해주세요")
                    Timber.e("중계방 목록 로드 실패: 네트워크 오류")
                }
            }
        }
    }

    /**
     * 중계 참여
     */
    fun joinBroadcast(challengeId: String) {
        viewModelScope.launch {
            runCatching {
                repository.joinBroadcast(challengeId.toLong())
                Timber.d("중계방 입장 성공: challengeId=%s", challengeId)
            }.onFailure { Timber.e(it, "중계 참여 실패: %s", challengeId) }
        }
    }

    /**
     * 챌린지 새로고침
     */
    fun refreshChallenges() {
        loadActiveBroadcasts()
    }

    /**
     * 검색어 변경
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * 검색 실행
     */
    fun searchChallenges() {
        loadActiveBroadcasts(keyword = _searchQuery.value)
    }

    /**
     * 정렬 기준 변경
     */
    fun updateSortType(sort: String) {
        _sortTypeUi.value = sort
        val sortCode = when (sort) {
            "인기순" -> "POPULAR"
            "임박순" -> "LATEST" // "최신순" -> "임박순"으로 텍스트 변경
            else -> "POPULAR"
        }
        _sortType.value = sortCode
        loadActiveBroadcasts(sort = sortCode)
    }

    /**
     * 모든 필터 한 번에 적용 (필터 화면에서 사용)
     */
    fun applyFilters(
        distance: List<String>? = null
    ) {
        Timber.d("applyFilters 호출: distance=$distance")
        _distanceFilter.value = distance
        loadActiveBroadcasts(
            distance = distance
        )
    }
}

/**
 * 중계 화면 UI 상태
 */
sealed class BroadcastUiState {
    object Loading : BroadcastUiState()
    data class Success(val broadcasts: List<BroadcastListItem>) : BroadcastUiState()
    data class Error(val message: String) : BroadcastUiState()
}
