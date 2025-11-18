package com.example.runnity.data.datalayer

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// 세션 제어 버스
object SessionControlBus {
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()
    fun emit(type: String) { _events.tryEmit(type) }
}
