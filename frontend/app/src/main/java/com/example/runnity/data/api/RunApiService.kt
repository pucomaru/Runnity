package com.example.runnity.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * Runnity API 서비스 인터페이스
 * TODO: 실제 API 명세에 맞춰 수정 필요
 */
interface RunApiService {

    // ==================== 인증 관련 ====================

    /**
     * 로그인
     * TODO: 백엔드와 맞는 Request/Response 구조
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    /**
     * 회원가입
     * TODO: 백엔드와 맞는 Request/Response 구조
     */
    @POST("api/auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

    /**
     * 로그아웃
     * TODO: 백엔드와 맞는 구조
     */
    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    /**
     * 토큰 갱신
     * TODO: 백엔드와 맞는 Request/Response 구조
     */
    @POST("api/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>


    // ==================== 러닝 기록 관련 ====================

    /**
     * 러닝 기록 저장
     * TODO: 백엔드와 맞는 Request/Response 구조
     */
    @POST("api/runs")
    suspend fun saveRunRecord(
        @Body request: RunRecordRequest
    ): Response<RunRecordResponse>

    /**
     * 러닝 기록 목록 조회
     * TODO: 백엔드와 맞는 Response 구조
     */
    @GET("api/runs")
    suspend fun getRunRecords(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<RunRecordsResponse>

    /**
     * 러닝 기록 상세 조회
     * TODO: 백엔드와 맞는 Response 구조
     */
    @GET("api/runs/{id}")
    suspend fun getRunRecordDetail(
        @Path("id") runId: Long
    ): Response<RunRecordDetailResponse>

    /**
     * 러닝 기록 삭제
     * TODO: 백엔드와 맞는 구조
     */
    @DELETE("api/runs/{id}")
    suspend fun deleteRunRecord(
        @Path("id") runId: Long
    ): Response<Unit>


    // ==================== 사용자 프로필 ====================

    /**
     * 내 프로필 조회
     * TODO: 백엔드와 맞는 Response 구조
     */
    @GET("api/users/me")
    suspend fun getMyProfile(): Response<UserProfileResponse>

    /**
     * 프로필 수정
     * TODO: 백엔드와 맞는 Request/Response 구조
     */
    @PUT("api/users/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserProfileResponse>
}

// ==================== Request/Response 데이터 클래스 ====================
// TODO: 실제 API 스펙에 맞춰 수정 필요

// 인증
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class SignupRequest(
    val email: String,
    val password: String,
    val nickname: String
)

data class SignupResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String
)

// 러닝 기록
data class RunRecordRequest(
    val distance: Double,      // 거리 (km)
    val duration: Long,        // 시간 (초)
    val pace: Double,          // 페이스 (분/km)
    val calories: Int,         // 칼로리
    val startTime: String,     // 시작 시간 (ISO 8601)
    val endTime: String        // 종료 시간 (ISO 8601)
)

data class RunRecordResponse(
    val id: Long,
    val distance: Double,
    val duration: Long,
    val pace: Double,
    val calories: Int,
    val startTime: String,
    val endTime: String,
    val createdAt: String
)

data class RunRecordsResponse(
    val records: List<RunRecordResponse>,
    val totalPages: Int,
    val totalElements: Long
)

data class RunRecordDetailResponse(
    val id: Long,
    val distance: Double,
    val duration: Long,
    val pace: Double,
    val calories: Int,
    val startTime: String,
    val endTime: String,
    val route: List<Location>?,  // 이동 경로
    val createdAt: String
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val timestamp: String
)

// 사용자
data class User(
    val id: Long,
    val email: String,
    val nickname: String,
    val profileImageUrl: String?
)

data class UserProfileResponse(
    val user: User,
    val totalDistance: Double,   // 총 거리
    val totalRuns: Int,          // 총 러닝 수
    val totalDuration: Long      // 총 시간
)

data class UpdateProfileRequest(
    val nickname: String?,
    val profileImageUrl: String?
)
