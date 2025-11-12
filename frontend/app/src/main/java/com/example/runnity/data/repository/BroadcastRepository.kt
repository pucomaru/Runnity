package com.example.runnity.data.repository

import android.net.Uri
import com.example.runnity.data.api.BroadcastResponse
import com.example.runnity.data.api.RetrofitInstance
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.request.AddInfoRequest
import com.example.runnity.data.model.request.SocialLoginRequest
import com.example.runnity.data.model.request.UpdateProfileRequest
import com.example.runnity.data.model.response.AddInfoResponse
import com.example.runnity.data.model.response.NicknameCheckResponse
import com.example.runnity.data.model.response.ProfileResponse
import com.example.runnity.data.model.response.SocialLoginResponse
import com.example.runnity.data.remote.api.AuthApiService
import com.example.runnity.data.remote.api.BroadcastApiService
import com.example.runnity.data.util.TokenManager
import com.example.runnity.data.util.UserProfileManager
import com.example.runnity.data.util.safeApiCall
import com.example.runnity.data.util.safeApiCallUnit
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Broadcast 관련 Repository
 * 현재 활성화된 중계방 로직 처리
 */
class BroadcastRepository(
    private val broadcastApiService: BroadcastApiService = RetrofitInstance.broadcastApi
) {
    /**
     * 활성화된 중계방 목록 조회
     */
    suspend fun getActiveBroadcasts(): ApiResponse<List<BroadcastResponse>> {
        return safeApiCall { broadcastApiService.getActiveBroadcasts() }
    }
}
