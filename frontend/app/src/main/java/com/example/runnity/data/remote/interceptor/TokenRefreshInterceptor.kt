package com.example.runnity.data.remote.interceptor

import com.example.runnity.BuildConfig
import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.request.RefreshTokenRequest
import com.example.runnity.data.model.response.TokenResponse
import com.example.runnity.data.util.TokenManager
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import timber.log.Timber

/**
 * 403 Forbidden 에러 발생 시 자동으로 토큰을 갱신하는 Interceptor
 *
 * 동작 방식:
 * 1. API 호출 후 응답 확인
 * 2. 403 Forbidden 에러 발생 시 Refresh Token으로 새로운 Access Token 발급 시도
 * 3. 성공 시 새로운 Access Token으로 원래 요청 재시도
 * 4. 실패 시 로그아웃 처리 (토큰 삭제)
 *
 * 무한 루프 방지:
 * - 이미 재시도한 요청은 다시 시도하지 않음 (Authorization-Retry 헤더 확인)
 * - 토큰 재발급 API 자체는 재시도하지 않음
 * - 인증 관련 공개 API는 재시도하지 않음
 */
class TokenRefreshInterceptor : Interceptor {

    private val gson = Gson()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // 403 Forbidden 에러가 아니면 그대로 반환
        if (response.code != 403) {
            return response
        }

        val url = request.url.toString()

        // 인증 관련 공개 API는 재시도 스킵
        // - 로그인 API: 인증 불필요한 공개 엔드포인트
        // - 토큰 API: 무한 재시도 방지
        // - 로그아웃 API: 어차피 로컬 토큰 삭제가 목적이므로 재시도 불필요
        if (url.contains("/auth/login/") ||
            url.contains("/auth/token") ||
            url.contains("/auth/logout")) {
            Timber.d("TokenRefreshInterceptor: 인증 관련 API 스킵 - $url")
            return response
        }

        // 이미 한 번 재시도한 경우 (무한 루프 방지)
        if (request.header("Authorization-Retry") != null) {
            Timber.w("토큰 재발급 이미 시도했으나 실패 - 로그아웃 필요")
            TokenManager.clearTokens()
            response.close()
            return response
        }

        Timber.w("403 Forbidden 발생 - 토큰 재발급 시도: $url")

        // Refresh Token 가져오기
        val refreshToken = TokenManager.getRefreshToken()
        if (refreshToken == null) {
            Timber.w("Refresh Token 없음 - 로그아웃 필요")
            TokenManager.clearTokens()
            return response
        }

        // 동기적으로 토큰 재발급 시도
        val newAccessToken = refreshAccessToken(refreshToken)

        return if (newAccessToken != null) {
            // 토큰 갱신 성공 - 원래 응답 닫고 새로운 토큰으로 재시도
            response.close()

            val newRequest = request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .header("Authorization-Retry", "true")  // 재시도 플래그
                .build()

            Timber.d("토큰 재발급 성공 - 요청 재시도: $url")
            chain.proceed(newRequest)
        } else {
            // 토큰 갱신 실패 - 로그아웃 처리
            Timber.w("토큰 재발급 실패 - 로그아웃 필요")
            TokenManager.clearTokens()
            response
        }
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급 (동기 호출)
     *
     * 참고: Interceptor는 동기적으로 동작해야 하므로 Retrofit이 아닌 OkHttp로 직접 호출
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

            // OkHttp로 동기 호출 (Interceptor/Authenticator를 사용하지 않는 새 클라이언트)
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
                        Timber.d("토큰 재발급 성공")
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
