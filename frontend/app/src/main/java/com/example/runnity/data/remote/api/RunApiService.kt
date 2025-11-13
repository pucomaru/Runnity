package com.example.runnity.data.remote.api

import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.request.RunCreateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RunApiService {

    // 러닝 기록 저장
    @POST("api/v1/me/runs")
    suspend fun createRun(
        @Body request: RunCreateRequest
    ): Response<BaseResponse<Any>>
}
