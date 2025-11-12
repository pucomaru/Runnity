package com.example.runnity.data.remote.api

import com.example.runnity.data.model.common.BaseResponse
import com.example.runnity.data.model.request.AddInfoRequest
import com.example.runnity.data.model.request.RefreshTokenRequest
import com.example.runnity.data.model.request.SocialLoginRequest
import com.example.runnity.data.model.request.UpdateProfileRequest
import com.example.runnity.data.model.response.AddInfoResponse
import com.example.runnity.data.model.response.LogoutResponse
import com.example.runnity.data.model.response.NicknameCheckResponse
import com.example.runnity.data.model.response.ProfileResponse
import com.example.runnity.data.model.response.SocialLoginResponse
import com.example.runnity.data.model.response.TokenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Auth 관련 API 서비스
 * 소셜 로그인, 토큰 재발급, 프로필 관리
 */
interface AuthApiService {

    // ==================== 소셜 로그인 ====================

    /**
     * 구글 로그인
     * @param request 구글 ID Token
     * @return Access Token, Refresh Token, 추가 정보 필요 여부
     */
    @POST("api/v1/auth/login/google")
    suspend fun loginWithGoogle(
        @Body request: SocialLoginRequest
    ): Response<BaseResponse<SocialLoginResponse>>

    /**
     * 카카오 로그인
     * @param request 카카오 ID Token
     * @return Access Token, Refresh Token, 추가 정보 필요 여부
     */
    @POST("api/v1/auth/login/kakao")
    suspend fun loginWithKakao(
        @Body request: SocialLoginRequest
    ): Response<BaseResponse<SocialLoginResponse>>


    // ==================== 토큰 관리 ====================

    /**
     * Access Token 재발급
     * @param request Refresh Token
     * @return 새로운 Access Token, Refresh Token
     */
    @POST("api/v1/auth/token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<BaseResponse<TokenResponse>>

    /**
     * 로그아웃
     * Access Token을 블랙리스트에 등록
     * @return 로그아웃 메시지
     */
    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<BaseResponse<LogoutResponse>>


    // ==================== 회원 정보 ====================

    /**
     * 추가 정보 입력 (신규 회원)
     * multipart/form-data로 data(JSON) + profileImage(File, 선택) 전송
     *
     * @param data AddInfoRequest JSON 문자열
     * @param profileImage 프로필 이미지 파일 (선택)
     * @return 성공 메시지
     */
    @Multipart
    @POST("api/v1/auth/addInfo")
    suspend fun addAdditionalInfo(
        @Part("data") data: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): Response<BaseResponse<AddInfoResponse>>

    /**
     * 내 프로필 조회
     * 로그인한 사용자의 프로필 정보를 조회
     *
     * 참고: 스웨거에서 POST로 정의되어 있음 (일반적으로 GET이지만 백엔드 스펙 따름)
     *
     * @return 프로필 정보 (memberId, email, nickname, gender, height, weight, birth)
     */
    @POST("api/v1/me/profile")
    suspend fun getMyProfile(): Response<BaseResponse<ProfileResponse>>

    /**
     * 내 프로필 수정
     * multipart/form-data로 data(JSON) + profileImage(File, 선택) 전송
     *
     * @param data UpdateProfileRequest JSON 문자열
     * @param profileImage 프로필 이미지 파일 (선택)
     * @return 수정된 프로필 정보
     */
    @Multipart
    @PUT("api/v1/me/profile")
    suspend fun updateMyProfile(
        @Part("data") data: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): Response<BaseResponse<ProfileResponse>>

    /**
     * 닉네임 중복 체크
     * 닉네임 사용 가능 여부를 확인 (대소문자/양끝 공백 무시)
     *
     * @param nickname 체크할 닉네임
     * @return available (true: 사용 가능, false: 중복)
     */
    @GET("api/v1/me/nicknameCheck")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<BaseResponse<NicknameCheckResponse>>
}
