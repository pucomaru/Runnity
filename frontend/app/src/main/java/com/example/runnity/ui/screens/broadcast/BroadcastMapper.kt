package com.example.runnity.ui.screens.broadcast

import com.example.runnity.data.model.response.BroadcastListItem
import com.example.runnity.data.model.response.BroadcastPage
import com.example.runnity.data.model.response.BroadcastResponse
import com.example.runnity.data.model.response.ChallengeListItem as ApiChallengeListItem
import com.example.runnity.ui.components.ChallengeListItem as UiChallengeListItem
import com.example.runnity.ui.components.ChallengeButtonState
import com.example.runnity.ui.screens.challenge.ChallengeMapper
import com.example.runnity.ui.screens.challenge.ChallengeMapper.formatDistance
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * API 응답을 UI 모델로 변환하는 Mapper
 */
object BroadcastMapper {

    /**
     * 네트워크 DTO → 화면 리스트 아이템
     * - 현재는 필드 1:1 매핑
     */
    fun toItem(dto: BroadcastResponse) = BroadcastListItem(
        challengeId = dto.challengeId,
        title = dto.title,
        viewerCount = dto.viewerCount,
        participantCount = dto.participantCount,
        createdAt = dto.createdAt,
        distance = dto.distance
    )

    // 배열 응답을 페이지 래퍼로 감싸기
    fun toPage(
        list: List<BroadcastResponse>,
        page: Int = 0,
        size: Int = list.size.coerceAtLeast(1)
    ): BroadcastPage {
        val items = list.map(::toItem)
        return BroadcastPage(
            content = items,
            totalElements = items.size.toLong(),
            totalPages = 1,      // 서버가 배열만 주므로 1페이지로 간주
            page = page,
            size = size
        )
    }

    /**
     * ISO 8601 형식의 시작 시간을 UI 형식으로 변환
     * "2025-11-12T21:00:00Z" -> "2025.11.12 21:00 시작"
     */
    private fun formatStartDateTime(createdAt: String): String {
        return try {
            val dateTime = LocalDateTime.parse(createdAt, DateTimeFormatter.ISO_DATE_TIME)
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
            "${dateTime.format(formatter)} 시작"
        } catch (e: Exception) {
            "시작 시간 미정"
        }
    }

    /**
     * 버튼 상태 결정 (리스트 전용)
     * - 시작 5분 전이고 예약한 챌린지: Join 버튼 (웹소켓 참가하기)
     * - 그 외 모든 경우: None (버튼 없음)
     *
     * 참고: 예약하기/예약 취소하기 버튼은 상세 화면에서만 제공
     */
    private fun determineButtonState(startAt: String, isJoined: Boolean): ChallengeButtonState {
        return try {
            val startTime = LocalDateTime.parse(startAt, DateTimeFormatter.ISO_DATE_TIME)
            val now = LocalDateTime.now()
            val minutesUntilStart = ChronoUnit.MINUTES.between(now, startTime)

            // 시작 5분 전이고 예약한 챌린지인 경우에만 참가하기 버튼 표시 (웹소켓 참가용)
            if (isJoined && minutesUntilStart in 0..5) {
                ChallengeButtonState.Join
            } else {
                ChallengeButtonState.None
            }
        } catch (e: Exception) {
            ChallengeButtonState.None
        }
    }
}

/**
 * API 거리 코드를 UI 텍스트로 변환
 * ONE -> "1km", FIVE -> "5km", HALF -> "하프" 등
 */
fun formatDistance(distance: String): String {
    return when (distance) {
        "ONE" -> "1"
        "TWO" -> "2"
        "THREE" -> "3"
        "FOUR" -> "4"
        "FIVE" -> "5"
        "SIX" -> "6"
        "SEVEN" -> "7"
        "EIGHT" -> "8"
        "NINE" -> "9"
        "TEN" -> "10"
        "FIFTEEN" -> "15"
        "HALF" -> "하프"
        "HUNDRED_METERS" -> "100m"
        "FIVE_HUNDRED_METERS" -> "500m"
        else -> "${distance}km"
    }
}
