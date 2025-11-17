package com.example.runnity.data.remote.api

import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.response.BroadcastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Broadcast 관련 API 서비스
 * 현재 활성화된 중계방 처리
 */
interface BroadcastApiService {

    /**
     * 현재 활성화된 중계 목록 조회
     *
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

    //TODO: url나오면 연결
//    @POST("/api/v1/broadcast/{challengeId}/join")
//    suspend fun joinBroadcast(@Path("challengeId") challengeId: Long): Response<Unit>
}
