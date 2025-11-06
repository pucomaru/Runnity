package com.example.runnity.ui.screens.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 챌린지 화면 ViewModel
 * - 챌린지 목록 조회 및 관리
 * - 챌린지 검색 및 필터링
 * - 챌린지 정렬 (인기순, 최신순)
 * - 챌린지 참여
 */
class ChallengeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ChallengeUiState>(ChallengeUiState.Loading)
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    // TODO: 챌린지 목록 State 추가
    // private val _challenges = MutableStateFlow<List<ChallengeListItem>>(emptyList())
    // val challenges: StateFlow<List<ChallengeListItem>> = _challenges.asStateFlow()

    // TODO: 검색어 State 추가
    // private val _searchQuery = MutableStateFlow("")
    // val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // TODO: 정렬 기준 State 추가
    // private val _sortType = MutableStateFlow("인기순")
    // val sortType: StateFlow<String> = _sortType.asStateFlow()

    init {
        loadChallenges()
    }

    private fun loadChallenges() {
        viewModelScope.launch {
            try {
                // TODO: Repository에서 챌린지 목록 로드
                _uiState.value = ChallengeUiState.Success(emptyList())
            } catch (e: Exception) {
                _uiState.value = ChallengeUiState.Error(e.message ?: "챌린지 로드 실패")
            }
        }
    }

    // TODO: 챌린지 새로고침 함수 구현
    // fun refreshChallenges() {
    //     viewModelScope.launch {
    //         try {
    //             // Repository에서 챌린지 목록 다시 로드
    //             loadChallenges()
    //         } catch (e: Exception) {
    //             // 에러 처리
    //         }
    //     }
    // }

    // TODO: 검색어 변경 함수 구현
    // fun updateSearchQuery(query: String) {
    //     _searchQuery.value = query
    //     // 검색어에 따라 챌린지 목록 필터링
    //     filterChallenges()
    // }

    // TODO: 정렬 기준 변경 함수 구현
    // fun updateSortType(sortType: String) {
    //     _sortType.value = sortType
    //     // 정렬 기준에 따라 챌린지 목록 정렬
    //     sortChallenges()
    // }

    // TODO: 챌린지 필터링 함수 구현
    // private fun filterChallenges() {
    //     // 검색어에 따라 챌린지 목록 필터링
    //     // title에 검색어가 포함된 챌린지만 표시
    // }

    // TODO: 챌린지 정렬 함수 구현
    // private fun sortChallenges() {
    //     // "인기순": participants 기준 정렬 (참여자 많은 순)
    //     // "최신순": startDateTime 기준 정렬 (최신순)
    // }

    /**
     * 챌린지 참여
     */
    fun joinChallenge(challengeId: String) {
        viewModelScope.launch {
            try {
                // TODO: 챌린지 참여 API 호출
            } catch (e: Exception) {
                // TODO: 에러 처리
            }
        }
    }
}

/**
 * 챌린지 화면 UI 상태
 */
sealed class ChallengeUiState {
    object Loading : ChallengeUiState()
    data class Success(val challenges: List<Any>) : ChallengeUiState() // TODO: ChallengeListItem으로 교체
    data class Error(val message: String) : ChallengeUiState()
}
