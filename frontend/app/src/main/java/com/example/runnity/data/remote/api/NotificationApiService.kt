package com.example.runnity.data.remote.api

import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.request.FcmTokenDeleteRequest
import com.example.runnity.data.model.request.FcmTokenRegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface NotificationApiService {

    @POST("api/v1/notifications/fcm-token")
    suspend fun registerToken(
        @Body request: FcmTokenRegisterRequest
    ): Response<BaseResponse<Void>>

    @DELETE("api/v1/notifications/fcm-token")
    suspend fun deleteToken(
        @Body request: FcmTokenDeleteRequest
    ): Response<BaseResponse<Void>>
}
