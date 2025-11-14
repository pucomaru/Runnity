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
    private val _sortType = MutableStateFlow("LATEST")
    val sortType: StateFlow<String> = _sortType.asStateFlow()

    // 필터 옵션
    private val _distanceFilter = MutableStateFlow<String?>(null)
    val distanceFilter: StateFlow<String?> = _distanceFilter.asStateFlow()

    init {
        loadActiveBroadcasts()
    }

    /**
     * 중계방 목록 로드
     */
    fun loadActiveBroadcasts(
        keyword: String? = _searchQuery.value.takeIf { it.isNotBlank() },
        distance: String? = _distanceFilter.value,
        startAt: String? = null,
        endAt: String? = null,
        visibility: String? = null,
        sort: String? = _sortType.value,
        page: Int = 0,
        size: Int = 20
    ) = viewModelScope.launch {
        viewModelScope.launch {
            _uiState.value = BroadcastUiState.Loading

            when (val response = repository.getActiveBroadcasts(
                keyword = keyword,
                distance = distance,
                startAt = startAt,
                endAt = endAt,
                visibility = visibility,
                sort = sort,
                page = page,
                size = size
            )) {
                is ApiResponse.Success -> {
                    val list = response.data
                    val sorted = list.sortedByDescending { it.viewerCount }
                    val items = sorted.map(BroadcastMapper::toItem)
                    _uiState.value = BroadcastUiState.Success(items)
                    Timber.d("중계방 목록 로드 성공: ${response.data.size}개")
                }
                is ApiResponse.Error -> {
                    _uiState.value = BroadcastUiState.Error(response.message)
                    Timber.e("중계방 목록 로드 실패: ${response.message}")
                }
                is ApiResponse.NetworkError -> {
                    _uiState.value = BroadcastUiState.Error("네트워크 연결을 확인해주세요")
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
            try {
                // TODO: 중계 참여 API 호출
            } catch (e: Exception) {
                // TODO: 에러 처리
            }
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
        _sortType.value = sort
        loadActiveBroadcasts(sort = sort)
    }

    /**
     * 거리 필터 변경
     */
    fun updateDistanceFilter(distance: String?) {
        _distanceFilter.value = distance
        loadActiveBroadcasts(distance = distance)
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
