package com.example.runnity.data.repository

import com.example.runnity.data.api.RetrofitInstance
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.request.SaveRunRecordRequest
import com.example.runnity.data.model.response.MonthlyRunsResponse
import com.example.runnity.data.model.response.MyChallengesResponse
import com.example.runnity.data.model.response.RunRecordDetailResponse
import com.example.runnity.data.model.response.RunRecordResponse
import com.example.runnity.data.remote.api.RunHistoryApiService
import com.example.runnity.data.util.safeApiCall

/**
 * RunHistory 관련 Repository
 * 러닝 기록 조회, 저장, 참가한 챌린지 조회 로직 처리
 */
class RunHistoryRepository(
    private val runHistoryApiService: RunHistoryApiService = RetrofitInstance.runHistoryApi
) {

    // ==================== 월간 러닝 기록 조회 ====================

    /**
     * 월간 참여한 챌린지 조회
     * 특정 월의 개인 러닝 기록 및 챌린지 기록 조회
     *
     * @param year 연도 (예: 2025)
     * @param month 월 (예: 11)
     * @return ApiResponse<MonthlyRunsResponse>
     */
    suspend fun getMonthlyRuns(
        year: Int,
        month: Int
    ): ApiResponse<MonthlyRunsResponse> {
        return safeApiCall {
            runHistoryApiService.getMonthlyRuns(year, month)
        }
    }

    // ==================== 러닝 기록 저장 ====================

    /**
     * 개인의 러닝 기록 저장
     * 개인 러닝 또는 챌린지 러닝 기록 저장
     *
     * @param request 러닝 기록 저장 요청 (type, startDateTime, endDateTime, totalDistance 등)
     * @return ApiResponse<RunRecordResponse>
     */
    suspend fun saveRunRecord(
        request: SaveRunRecordRequest
    ): ApiResponse<RunRecordResponse> {
        return safeApiCall {
            runHistoryApiService.saveRunRecord(request)
        }
    }

    // ==================== 러닝 기록 상세 조회 ====================

    /**
     * 참여한 챌린지 상세 조회
     * 특정 러닝 기록의 상세 정보 및 랩 타임 조회
     *
     * @param runRecordId 러닝 기록 ID
     * @return ApiResponse<RunRecordDetailResponse>
     */
    suspend fun getRunRecordDetail(
        runRecordId: Long
    ): ApiResponse<RunRecordDetailResponse> {
        return safeApiCall {
            runHistoryApiService.getRunRecordDetail(runRecordId)
        }
    }

    // ==================== 내가 참가한 챌린지 목록 조회 ====================

    /**
     * 내가 참가 신청한 챌린지 목록 조회
     * 입장 가능한 챌린지 (진행 중) 및 참가 신청한 챌린지 목록
     *
     * @return ApiResponse<MyChallengesResponse>
     */
    suspend fun getMyChallenges(): ApiResponse<MyChallengesResponse> {
        return safeApiCall {
            runHistoryApiService.getMyChallenges()
        }
    }
}
