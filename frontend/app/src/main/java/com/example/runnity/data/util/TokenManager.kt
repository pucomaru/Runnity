package com.example.runnity.data.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * JWT 토큰 관리 매니저
 * - EncryptedSharedPreferences로 보안성 높은 토큰 저장
 * - Access Token, Refresh Token 관리
 * - 인증 상태 변화 감지 (로그아웃 시 자동 로그인 화면 이동)
 */
object TokenManager {

    private const val PREFS_NAME = "runnity_secure_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_PROFILE_COMPLETED = "profile_completed"

    private var encryptedPrefs: android.content.SharedPreferences? = null

    /**
     * 인증 상태 Flow
     * - true: 로그인 상태 (토큰 있음)
     * - false: 로그아웃 상태 (토큰 없음)
     *
     * UI에서 이 Flow를 관찰하여 로그인 화면으로 자동 이동 가능
     */
    private val _authenticationState = MutableStateFlow(false)
    val authenticationState: StateFlow<Boolean> = _authenticationState.asStateFlow()

    /**
     * TokenManager 초기화
     * GlobalApplication onCreate에서 호출 필요
     */
    fun init(context: Context) {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // 초기 인증 상태 설정
            _authenticationState.value = isLoggedIn()
            Timber.d("TokenManager 초기화 완료 (인증 상태: ${_authenticationState.value})")
        } catch (e: Exception) {
            Timber.e(e, "TokenManager 초기화 실패")
        }
    }

    /**
     * Access Token 저장
     */
    fun saveAccessToken(token: String) {
        encryptedPrefs?.edit()?.putString(KEY_ACCESS_TOKEN, token)?.apply()
        Timber.d("Access Token 저장됨")
    }

    /**
     * Access Token 가져오기
     */
    fun getAccessToken(): String? {
        return encryptedPrefs?.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Refresh Token 저장
     */
    fun saveRefreshToken(token: String) {
        encryptedPrefs?.edit()?.putString(KEY_REFRESH_TOKEN, token)?.apply()
        Timber.d("Refresh Token 저장됨")
    }

    /**
     * Refresh Token 가져오기
     */
    fun getRefreshToken(): String? {
        return encryptedPrefs?.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * 두 토큰 한 번에 저장
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs?.edit()?.apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
        _authenticationState.value = true
        Timber.d("Access Token & Refresh Token 저장됨 (인증 상태: true)")
    }

    /**
     * 모든 토큰 삭제 (로그아웃 시)
     */
    fun clearTokens() {
        encryptedPrefs?.edit()?.clear()?.apply()
        _authenticationState.value = false
        Timber.d("모든 토큰 삭제됨 (인증 상태: false)")
    }

    /**
     * 로그인 여부 체크
     */
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    /**
     * 프로필 완성 상태 저장
     * 네트워크 에러 시 로컬 캐시로 사용
     *
     * @param completed true: 프로필 완성됨, false: 추가 정보 필요
     */
    fun setProfileCompleted(completed: Boolean) {
        encryptedPrefs?.edit()?.putBoolean(KEY_PROFILE_COMPLETED, completed)?.apply()
        Timber.d("프로필 완성 상태 저장: $completed")
    }

    /**
     * 프로필 완성 상태 조회
     *
     * @return true: 프로필 완성, false: 추가 정보 필요, null: 아직 모름 (첫 실행)
     */
    fun getProfileCompleted(): Boolean? {
        return if (encryptedPrefs?.contains(KEY_PROFILE_COMPLETED) == true) {
            encryptedPrefs?.getBoolean(KEY_PROFILE_COMPLETED, false)
        } else {
            null  // 한 번도 저장한 적 없음
        }
    }
}
