package com.example.runnity.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.repository.AuthRepository
import com.example.runnity.data.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 로그인 화면 ViewModel
 * - 소셜 로그인 처리 (구글, 카카오)
 * - 자동 로그인 체크
 */
class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * 구글 로그인
     * @param idToken 구글에서 받은 ID Token
     */
    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = authRepository.loginWithGoogle(idToken)) {
                is ApiResponse.Success -> {
                    val data = result.data
                    if (data.needAdditionalInfo) {
                        // 추가 정보 입력 필요
                        _uiState.value = LoginUiState.NeedAdditionalInfo
                    } else {
                        // 로그인 완료
                        _uiState.value = LoginUiState.Success
                    }
                }
                is ApiResponse.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                }
                ApiResponse.NetworkError -> {
                    _uiState.value = LoginUiState.Error("네트워크 연결을 확인해주세요")
                }
            }
        }
    }

    /**
     * 카카오 로그인
     * @param idToken 카카오에서 받은 ID Token
     */
    fun loginWithKakao(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = authRepository.loginWithKakao(idToken)) {
                is ApiResponse.Success -> {
                    val data = result.data
                    if (data.needAdditionalInfo) {
                        // 추가 정보 입력 필요
                        _uiState.value = LoginUiState.NeedAdditionalInfo
                    } else {
                        // 로그인 완료
                        _uiState.value = LoginUiState.Success
                    }
                }
                is ApiResponse.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                }
                ApiResponse.NetworkError -> {
                    _uiState.value = LoginUiState.Error("네트워크 연결을 확인해주세요")
                }
            }
        }
    }

    /**
     * 자동 로그인 체크
     * 저장된 토큰이 있으면 자동으로 홈 화면으로 이동
     */
    fun checkAutoLogin() {
        viewModelScope.launch {
            if (TokenManager.isLoggedIn()) {
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Idle
            }
        }
    }

    /**
     * 에러 상태 초기화
     * 에러 토스트 표시 후 상태를 Idle로 돌림
     */
    fun resetErrorState() {
        _uiState.value = LoginUiState.Idle
    }
}

/**
 * 로그인 화면 UI 상태
 */
sealed class LoginUiState {
    /**
     * 초기 상태
     */
    object Idle : LoginUiState()

    /**
     * 로그인 진행 중
     */
    object Loading : LoginUiState()

    /**
     * 로그인 성공 - 홈 화면으로 이동
     */
    object Success : LoginUiState()

    /**
     * 추가 정보 입력 필요 (신규 회원)
     * 추가 정보 입력 화면으로 이동
     */
    object NeedAdditionalInfo : LoginUiState()

    /**
     * 에러 발생
     */
    data class Error(val message: String) : LoginUiState()
}
