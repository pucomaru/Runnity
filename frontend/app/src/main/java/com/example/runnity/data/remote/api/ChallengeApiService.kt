package com.example.runnity.data.remote.api

import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.request.CreateChallengeRequest
import com.example.runnity.data.model.request.JoinChallengeRequest
import com.example.runnity.data.model.response.ChallengeDetailResponse
import com.example.runnity.data.model.response.ChallengeListResponse
import com.example.runnity.data.model.response.ChallengeParticipantResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Challenge 관련 API 서비스
 * 챌린지 목록 조회, 생성, 참가, 취소, 상세 조회
 */
interface ChallengeApiService {

    // ==================== 챌린지 목록 조회 ====================

    /**
     * 챌린지 목록 조회
     * 검색, 필터링, 정렬, 페이징 지원
     *
     * @param keyword 검색 키워드 (선택)
     * @param distances 거리 필터 (예: FIVE, TEN, HALF - 여러 개 선택 가능)
     * @param startDate 시작 날짜 (이 날짜 이후 시작하는 챌린지만 조회, 최소 오늘, 예: 2025-11-14)
     * @param endDate 종료 날짜 (이 날짜 이전에 시작하는 챌린지만 조회, 최대 일주일 후, 예: 2025-11-17)
     * @param startTime 시작 시간 (이 시간 이후 시작하는 챌린지만 조회, 예: 09:00:00)
     * @param endTime 종료 시간 (이 시간 이전에 시작하는 챌린지만 조회, 예: 23:00:00)
     * @param visibility 공개 여부 (PUBLIC: 공개방만, ALL: 전체)
     * @param sort 정렬 기준 (LATEST: 임박순, POPULAR: 인기순)
     * @param page 페이지 번호 (기본: 0)
     * @param size 페이지 크기 (기본: 10)
     * @return 챌린지 목록
     */
    @GET("api/v1/challenges")
    suspend fun getChallenges(
        @Query("keyword") keyword: String? = null,
        @Query("distances") distances: List<String>? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("startTime") startTime: String? = null,
        @Query("endTime") endTime: String? = null,
        @Query("visibility") visibility: String? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<BaseResponse<ChallengeListResponse>>


    // ==================== 챌린지 생성 ====================

    /**
     * 챌린지 생성
     *
     * @param request 챌린지 생성 요청 (title, description, maxParticipants 등)
     * @return 생성된 챌린지 상세 정보
     */
    @POST("api/v1/challenges")
    suspend fun createChallenge(
        @Body request: CreateChallengeRequest
    ): Response<BaseResponse<ChallengeDetailResponse>>


    // ==================== 챌린지 참가 ====================

    /**
     * 챌린지 참가 신청
     * 비밀방인 경우 비밀번호 필요
     *
     * @param challengeId 챌린지 ID
     * @param request 참가 요청 (password 선택)
     * @return 참가자 정보
     */
    @POST("api/v1/challenges/{challengeId}/join")
    suspend fun joinChallenge(
        @Path("challengeId") challengeId: Long,
        @Body request: JoinChallengeRequest = JoinChallengeRequest()
    ): Response<BaseResponse<ChallengeParticipantResponse>>


    // ==================== 챌린지 참가 취소 ====================

    /**
     * 챌린지 참가 취소
     *
     * @param challengeId 챌린지 ID
     * @return 참가 취소 결과
     */
    @DELETE("api/v1/challenges/{challengeId}/join")
    suspend fun cancelChallenge(
        @Path("challengeId") challengeId: Long
    ): Response<BaseResponse<ChallengeParticipantResponse>>


    // ==================== 챌린지 상세 조회 ====================

    /**
     * 챌린지 상세 조회
     * 챌린지 정보와 참가자 목록 포함
     *
     * @param challengeId 챌린지 ID
     * @return 챌린지 상세 정보
     */
    @GET("api/v1/challenges/{challengeId}")
    suspend fun getChallengeDetail(
        @Path("challengeId") challengeId: Long
    ): Response<BaseResponse<ChallengeDetailResponse>>

    // 예약한 챌린지 조회
    @GET("api/v1/me/challenges")
    suspend fun gettReservedChallenges(

    )
}
