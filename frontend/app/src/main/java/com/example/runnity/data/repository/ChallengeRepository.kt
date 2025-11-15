package com.example.runnity.data.repository

import com.example.runnity.data.api.RetrofitInstance
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.request.CreateChallengeRequest
import com.example.runnity.data.model.request.JoinChallengeRequest
import com.example.runnity.data.model.response.ChallengeDetailResponse
import com.example.runnity.data.model.response.ChallengeListResponse
import com.example.runnity.data.model.response.ChallengeParticipantResponse
import com.example.runnity.data.model.response.ChallengeEnterResponse
import com.example.runnity.data.remote.api.ChallengeApiService
import com.example.runnity.data.util.safeApiCall

/**
 * Challenge 관련 Repository
 * 챌린지 목록 조회, 생성, 참가, 취소, 상세 조회 로직 처리
 */
class ChallengeRepository(
    private val challengeApiService: ChallengeApiService = RetrofitInstance.challengeApi
) {

    // ==================== 챌린지 목록 조회 ====================

    /**
     * 챌린지 목록 조회
     * 검색, 필터링, 정렬, 페이징 지원
     *
     * @param keyword 검색 키워드 (선택)
     * @param distances 거리 필터 (예: FIVE, TEN, HALF - 여러 개 선택 가능)
     * @param startDate 시작 날짜 (이 날짜 이후 시작하는 챌린지만 조회, 예: 2025-11-14)
     * @param endDate 종료 날짜 (이 날짜 이전에 시작하는 챌린지만 조회, 예: 2025-11-17)
     * @param startTime 시작 시간 (이 시간 이후 시작하는 챌린지만 조회, 예: 09:00:00)
     * @param endTime 종료 시간 (이 시간 이전에 시작하는 챌린지만 조회, 예: 23:00:00)
     * @param visibility 공개 여부 (PUBLIC: 공개방만, ALL: 전체)
     * @param sort 정렬 기준 (LATEST: 임박순, POPULAR: 인기순)
     * @param page 페이지 번호 (기본: 0)
     * @param size 페이지 크기 (기본: 10)
     * @return ApiResponse<ChallengeListResponse>
     */
    suspend fun getChallenges(
        keyword: String? = null,
        distances: List<String>? = null,
        startDate: String? = null,
        endDate: String? = null,
        startTime: String? = null,
        endTime: String? = null,
        visibility: String? = null,
        sort: String? = null,
        page: Int = 0,
        size: Int = 10
    ): ApiResponse<ChallengeListResponse> {
        return safeApiCall {
            challengeApiService.getChallenges(
                keyword = keyword,
                distances = distances,
                startDate = startDate,
                endDate = endDate,
                startTime = startTime,
                endTime = endTime,
                visibility = visibility,
                sort = sort,
                page = page,
                size = size
            )
        }
    }

    // ==================== 챌린지 생성 ====================

    /**
     * 챌린지 생성
     *
     * @param request 챌린지 생성 요청 (title, description, maxParticipants 등)
     * @return ApiResponse<ChallengeDetailResponse>
     */
    suspend fun createChallenge(
        request: CreateChallengeRequest
    ): ApiResponse<ChallengeDetailResponse> {
        return safeApiCall {
            challengeApiService.createChallenge(request)
        }
    }

    // ==================== 챌린지 참가 ====================

    /**
     * 챌린지 참가 신청
     * 비밀방인 경우 비밀번호 필요
     *
     * @param challengeId 챌린지 ID
     * @param password 비밀번호 (비공개 챌린지인 경우, 선택)
     * @return ApiResponse<ChallengeParticipantResponse>
     */
    suspend fun joinChallenge(
        challengeId: Long,
        password: String? = null
    ): ApiResponse<ChallengeParticipantResponse> {
        val request = JoinChallengeRequest(password = password)
        return safeApiCall {
            challengeApiService.joinChallenge(challengeId, request)
        }
    }

    // ==================== 챌린지 참가 취소 ====================

    /**
     * 챌린지 참가 취소
     *
     * @param challengeId 챌린지 ID
     * @return ApiResponse<ChallengeParticipantResponse>
     */
    suspend fun cancelChallenge(
        challengeId: Long
    ): ApiResponse<ChallengeParticipantResponse> {
        return safeApiCall {
            challengeApiService.cancelChallenge(challengeId)
        }
    }

    // ==================== 챌린지 상세 조회 ====================

    /**
     * 챌린지 상세 조회
     * 챌린지 정보와 참가자 목록 포함
     *
     * @param challengeId 챌린지 ID
     * @return ApiResponse<ChallengeDetailResponse>
     */
    suspend fun getChallengeDetail(
        challengeId: Long
    ): ApiResponse<ChallengeDetailResponse> {
        return safeApiCall {
            challengeApiService.getChallengeDetail(challengeId)
        }
    }

    /**
     * 챌린지 입장(티켓 발급)
     * @param challengeId 챌린지 ID
     * @return ApiResponse<ChallengeEnterResponse>
     */
    suspend fun enterChallenge(
        challengeId: Long
    ): ApiResponse<ChallengeEnterResponse> {
        return safeApiCall {
            challengeApiService.enterChallenge(challengeId)
        }
    }
}
