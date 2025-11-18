package com.example.runnity.data.util

import android.content.Context
import android.content.SharedPreferences
import com.example.runnity.data.model.response.ProfileResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * 사용자 프로필 정보 관리 매니저
 * - 메모리 캐시 + SharedPreferences로 프로필 정보 저장
 * - 앱 전역에서 사용자 프로필 접근 가능
 */
object UserProfileManager {

    private const val PREFS_NAME = "user_profile_prefs"
    private const val KEY_PROFILE = "profile_data"

    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    // 메모리 캐시 (빠른 접근)
    private var cachedProfile: ProfileResponse? = null

    // StateFlow로 프로필 상태 관리 (UI에서 구독 가능)
    private val _profile = MutableStateFlow<ProfileResponse?>(null)
    val profile: StateFlow<ProfileResponse?> = _profile.asStateFlow()

    /**
     * UserProfileManager 초기화
     * GlobalApplication onCreate에서 호출 필요
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // 앱 시작 시 로컬 캐시에서 프로필 로드
        loadProfileFromCache()
        Timber.d("UserProfileManager 초기화 완료")
    }

    /**
     * 프로필 정보 저장
     * - 메모리 캐시 + SharedPreferences + StateFlow 모두 저장
     * - commit() 사용으로 동기적 저장 보장
     */
    fun saveProfile(profile: ProfileResponse) {
        cachedProfile = profile
        _profile.value = profile  // StateFlow 갱신
        val json = gson.toJson(profile)
        val success = prefs?.edit()?.putString(KEY_PROFILE, json)?.commit() ?: false
        if (success) {
            Timber.d("프로필 저장 완료: memberId=${profile.memberId}, nickname=${profile.nickname}")
        } else {
            Timber.e("프로필 저장 실패: SharedPreferences write 실패")
        }
    }

    /**
     * 프로필 정보 조회
     * @return ProfileResponse? (없으면 null)
     */
    fun getProfile(): ProfileResponse? {
        // 메모리 캐시 우선 반환
        if (cachedProfile != null) {
            return cachedProfile
        }

        // 메모리에 없으면 SharedPreferences에서 로드
        loadProfileFromCache()
        return cachedProfile
    }

    /**
     * 프로필 정보 업데이트
     * 기존 프로필의 일부 필드만 변경할 때 사용
     */
    fun updateProfile(profile: ProfileResponse) {
        saveProfile(profile)
        Timber.d("프로필 업데이트 완료")
    }

    /**
     * 프로필 정보 삭제
     * 로그아웃 시 호출
     * - commit() 사용으로 동기적 삭제 보장
     */
    fun clearProfile() {
        cachedProfile = null
        _profile.value = null  // StateFlow 갱신
        prefs?.edit()?.clear()?.commit()
        Timber.d("프로필 삭제 완료")
    }

    /**
     * 프로필 존재 여부 확인
     */
    fun hasProfile(): Boolean {
        return getProfile() != null
    }

    /**
     * SharedPreferences에서 프로필 로드
     */
    private fun loadProfileFromCache() {
        val json = prefs?.getString(KEY_PROFILE, null)
        if (json != null) {
            try {
                cachedProfile = gson.fromJson(json, ProfileResponse::class.java)
                _profile.value = cachedProfile  // StateFlow 갱신
                Timber.d("로컬 캐시에서 프로필 로드 완료: memberId=${cachedProfile?.memberId}")
            } catch (e: Exception) {
                Timber.e(e, "프로필 JSON 파싱 실패")
                cachedProfile = null
                _profile.value = null
            }
        } else {
            Timber.d("로컬 캐시에 프로필 없음")
        }
    }
}
