package com.example.runnity.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.common.Gender
import com.example.runnity.data.model.request.AddInfoRequest
import com.example.runnity.data.repository.AuthRepository
import com.example.runnity.data.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 프로필 추가 정보 입력 ViewModel
 * - 닉네임 중복 체크
 * - 추가 정보 입력 (키, 몸무게, 생년월일, 성별)
 */
class ProfileSetupViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _nicknameCheckState = MutableStateFlow<NicknameCheckState>(NicknameCheckState.Idle)
    val nicknameCheckState: StateFlow<NicknameCheckState> = _nicknameCheckState.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    /**
     * 닉네임 중복 체크
     */
    fun checkNickname(nickname: String) {
        viewModelScope.launch {
            _nicknameCheckState.value = NicknameCheckState.Loading

            when (val result = authRepository.checkNickname(nickname)) {
                is ApiResponse.Success -> {
                    if (result.data.available) {
                        _nicknameCheckState.value = NicknameCheckState.Available
                    } else {
                        _nicknameCheckState.value = NicknameCheckState.Unavailable
                    }
                }
                is ApiResponse.Error -> {
                    _nicknameCheckState.value = NicknameCheckState.Error(result.message)
                }
                ApiResponse.NetworkError -> {
                    _nicknameCheckState.value = NicknameCheckState.Error("네트워크 연결을 확인해주세요")
                }
            }
        }
    }

    /**
     * 추가 정보 제출
     */
    fun submitAdditionalInfo(
        nickname: String,
        gender: Gender,
        height: Double,
        weight: Double,
        birth: String  // YYYY-MM-DD 형식
    ) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading

            val request = AddInfoRequest(
                nickname = nickname,
                gender = gender,
                height = height,
                weight = weight,
                birth = birth
            )

            when (val result = authRepository.addAdditionalInfo(request, null)) {
                is ApiResponse.Success -> {
                    // 추가 정보 입력 완료 → 프로필 완성 상태 저장
                    TokenManager.setProfileCompleted(true)
                    _submitState.value = SubmitState.Success
                }
                is ApiResponse.Error -> {
                    _submitState.value = SubmitState.Error(result.message)
                }
                ApiResponse.NetworkError -> {
                    _submitState.value = SubmitState.Error("네트워크 연결을 확인해주세요")
                }
            }
        }
    }

    /**
     * 닉네임 체크 상태 초기화
     */
    fun resetNicknameCheck() {
        _nicknameCheckState.value = NicknameCheckState.Idle
    }

    /**
     * 제출 에러 상태 초기화
     */
    fun resetSubmitError() {
        _submitState.value = SubmitState.Idle
    }
}

/**
 * 닉네임 체크 상태
 */
sealed class NicknameCheckState {
    object Idle : NicknameCheckState()
    object Loading : NicknameCheckState()
    object Available : NicknameCheckState()
    object Unavailable : NicknameCheckState()
    data class Error(val message: String) : NicknameCheckState()
}

/**
 * 제출 상태
 */
sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    object Success : SubmitState()
    data class Error(val message: String) : SubmitState()
}
