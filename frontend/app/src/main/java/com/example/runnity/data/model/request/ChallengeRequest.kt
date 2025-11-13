package com.example.runnity.data.model.request

/**
 * 챌린지 생성 요청
 *
 * @param title 챌린지 제목
 * @param description 챌린지 설명
 * @param maxParticipants 최대 참여 인원
 * @param startAt 시작 일시 (ISO 8601 형식, 예: "2025-11-12T21:00:00Z")
 * @param distance 목표 거리 (FIVE, TEN 등)
 * @param isPrivate 비공개 여부 (true: 비공개, false: 공개)
 * @param password 비밀번호 (비공개 챌린지인 경우 필수)
 * @param isBroadcast 중계방 사용 여부
 */
data class CreateChallengeRequest(
    val title: String,
    val description: String,
    val maxParticipants: Int,
    val startAt: String,
    val distance: String,
    val isPrivate: Boolean,
    val password: String? = null,
    val isBroadcast: Boolean
)

/**
 * 챌린지 참가 신청 요청
 *
 * @param password 비밀번호 (비공개 챌린지인 경우 필수)
 */
data class JoinChallengeRequest(
    val password: String? = null
)

/**
 * 챌린지 목록 조회 필터 요청
 *
 * @param keyword 검색 키워드
 * @param distance 거리 필터 (FIVE, TEN 등)
 * @param startAt 시작 일시 필터 (이후)
 * @param endAt 종료 일시 필터 (이전)
 * @param visibility 공개 여부 (PUBLIC, PRIVATE)
 * @param sort 정렬 기준 (LATEST, POPULAR 등)
 */
data class ChallengeFilterRequest(
    val keyword: String? = null,
    val distance: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val visibility: String? = null,
    val sort: String? = null
)
