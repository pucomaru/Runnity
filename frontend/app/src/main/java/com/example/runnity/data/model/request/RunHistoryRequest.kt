package com.example.runnity.data.model.request

/**
 * 러닝 기록 저장 요청
 *
 * @param type 러닝 타입 (PERSONAL, CHALLENGE)
 * @param startDateTime 시작 일시 (ISO 8601)
 * @param endDateTime 종료 일시 (ISO 8601)
 * @param totalDistance 총 거리 (km)
 * @param durationSec 총 소요 시간 (초)
 * @param avgPaceSec 평균 페이스 (초/km)
 * @param avgBpm 평균 심박수
 * @param laps 랩 타임 목록
 */
data class SaveRunRecordRequest(
    val type: String,
    val startDateTime: String,
    val endDateTime: String,
    val totalDistance: Double,
    val durationSec: Int,
    val avgPaceSec: Int,
    val avgBpm: Int,
    val laps: List<LapRequest>
)

/**
 * 랩 타임 정보
 *
 * @param sequence 랩 순서
 * @param distance 랩 거리 (km)
 * @param durationSec 랩 소요 시간 (초)
 * @param pace 랩 페이스 (초/km)
 * @param bpm 랩 심박수
 */
data class LapRequest(
    val sequence: Int,
    val distance: Double,
    val durationSec: Int,
    val pace: Int,
    val bpm: Int
)
