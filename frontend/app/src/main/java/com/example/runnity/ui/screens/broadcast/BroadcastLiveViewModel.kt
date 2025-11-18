package com.example.runnity.ui.screens.broadcast

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.repository.BroadcastRepository
import com.example.runnity.data.util.TokenManager
import com.google.gson.Gson
import com.google.gson.JsonObject
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
import java.util.Locale

class BroadcastLiveViewModel(
    application: Application
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val gson = Gson()
    private val tokenProvider: () -> String? = { TokenManager.getAccessToken() }
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
        val highlightCommentary: String? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    data class RunnerUi(
        val runnerId: Long,
        val nickname: String,
        val profileImage: String? = null,
        val color: Color,
        val distanceMeter: Double,  // Double로 변경
        val ratio: Float,
        val pace: Double,          // Double로 변경
        val rank: Int
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

    private var pingJob = viewModelScope.launch { } // 초기 dummy, 실제는 startPingLoop에서 교체

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(application.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Timber.e("TTS: 한국어를 지원하지 않습니다.")
                _uiState.update { it.copy(errorMessage = "음성 안내를 지원하지 않는 기기입니다.") }
            } else {
                Timber.d("TTS 엔진 초기화 성공")
            }
        } else {
            Timber.e("TTS 초기화 실패")
        }
    }

    // 러너 선택 (말풍선용)
    fun selectRunner(runnerId: Long?) {
        _uiState.update { it.copy(selectedRunnerId = runnerId) }
    }

    // HLS 플레이어
    fun preparePlayer(url: String?) {
        if (!url.isNullOrBlank()) {
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.playWhenReady = true
        }
    }

    fun releasePlayer() {
        _player?.release()
        _player = null
    }

    fun initializeFrom(item: com.example.runnity.data.model.response.BroadcastListItem) {
        _uiState.update {
            it.copy(
                title = item.title,
                viewerCount = item.viewerCount,
                participantCount = item.participantCount,
                distance = com.example.runnity.data.util.DistanceUtils.codeToLabel(item.distance),
                totalDistanceMeter = com.example.runnity.data.util.DistanceUtils.codeToMeter(item.distance)
            )
        }
    }

    /**
     * 1. /api/v1/broadcast/join → wsUrl, topic
     * 2. wsUrl 로 STOMP 연결
     * 3. topic 구독 → STREAM / LLM 수신
     */
    fun joinAndConnect(challengeId: Long) {

        if (_uiState.value.isLoading.not() && stompClient?.isConnected == true) {
            Timber.d("이미 STOMP 연결 상태")
            return
        }
        viewModelScope.launch {
            when (val response = repository.joinBroadcast(challengeId)) {
                is ApiResponse.Success -> {
                    val join = response.data
                    _uiState.update {
                        it.copy(
                            hlsUrl = join.wsUrl,
                            title = uiState.value.title,
                            totalDistanceMeter = uiState.value.totalDistanceMeter,
                            viewerCount = uiState.value.viewerCount
                        )
                    }
                }

                else -> {}
            }
        }

        reconnectAttempts = 0

        viewModelScope.launch {
            when (val response = repository.joinBroadcast(challengeId)) {
                is ApiResponse.Success -> {
                    val join = response.data
                    Timber.d("중계방 입장 성공: wsUrl=${join.wsUrl}, topic=${join.topic}")
                    connectStomp(join.wsUrl, join.topic, challengeId)
                }
                is ApiResponse.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "중계방 입장에 실패했습니다: ${response.message}"
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    private fun connectStomp(wsUrl: String, topic: String, challengeId: Long) {
        Timber.d("STOMP 연결 시도: $wsUrl")
        disconnectStomp()

        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)
        stompClient = client

        val headers = listOf(
            StompHeader("Authorization", "Bearer ${tokenProvider() ?: ""}"),
            StompHeader("challengeId", challengeId.toString()),
            StompHeader("accept-version", "1.1,1.2"),
            StompHeader("heart-beat", "0,0")
        )

        lifecycleSub = client.lifecycle().subscribe { event ->
            Timber.d("STOMP Lifecycle: ${event.type}")
            when (event.type) {
                LifecycleEvent.Type.OPENED -> {
                    Timber.d("STOMP 연결 성공, 토픽 구독 시작")
                    reconnectAttempts = 0
                    subscribeToTopic(client, topic)
                    startPingLoop(client)
                    _uiState.update { it.copy(isLoading = false) }
                }
                LifecycleEvent.Type.ERROR -> {
                    Timber.e(event.exception, "STOMP 연결 에러")
                    attemptReconnect(wsUrl, topic, challengeId)
                }
                LifecycleEvent.Type.CLOSED -> {
                    Timber.d("STOMP 연결 종료")
                    stopPingLoop()
                }
                else -> {}
            }
        }

        client.connect(headers)
    }

    private fun startPingLoop(client: ua.naiksoftware.stomp.StompClient) {
        stopPingLoop()
        pingJob = viewModelScope.launch {
            while (true) {
                delay(30_000L)
                try {
                    client.send("/app/ping", "PING").subscribe()
                } catch (e: Exception) {
                    Timber.e(e, "STOMP 핑 전송 실패")
                }
            }
        }
    }

    private fun stopPingLoop() {
        pingJob.cancel()
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
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "WebSocket 연결에 실패했습니다."
                )
            }
        }
    }

    private fun subscribeToTopic(client: ua.naiksoftware.stomp.StompClient, topic: String) {
        Timber.d("토픽 구독 시작: $topic")

        subscription?.dispose()

        subscription = client.topic(topic).subscribe(
            { msg ->
                val payload = msg.payload
                Timber.d("📡 수신한 메시지: $payload")
                viewModelScope.launch(Dispatchers.Default) {
                    handleSocketPayload(payload)
                }
            },
            { error ->
                Timber.e(error, "STOMP 구독 에러: ${error.message}")
                _uiState.update {
                    it.copy(errorMessage = "실시간 데이터 수신 실패: ${error.localizedMessage}")
                }
            }
        )
    }

    private fun handleSocketPayload(json: String) {
        try {
            val wrapper = gson.fromJson(json, WebSocketWrapper::class.java)

            Timber.d(
                "[BroadcastWS] raw wrapper: type=%s, subtype=%s, challengeId=%d, payload=%s",
                wrapper.type,
                wrapper.subtype,
                wrapper.challengeId,
                wrapper.payload?.toString()
            )

            when (wrapper.type) {
                "STREAM" -> handleStreamMessage(wrapper)
                "LLM"    -> handleLlmMessage(wrapper)
                else     -> Timber.d("알 수 없는 type=${wrapper.type}")
            }
        } catch (e: Exception) {
            Timber.e(e, "WebSocketWrapper 파싱 실패")
        }
    }

    /**
     * STREAM: distance / pace / ranking 은 서버가 다 계산해서 줌
     * → 여기서는 그대로 UI에 반영만 한다.
     */
    private fun handleStreamMessage(wrapper: WebSocketWrapper) {
        val payloadObj: JsonObject = wrapper.payload
        val stream = gson.fromJson(payloadObj, StreamPayload::class.java)


        Timber.d(
            "[BroadcastWS][STREAM-%s] runnerId=%d, nick=%s, dist=%.2f, pace=%.2f, rank=%d",
            wrapper.subtype,
            stream.runnerId,
            stream.nickname,
            stream.distance,
            stream.pace,
            stream.ranking
        )


        val total = _uiState.value.totalDistanceMeter.takeIf { it > 0 } ?: 1
        val current = _uiState.value.runners
        val existing = current.find { it.runnerId == stream.runnerId }
        val color = existing?.color ?: pickColor(current.size)


        when (wrapper.subtype) {
            "START" -> {
                Timber.d("STREAM START")
            }

            "RUNNING" -> {
//                val updated = RunnerUi( //더미
//                    runnerId = stream.runnerId,
//                    nickname = stream.nickname,
//                    profileImage = stream.profileImage,
//                    color = color,
//                    distanceMeter = stream.distance.toInt(),
//                    ratio = (stream.distance.toFloat() / total).coerceIn(0f, 1f),
//                    pace = String.format("%.2f", stream.pace),
//                    rank = stream.ranking
//                )
//
//                val merged = if (existing == null) {
//                    current + updated
//                } else {
//                    current.map { if (it.runnerId == stream.runnerId) updated else it }
//                }
//
//                _uiState.update {
//                    it.copy(runners = merged.sortedBy { r -> r.rank })
//                }

                val total = _uiState.value.totalDistanceMeter.takeIf { it > 0 } ?: 1
                val current = _uiState.value.runners
                val existing = current.find { it.runnerId == stream.runnerId }
                val color = existing?.color ?: pickColor(current.size)

                val updated = RunnerUi(
                    runnerId = stream.runnerId,
                    nickname = stream.nickname,
                    profileImage = stream.profileImage,
                    color = color,
                    distanceMeter = stream.distance,
                    ratio = (stream.distance.toFloat() / total).coerceIn(0f, 1f),
                    pace = stream.pace,
                    rank = stream.ranking
                )

                val merged = if (existing == null) {
                    current + updated
                } else {
                    current.map { if (it.runnerId == stream.runnerId) updated else it }
                }

                Timber.d("Updating runners: new size = ${merged.size}")

                _uiState.update {
                    it.copy(
                        runners = merged.sortedBy { r -> r.rank }
                    )
                }
            }

            "FINISH" -> {
                Timber.d("STREAM FINISH runnerId=${stream.runnerId}")
            }

            "LEAVE" -> {
                Timber.d("STREAM LEAVE runnerId=${stream.runnerId}")
                val current = _uiState.value.runners
                _uiState.update {
                    it.copy(
                        runners = current.filterNot { r -> r.runnerId == stream.runnerId }
                    )
                }
            }
        }
    }

    private fun handleLlmMessage(wrapper: WebSocketWrapper) {
        val payloadObj: JsonObject = wrapper.payload
        val llm = gson.fromJson(payloadObj, LlmPayload::class.java)

        Timber.d("LLM ${wrapper.subtype} commentary=${llm.commentary}")

        _uiState.update {
            it.copy(
                highlightCommentary = llm.commentary
            )
        }

        speakOut(llm.commentary)
    }

    private fun speakOut(text: String) {
        if (tts == null) {
            Timber.e("TTS가 초기화되지 않았습니다.")
            return
        }
        // 음성이 겹치지 않도록 QUEUE_FLUSH 사용
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun pickColor(index: Int): Color {
        val palette = listOf(
            Color(0xFF3DDC84), Color(0xFFFF6F61), Color(0xFF42A5F5),
            Color(0xFFFFB300), Color(0xFF7E57C2), Color(0xFF26C6DA),
            Color(0xFFEF5350), Color(0xFF66BB6A), Color(0xFFAB47BC),
            Color(0xFFFF7043)
        )
        return palette[index % palette.size]
    }

    fun disconnectStomp() {
        subscription?.dispose()
        lifecycleSub?.dispose()
        stompClient?.disconnect()
        subscription = null
        lifecycleSub = null
        stompClient = null
        stopPingLoop()
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
        disconnectStomp()

        tts?.stop()
        tts?.shutdown()
        tts = null
        Timber.d("TTS 엔진 해제 완료")
    }
}
