package com.example.runnity.data.model.response

/**
 * 월간 러닝 기록 조회 응답
 *
 * @param personals 개인 러닝 기록 목록
 * @param challenges 챌린지 러닝 기록 목록
 */
data class MonthlyRunsResponse(
    val personals: List<RunRecordResponse>,
    val challenges: List<ChallengeRunRecordResponse>
)

/**
 * 러닝 기록 (개인)
 *
 * @param runRecordId 러닝 기록 ID
 * @param type 러닝 타입 (PERSONAL, CHALLENGE)
 * @param startDateTime 시작 일시 (ISO 8601)
 * @param endDateTime 종료 일시 (ISO 8601)
 * @param totalDistance 총 거리 (km)
 * @param durationSec 총 소요 시간 (초)
 * @param avgPaceSec 평균 페이스 (초/km)
 * @param avgBpm 평균 심박수
 */
data class RunRecordResponse(
    val runRecordId: Long,
    val type: String,
    val startDateTime: String,
    val endDateTime: String,
    val totalDistance: Double,
    val durationSec: Int,
    val avgPaceSec: Int,
    val avgBpm: Int
)

/**
 * 챌린지 러닝 기록
 *
 * @param runRecordId 러닝 기록 ID
 * @param type 러닝 타입 (PERSONAL, CHALLENGE)
 * @param startDateTime 시작 일시 (ISO 8601)
 * @param endDateTime 종료 일시 (ISO 8601)
 * @param totalDistance 총 거리 (km)
 * @param durationSec 총 소요 시간 (초)
 * @param avgPaceSec 평균 페이스 (초/km)
 * @param avgBpm 평균 심박수
 * @param challengeId 챌린지 ID
 * @param challengeTitle 챌린지 제목
 */
data class ChallengeRunRecordResponse(
    val runRecordId: Long,
    val type: String,
    val startDateTime: String,
    val endDateTime: String,
    val totalDistance: Double,
    val durationSec: Int,
    val avgPaceSec: Int,
    val avgBpm: Int,
    val challengeId: Long,
    val challengeTitle: String
)

/**
 * 러닝 기록 상세 조회 응답
 *
 * @param runRecordId 러닝 기록 ID
 * @param type 러닝 타입 (PERSONAL, CHALLENGE)
 * @param startDateTime 시작 일시 (ISO 8601)
 * @param endDateTime 종료 일시 (ISO 8601)
 * @param totalDistance 총 거리 (km)
 * @param durationSec 총 소요 시간 (초)
 * @param avgPaceSec 평균 페이스 (초/km)
 * @param avgBpm 평균 심박수
 * @param laps 랩 타임 목록
 */
data class RunRecordDetailResponse(
    val runRecordId: Long,
    val type: String,
    val startDateTime: String,
    val endDateTime: String,
    val totalDistance: Double,
    val durationSec: Int,
    val avgPaceSec: Int,
    val avgBpm: Int,
    val laps: List<LapResponse>
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
data class LapResponse(
    val sequence: Int,
    val distance: Double,
    val durationSec: Int,
    val pace: Int,
    val bpm: Int
)

/**
 * 내가 참가한 챌린지 목록 조회 응답
 *
 * @param enterableChallenge 입장 가능한 챌린지 (진행 중이면서 내가 참가한)
 * @param joinedChallenges 참가 신청한 챌린지 목록
 */
data class MyChallengesResponse(
    val enterableChallenge: ChallengeSimpleInfo?,
    val joinedChallenges: List<ChallengeSimpleInfo>
)

/**
 * 챌린지 간단 정보
 *
 * @param challengeId 챌린지 ID
 * @param title 챌린지 제목
 * @param status 챌린지 상태 (RECRUITING, IN_PROGRESS, COMPLETED 등)
 * @param currentParticipants 현재 참여 인원
 * @param maxParticipants 최대 참여 인원
 * @param startAt 시작 일시 (ISO 8601)
 * @param distance 목표 거리 (FIVE, TEN 등)
 */
data class ChallengeSimpleInfo(
    val challengeId: Long,
    val title: String,
    val status: String,
    val currentParticipants: Int,
    val maxParticipants: Int,
    val startAt: String,
    val distance: String
)
