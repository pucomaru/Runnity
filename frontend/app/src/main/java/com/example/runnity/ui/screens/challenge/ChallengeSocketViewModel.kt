package com.example.runnity.ui.screens.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.repository.ChallengeRepository
import com.example.runnity.data.util.TokenManager
import com.example.runnity.data.util.UserProfileManager
import com.example.runnity.socket.WebSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class ChallengeSocketViewModel : ViewModel() {

    // 대기방/세션 참가자 정보 (실시간)
    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants.asStateFlow()

    private val gson = Gson()
    private var observeJob: Job? = null
    private var reconnectJob: Job? = null

    private val challengeRepository = ChallengeRepository()

    // 현재 사용자 ID (참가자 리스트에서 isMe 여부 판단용)
    private val currentUserId: String? = UserProfileManager.getProfile()?.memberId?.toString()

    /**
     * 서버가 나에 대한 PARTICIPANT_UPDATE 를 보내지 않는 경우를 대비해서,
     * 클라이언트에서 RECORD 전송 시 내 distance/pace 를 직접 참가자 리스트에 반영하기 위한 helper.
     */
    fun updateMyStats(distanceKm: Double, paceSecPerKm: Double) {
        val myId = currentUserId ?: return
        val current = _participants.value
        if (current.isEmpty()) return

        val updated = current.map { p ->
            if (p.id == myId) {
                p.copy(
                    distanceKm = distanceKm,
                    paceSecPerKm = paceSecPerKm
                )
            } else p
        }

        _participants.value = applyRanking(updated)
    }

    /**
     * 특정 챌린지 세션의 WebSocket 메시지를 관찰하고 참가자 목록을 갱신
     */
    fun observeSession(challengeId: Long) {
        Timber.d("[ChallengeSocket] observeSession start, challengeId=%d", challengeId)
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            // 주기적인 클라이언트 PING 전송 (연결 유지용)
            launch {
                while (true) {
                    delay(30_000L)
                    if (WebSocketManager.isOpen) {
                        val pingJson = "{" +
                            "\"type\":\"PING\"," +
                            "\"timestamp\":" + System.currentTimeMillis() +
                            "}"
                        WebSocketManager.send(pingJson)
                    } else {
                        Timber.w("[ChallengeSocket] 웹소켓이 닫혀 있어 PING 생략")
                    }
                }
            }

            // 웹소켓 상태를 감시하여 Closed/Failed 시 제한된 자동 재연결 시도
            launch {
                WebSocketManager.state.collect { state ->
                    when (state) {
                        is WebSocketManager.WsState.Closed,
                        is WebSocketManager.WsState.Failed -> {
                            if (reconnectJob == null || reconnectJob?.isActive == false) {
                                reconnectJob = launch {
                                    val delays = listOf(1000L, 3000L, 5000L)
                                    for (delayMs in delays) {
                                        // 화면을 벗어나 observeJob 이 취소되면 재연결 루프도 중단
                                        if (!this.isActive) return@launch
                                        kotlinx.coroutines.delay(delayMs)
                                        Timber.w("[ChallengeSocket] 웹소켓 상태=%s, 재연결 시도 (delay=%dms)", state, delayMs)

                                        // 티켓이 1회용이므로 매 재시도마다 enterChallenge 를 다시 호출해
                                        // 새로운 ticket/wsUrl 을 받아온 뒤 WebSocketManager.connect 로 재연결한다.
                                        val resp = challengeRepository.enterChallenge(challengeId)
                                        when (resp) {
                                            is ApiResponse.Success -> {
                                                val ticket = resp.data.ticket
                                                val wsUrl = resp.data.wsUrl
                                                val url = "$wsUrl?ticket=$ticket"

                                                WebSocketManager.connect(
                                                    url = url,
                                                    tokenProvider = { TokenManager.getAccessToken() }
                                                )

                                                // 이번 시도에서 Open 또는 Failed 로 전이될 때까지 대기
                                                val resultState = WebSocketManager.state.first {
                                                    it is WebSocketManager.WsState.Open || it is WebSocketManager.WsState.Failed
                                                }
                                                if (resultState is WebSocketManager.WsState.Open) {
                                                    Timber.d("[ChallengeSocket] 웹소켓 재연결 성공")
                                                    return@launch
                                                } else {
                                                    Timber.w("[ChallengeSocket] 웹소켓 재연결 실패, 다음 딜레이로 재시도")
                                                }
                                            }
                                            is ApiResponse.Error -> {
                                                Timber.e(
                                                    "[ChallengeSocket] enterChallenge 재시도 실패: code=%d, message=%s",
                                                    resp.code,
                                                    resp.message
                                                )
                                            }
                                            is ApiResponse.NetworkError -> {
                                                Timber.e("[ChallengeSocket] enterChallenge 재시도 네트워크 오류")
                                            }
                                        }
                                    }
                                    Timber.e("[ChallengeSocket] 웹소켓 자동 재연결 최대 횟수 초과")
                                }
                            }
                        }
                        is WebSocketManager.WsState.Open,
                        is WebSocketManager.WsState.Connecting -> {
                            // 연결이 다시 열리면 재연결 job은 더 이상 필요 없다
                            reconnectJob?.cancel()
                            reconnectJob = null
                        }
                    }
                }
            }

            WebSocketManager.incoming.collect { text ->
                try {
                    // 먼저 type만 확인
                    val base = gson.fromJson(text, BaseSocketMessage::class.java)
                    Timber.d("[ChallengeSocket] incoming type=%s raw=%s", base.type, text)
                    when (base.type) {
                        "CONNECTED" -> {
                            val message = gson.fromJson(text, ConnectedMessage::class.java)
                            Timber.d(
                                "[ChallengeSocket] CONNECTED received: msg.challengeId=%d, sessionId=%d, meUserId=%d",
                                message.challengeId,
                                challengeId,
                                message.userId
                            )
                            if (message.challengeId == challengeId) {
                                // 서버 participants 리스트와 me 필드를 모두 반영해 참가자 목록 구성하되,
                                // 이미 클라이언트에 쌓여 있는 distance/pace 기록은 유지한다 (재연결 시 0으로 초기화 방지).
                                val myId = currentUserId ?: message.userId.toString()

                                val serverList = mutableListOf<Participant>()
                                serverList += message.participants.map { p ->
                                    Participant(
                                        id = p.userId.toString(),
                                        nickname = p.nickname,
                                        avatarUrl = p.profileImage,
                                        averagePace = "",
                                        distanceKm = p.distance,
                                        paceSecPerKm = p.pace,
                                        isMe = (myId == p.userId.toString())
                                    )
                                }

                                message.me?.let { m ->
                                    val meId = m.userId.toString()
                                    if (serverList.none { it.id == meId }) {
                                        serverList += Participant(
                                            id = meId,
                                            nickname = m.nickname,
                                            avatarUrl = m.profileImage,
                                            averagePace = "",
                                            distanceKm = m.distance,
                                            paceSecPerKm = m.pace,
                                            isMe = true
                                        )
                                    }
                                }

                                val current = _participants.value
                                val byId = serverList.associateBy { it.id }.toMutableMap()
                                val merged = mutableListOf<Participant>()

                                // 1) 이미 존재하는 참가자는 distance/pace/isRetired 를 유지하면서
                                //    닉네임/프로필 등 메타데이터만 서버 값으로 갱신
                                current.forEach { existing ->
                                    val fromServer = byId.remove(existing.id)
                                    if (fromServer != null) {
                                        merged += existing.copy(
                                            nickname = fromServer.nickname,
                                            avatarUrl = fromServer.avatarUrl ?: existing.avatarUrl,
                                            isMe = existing.isMe || fromServer.isMe
                                        )
                                    } else {
                                        merged += existing
                                    }
                                }

                                // 2) 재연결 이후 새로 발견된 참가자는 그대로 추가
                                byId.values.forEach { p -> merged += p }

                     Timber.d(
                                    "[ChallengeSocket] CONNECTED merged, before=%d, after=%d, server=%d",
                                    current.size,
                                    merged.size,
                                    serverList.size
                                )

                                _participants.value = applyRanking(merged).also {
                                    Timber.d("[ChallengeSocket] participants after CONNECTED, size=%d", it.size)
                                }
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
                                val existing = current.find { it.id == newParticipant.id }

                                val next = if (existing == null) {
                                    // 처음 들어오는 참가자: 그대로 추가
                                    applyRanking(current + newParticipant).also {
                                        Timber.d(
                                            "[ChallengeSocket] USER_ENTERED new userId=%s, nickname=%s, size(before)=%d, size(after)=%d",
                                            newParticipant.id,
                                            newParticipant.nickname,
                                            current.size,
                                            it.size
                                        )
                                    }
                                } else {
                                    // 이미 존재하던 참가자가 다시 들어온 경우:
                                    // - isRetired 를 해제하고
                                    // - 닉네임/프로필/거리/페이스를 서버 값으로 갱신
                                    val updated = current.map { p ->
                                        if (p.id == newParticipant.id) {
                                            p.copy(
                                                nickname = newParticipant.nickname,
                                                avatarUrl = newParticipant.avatarUrl ?: p.avatarUrl,
                                                distanceKm = newParticipant.distanceKm,
                                                paceSecPerKm = newParticipant.paceSecPerKm,
                                                isRetired = false,
                                                isMe = p.isMe || newParticipant.isMe
                                            )
                                        } else p
                                    }
                                    applyRanking(updated).also {
                                        Timber.d(
                                            "[ChallengeSocket] USER_ENTERED revive userId=%s, nickname=%s, size=%d",
                                            newParticipant.id,
                                            newParticipant.nickname,
                                            it.size
                                        )
                                    }
                                }

                                _participants.value = next
                            }
                        }
                        "USER_LEFT" -> {
                            val left = gson.fromJson(text, UserLeftMessage::class.java)
                            if (left.userId != null) {
                                val current = _participants.value
                                val updated = current.map { p ->
                                    if (p.id == left.userId.toString()) {
                                        // 챌린지 도중 퇴장한 참가자는 리스트에서 제거하지 않고 리타이어 상태로 표시
                                        p.copy(isRetired = true)
                                    } else p
                                }
                                _participants.value = applyRanking(updated)
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
                            val next = applyRanking(updated)
                            Timber.d(
                                "[ChallengeSocket] PARTICIPANT_UPDATE userId=%d, distance=%.3f, pace=%.2f, size(before)=%d, size(after)=%d",
                                update.userId,
                                update.distance,
                                update.pace,
                                current.size,
                                next.size
                            )
                            _participants.value = next
                        }
                        "PING" -> {
                            // 서버에서 보낸 PING에 대한 PONG 응답
                            val pongJson = "{" +
                                "\"type\":\"PONG\"," +
                                "\"timestamp\":" + System.currentTimeMillis() +
                                "}"
                            WebSocketManager.send(pongJson)
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

    // 모든 참가자의 거리가 0이면 순위를 매기지 않는다 (rank=0 유지)
    if (list.all { it.distanceKm <= 0.0 }) {
        return list.map { it.copy(rank = 0) }
    }

    // 우선 distanceKm 내림차순, 동률일 때는 pace가 빠른 순으로 정렬
    val sorted = list.sortedWith(
        compareByDescending<Participant> { it.distanceKm }
            .thenBy { it.paceSecPerKm ?: Double.MAX_VALUE }
    )

    var currentRank = 1
    return sorted.map { p ->
        if (p.distanceKm > 0.0) {
            // 기록이 있는 참가자만 1,2,3... 순위를 부여
            val ranked = p.copy(rank = currentRank)
            currentRank += 1
            ranked
        } else {
            // 기록 없는 참가자는 rank=0으로 두고 UI에서 "--위" 처리
            p.copy(rank = 0)
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
    val me: ConnectedParticipant?,
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
