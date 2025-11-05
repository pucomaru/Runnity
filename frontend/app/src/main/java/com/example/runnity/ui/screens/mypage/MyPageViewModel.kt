package com.example.runnity.ui.screens.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 마이페이지 화면 ViewModel
 * - 사용자 프로필 조회
 * - 러닝 통계 조회
 * - 로그아웃 처리
 */
class MyPageViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MyPageUiState>(MyPageUiState.Loading)
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                // TODO: Repository에서 사용자 프로필 로드
                _uiState.value = MyPageUiState.Success
            } catch (e: Exception) {
                _uiState.value = MyPageUiState.Error(e.message ?: "프로필 로드 실패")
            }
        }
    }

    /**
     * 로그아웃
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // TODO: 로그아웃 처리
            } catch (e: Exception) {
                // TODO: 에러 처리
            }
        }
    }
}

/**
 * 마이페이지 화면 UI 상태
 */
sealed class MyPageUiState {
    object Loading : MyPageUiState()
    object Success : MyPageUiState()
    data class Error(val message: String) : MyPageUiState()
}
