package com.example.runnity.data.remote.api

import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.response.StatsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Stats 관련 API 서비스
 * 개인의 러닝 기록 통계 조회
 */
interface StatsApiService {

    // ==================== 개인의 기간별 러닝 기록 통계 조회 ====================

    /**
     * 개인의 기간별 러닝 기록 통계 조회
     * 특정 기간의 총 거리, 총 시간, 평균 페이스 및 기간별 통계 조회
     * 개인/챌린지 러닝 기록 5개씩 포함
     *
     * @param startDate 시작 날짜 (ISO 8601, 예: "2025-11-01")
     * @param endDate 종료 날짜 (ISO 8601, 예: "2025-11-30")
     * @param period 기간 단위 (WEEK: 주별, MONTH: 월별, YEAR: 년별, ALL: 전체)
     * @return 기간별 러닝 통계
     */
    @GET("api/v1/stats/summary")
    suspend fun getStatsSummary(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("period") period: String
    ): Response<BaseResponse<StatsResponse>>
}
