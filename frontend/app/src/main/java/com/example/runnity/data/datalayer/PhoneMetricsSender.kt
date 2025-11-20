package com.example.runnity.data.datalayer

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject

private const val METRICS_TAG = "PhoneMetricsSender"
private const val METRICS_PATH = "/phone/metrics"

// 폰에서 계산한 세션 메트릭을 워치로 전송
// - 거리/페이스/칼로리는 항상 폰(GPS) 기준
// - 워치는 HR만 자체 센서 값을 사용
fun sendSessionMetricsToWatch(
    context: Context,
    distanceMeters: Double,
    paceSecPerKm: Double?,
    caloriesKcal: Double,
    elapsedMs: Long,
) {
    // 의미있는 값이 없으면 전송하지 않음
    if (distanceMeters <= 0.0 && caloriesKcal <= 0.0 && (paceSecPerKm == null || paceSecPerKm <= 0.0)) {
        return
    }

    val json = JSONObject().apply {
        put("type", "metrics")
        if (distanceMeters > 0.0) put("distance_m", distanceMeters)
        if (paceSecPerKm != null && paceSecPerKm.isFinite() && paceSecPerKm > 0.0) put("pace_spkm", paceSecPerKm)
        if (caloriesKcal > 0.0) put("cal_kcal", caloriesKcal)
        if (elapsedMs > 0L) put("elapsed_ms", elapsedMs)
    }.toString()

    val bytes = json.toByteArray()
    val nodeClient = Wearable.getNodeClient(context)
    val msgClient = Wearable.getMessageClient(context)

    nodeClient.connectedNodes
        .addOnSuccessListener { nodes ->
            val targets = nodes.filter { it.isNearby }
            if (targets.isEmpty()) {
                Log.w(METRICS_TAG, "no nearby watch nodes; skip send metrics: $json")
                return@addOnSuccessListener
            }
            targets.forEach { node ->
                msgClient.sendMessage(node.id, METRICS_PATH, bytes)
                    .addOnSuccessListener { Log.d(METRICS_TAG, "sent metrics to ${node.displayName}: $json") }
                    .addOnFailureListener { t -> Log.e(METRICS_TAG, "send metrics fail to ${node.displayName}", t) }
            }
        }
        .addOnFailureListener { t ->
            Log.e(METRICS_TAG, "connectedNodes fail (metrics)", t)
        }
}
