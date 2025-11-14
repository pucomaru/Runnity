package com.example.runnity.data.model.request

// 러닝 기록 생성 요청
data class RunCreateRequest(
    val runType: String,
    val distance: Double,
    val durationSec: Int,
    val startAt: String,
    val endAt: String,
    val pace: Int,
    val bpm: Int,
    val calories: Double,
    val route: String?,
    val laps: List<RunLapCreateRequest>? = null
)

data class RunLapCreateRequest(
    val sequence: Int,
    val distance: Double,
    val durationSec: Int,
    val pace: Int,
    val bpm: Int
)
