package com.example.runnity.data.util

import android.util.Log
import com.example.runnity.data.api.RetrofitInstance
import com.example.runnity.data.model.request.FcmTokenDeleteRequest
import com.example.runnity.data.model.request.FcmTokenRegisterRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.runnity.data.util.TokenManager.isLoggedIn


object FcmTokenManager {

    private const val TAG = "FcmTokenManager"

    /**
     * FCM 토큰 가져와서 서버에 저장 (로그인된 경우만)
     */
    fun updateTokenToServer() {
        if (!isLoggedIn()) {
            Log.d(TAG, "로그인되지 않아 FCM 토큰 저장을 스킵합니다")
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM 토큰 획득: $token")
                sendTokenToServer(token)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "FCM 토큰 가져오기 실패", e)
            }
    }

    /**
     * FCM 토큰 저장 API 호출
     */
    fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.notificationApi.registerToken(
                    FcmTokenRegisterRequest(token)
                )
                Log.d(TAG, "FCM 토큰 저장 성공: ${response.code()}")
            } catch (e: Exception) {
                Log.e(TAG, "FCM 토큰 저장 실패", e)
            }
        }
    }

    /**
     * 로그아웃 시 토큰 삭제
     */
    fun deleteTokenFromServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.notificationApi.deleteToken(
                    FcmTokenDeleteRequest(token)
                )
                Log.d(TAG, "FCM 토큰 삭제 성공: ${response.code()}")
            } catch (e: Exception) {
                Log.e(TAG, "FCM 토큰 삭제 실패", e)
            }
        }
    }
}
