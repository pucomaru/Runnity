package com.example.runnity.data.remote.api

import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.request.SaveRunRecordRequest
import com.example.runnity.data.model.response.MonthlyRunsResponse
import com.example.runnity.data.model.response.MyChallengesResponse
import com.example.runnity.data.model.response.RunRecordDetailResponse
import com.example.runnity.data.model.response.RunRecordResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * RunHistory 관련 API 서비스
 * 러닝 기록 조회, 저장, 참가한 챌린지 조회
 */
interface RunHistoryApiService {

    // ==================== 월간 러닝 기록 조회 ====================

    /**
     * 월간 참여한 챌린지 조회
     * 특정 월의 개인 러닝 기록 및 챌린지 기록 조회
     *
     * @param year 연도 (예: 2025)
     * @param month 월 (예: 11)
     * @return 개인 러닝 기록 및 챌린지 기록
     */
    @GET("api/v1/me/runs")
    suspend fun getMonthlyRuns(
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<BaseResponse<MonthlyRunsResponse>>


    // ==================== 러닝 기록 저장 ====================

    /**
     * 개인의 러닝 기록 저장
     * 개인 러닝 또는 챌린지 러닝 기록 저장
     *
     * @param request 러닝 기록 저장 요청 (type, startDateTime, endDateTime, totalDistance 등)
     * @return 저장된 러닝 기록
     */
    @POST("api/v1/me/runs")
    suspend fun saveRunRecord(
        @Body request: SaveRunRecordRequest
    ): Response<BaseResponse<RunRecordResponse>>


    // ==================== 러닝 기록 상세 조회 ====================

    /**
     * 참여한 챌린지 상세 조회
     * 특정 러닝 기록의 상세 정보 및 랩 타임 조회
     *
     * @param runRecordId 러닝 기록 ID
     * @return 러닝 기록 상세 정보 (랩 타임 포함)
     */
    @GET("api/v1/me/runs/{runRecordId}")
    suspend fun getRunRecordDetail(
        @Path("runRecordId") runRecordId: Long
    ): Response<BaseResponse<RunRecordDetailResponse>>


    // ==================== 내가 참가한 챌린지 목록 조회 ====================

    /**
     * 내가 참가 신청한 챌린지 목록 조회
     * 입장 가능한 챌린지 (진행 중) 및 참가 신청한 챌린지 목록
     *
     * @return 입장 가능한 챌린지 및 참가 신청한 챌린지 목록
     */
    @GET("api/v1/me/challenges")
    suspend fun getMyChallenges(): Response<BaseResponse<MyChallengesResponse>>
}
