package com.example.runnity.data.api

import com.example.runnity.BuildConfig
import com.example.runnity.data.remote.api.AuthApiService
import com.example.runnity.data.remote.api.BroadcastApiService
import com.example.runnity.data.remote.api.ChallengeApiService
import com.example.runnity.data.remote.api.NotificationApiService
import com.example.runnity.data.remote.api.RunApiService
import com.example.runnity.data.remote.api.RunHistoryApiService
import com.example.runnity.data.remote.api.StatsApiService
import com.example.runnity.data.remote.interceptor.AuthInterceptor
import com.example.runnity.data.remote.interceptor.TokenAuthenticator
import com.example.runnity.data.remote.interceptor.TokenRefreshInterceptor
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Retrofit 인스턴스 관리
 * - API 통신 설정
 * - 토큰 자동 추가 (AuthInterceptor)
 * - 토큰 자동 갱신 (TokenAuthenticator)
 * - 로깅 인터셉터
 */
object RetrofitInstance {

    /**
     * ⚠️ BASE URL 설정
     * local.properties 파일에서 BASE_URL을 설정하세요.
     * 예: BASE_URL = https://runnity.p-e.kr/
     *
     * 개발/운영 환경별로 다른 URL 사용 가능
     */
    private val BASE_URL = BuildConfig.BASE_URL

    // 타임아웃 설정
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    /**
     * 로깅 Interceptor
     * - 디버그 모드에서만 HTTP 로그 출력
     */
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Timber.tag("HTTP").d(message)
    }.apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    /**
     * OkHttpClient 설정
     * - AuthInterceptor: 모든 요청에 Access Token 자동 추가
     * - TokenRefreshInterceptor: 403 에러 시 자동 토큰 갱신 및 재시도
     * - TokenAuthenticator: 401 에러 시 자동 토큰 갱신 및 재시도
     */
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor())           // 토큰 자동 추가
        .addInterceptor(TokenRefreshInterceptor())   // 403 에러 시 자동 토큰 갱신
        .addInterceptor(loggingInterceptor)          // HTTP 로깅
        .authenticator(TokenAuthenticator())         // 401 에러 시 자동 토큰 갱신
        .build()

    /**
     * Gson 설정
     */
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    /**
     * Retrofit 인스턴스
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * API 서비스 생성 헬퍼 함수
     */
    private fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }

    // ==================== API 서비스 인스턴스 ====================

    /**
     * AuthApiService 인스턴스
     * 소셜 로그인, 토큰 관리, 프로필 관리
     */
    val authApi: AuthApiService by lazy {
        createService(AuthApiService::class.java)
    }

    /**
     * ChallengeApiService 인스턴스
     * 챌린지 목록 조회, 생성, 참가, 취소, 상세 조회
     */
    val challengeApi: ChallengeApiService by lazy {
        createService(ChallengeApiService::class.java)
    }

    /**
     * RunApiService 인스턴스
     * 개인 운동 러닝 결과 저장
     */
    val runApi: RunApiService by lazy {
        createService(RunApiService::class.java)
    }

    /**
     * NotificationApiService 인스턴스
     * FCM 토큰 저장/삭제
     */
    val notificationApi: NotificationApiService by lazy {
        createService(NotificationApiService::class.java)
    }

    /**
     * RunHistoryApiService 인스턴스
     * 러닝 기록 조회, 저장, 참가한 챌린지 조회
     */
    val runHistoryApi: RunHistoryApiService by lazy {
        createService(RunHistoryApiService::class.java)
    }

    /**
     * StatsApiService 인스턴스
     * 개인의 러닝 기록 통계 조회
     */
    val statsApi: StatsApiService by lazy {
        createService(StatsApiService::class.java)
    }

    /**
     * BroadcastApiService 인스턴스
     * 현재 활성화된 중계방 처리
     */
    val broadcastApi: BroadcastApiService by lazy {
        createService(BroadcastApiService::class.java)
    }
}
