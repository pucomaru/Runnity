package com.example.runnity.socket

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import java.util.concurrent.TimeUnit
import timber.log.Timber


object WebSocketManager : WebSocketListener() {
    // OkHttp 클라이언트: 서버가 주도하는 핑/퐁 시나리오 가정, 스트리밍 수신에 맞춘 설정
    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // 서버 푸시 수신을 위해 무한 대기(스트리밍)
        .build()

    private var socket: WebSocket? = null

    // 연결 상태를 외부에 노출하기 위한 모델
    sealed class WsState {
        object Closed : WsState()
        object Connecting : WsState()
        object Open : WsState()
        data class Failed(val t: Throwable?, val httpCode: Int?) : WsState()
    }

    // 현재 소켓 연결 상태
    private val _state = MutableStateFlow<WsState>(WsState.Closed)
    val state: StateFlow<WsState> = _state

    // 서버에서 수신되는 텍스트 메시지 스트림
    // replay=1 로 설정하여 새 구독자도 직전 메시지(CONNECTED 등)를 한 번은 받을 수 있도록 함
    private val _incoming = MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 64)
    val incoming: SharedFlow<String> = _incoming

    fun connect(url: String, tokenProvider: () -> String?, extraHeaders: Map<String, String> = emptyMap()) {
        // 기존 연결이 있다면 즉시 취소하여 중복 연결 방지
        socket?.cancel()
        socket = null

        if (url.isBlank()) {
            Timber.e("웹소켓 URL이 비어 있습니다.")
            _state.value = WsState.Failed(IllegalArgumentException("WebSocket URL is blank"), null)
            return
        }

        // 인증 토큰을 Bearer 헤더로 부착(필요 시)
        val headersBuilder = Headers.Builder()
        tokenProvider()?.takeIf { it.isNotBlank() }?.let { token ->
            headersBuilder.add("Authorization", "Bearer $token")
        }
        extraHeaders.forEach { (k, v) -> headersBuilder.add(k, v) }

        // 요청 생성 후 비동기 웹소켓 연결 시작
        val request = Request.Builder()
            .url(url)
            .headers(headersBuilder.build())
            .build()

        Timber.d("웹소켓 연결 시도: url=%s", url)
        _state.value = WsState.Connecting
        socket = client.newWebSocket(request, this)
    }

    // 텍스트 메시지 전송(연결되어 있지 않으면 false)
    fun send(text: String): Boolean = socket?.send(text) ?: false

    @Synchronized
    fun close(code: Int = 1000, reason: String = "bye") {
        // 정상 종료 코드(1000)와 함께 연결 종료 요청
        socket?.close(code, reason)
        socket = null
        _state.value = WsState.Closed
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        // 연결 완료
        Timber.d("웹소켓 연결 성공: code=%d", response.code)
        _state.value = WsState.Open
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // 서버에서 도착한 텍스트 메시지를 스트림으로 방출
        Timber.d("웹소켓 메시지 수신: %s", text)
        _incoming.tryEmit(text)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        // 서버/클라이언트가 종료를 예고한 상태 → 실제 종료 호출
        Timber.d("웹소켓 종료 진행 중: code=%d, reason=%s", code, reason)
        webSocket.close(code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        // 종료 완료
        Timber.d("웹소켓 종료 완료: code=%d, reason=%s", code, reason)
        _state.value = WsState.Closed
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // 네트워크 오류 또는 서버 응답 오류 등으로 실패
        Timber.e(t, "웹소켓 연결 실패: httpCode=%s, message=%s", response?.code, t.message)
        _state.value = WsState.Failed(t, response?.code)
    }
}
