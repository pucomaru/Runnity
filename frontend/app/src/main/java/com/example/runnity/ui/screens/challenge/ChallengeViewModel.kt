package com.example.runnity.ui.screens.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 챌린지 화면 ViewModel
 * - 챌린지 목록 조회
 * - 챌린지 참여/취소
 * - 챌린지 검색 및 필터링
 */
class ChallengeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ChallengeUiState>(ChallengeUiState.Loading)
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

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
    data class Success(val challenges: List<Any>) : ChallengeUiState() // TODO: Challenge 모델로 교체
    data class Error(val message: String) : ChallengeUiState()
}
