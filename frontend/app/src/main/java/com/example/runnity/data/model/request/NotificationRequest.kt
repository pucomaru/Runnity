package com.example.runnity.data.model.request

/**
 * FCM 토큰 저장 요청
 *
 * 로그인 또는 회원가입 시 발급받은 FCM 토큰을 서버에 저장합니다.
 *
 * @param token 발급된 FCM 토큰 (예: "dY6sAfX-123abc_xyz...Uo")
 */
data class FcmTokenRegisterRequest(
    val token: String
)

/**
 * FCM 토큰 삭제 요청
 *
 * 로그아웃 시 기존에 저장된 FCM 토큰을 서버에서 삭제합니다.
 *
 * @param token 삭제할 FCM 토큰 (예: "dY6sAfX-123abc_xyz...Uo")
 */
data class FcmTokenDeleteRequest(
    val token: String
)
