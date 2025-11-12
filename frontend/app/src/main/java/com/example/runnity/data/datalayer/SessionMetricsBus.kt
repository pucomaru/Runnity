package com.example.runnity.data.datalayer

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// 워치 메트릭 버스 (워치 우선 적용을 위한 단순 버스)
object SessionMetricsBus {
    data class WatchMetrics(
        val hrBpm: Int? = null,
        val distanceM: Double? = null,
        val elapsedMs: Long? = null,
        val paceSpKm: Double? = null,
        val caloriesKcal: Double? = null,
        val ts: Long = System.currentTimeMillis()
    )
    private val _events = MutableSharedFlow<WatchMetrics>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()
    fun emit(m: WatchMetrics) { _events.tryEmit(m) }
}
