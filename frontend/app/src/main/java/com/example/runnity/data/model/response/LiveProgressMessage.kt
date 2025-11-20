package com.example.runnity.data.model.response

import com.google.gson.annotations.SerializedName

data class RunnerProgress(
    val runnerId: Long,
    val nickname: String,
    val color: String?,
    val distanceMeter: Int
)

data class LiveProgressMessage(
    @SerializedName("challengeId")
    val challengeId: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("viewerCount")
    val viewerCount: Int,

    @SerializedName("participantCount")
    val participantCount: Int,

    @SerializedName("distance")
    val distance: String,

    @SerializedName("totalDistanceMeter")
    val totalDistanceMeter: Int,  // ← 목표 거리 (미터)

    @SerializedName("participants")
    val participants: List<ParticipantProgress>
)

data class ParticipantProgress(
    @SerializedName("runnerId")
    val runnerId: Long,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("color")
    val color: String?,

    @SerializedName("distanceMeter")
    val distanceMeter: Int,

    @SerializedName("elapsedTime")
    val elapsedTime: Int,  // 경과 시간 (초)

    @SerializedName("pace")
    val pace: String?,  // 백엔드에서 계산한 페이스

    @SerializedName("currentSpeed")
    val currentSpeed: Float?  // 현재 속도 (m/s)
)
