package com.example.runnity.data.remote.api

import com.example.runnity.data.api.BroadcastResponse
import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.request.AddInfoRequest
import com.example.runnity.data.model.request.RefreshTokenRequest
import com.example.runnity.data.model.request.SocialLoginRequest
import com.example.runnity.data.model.request.UpdateProfileRequest
import com.example.runnity.data.model.response.AddInfoResponse
import com.example.runnity.data.model.response.LogoutResponse
import com.example.runnity.data.model.response.NicknameCheckResponse
import com.example.runnity.data.model.response.ProfileResponse
import com.example.runnity.data.model.response.SocialLoginResponse
import com.example.runnity.data.model.response.TokenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Broadcast 관련 API 서비스
 * 현재 활성화된 중계방 처리
 */
interface BroadcastApiService {

    /**
     * 현재 활성화된 중계 목록 조회
     *
     * @param nickname 체크할 닉네임
     * @return available (true: 사용 가능, false: 중복)
     */
    @GET("api/v1/broadcast/active")
    suspend fun getActiveBroadcasts(): Response<BaseResponse<List<BroadcastResponse>>>
}
