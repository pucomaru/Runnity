package com.example.runnity.ui.screens.broadcast

import com.google.gson.JsonObject

// WebSocket 공통 래퍼
data class WebSocketWrapper(
    val type: String,          // "STREAM" | "LLM" | "VIEWER"
    val subtype: String,       // STREAM: RUNNING/START/FINISH/LEAVE, LLM: OVERTAKE/..., VIEWER: COUNT
    val challengeId: Long,
    val timestamp: Long,
    val payload: JsonObject    // 내부에서 다시 파싱
)

// STREAM payload
data class StreamPayload(
    val runnerId: Long,
    val nickname: String,
    val profileImage: String?,
    val distance: Double,   // 누적 거리(km) - 서버에서 km 단위로 전송됨
    val pace: Double,       // 1km당 속도 (서버 기준)
    val ranking: Int
)

// LLM payload
data class LlmPayload(
    val runnerId: Long,
    val nickname: String,
    val profileImage: String?,
    val highlightType: String,      // "OVERTAKE" | "FINISH" | ...
    val commentary: String,         // 멘트 텍스트
    val targetRunnerId: Long?,
    val targetNickname: String?,
    val targetProfileImage: String?,
    val distance: Double,
    val pace: Double,
    val ranking: Int
)

// VIEWER payload
data class ViewerPayload(
    val viewerCount: Int           // 현재 시청자 수
)
