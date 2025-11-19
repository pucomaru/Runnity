package com.example.runnity.data.datalayer.wear

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONObject

// 폰에서 전송한 세션 메트릭(distance/pace/calories 등)을 수신해
// 워치 로컬 브로드캐스트(ACTION_METRICS)로 전달한다.
// - HR과 시간(elapsed)은 여전히 ExerciseFgService 가 브로드캐스트
// - 이 서비스는 거리/페이스/칼로리만 덮어쓰는 역할
class PhoneMetricsListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != "/phone/metrics") return
        val payload = runCatching { String(messageEvent.data) }.getOrNull() ?: return
        Log.d(TAG, "msg=/phone/metrics payload=$payload")

        val json = runCatching { JSONObject(payload) }.getOrNull() ?: return
        val intent = Intent("com.example.runnity.action.METRICS").apply {
            setPackage(packageName)
            if (json.has("elapsed_ms")) putExtra("elapsed_ms", json.optLong("elapsed_ms"))
            if (json.has("distance_m")) putExtra("distance_m", json.optDouble("distance_m"))
            if (json.has("pace_spkm")) putExtra("pace_spkm", json.optDouble("pace_spkm"))
            if (json.has("cal_kcal")) putExtra("cal_kcal", json.optDouble("cal_kcal"))
        }
        sendBroadcast(intent)
    }

    companion object {
        private const val TAG = "PhoneMetricsListener"
    }
}
