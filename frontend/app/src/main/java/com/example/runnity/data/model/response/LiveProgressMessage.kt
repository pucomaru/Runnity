package com.example.runnity.data.model.response

data class LiveProgressMessage(
    val challengeId: Long,
    val totalDistanceMeter: Int,
    val participants: List<RunnerProgress>
)

data class RunnerProgress(
    val runnerId: Long,
    val nickname: String,
    val color: String?,
    val distanceMeter: Int
)
