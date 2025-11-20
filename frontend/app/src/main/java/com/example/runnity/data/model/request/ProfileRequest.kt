package com.example.runnity.data.model.request

import com.example.runnity.data.model.common.Gender

/**
 * 신규 회원 추가 정보 입력 요청
 * multipart/form-data로 전송 (data + profileImage)
 */
data class AddInfoRequest(
    val nickname: String,
    val gender: Gender,
    val height: Double,
    val weight: Double,
    val birth: String  // YYYY-MM-DD 형식
)

/**
 * 프로필 수정 요청
 * multipart/form-data로 전송 (data + profileImage)
 *
 * API 스펙: nickname, height, weight만 수정 가능
 */
data class UpdateProfileRequest(
    val nickname: String,
    val height: Double,
    val weight: Double
)
