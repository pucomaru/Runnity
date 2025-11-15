package com.example.runnity.data.model.response

/**
 * 개인의 기간별 러닝 기록 통계 조회 응답
 *
 * @param totalDistance 총 거리 (km)
 * @param totalTime 총 시간 (초)
 * @param avgPace 평균 페이스 (초/km)
 * @param totalRunDays 총 달린 날
 * @param periodStats 기간별 통계 목록
 * @param personals 개인 러닝 기록 목록
 * @param challenges 챌린지 러닝 기록 목록
 */
data class StatsResponse(
    val totalDistance: Double,
    val totalTime: Int,
    val avgPace: Int,
    val totalRunDays: Int,
    val periodStats: List<PeriodStatResponse>,
    val personals: List<RunRecordResponse>,
    val challenges: List<ChallengeRunRecordResponse>
)

/**
 * 기간별 통계
 *
 * @param label 기간 레이블 (예: "2025-W46" 또는 "2025-11" 또는 "2025-11-14")
 * @param distance 총 거리 (km)
 * @param time 총 시간 (초)
 * @param pace 평균 페이스 (초/km)
 * @param count 러닝 횟수
 */
data class PeriodStatResponse(
    val label: String,
    val distance: Double,
    val time: Int,
    val pace: Int,
    val count: Int
)
