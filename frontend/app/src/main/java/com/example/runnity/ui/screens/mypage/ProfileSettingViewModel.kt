package com.example.runnity.ui.screens.mypage

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.request.UpdateProfileRequest
import com.example.runnity.data.repository.AuthRepository
import com.example.runnity.data.util.UserProfileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 프로필 설정 ViewModel
 * - 프로필 수정
 * - 닉네임 중복 체크
 * - 로그아웃
 */
class ProfileSettingViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    // 닉네임 중복 체크 상태
    private val _nicknameCheckState = MutableStateFlow<NicknameCheckState>(NicknameCheckState.Idle)
    val nicknameCheckState: StateFlow<NicknameCheckState> = _nicknameCheckState.asStateFlow()

    // 프로필 수정 상태
    private val _updateState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState: StateFlow<UpdateProfileState> = _updateState.asStateFlow()

    // 로그아웃 상태
    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

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
     * 프로필 수정
     */
    fun updateProfile(
        nickname: String,
        height: Double,
        weight: Double,
        profileImageUri: Uri?
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateProfileState.Loading

            val request = UpdateProfileRequest(
                nickname = nickname,
                height = height,
                weight = weight
            )

            when (val result = authRepository.updateMyProfile(request, profileImageUri)) {
                is ApiResponse.Success -> {
                    Timber.d("ProfileSetting: 프로필 수정 성공")
                    // UserProfileManager에 저장
                    UserProfileManager.saveProfile(result.data)
                    _updateState.value = UpdateProfileState.Success
                }
                is ApiResponse.Error -> {
                    Timber.e("ProfileSetting: 프로필 수정 실패 (${result.code}: ${result.message})")
                    _updateState.value = UpdateProfileState.Error(result.message)
                }
                ApiResponse.NetworkError -> {
                    Timber.e("ProfileSetting: 프로필 수정 실패 (네트워크 에러)")
                    _updateState.value = UpdateProfileState.Error("네트워크 연결을 확인해주세요")
                }
            }
        }
    }

    /**
     * 로그아웃
     * 성공/실패 여부와 관계없이 로컬 토큰은 삭제되므로 항상 성공으로 처리
     */
    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading

            when (authRepository.logout()) {
                is ApiResponse.Success,
                is ApiResponse.Error,
                ApiResponse.NetworkError -> {
                    // 모든 경우 성공으로 처리 (로컬 토큰은 이미 삭제됨)
                    _logoutState.value = LogoutState.Success
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
     * 수정 상태 초기화
     */
    fun resetUpdateState() {
        _updateState.value = UpdateProfileState.Idle
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
 * 프로필 수정 상태
 */
sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

/**
 * 로그아웃 상태
 */
sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    object Success : LogoutState()
    data class Error(val message: String) : LogoutState()
}
