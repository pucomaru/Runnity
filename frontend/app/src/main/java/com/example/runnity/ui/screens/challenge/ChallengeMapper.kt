package com.example.runnity.ui.screens.challenge

import com.example.runnity.data.model.response.ChallengeListItem as ApiChallengeListItem
import com.example.runnity.ui.components.ChallengeListItem as UiChallengeListItem
import com.example.runnity.ui.components.ChallengeButtonState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * API 응답을 UI 모델로 변환하는 Mapper
 */
object ChallengeMapper {

    /**
     * API ChallengeListItem을 UI ChallengeListItem으로 변환
     */
    fun toUiModel(apiItem: ApiChallengeListItem): UiChallengeListItem {
        return UiChallengeListItem(
            id = apiItem.challengeId.toString(),
            distance = formatDistance(apiItem.distance),
            title = apiItem.title,
            startDateTime = formatStartDateTime(apiItem.startAt),
            participants = "${apiItem.currentParticipants}/${apiItem.maxParticipants}명",
            buttonState = determineButtonState(apiItem.startAt, apiItem.isJoined)
        )
    }

    /**
     * API 거리 코드를 UI 텍스트로 변환
     * ONE -> "1km", FIVE -> "5km", HALF -> "하프" 등
     */
    fun formatDistance(distance: String): String {
        return when (distance) {
            "ONE" -> "1km"
            "TWO" -> "2km"
            "THREE" -> "3km"
            "FOUR" -> "4km"
            "FIVE" -> "5km"
            "SIX" -> "6km"
            "SEVEN" -> "7km"
            "EIGHT" -> "8km"
            "NINE" -> "9km"
            "TEN" -> "10km"
            "FIFTEEN" -> "15km"
            "HALF" -> "하프"
            "M100" -> "100m"
            "M500" -> "500m"
            else -> "${distance}km"
        }
    }

    /**
     * ISO 8601 형식의 시작 시간을 UI 형식으로 변환
     * "2025-11-12T21:00:00Z" -> "2025.11.12 21:00 시작"
     */
    private fun formatStartDateTime(startAt: String): String {
        return try {
            val dateTime = LocalDateTime.parse(startAt, DateTimeFormatter.ISO_DATE_TIME)
            val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
            "${dateTime.format(formatter)} 시작"
        } catch (e: Exception) {
            "시작 시간 미정"
        }
    }

    /**
     * 버튼 상태 결정 (리스트 전용)
     * - 시작 5분 전 ~ 시작 10초 전까지 예약한 챌린지: Join 버튼 (웹소켓 참가하기)
     * - 그 외 모든 경우: None (버튼 없음)
     *
     * 참고: 예약하기/예약 취소하기 버튼은 상세 화면에서만 제공
     */
    private fun determineButtonState(startAt: String, isJoined: Boolean): ChallengeButtonState {
        return try {
            val startTime = LocalDateTime.parse(startAt, DateTimeFormatter.ISO_DATE_TIME)
            val now = LocalDateTime.now()
            val secondsUntilStart = ChronoUnit.SECONDS.between(now, startTime)

            // 시작 10초 전(예: 20:59:50)부터 시작 5분 전(20:55:00)까지 참가하기 버튼 표시 (웹소켓 참가용)
            // 예: 21:00 시작 -> 20:55:00 ~ 20:59:50 활성화, 20:59:51부터 비활성화
            if (isJoined && secondsUntilStart in 10..300) {
                ChallengeButtonState.Join
            } else {
                ChallengeButtonState.None
            }
        } catch (e: Exception) {
            ChallengeButtonState.None
        }
    }
}
