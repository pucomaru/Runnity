package com.example.runnity.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.repository.AuthRepository
import com.example.runnity.data.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 앱 시작 시 초기 화면 결정 ViewModel
 *
 * 로직:
 * 1. 토큰 없음 → welcome
 * 2. 토큰 있음 → 프로필 조회 API 호출
 *    - 성공: needAdditionalInfo에 따라 profile_setup or main
 *    - 에러: 401 → login, 기타 → welcome
 *    - 네트워크 에러: 로컬 캐시 확인
 *      - 캐시 없음 (첫 실행) → welcome
 *      - 캐시 있음 → 캐시 값에 따라 profile_setup or main
 */
class AppNavigationViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    /**
     * 앱 시작 시 시작 화면 결정
     * 로컬 캐시 방식 (오프라인 러닝 기록 지원)
     */
    fun checkStartDestination() {
        viewModelScope.launch {
            // 1단계: 토큰 확인
            if (!TokenManager.isLoggedIn()) {
                Timber.d("AppNavigation: 토큰 없음 → welcome")
                _startDestination.value = "welcome"
                return@launch
            }

            // 2단계: 토큰 있으면 프로필 조회
            Timber.d("AppNavigation: 토큰 있음 → 프로필 조회 API 호출")
            when (val result = authRepository.getMyProfile()) {
                is ApiResponse.Success -> {
                    val profile = result.data
                    val needInfo = profile.needAdditionalInfo

                    // 캐시 저장 (다음 네트워크 에러 대비)
                    TokenManager.setProfileCompleted(!needInfo)

                    if (needInfo) {
                        // 추가 정보 입력 필요
                        Timber.d("AppNavigation: needAdditionalInfo=true → profile_setup")
                        _startDestination.value = "profile_setup"
                    } else {
                        // 프로필 완성됨
                        Timber.d("AppNavigation: needAdditionalInfo=false → main")
                        _startDestination.value = "main"
                    }
                }

                is ApiResponse.Error -> {
                    when (result.code) {
                        401 -> {
                            // 토큰 만료/무효 → 토큰 삭제 후 로그인
                            Timber.w("AppNavigation: 토큰 만료 (401) → login (토큰 삭제)")
                            TokenManager.clearTokens()
                            _startDestination.value = "login"
                        }
                        404 -> {
                            // 회원 없음 → 로그인
                            Timber.w("AppNavigation: 회원 없음 (404) → login (토큰 삭제)")
                            TokenManager.clearTokens()
                            _startDestination.value = "login"
                        }
                        else -> {
                            // 기타 에러 → welcome (안전하게 처리)
                            Timber.e("AppNavigation: 프로필 조회 실패 (${result.code}: ${result.message}) → welcome")
                            _startDestination.value = "welcome"
                        }
                    }
                }

                ApiResponse.NetworkError -> {
                    // 네트워크 에러 → 로컬 캐시 확인 (오프라인 모드)
                    val cachedStatus = TokenManager.getProfileCompleted()

                    when (cachedStatus) {
                        null -> {
                            // 캐시 없음 (첫 로그인) → welcome (재시도 유도)
                            Timber.w("AppNavigation: 네트워크 에러 + 캐시 없음 → welcome")
                            _startDestination.value = "welcome"
                        }
                        true -> {
                            // 이전에 프로필 완성 상태 → main (오프라인 모드)
                            Timber.w("AppNavigation: 네트워크 에러 → 캐시 사용 (프로필 완성) → main")
                            _startDestination.value = "main"
                        }
                        false -> {
                            // 이전에 추가정보 필요 상태 → profile_setup
                            Timber.w("AppNavigation: 네트워크 에러 → 캐시 사용 (추가정보 필요) → profile_setup")
                            _startDestination.value = "profile_setup"
                        }
                    }
                }
            }
        }
    }
}
