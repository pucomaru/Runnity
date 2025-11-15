package com.example.runnity.ui.screens.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.socket.WebSocketManager
import com.example.runnity.data.util.UserProfileManager
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ChallengeSocketViewModel : ViewModel() {

    // 대기방/세션 참가자 정보 (실시간)
    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants.asStateFlow()

    private val gson = Gson()
    private var observeJob: Job? = null

    // 현재 사용자 ID (참가자 리스트에서 isMe 여부 판단용)
    private val currentUserId: String? = UserProfileManager.getProfile()?.memberId?.toString()

    /**
     * 특정 챌린지 세션의 WebSocket 메시지를 관찰하고 참가자 목록을 갱신
     */
    fun observeSession(challengeId: Long) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            WebSocketManager.incoming.collect { text ->
                try {
                    // 먼저 type만 확인
                    val base = gson.fromJson(text, BaseSocketMessage::class.java)
                    when (base.type) {
                        "CONNECTED" -> {
                            val message = gson.fromJson(text, ConnectedMessage::class.java)
                            if (message.challengeId == challengeId) {
                                val mapped = message.participants.map { p ->
                                    Participant(
                                        id = p.userId.toString(),
                                        nickname = p.nickname,
                                        avatarUrl = p.profileImage,
                                        averagePace = "",
                                        distanceKm = p.distance,
                                        paceSecPerKm = p.pace,
                                        isMe = (currentUserId != null && currentUserId == p.userId.toString())
                                    )
                                }
                                _participants.value = applyRanking(mapped)
                            }
                        }
                        "USER_ENTERED" -> {
                            val entered = gson.fromJson(text, UserEnteredMessage::class.java)
                            if (entered.userId != null) {
                                val newParticipant = Participant(
                                    id = entered.userId.toString(),
                                    nickname = entered.nickname,
                                    avatarUrl = entered.profileImage,
                                    averagePace = "",
                                    distanceKm = entered.distance,
                                    paceSecPerKm = entered.pace,
                                    isMe = (currentUserId != null && currentUserId == entered.userId.toString())
                                )
                                val current = _participants.value
                                // 이미 존재하지 않을 때만 추가
                                if (current.none { it.id == newParticipant.id }) {
                                    _participants.value = applyRanking(current + newParticipant)
                                }
                            }
                        }
                        "USER_LEFT" -> {
                            val left = gson.fromJson(text, UserLeftMessage::class.java)
                            if (left.userId != null) {
                                val current = _participants.value
                                _participants.value = applyRanking(current.filterNot { it.id == left.userId.toString() })
                            }
                        }
                        "PARTICIPANT_UPDATE" -> {
                            val update = gson.fromJson(text, ParticipantUpdateMessage::class.java)
                            val current = _participants.value
                            val updated = current.map { p ->
                                if (p.id == update.userId.toString()) {
                                    p.copy(
                                        distanceKm = update.distance,
                                        paceSecPerKm = update.pace
                                    )
                                } else p
                            }
                            _participants.value = applyRanking(updated)
                        }
                        else -> Unit
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse WebSocket session message")
                }
            }
        }
    }
}

// PARTICIPANT_UPDATE 메시지
data class ParticipantUpdateMessage(
    val type: String,
    val userId: Long,
    val distance: Double,
    val pace: Double,
    val timestamp: Long
)

// 거리/페이스 기준으로 정렬하고 rank/isMe를 일관되게 적용
private fun applyRanking(list: List<Participant>): List<Participant> {
    if (list.isEmpty()) return list
    // 우선 distanceKm 내림차순, 동률일 때는 pace가 빠른 순
    val sorted = list.sortedWith(compareByDescending<Participant> { it.distanceKm }.thenBy { it.paceSecPerKm ?: Double.MAX_VALUE })
    return sorted.mapIndexed { index, p ->
        p.copy(rank = index + 1)
    }
}

// 공통 타입 구분용
data class BaseSocketMessage(
    val type: String
)

// 연결된 참가자 정보 (WebSocket CONNECTED payload)
data class ConnectedParticipant(
    val userId: Long,
    val nickname: String,
    val profileImage: String?,
    val distance: Double,
    val pace: Double
)

data class ConnectedMessage(
    val type: String,
    val challengeId: Long,
    val userId: Long,
    val participants: List<ConnectedParticipant>,
    val timestamp: Long
)

// 다른 참가자 입장
data class UserEnteredMessage(
    val type: String,
    val userId: Long?,
    val nickname: String,
    val profileImage: String?,
    val distance: Double,
    val pace: Double,
    val timestamp: Long
)

// 다른 참가자 퇴장
data class UserLeftMessage(
    val type: String,
    val userId: Long?,
    val reason: String?,
    val timestamp: Long
)
