package com.example.runnity.data.model.response

/**
 * 소셜 로그인 응답
 * 구글/카카오 로그인 성공 시 받는 데이터
 */
data class SocialLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val needAdditionalInfo: Boolean,  // 추가 정보 입력 필요 여부
    val newUser: Boolean               // 신규 회원 여부
)

/**
 * 토큰 재발급 응답
 * 새로운 Access Token과 Refresh Token
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

/**
 * 로그아웃 응답
 */
data class LogoutResponse(
    val message: String
)
