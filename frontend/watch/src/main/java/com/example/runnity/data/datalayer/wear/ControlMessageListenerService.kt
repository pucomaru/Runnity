package com.example.runnity.data.datalayer.wear

import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.runnity.health.ExerciseFgService
import com.example.runnity.ui.WatchCountdownActivity
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONObject

class ControlMessageListenerService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != "/session/control") return
        val payload = runCatching { String(messageEvent.data) }.getOrNull() ?: return
        Log.d(TAG, "msg=/session/control payload=$payload")

        val json = runCatching { JSONObject(payload) }.getOrNull() ?: return
        when (json.optString("type")) {
            "prepare" -> {
                Log.d(TAG, "prepare -> ExerciseFgService ACTION_PREPARE")
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, ExerciseFgService::class.java).apply {
                        action = "com.example.runnity.action.PREPARE"
                    }
                )
            }
            "countdown" -> {
                val sec = json.optInt("seconds", 3)
                Log.d(TAG, "countdown $sec s -> launch WatchCountdownActivity")
                try {
                    val i = Intent(this, WatchCountdownActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("seconds", sec)
                    }
                    startActivity(i)
                } catch (t: Throwable) {
                    Log.w(TAG, "failed to launch countdown", t)
                }
            }
            "start" -> {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, ExerciseFgService::class.java).apply {
                        action = "com.example.runnity.action.START"
                    }
                )
            }
            "stop" -> {
                startService(
                    Intent(this, ExerciseFgService::class.java).apply {
                        action = "com.example.runnity.action.STOP"
                    }
                )
            }
            "pause" -> {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, ExerciseFgService::class.java).apply {
                        action = "com.example.runnity.action.PAUSE"
                    }
                )
            }
            "resume" -> {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, ExerciseFgService::class.java).apply {
                        action = "com.example.runnity.action.RESUME"
                    }
                )
            }
            "rank" -> {
                // 폰에서 보낸 현재 순위 정보를 워치 내부 브로드캐스트로 전달
                val rank = json.optInt("seconds", -1)
                if (rank > 0) {
                    val intent = Intent("com.example.runnity.action.RANK").apply {
                        setPackage(packageName)
                        putExtra("rank", rank)
                    }
                    sendBroadcast(intent)
                }
            }
            else -> Log.w(TAG, "unknown type=${json.optString("type")}")
        }
    }

    companion object {
        private const val TAG = "ControlRouter"
    }
}
