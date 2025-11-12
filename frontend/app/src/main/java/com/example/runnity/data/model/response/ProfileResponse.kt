package com.example.runnity.data.model.response

import com.example.runnity.data.model.common.Gender

/**
 * 프로필 정보 응답
 * 내 프로필 조회 시 받는 데이터
 */
data class ProfileResponse(
    val memberId: Long,
    val email: String,
    val profileImageUrl: String?,           // 프로필 이미지 URL
    val nickname: String?,                  // 닉네임 (추가 정보 미입력 시 null)
    val gender: Gender?,                    // 성별 (추가 정보 미입력 시 null)
    val height: Double?,                    // 키 (추가 정보 미입력 시 null)
    val weight: Double?,                    // 몸무게 (추가 정보 미입력 시 null)
    val birth: String?,                     // 생년월일 YYYY-MM-DD (추가 정보 미입력 시 null)
    val averagePace: Int?,                  // 평균 페이스 (초 단위)
    val needAdditionalInfo: Boolean         // 추가 정보 입력 필요 여부
)

/**
 * 닉네임 중복 체크 응답
 */
data class NicknameCheckResponse(
    val available: Boolean  // true: 사용 가능, false: 중복
)

/**
 * 추가 정보 입력 성공 응답
 */
data class AddInfoResponse(
    val message: String
)
