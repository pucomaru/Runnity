package com.example.runnity.ui.screens.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.socket.WebSocketManager
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
                                        averagePace = ""
                                    )
                                }
                                _participants.value = mapped
                            }
                        }
                        "USER_ENTERED" -> {
                            val entered = gson.fromJson(text, UserEnteredMessage::class.java)
                            if (entered.userId != null) {
                                val newParticipant = Participant(
                                    id = entered.userId.toString(),
                                    nickname = entered.nickname,
                                    avatarUrl = entered.profileImage,
                                    averagePace = ""
                                )
                                // 이미 존재하지 않을 때만 추가
                                val current = _participants.value
                                if (current.none { it.id == newParticipant.id }) {
                                    _participants.value = current + newParticipant
                                }
                            }
                        }
                        "USER_LEFT" -> {
                            val left = gson.fromJson(text, UserLeftMessage::class.java)
                            if (left.userId != null) {
                                val current = _participants.value
                                _participants.value = current.filterNot { it.id == left.userId.toString() }
                            }
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
