package com.example.runnity.data.model.request

/**
 * 소셜 로그인 요청
 * 구글/카카오 로그인에 사용
 */
data class SocialLoginRequest(
    val provider: String,  // "GOOGLE" or "KAKAO"
    val idToken: String    // 소셜 플랫폼에서 받은 ID Token
)

/**
 * 토큰 재발급 요청
 * Refresh Token으로 새로운 Access/Refresh Token 발급
 */
data class RefreshTokenRequest(
    val refreshToken: String
)
