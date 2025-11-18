package com.example.runnity.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 중계방 입장 API 응답 모델
 * GET /api/v1/broadcast/join
 */
data class BroadcastJoinResponse(
    @SerializedName("wsUrl")
    val wsUrl: String,

    @SerializedName("topic")
    val topic: String,

    @SerializedName("challengeId")
    val challengeId: Long
)
