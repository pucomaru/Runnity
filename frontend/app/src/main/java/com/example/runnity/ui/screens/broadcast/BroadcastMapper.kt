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
}
