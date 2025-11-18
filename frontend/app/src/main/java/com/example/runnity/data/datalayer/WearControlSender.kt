package com.example.runnity.data.datalayer

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import org.json.JSONObject

private const val TAG = "WearControlSender"
private const val CONTROL_PATH = "/session/control"

// 최소 구현: 폰 -> 워치 제어 메시지 전송 유틸
// 사용 예: sendSessionControl(context, "start")
fun sendSessionControl(
    context: Context,
    type: String,
    seconds: Int? = null
) {
    
    val json = JSONObject().apply {
        put("type", type)
        if (seconds != null) put("seconds", seconds)
    }.toString()

    val bytes = json.toByteArray()
    val nodeClient = Wearable.getNodeClient(context)
    val msgClient = Wearable.getMessageClient(context)

    nodeClient.connectedNodes
        .addOnSuccessListener { nodes ->
            val targets = nodes.filter { it.isNearby }
            if (targets.isEmpty()) {
                Log.w(TAG, "no nearby watch nodes; skip send: $json")
                return@addOnSuccessListener
            }
            targets.forEach { node ->
                msgClient.sendMessage(node.id, CONTROL_PATH, bytes)
                    .addOnSuccessListener { Log.d(TAG, "sent to ${node.displayName}: $json") }
                    .addOnFailureListener { t -> Log.e(TAG, "send fail to ${node.displayName}", t) }
            }
        }
        .addOnFailureListener { t ->
            Log.e(TAG, "connectedNodes fail", t)
        }
}
