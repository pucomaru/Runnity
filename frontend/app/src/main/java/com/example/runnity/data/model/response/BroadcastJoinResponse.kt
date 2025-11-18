package com.example.runnity.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 중계방 입장 API 응답 모델
 * GET /api/v1/broadcast/join
 */
data class BroadcastJoinResponse(
    val wsUrl: String,
    val topic: String,
    val challengeId: Long,
    val hlsUrl: String?,      // 백엔드에서 추가
    val title: String?,       // 선택
    val distance: String?,    // 선택 (예: "5km")
    val totalDistanceMeter: Int?
)
