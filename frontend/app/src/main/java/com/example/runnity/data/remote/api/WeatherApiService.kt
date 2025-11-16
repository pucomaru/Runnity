package com.example.runnity.data.remote.api

import com.example.runnity.data.model.response.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * OpenWeatherMap API 서비스
 */
interface WeatherApiService {

    /**
     * 현재 날씨 조회 (좌표 기반)
     */
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",  // 섭씨
        @Query("lang") lang: String = "ko"         // 한국어
    ): WeatherResponse
}
