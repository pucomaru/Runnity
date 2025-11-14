package com.example.runnity.data.repository

import com.example.runnity.data.api.RetrofitInstance
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.request.RunCreateRequest
import com.example.runnity.data.remote.api.RunApiService
import com.example.runnity.data.util.safeApiCallUnit


class RunRepository(
    private val runApiService: RunApiService = RetrofitInstance.runApi
) {
    suspend fun createRun(request: RunCreateRequest): ApiResponse<Unit> {
        return safeApiCallUnit {
            runApiService.createRun(request)
        }
    }
}
