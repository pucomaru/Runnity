package com.example.runnity.data.datalayer

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONObject

// 워치에서 세션 제어를 받고 ViewModel에 전달
class PhoneControlListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        val payload = runCatching { String(messageEvent.data) }.getOrNull() ?: return
        when (messageEvent.path) {
            "/session/control" -> {
                val json = runCatching { JSONObject(payload) }.getOrNull() ?: return
                val type = json.optString("type")
                when (type) {
                    "pause", "resume", "stop" -> {
                        Log.d(TAG, "relay control from watch -> $type")
                        SessionControlBus.emit(type)
                    }
                }
            }
            "/metrics/update" -> {
                val json = runCatching { JSONObject(payload) }.getOrNull() ?: return
                val m = SessionMetricsBus.WatchMetrics(
                    hrBpm = json.optInt("hr_bpm").let { if (json.has("hr_bpm")) it else null },
                    distanceM = null,
                    elapsedMs = null,
                    paceSpKm = null,
                    caloriesKcal = null,
                )
                SessionMetricsBus.emit(m)
            }
        }
    }
    companion object { private const val TAG = "PhoneWearCtrl" }
}
