package com.example.runnity.data.remote.interceptor

import com.example.runnity.data.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 모든 API 요청에 Access Token을 자동으로 추가하는 Interceptor
 *
 * Authorization: Bearer {accessToken} 헤더를 추가
 */
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getAccessToken()

        val request = if (token != null) {
            // Access Token이 있으면 헤더에 추가
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            // 토큰이 없으면 그대로 전달
            chain.request()
        }

        return chain.proceed(request)
    }
}
