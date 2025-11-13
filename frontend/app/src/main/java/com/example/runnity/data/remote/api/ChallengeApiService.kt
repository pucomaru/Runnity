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
     * @param distance 거리 필터 (FIVE, TEN 등, 선택)
     * @param startAt 시작 일시 (이 시간 이후 시작하는 챌린지만 조회)
     * @param endAt 종료 일시 (이 시간 이전에 시작하는 챌린지만 조회)
     * @param visibility 공개 여부 (PUBLIC: 공개방만, ALL: 전체)
     * @param sort 정렬 기준 (LATEST, POPULAR 등, 선택)
     * @param page 페이지 번호 (기본: 0)
     * @param size 페이지 크기 (기본: 10)
     * @return 챌린지 목록
     */
    @GET("api/v1/challenges")
    suspend fun getChallenges(
        @Query("keyword") keyword: String? = null,
        @Query("distance") distance: String? = null,
        @Query("startAt") startAt: String? = null,
        @Query("endAt") endAt: String? = null,
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
}
