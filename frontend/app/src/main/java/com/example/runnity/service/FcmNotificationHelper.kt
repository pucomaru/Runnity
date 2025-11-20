package com.example.runnity.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.runnity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.runnity.R
import com.example.runnity.data.util.FcmTokenManager
import com.example.runnity.data.util.TokenManager


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM_MSG", " 수신된 메시지: ${message.data}")

        // 🔹 Notification payload (title, body)가 있을 경우
        message.notification?.let {
            Log.d("FCM_MSG", "제목: ${it.title}, 내용: ${it.body}")
            showNotification(it.title ?: "알림", it.body ?: "")
        }

        // 🔹 Data payload (custom key-value)가 있을 경우
        if (message.data.isNotEmpty()) {
            val title = message.data["title"] ?: "데이터 알림"
            val body = message.data["body"] ?: message.data.toString()
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "fcm_default_channel"
        val channelName = "Runnity Notifications"

        // Android 8.0 이상은 NotificationChannel 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            // ⚙️ 채널이 이미 존재하는지 확인 (중복 방지)
            if (manager?.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "런니티 푸시 알림 채널"
                    enableVibration(true)
                    enableLights(true)
                }
                manager?.createNotificationChannel(channel)
            }
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "새로운 FCM 토큰: $token")

        if (TokenManager.isLoggedIn()) {
            FcmTokenManager.sendTokenToServer(token)
        }
    }

}
