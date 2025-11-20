package com.example.runnity.data.repository

import com.example.runnity.data.api.RetrofitInstance
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.StatsResponse
import com.example.runnity.data.remote.api.StatsApiService
import com.example.runnity.data.util.safeApiCall

/**
 * Stats 관련 Repository
 * 개인의 러닝 기록 통계 조회 로직 처리
 */
class StatsRepository(
    private val statsApiService: StatsApiService = RetrofitInstance.statsApi
) {

    // ==================== 개인의 기간별 러닝 기록 통계 조회 ====================

    /**
     * 개인의 기간별 러닝 기록 통계 조회
     * 특정 기간의 총 거리, 총 시간, 평균 페이스 및 기간별 통계 조회
     * 개인/챌린지 러닝 기록 5개씩 포함
     *
     * @param startDate 시작 날짜 (ISO 8601 DateTime, 예: "2025-11-01T00:00:00")
     * @param endDate 종료 날짜 (ISO 8601 DateTime, 예: "2025-11-30T23:59:59")
     * @param period 기간 단위 (week: 주별, month: 월별, year: 년별, all: 전체)
     * @return ApiResponse<StatsResponse>
     */
    suspend fun getStatsSummary(
        startDate: String,
        endDate: String,
        period: String
    ): ApiResponse<StatsResponse> {
        return safeApiCall {
            statsApiService.getStatsSummary(startDate, endDate, period)
        }
    }
}
