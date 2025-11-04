package com.example.runnity.data.api

import com.example.runnity.BuildConfig
import com.example.runnity.data.util.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Retrofit 인스턴스 관리
 * - API 통신 설정
 * - 토큰 자동 추가
 * - 로깅 인터셉터
 */
object RetrofitInstance {

    // TODO: 실제 서버 URL로 교체 필요
    private const val BASE_URL = "https://api.runnity.com/"

    // 타임아웃 설정
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    /**
     * 인증 토큰 자동 추가 Interceptor
     * - Access Token을 헤더에 자동으로 추가
     */
    private val authInterceptor = Interceptor { chain ->
        val token = TokenManager.getAccessToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

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
     */
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
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
     * API 서비스 생성
     */
    fun <T> createService(service: Class<T>): T {
        return retrofit.create(service)
    }

    /**
     * RunApiService 인스턴스
     */
    val runApi: RunApiService by lazy {
        createService(RunApiService::class.java)
    }
}
