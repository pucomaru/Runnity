package com.example.runnity.ui.screens.broadcast

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.runnity.BuildConfig
import com.example.runnity.data.model.response.LiveProgressMessage
import com.example.runnity.data.util.TokenManager
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.dto.StompHeader
import kotlin.math.max

class BroadcastLiveViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val gson = Gson()

    // 내부에서 직접 정의
    private val tokenProvider: () -> String = { TokenManager.getAccessToken().toString() }

    data class LiveUi(
        val title: String = "",
        val viewerCount: Int = 0,
        val hlsUrl: String = "",
        val totalDistance: Int = 0, // m
        val runners: List<RunnerUi> = emptyList()
    )

    data class RunnerUi(
        val runnerId: Long,
        val nickname: String,
        val color: Color,
        val distance: Int,      // m
        val ratio: Float,       // 0f..1f
        val pace: String = "0'00\"",  // ← 페이스 추가 (예: "5'30\"")
        val rank: Int = 0       // ← 현재 순위 추가
    )

    private val _uiState = MutableStateFlow(LiveUi())
    val uiState: StateFlow<LiveUi> = _uiState.asStateFlow()

    private var _player: ExoPlayer? = null
    val player: ExoPlayer
        get() = _player ?: ExoPlayer.Builder(getApplication()).build().also { _player = it }

    private var stompClient: ua.naiksoftware.stomp.StompClient? = null
    private var subscription: Disposable? = null
    private var lifecycleSub: Disposable? = null

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

    fun connectStomp(challengeId: String) {
        val base = BuildConfig.BASE_URL.trimEnd('/')
        val wsBase = base.replace("https://", "wss://").replace("http://", "ws://")
        val wsUrl = "$wsBase/ws-stomp/websocket"

        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl, null)
        stompClient = client

        lifecycleSub?.dispose()
        lifecycleSub = client.lifecycle().subscribe { /* log if needed */ }

        val headers = arrayListOf(
            StompHeader("Authorization", "Bearer ${tokenProvider()}"),
            StompHeader("accept-version", "1.1,1.2"),
            StompHeader("heart-beat", "10000,10000")
        )
        client.connect(headers)

        subscription?.dispose()
        subscription = client.topic("/topic/broadcast/$challengeId").subscribe { msg ->
            val payload = msg.payload
            Timber.d("수신한 메시지: $payload")

            val model = runCatching {
                gson.fromJson(payload, LiveProgressMessage::class.java)
            }.getOrNull()

            if (model == null) {
                Timber.e("메시지 파싱 실패!")
                return@subscribe
            }

            Timber.d("파싱 성공: participants=%d", model.participants.size)

            viewModelScope.launch(Dispatchers.Main) {
                val total = max(model.totalDistanceMeter, 1)
                val runners = model.participants.mapIndexed { idx, rp ->
                    val ratio = (rp.distanceMeter.toFloat() / total).coerceIn(0f, 1f)
                    RunnerUi(
                        runnerId = rp.runnerId,
                        nickname = rp.nickname,
                        color = rp.color?.let { parseHexColor(it) } ?: pickColor(idx),
                        distance = rp.distanceMeter,
                        ratio = ratio
                    )
                }
                Timber.d("Runners 업데이트: count=%d", runners.size)
                _uiState.update { it.copy(totalDistance = total, runners = runners) }
            }
        }
    }

    private fun parseHexColor(hex: String): Color = try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        Color(0xFF3DDC84)
    }

    private fun pickColor(index: Int): Color {
        val palette = listOf(  // ← 제네릭 타입 명시 필요 없음
            Color(0xFF3DDC84),
            Color(0xFFFF6F61),
            Color(0xFF42A5F5),
            Color(0xFFFFB300),
            Color(0xFF7E57C2),
            Color(0xFF26C6DA),
            Color(0xFFEF5350),
            Color(0xFF66BB6A),
            Color(0xFFAB47BC),
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
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
        disconnectStomp()
    }
}

