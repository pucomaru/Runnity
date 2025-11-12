package com.example.runnity.ui.screens.broadcast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.api.BroadcastResponse
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.repository.BroadcastRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 중계 화면 ViewModel
 * - 중계 목록 조회 및 관리
 * - 중계 검색 및 필터링
 * - 중계 정렬 (인기순, 최신순)
 * - 중계 참여
 */

class BroadcastViewModel (
    private val broadcastRepository: BroadcastRepository = BroadcastRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BroadcastUiState>(BroadcastUiState.Loading)
    val uiState: StateFlow<BroadcastUiState> = _uiState.asStateFlow()

    // TODO: 중계 목록 State 추가
    // private val _challenges = MutableStateFlow<List<ChallengeListItem>>(emptyList())
    // val challenges: StateFlow<List<ChallengeListItem>> = _challenges.asStateFlow()

    // TODO: 중계 State 추가
    // private val _searchQuery = MutableStateFlow("")
    // val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // TODO: 정렬 기준 State 추가
    // private val _sortType = MutableStateFlow("인기순")
    // val sortType: StateFlow<String> = _sortType.asStateFlow()

    init {
        loadActiveBroadcasts()
    }

    private fun loadActiveBroadcasts() {
        viewModelScope.launch {
            _uiState.value = BroadcastUiState.Loading
            when (val result = broadcastRepository.getActiveBroadcasts()) {
                is ApiResponse.Success -> {
                    _uiState.value = BroadcastUiState.Success(result.data ?: emptyList())
                }
                is ApiResponse.Error -> {
                    _uiState.value = BroadcastUiState.Error(result.message ?: "중계방 목록 로드 실패")
                }

                else -> {}
            }
        }
    }

    // TODO: 중계 새로고침 함수 구현
    // fun refreshChallenges() {
    //     viewModelScope.launch {
    //         try {
    //             // Repository에서 중계 목록 다시 로드
    //             loadChallenges()
    //         } catch (e: Exception) {
    //             // 에러 처리
    //         }
    //     }
    // }

    // TODO: 검색어 변경 함수 구현
    // fun updateSearchQuery(query: String) {
    //     _searchQuery.value = query
    //     // 검색어에 따라 중계 목록 필터링
    //     filterChallenges()
    // }

    // TODO: 정렬 기준 변경 함수 구현
    // fun updateSortType(sortType: String) {
    //     _sortType.value = sortType
    //     // 정렬 기준에 따라 중계 목록 정렬
    //     sortChallenges()
    // }

    // TODO: 중계 필터링 함수 구현
    // private fun filterChallenges() {
    //     // 검색어에 따라 중계 목록 필터링
    //     // title에 검색어가 포함된 중계만 표시
    // }

    // TODO: 중계 정렬 함수 구현
    // private fun sortChallenges() {
    //     // "인기순": participants 기준 정렬 (참여자 많은 순)
    //     // "최신순": startDateTime 기준 정렬 (최신순)
    // }

    /**
     * 중계 참여
     */
    fun joinChallenge(challengeId: String) {
        viewModelScope.launch {
            try {
                // TODO: 중계 참여 API 호출
            } catch (e: Exception) {
                // TODO: 에러 처리
            }
        }
    }
}

/**
 * 중계 화면 UI 상태
 */
sealed class BroadcastUiState {
    object Loading : BroadcastUiState()
    data class Success(val broadcasts: List<BroadcastResponse>) : BroadcastUiState()
    data class Error(val message: String) : BroadcastUiState()
}
