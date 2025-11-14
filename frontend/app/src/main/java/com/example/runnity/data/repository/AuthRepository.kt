package com.example.runnity.data.repository

import android.net.Uri
import com.example.runnity.data.api.RetrofitInstance
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.request.AddInfoRequest
import com.example.runnity.data.model.request.SocialLoginRequest
import com.example.runnity.data.model.request.UpdateProfileRequest
import com.example.runnity.data.model.response.AddInfoResponse
import com.example.runnity.data.model.response.NicknameCheckResponse
import com.example.runnity.data.model.response.ProfileResponse
import com.example.runnity.data.model.response.SocialLoginResponse
import com.example.runnity.data.remote.api.AuthApiService
import com.example.runnity.data.util.FcmTokenManager
import com.example.runnity.data.util.TokenManager
import com.example.runnity.data.util.UserProfileManager
import com.example.runnity.data.util.safeApiCall
import com.example.runnity.data.util.safeApiCallUnit
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber

/**
 * Auth 관련 Repository
 * 소셜 로그인, 토큰 관리, 프로필 관리 로직 처리
 */
class AuthRepository(
    private val authApiService: AuthApiService = RetrofitInstance.authApi,
    private val tokenManager: TokenManager = TokenManager
) {

    private val gson = Gson()

    // ==================== 소셜 로그인 ====================

    /**
     * 구글 로그인
     * @param idToken 구글 ID Token
     * @return ApiResponse<SocialLoginResponse>
     */
    suspend fun loginWithGoogle(idToken: String): ApiResponse<SocialLoginResponse> {
        val request = SocialLoginRequest(
            provider = "GOOGLE",
            idToken = idToken
        )

        return safeApiCall {
            authApiService.loginWithGoogle(request)
        }.also { response ->
            // 로그인 성공 시 토큰 저장
            if (response is ApiResponse.Success) {
                tokenManager.saveTokens(
                    response.data.accessToken,
                    response.data.refreshToken
                )
            }
        }
    }

    /**
     * 카카오 로그인
     * @param idToken 카카오 ID Token
     * @return ApiResponse<SocialLoginResponse>
     */
    suspend fun loginWithKakao(idToken: String): ApiResponse<SocialLoginResponse> {
        val request = SocialLoginRequest(
            provider = "KAKAO",
            idToken = idToken
        )

        return safeApiCall {
            authApiService.loginWithKakao(request)
        }.also { response ->
            // 로그인 성공 시 토큰 저장
            if (response is ApiResponse.Success) {
                tokenManager.saveTokens(
                    response.data.accessToken,
                    response.data.refreshToken
                )
            }
        }
    }

    /**
     * 로그아웃
     * Access Token을 블랙리스트에 등록하고 로컬 토큰 및 프로필 정보 삭제
     */
    suspend fun logout(): ApiResponse<Unit> {
        return safeApiCallUnit {
            authApiService.logout()
        }.also {
            try {
                val token = FirebaseMessaging.getInstance().token.await()

                FcmTokenManager.deleteTokenFromServer(token)

            } catch (e: Exception) {
                // 실패해도 로그아웃은 진행해야 함
                Timber.e(e, "로그아웃 중 FCM 토큰 삭제 실패")
            }

            tokenManager.clearTokens()
            UserProfileManager.clearProfile()
        }
    }

    // ==================== 회원 정보 ====================

    /**
     * 추가 정보 입력 (신규 회원)
     * multipart/form-data로 data + profileImage 전송
     *
     * @param request 추가 정보 (nickname, gender, height, weight, birth)
     * @param profileImageUri 프로필 이미지 URI (선택)
     * @return ApiResponse<AddInfoResponse>
     */
    suspend fun addAdditionalInfo(
        request: AddInfoRequest,
        profileImageUri: Uri?
    ): ApiResponse<AddInfoResponse> {
        // JSON을 RequestBody로 변환
        val json = gson.toJson(request)
        val dataBody = json.toRequestBody("application/json".toMediaType())

        // 이미지를 MultipartBody.Part로 변환
        val imagePart = profileImageUri?.let { uri ->
            createMultipartFromUri(uri, "profileImage")
        }

        return safeApiCall {
            authApiService.addAdditionalInfo(dataBody, imagePart)
        }
    }

    /**
     * 내 프로필 조회
     * @return ApiResponse<ProfileResponse>
     */
    suspend fun getMyProfile(): ApiResponse<ProfileResponse> {
        return safeApiCall {
            authApiService.getMyProfile()
        }
    }

    /**
     * 내 프로필 수정
     * multipart/form-data로 data + profileImage 전송
     *
     * @param request 수정할 정보 (nullable)
     * @param profileImageUri 프로필 이미지 URI (선택)
     * @return ApiResponse<ProfileResponse>
     */
    suspend fun updateMyProfile(
        request: UpdateProfileRequest,
        profileImageUri: Uri?
    ): ApiResponse<ProfileResponse> {
        // JSON을 RequestBody로 변환
        val json = gson.toJson(request)
        val dataBody = json.toRequestBody("application/json".toMediaType())

        // 이미지를 MultipartBody.Part로 변환
        val imagePart = profileImageUri?.let { uri ->
            createMultipartFromUri(uri, "profileImage")
        }

        return safeApiCall {
            authApiService.updateMyProfile(dataBody, imagePart)
        }
    }

    /**
     * 닉네임 중복 체크
     * @param nickname 체크할 닉네임
     * @return ApiResponse<NicknameCheckResponse> (available: true/false)
     */
    suspend fun checkNickname(nickname: String): ApiResponse<NicknameCheckResponse> {
        return safeApiCall {
            authApiService.checkNickname(nickname)
        }
    }

    // ==================== 헬퍼 함수 ====================

    /**
     * Uri를 MultipartBody.Part로 변환
     * 실제 구현은 Context가 필요하므로 ViewModel에서 처리하거나
     * Repository에 Context를 주입해야 함
     *
     * TODO: 실제 구현 필요 (ContentResolver로 파일 읽기)
     */
    private fun createMultipartFromUri(uri: Uri, paramName: String): MultipartBody.Part? {
        // 실제 구현에서는 Context를 통해 Uri → File 변환 필요
        // 임시로 null 반환
        return null
    }
}
