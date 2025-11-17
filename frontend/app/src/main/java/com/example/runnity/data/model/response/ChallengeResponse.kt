package com.example.runnity.data.model.response

/**
 * 챌린지 목록 조회 응답
 *
 * @param content 챌린지 리스트
 * @param totalElements 전체 챌린지 수
 * @param totalPages 전체 페이지 수
 * @param page 현재 페이지 번호
 * @param size 페이지 크기
 */
data class ChallengeListResponse(
    val content: List<ChallengeListItem>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)

/**
 * 챌린지 목록 아이템
 *
 * @param challengeId 챌린지 고유 ID
 * @param title 챌린지 제목
 * @param status 챌린지 상태 (RECRUITING, IN_PROGRESS, COMPLETED 등)
 * @param maxParticipants 최대 참여 인원
 * @param currentParticipants 현재 참여 인원
 * @param startAt 시작 일시 (ISO 8601)
 * @param endAt 종료 일시 (ISO 8601)
 * @param distance 목표 거리 (FIVE, TEN 등)
 * @param isPrivate 비공개 여부
 * @param isBroadcast 중계방 사용 여부
 * @param isJoined 내가 참여했는지 여부
 * @param createdAt 생성 일시
 */
data class ChallengeListItem(
    val challengeId: Long,
    val title: String,
    val status: String,
    val maxParticipants: Int,
    val currentParticipants: Int,
    val startAt: String,
    val endAt: String,
    val distance: String,
    val isPrivate: Boolean,
    val isBroadcast: Boolean,
    val isJoined: Boolean,
    val createdAt: String
)

/**
 * 챌린지 상세 조회 응답
 *
 * @param challengeId 챌린지 고유 ID
 * @param title 챌린지 제목
 * @param status 챌린지 상태 (RECRUITING, IN_PROGRESS, COMPLETED 등)
 * @param currentParticipants 현재 참여 인원
 * @param maxParticipants 최대 참여 인원
 * @param startAt 시작 일시 (ISO 8601)
 * @param endAt 종료 일시 (ISO 8601)
 * @param description 챌린지 설명
 * @param distance 목표 거리 (FIVE, TEN 등)
 * @param isPrivate 비공개 여부
 * @param isBroadcast 중계방 사용 여부
 * @param joined 내가 참여했는지 여부
 * @param participants 참여자 목록
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 */
data class ChallengeDetailResponse(
    val challengeId: Long,
    val title: String,
    val status: String,
    val currentParticipants: Int,
    val maxParticipants: Int,
    val startAt: String,
    val endAt: String,
    val description: String,
    val distance: String,
    val isPrivate: Boolean,
    val isBroadcast: Boolean,
    val joined: Boolean,
    val participants: List<ChallengeParticipantInfo>,
    val createdAt: String,
    val updatedAt: String
)

/**
 * 챌린지 참여자 정보
 *
 * @param memberId 회원 ID
 * @param nickname 닉네임
 * @param profileImage 프로필 이미지 URL
 * @param rank 랭킹
 * @param status 참여 상태 (JOINED 등)
 * @param averagePaceSec 평균 페이스 (초 단위)
 * @param paceSec 페이스 (초 단위)
 */
data class ChallengeParticipantInfo(
    val memberId: Long,
    val nickname: String,
    val profileImage: String?,
    val rank: Int,
    val status: String,
    val averagePaceSec: Int,
    val paceSec: Int? = null
)

/**
 * 챌린지 참가/취소 응답
 *
 * @param participantId 참가자 ID
 * @param challengeId 챌린지 ID
 * @param memberId 회원 ID
 * @param status 참가 상태 (WAITING, CONFIRMED 등)
 * @param rank 순위
 * @param averagePace 평균 페이스 (밀리초 단위)
 */
data class ChallengeParticipantResponse(
    val participantId: Long,
    val challengeId: Long,
    val memberId: Long,
    val status: String,
    val rank: Int,
    val averagePace: Int
)
