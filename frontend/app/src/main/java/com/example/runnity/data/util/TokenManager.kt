package com.example.runnity.data.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber

/**
 * JWT 토큰 관리 매니저
 * - EncryptedSharedPreferences로 보안성 높은 토큰 저장
 * - Access Token, Refresh Token 관리
 */
object TokenManager {

    private const val PREFS_NAME = "runnity_secure_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private var encryptedPrefs: android.content.SharedPreferences? = null

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
            Timber.d("TokenManager 초기화 완료")
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
        Timber.d("Access Token & Refresh Token 저장됨")
    }

    /**
     * 모든 토큰 삭제 (로그아웃 시)
     */
    fun clearTokens() {
        encryptedPrefs?.edit()?.clear()?.apply()
        Timber.d("모든 토큰 삭제됨")
    }

    /**
     * 로그인 여부 체크
     */
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }
}
