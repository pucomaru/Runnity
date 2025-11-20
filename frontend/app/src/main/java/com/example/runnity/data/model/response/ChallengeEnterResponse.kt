package com.example.runnity.data.model.response

// 챌린지 입장 응답
data class ChallengeEnterResponse(
    val ticket: String,
    val userId: Long,
    val challengeId: Long,
    val wsUrl: String,
    val expiresIn: Long
)
