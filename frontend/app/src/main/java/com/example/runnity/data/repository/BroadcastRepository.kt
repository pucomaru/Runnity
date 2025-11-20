package com.example.runnity.data.repository

import com.example.runnity.data.api.RetrofitInstance
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.response.BroadcastJoinResponse
import com.example.runnity.data.model.response.BroadcastResponse
import com.example.runnity.data.remote.api.BroadcastApiService
import com.example.runnity.data.util.safeApiCall
import retrofit2.Response
import timber.log.Timber

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

            if (resp.isSuccessful) {
                val body = resp.body() ?: emptyList()
                Response.success(
                    BaseResponse(
                        isSuccess = true,
                        code = resp.code(),
                        message = "OK",
                        data = body
                    )
                )
            } else {
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

    /**
     * 중계방 입장 (WebSocket 정보 요청)
     *
     * @param challengeId 챌린지 ID
     * @return WebSocket URL, Topic, ChallengeId
     */
    suspend fun joinBroadcast(challengeId: Long): ApiResponse<BroadcastJoinResponse> {
        Timber.d("joinBroadcast 호출: challengeId=$challengeId")

        return try {
            val response = broadcastApiService.joinBroadcast(challengeId)

            Timber.d("API 응답 코드: ${response.code()}")
            Timber.d("API 응답 성공 여부: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Timber.d("응답 바디: wsUrl=${body.wsUrl}, topic=${body.topic}, challengeId=${body.challengeId}")
                    ApiResponse.Success(body)
                } else {
                    Timber.e("응답 바디가 null입니다")
                    ApiResponse.Error( response.code(),"응답 데이터가 비어있습니다")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Timber.e("API 에러 응답: code=${response.code()}, message=${response.message()}, body=$errorBody")
                ApiResponse.Error(
                    message = errorBody ?: response.message() ?: "Unknown error",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "joinBroadcast API 호출 중 예외 발생")
            ApiResponse.Error(404, "네트워크 오류: ${e.localizedMessage}")
        }
    }
}
