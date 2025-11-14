package com.example.runnity.ui.screens.broadcast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.runnity.BuildConfig
import com.example.runnity.data.util.TokenManager
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.dto.StompHeader

class BroadcastLiveViewModel(
    application: Application,
    private val tokenProvider: () -> String = { TokenManager.getAccessToken().toString() }
) : AndroidViewModel(application) {

    data class LiveUi(
        val title: String = "",
        val viewerCount: Int = 0,
        val hlsUrl: String = ""
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
        lifecycleSub = client.lifecycle().subscribe { /* 필요 시 로깅 */ }

        val headers = arrayListOf(
            StompHeader("Authorization", "Bearer ${tokenProvider()}"),
            StompHeader("accept-version", "1.1,1.2"),
            StompHeader("heart-beat", "10000,10000")
        )
        client.connect(headers)

        subscription?.dispose()
        subscription = client.topic("/topic/broadcast/$challengeId").subscribe { msg ->
            viewModelScope.launch {
                val prev = _uiState.value
                _uiState.value = prev.copy(
                    title = prev.title,
                    viewerCount = prev.viewerCount,
                    hlsUrl = prev.hlsUrl
                )
            }
        }
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
