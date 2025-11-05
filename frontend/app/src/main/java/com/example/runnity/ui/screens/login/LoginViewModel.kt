package com.example.runnity.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 로그인 화면 ViewModel
 * - 로그인 처리
 * - 입력 유효성 검증
 * - 자동 로그인 체크
 */
class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * 로그인 시도
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = LoginUiState.Loading
                // TODO: Repository를 통한 로그인 API 호출
                // TODO: 토큰 저장
                _uiState.value = LoginUiState.Success
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "로그인 실패")
            }
        }
    }

    /**
     * 자동 로그인 체크
     */
    fun checkAutoLogin() {
        viewModelScope.launch {
            try {
                // TODO: 저장된 토큰 확인
                // TODO: 토큰 유효성 검증
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Idle
            }
        }
    }
}

/**
 * 로그인 화면 UI 상태
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
