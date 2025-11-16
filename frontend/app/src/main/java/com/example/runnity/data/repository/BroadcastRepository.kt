package com.example.runnity.data.repository

import com.example.runnity.data.api.RetrofitInstance
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.response.BroadcastResponse
import com.example.runnity.data.remote.api.BroadcastApiService
import com.example.runnity.data.util.safeApiCall
import retrofit2.Response

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
    suspend fun getActiveBroadcasts(
        keyword: String? = null,
        distance: List<String>? = null,
        startAt: String? = null,
        endAt: String? = null,
        visibility: String? = null,
        sort: String? = null,
        page: Int = 0,
        size: Int = 10
    ): ApiResponse<List<BroadcastResponse>> {
        return safeApiCall {
            // Retrofit call: Response<List<BroadcastResponse>>
            val resp = broadcastApiService.getActiveBroadcasts(
                keyword = keyword,
                distance = distance,
                startAt = startAt,
                endAt = endAt,
                visibility = visibility,
                sort = sort,
                page = page,
                size = size
            )
            // safeApiCall은 일반적으로 retrofit Response<T>를 받으므로
            // 여기서 바로 BaseResponse로 변환해 반환
            if (resp.isSuccessful) {
                val body = resp.body() ?: emptyList()
                // 표준 래퍼로 어댑트
                Response.success(
                    BaseResponse(
                        isSuccess = true,
                        code = resp.code(),
                        message = "OK",
                        data = body
                    )
                )
            } else {
                // 에러일 때도 래퍼 형태로 내려주면 상위에서 일관 처리 쉬움
                Response.success(
                    BaseResponse(
                        isSuccess = false,
                        code = resp.code(),
                        message = resp.message(),
                        data = null
                    )
                )
            }
        }
    }
//
//    /**
//     * 활성화된 중계방 목록 조회
//     */
//    suspend fun getBroadcastLive(): ApiResponse<BroadcastPage> =
//        safeApiCall {
//            val res = broadcastApiService.getActiveBroadcasts()
//            // safeApiCall 내부에서 body() 반환
//        }.map { list -> BroadcastMapper.toPage(list ?: emptyList()) }
}
