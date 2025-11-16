package com.example.runnity.data.model.response

/**
 * 중계방 목록 조회 응답
 *
 * @param challengeId 챌린지 고유 ID
 * @param title 챌린지 제목
 * @param viewerCount 현재 시청자 수
 * @param participantCount 참가자 수
 * @param createdAt 생성 일시
 */
data class BroadcastResponse(
    val challengeId: Long,
    val title: String,
    val viewerCount: Int,
    val participantCount: Int,
    val createdAt: String,
    val distance: String
)

/**
 * 챌린지 목록 아이템
 *
 * @param challengeId 챌린지 고유 ID
 * @param title 챌린지 제목
 * @param viewerCount 현재 시청자 수
 * @param participantCount 참가자 수
 * @param createdAt 생성 일시
 */
data class BroadcastListItem(
    val challengeId: Long, // 챌린지 고유 ID
    val title: String, // 챌린지 제목
    val viewerCount: Int, // 현재 시청자 수
    val participantCount: Int, // 챌린지 참가자 수
    val createdAt: String, // 세션 생성 시작 날짜
    val distance: String // 거리
)

data class BroadcastPage(
    val content: List<BroadcastListItem>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)
