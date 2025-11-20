package com.example.runnity.data.remote.api

import com.example.runnity.data.model.response.BroadcastJoinResponse
import com.example.runnity.data.model.response.BroadcastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Broadcast 관련 API 서비스
 * 현재 활성화된 중계방 처리
 */
interface BroadcastApiService {

    /**
     * 현재 활성화된 중계 목록 조회
     * GET /api/v1/broadcast/active
     */
    @GET("api/v1/broadcast/active")
    suspend fun getActiveBroadcasts(
        @Query("keyword") keyword: String? = null,
        @Query("distance") distance: List<String>? = null,
        @Query("startAt") startAt: String? = null,
        @Query("endAt") endAt: String? = null,
        @Query("visibility") visibility: String? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<BroadcastResponse>>

    /**
     * 중계방 입장 (WebSocket URL 발급)
     * GET /api/v1/broadcast/join
     *
     * @param challengeId 챌린지 ID
     * @return wsUrl, topic, challengeId를 포함한 응답
     */
    @GET("api/v1/broadcast/join")
    suspend fun joinBroadcast(
        @Query("challengeId") challengeId: Long
    ): Response<BroadcastJoinResponse>
}
