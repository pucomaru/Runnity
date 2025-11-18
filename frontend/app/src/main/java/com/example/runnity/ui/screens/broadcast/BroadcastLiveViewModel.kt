package com.example.runnity.ui.screens.broadcast

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.LiveProgressMessage
import com.example.runnity.data.repository.BroadcastRepository
import com.example.runnity.data.util.TokenManager
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import kotlin.math.max

class BroadcastLiveViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val gson = Gson()
    private val tokenProvider: () -> String = { TokenManager.getAccessToken().toString() }
    private val repository = BroadcastRepository()

    data class LiveUi(
        val title: String = "",
        val viewerCount: Int = 0,
        val participantCount: Int = 0,
        val distance: String = "",
        val totalDistanceMeter: Int = 0,
        val hlsUrl: String = "",
        val runners: List<RunnerUi> = emptyList(),
        val selectedRunnerId: Long? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    data class RunnerUi(
        val runnerId: Long,
        val nickname: String,
        val color: Color,
        val distanceMeter: Int,
        val ratio: Float,
        val pace: String = "0'00\"",
        val rank: Int = 0,
        val currentSpeed: Float = 0f
    )

    private val _uiState = MutableStateFlow(LiveUi())
    val uiState: StateFlow<LiveUi> = _uiState.asStateFlow()

    private var _player: ExoPlayer? = null
    val player: ExoPlayer
        get() = _player ?: ExoPlayer.Builder(getApplication()).build().also { _player = it }

    private var stompClient: ua.naiksoftware.stomp.StompClient? = null
    private var subscription: Disposable? = null
    private var lifecycleSub: Disposable? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 3

    /**
     * STOMP 연결 및 구독
     */
    private fun connectStomp(wsUrl: String, topic: String, challengeId: Long) {
        Timber.d("STOMP 연결 시도: $wsUrl")
        disconnectStomp()

        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)
        stompClient = client

        Timber.d("tokenProvider: $tokenProvider")
        val headers = listOf(
            StompHeader("Authorization", "Bearer ${tokenProvider()}"),
            StompHeader("challengeId", challengeId.toString()),
            StompHeader("accept-version", "1.1,1.2"),
            StompHeader("heart-beat", "0,0")
        )

        lifecycleSub = client.lifecycle().subscribe { event ->
            Timber.d("STOMP Lifecycle: ${event.type}")
            when (event.type) {
                LifecycleEvent.Type.OPENED -> {
                    Timber.d("✅ STOMP 연결 성공! 토픽 구독 시작...")
                    reconnectAttempts = 0
                    subscribeToTopic(client, topic)
                }
                LifecycleEvent.Type.ERROR -> {
                    Timber.e(event.exception, "❌ STOMP 연결 에러")
                    attemptReconnect(wsUrl, topic, challengeId)
                }
                LifecycleEvent.Type.CLOSED -> Timber.d("STOMP 연결 종료")
                else -> {}
            }
        }
        client.connect(headers)
    }

    private fun attemptReconnect(wsUrl: String, topic: String, challengeId: Long) {
        if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++
            viewModelScope.launch {
                delay(2000L * reconnectAttempts)
                Timber.d("재연결 시도... ($reconnectAttempts/$maxReconnectAttempts)")
                connectStomp(wsUrl, topic, challengeId)
            }
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "WebSocket 연결에 실패했습니다.") }
        }
    }

    /**
     * 러너 선택 (말풍선 표시용)
     */
    fun selectRunner(runnerId: Long?) {
        _uiState.update { it.copy(selectedRunnerId = runnerId) }
    }

    /**
     * HLS 영상 플레이어 준비
     */
    fun preparePlayer(url: String?) {
        if (!url.isNullOrBlank()) {
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.playWhenReady = true
        }
    }

    /**
     * 플레이어 해제
     */
    fun releasePlayer() {
        _player?.release()
        _player = null
    }

    /**
     * 중계방 입장 및 WebSocket 연결
     *
     * 1. POST /api/v1/broadcast/join → wsUrl, topic 받기
     * 2. 받은 wsUrl로 STOMP 연결
     * 3. 받은 topic 구독
     *
     * @param challengeId 챌린지 ID
     */
    fun joinAndConnect(challengeId: Long) {
        if (_uiState.value.isLoading.not() && stompClient?.isConnected == true) {
            Timber.d("이미 연결되어 있습니다.")
            return
        }
        Timber.d("joinAndConnect 시작: challengeId=$challengeId")
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        reconnectAttempts = 0

        viewModelScope.launch {
            try {
                val response = repository.joinBroadcast(challengeId)
                when (response) {
                    is ApiResponse.Success -> {
                        val joinData = response.data
                        Timber.d("중계방 입장 성공: wsUrl=${joinData.wsUrl}, topic=${joinData.topic}")
                        connectStomp(joinData.wsUrl, joinData.topic, challengeId)
                    }
                    is ApiResponse.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "중계방 입장에 실패했습니다: ${response.message}") }
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                Timber.e(e, "중계방 입장 중 예외 발생")
                _uiState.update { it.copy(isLoading = false, errorMessage = "오류가 발생했습니다: ${e.localizedMessage}") }
            }
        }
    }

    /**
     * STOMP 클라이언트 연결 및 구독
     *
     * @param wsUrl WebSocket URL (예: "ws://43.203.250.119:8080/ws")
     * @param topic 구독할 토픽 (예: "/topic/broadcast/13")
     */
    private fun connectStompWithUrl(wsUrl: String, topic: String, challengeId: Long) {
        Timber.d("STOMP 연결 시도: wsUrl=$wsUrl, topic=$topic")

        disconnectStomp()

        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl, null)
        stompClient = client

        val headers = arrayListOf(
            StompHeader("Authorization", "Bearer ${tokenProvider()}"),
            StompHeader("challengeId", challengeId.toString()),
            StompHeader("accept-version", "1.1,1.2"),
            StompHeader("heart-beat", "0,0")  // 하트비트 비활성화
        )

        client.connect(headers)
    }

    /**
     * 토픽 구독 (연결 성공 후 호출)
     */
    private fun subscribeToTopic(client: ua.naiksoftware.stomp.StompClient, topic: String) {
        Timber.d("토픽 구독 시작: $topic")

        subscription?.dispose() // 기존 구독 해제

        subscription = client.topic(topic).subscribe(
            { msg ->
                val payload = msg.payload
                Timber.d("📡 수신한 메시지: $payload")

                // JSON 파싱
                val model = runCatching {
                    gson.fromJson(payload, LiveProgressMessage::class.java)
                }.getOrNull()

                if (model == null) {
                    Timber.e("메시지 파싱 실패! payload=$payload")
                    return@subscribe
                }

                Timber.d("✅ 파싱 성공: participants=${model.participants.size}, title=${model.title}")

                // UI 업데이트 (메인 스레드)
                viewModelScope.launch(Dispatchers.Main) {
                    updateUiFromMessage(model)
                }
            },
            { error ->
                Timber.e(error, "❌ STOMP 구독 에러: ${error.message}")
                _uiState.update {
                    it.copy(errorMessage = "실시간 데이터 수신 실패: ${error.localizedMessage}")
                }
            }
        )

        Timber.d("✅ 토픽 구독 완료: $topic")
    }

    /**
     * WebSocket 메시지로부터 UI 업데이트
     *
     * @param message 서버로부터 받은 실시간 진행 데이터
     */
    private fun updateUiFromMessage(message: LiveProgressMessage) {
        val total = max(message.totalDistanceMeter, 1)

        // 참가자 데이터를 RunnerUi로 변환
        val runners = message.participants.mapIndexed { idx, participant ->
            val ratio = (participant.distanceMeter.toFloat() / total).coerceIn(0f, 1f)

            // 완주한 러너는 ratio를 0으로 (시작점으로 복귀)
            val finalRatio = if (ratio >= 1.0f) 0f else ratio

            // 페이스 계산
            val pace = when {
                participant.pace != null -> participant.pace
                participant.distanceMeter > 0 && participant.elapsedTime > 0 ->
                    calculatePace(participant.distanceMeter, participant.elapsedTime)
                else -> "0'00\""
            }

            // 색상 파싱 (hex → Color)
            val color = participant.color?.let { parseHexColor(it) } ?: pickColor(idx)

            RunnerUi(
                runnerId = participant.runnerId,
                nickname = participant.nickname,
                color = color,
                distanceMeter = participant.distanceMeter,
                ratio = finalRatio,
                pace = pace,
                rank = 0, // 순위는 아래에서 계산
                currentSpeed = if (ratio >= 1.0f) 0f else (participant.currentSpeed ?: 0f)
            )
        }

        // 거리 기준으로 순위 계산
        val sorted = runners.sortedByDescending { it.distanceMeter }
        val runnersWithRank = runners.map { runner ->
            val rank = sorted.indexOfFirst { it.runnerId == runner.runnerId } + 1
            runner.copy(rank = rank)
        }

        Timber.d("Runners 업데이트: count=${runnersWithRank.size}, 1위=${runnersWithRank.firstOrNull()?.nickname}")

        // UI 상태 업데이트
        _uiState.update {
            it.copy(
                title = message.title,
                viewerCount = message.viewerCount,
                participantCount = message.participantCount,
                distance = message.distance,
                totalDistanceMeter = total,
                runners = runnersWithRank
            )
        }
    }

    /**
     * 페이스 계산 (분'초" 형식)
     *
     * @param distanceMeter 달린 거리 (미터)
     * @param elapsedTime 경과 시간 (초)
     * @return "5'30\"" 형식의 페이스
     */
    private fun calculatePace(distanceMeter: Int, elapsedTime: Int): String {
        if (distanceMeter <= 0 || elapsedTime <= 0) return "0'00\""

        val paceSeconds = (elapsedTime.toFloat() / (distanceMeter / 1000f))
        val min = (paceSeconds / 60).toInt()
        val sec = (paceSeconds % 60).toInt()

        return "${min}'${sec.toString().padStart(2, '0')}\""
    }

    /**
     * Hex 색상 문자열 → Color 변환
     *
     * @param hex "#FF5733" 형식
     * @return Color 객체
     */
    private fun parseHexColor(hex: String): Color = try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color(0xFF3DDC84) // 기본 색상
    }

    /**
     * 인덱스 기반 색상 선택 (파싱 실패 시 폴백)
     */
    private fun pickColor(index: Int): Color {
        val palette = listOf(
            Color(0xFF3DDC84), Color(0xFFFF6F61), Color(0xFF42A5F5),
            Color(0xFFFFB300), Color(0xFF7E57C2), Color(0xFF26C6DA),
            Color(0xFFEF5350), Color(0xFF66BB6A), Color(0xFFAB47BC),
            Color(0xFFFF7043)
        )
        return palette[index % palette.size]
    }

    /**
     * STOMP 연결 해제 및 리소스 정리
     */
    fun disconnectStomp() {
        subscription?.dispose()
        lifecycleSub?.dispose()
        stompClient?.disconnect()
        subscription = null
        lifecycleSub = null
        stompClient = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
        disconnectStomp()
    }
}
