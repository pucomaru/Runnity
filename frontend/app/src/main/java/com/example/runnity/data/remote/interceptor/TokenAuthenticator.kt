package com.example.runnity.data.remote.interceptor

import com.example.runnity.BuildConfig
import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.request.RefreshTokenRequest
import com.example.runnity.data.model.response.TokenResponse
import com.example.runnity.data.util.TokenManager
import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

/**
 * 401 에러 발생 시 자동으로 토큰을 갱신하는 Authenticator
 *
 * 동작 방식:
 * 1. API 호출 중 401 Unauthorized 에러 발생
 * 2. Refresh Token으로 새로운 Access Token 발급 시도
 * 3. 성공 시 새로운 Access Token으로 원래 요청 재시도
 * 4. 실패 시 null 반환 (로그아웃 처리)
 */
class TokenAuthenticator : Authenticator {

    private val gson = Gson()

    /**
     * 401 에러 발생 시 호출됨
     *
     * @param route 요청 경로
     * @param response 401 응답
     * @return 새로운 요청 (토큰 갱신 성공) 또는 null (갱신 실패)
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        // 이미 한 번 재시도한 경우 (무한 루프 방지)
        if (response.request.header("Authorization-Retry") != null) {
            Timber.w("토큰 재발급 이미 시도했으나 실패 - 로그아웃 필요")
            TokenManager.clearTokens()
            return null
        }

        // Refresh Token 가져오기
        val refreshToken = TokenManager.getRefreshToken()
        if (refreshToken == null) {
            Timber.w("Refresh Token 없음 - 로그아웃 필요")
            TokenManager.clearTokens()
            return null
        }

        // 동기적으로 토큰 재발급 시도
        val newAccessToken = refreshAccessToken(refreshToken)

        return if (newAccessToken != null) {
            // 토큰 갱신 성공 - 새로운 토큰으로 원래 요청 재시도
            Timber.d("토큰 재발급 성공 - 요청 재시도")
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .header("Authorization-Retry", "true")  // 재시도 플래그
                .build()
        } else {
            // 토큰 갱신 실패 - 로그아웃 처리
            Timber.w("토큰 재발급 실패 - 로그아웃 필요")
            TokenManager.clearTokens()
            null
        }
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급 (동기 호출)
     *
     * 참고: Authenticator는 동기적으로 동작해야 하므로 Retrofit이 아닌 OkHttp로 직접 호출
     *
     * @param refreshToken Refresh Token
     * @return 새로운 Access Token (실패 시 null)
     */
    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            // BASE_URL은 BuildConfig에서 가져옴 (local.properties에서 설정)
            val baseUrl = BuildConfig.BASE_URL

            // Request Body 생성
            val requestBody = RefreshTokenRequest(refreshToken)
            val json = gson.toJson(requestBody)
            val body = json.toRequestBody("application/json".toMediaType())

            // HTTP 요청 생성
            val request = Request.Builder()
                .url("${baseUrl}api/v1/auth/token")
                .post(body)
                .build()

            // OkHttp로 동기 호출
            val client = OkHttpClient()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    // BaseResponse<TokenResponse> 파싱
                    val baseResponse = gson.fromJson(
                        responseBody,
                        object : com.google.gson.reflect.TypeToken<BaseResponse<TokenResponse>>() {}.type
                    ) as BaseResponse<TokenResponse>

                    if (baseResponse.isSuccess && baseResponse.data != null) {
                        val tokenResponse = baseResponse.data
                        // 새로운 토큰 저장
                        TokenManager.saveTokens(
                            tokenResponse.accessToken,
                            tokenResponse.refreshToken
                        )
                        return tokenResponse.accessToken
                    }
                }
            }

            Timber.e("토큰 재발급 실패 - HTTP ${response.code}")
            null
        } catch (e: Exception) {
            Timber.e(e, "토큰 재발급 중 예외 발생")
            null
        }
    }
}
