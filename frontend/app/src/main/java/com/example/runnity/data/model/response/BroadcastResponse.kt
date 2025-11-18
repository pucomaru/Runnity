package com.example.runnity.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 중계방 목록 조회 응답
 *
 * @param challengeId 챌린지 고유 ID
 * @param title 챌린지 제목
 * @param viewerCount 현재 시청자 수
 * @param participantCount 참가자 수
 * @param createdAt 생성 일시
 * @param distance 거리
 */
data class BroadcastResponse(
    @SerializedName("challengeId")
    val challengeId: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("viewerCount")
    val viewerCount: Int,

    @SerializedName("participantCount")
    val participantCount: Int,

    @SerializedName("createdAt")
    val createdAt: String,  // ISO 8601 형식 (예: "2025-11-17T08:00:00")

    @SerializedName("distance")
    val distance: String  // ← 거리 코드 (예: "FIVE" = 5km)
)

/**
 * 중계 목록 아이템
 *
 * @param challengeId 챌린지 고유 ID
 * @param title 챌린지 제목
 * @param viewerCount 현재 시청자 수
 * @param participantCount 참가자 수
 * @param createdAt 생성 일시
 * @param distance 거리
 *
 */
data class BroadcastListItem(
    val challengeId: Long, // 챌린지 고유 ID
    val title: String, // 챌린지 제목
    val viewerCount: Int, // 현재 시청자 수
    val participantCount: Int, // 챌린지 참가자 수
    val createdAt: String, // 세션 생성 시작 날짜
    val distance: String // 거리
)


/**
 * 중계 페이지
 *
 * @param content 컨텐츠
 * @param totalElements 총 요소
 * @param totalPages 총 페이지
 * @param page 페이지
 * @param size 보여줄 요소 개수
 *
 */
data class BroadcastPage(
    val content: List<BroadcastListItem>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)
