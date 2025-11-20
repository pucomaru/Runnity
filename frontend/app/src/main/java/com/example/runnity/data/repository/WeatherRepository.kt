package com.example.runnity.data.repository

import com.example.runnity.BuildConfig
import com.example.runnity.data.api.WeatherRetrofitInstance
import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.response.ForecastResponse
import com.example.runnity.data.model.response.WeatherResponse
import com.example.runnity.data.remote.api.WeatherApiService
import timber.log.Timber
import java.io.IOException

/**
 * 날씨 데이터 Repository
 */
class WeatherRepository(
    private val weatherApiService: WeatherApiService = WeatherRetrofitInstance.weatherApi
) {

    /**
     * 현재 위치 기반 날씨 조회
     */
    suspend fun getCurrentWeather(
        lat: Double,
        lon: Double
    ): ApiResponse<WeatherResponse> {
        return try {
            val response = weatherApiService.getCurrentWeather(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.WEATHER_API_KEY,
                units = "metric",
                lang = "ko"
            )
            Timber.d("날씨 조회 성공: ${response.cityName}, ${response.main.temp}°C")
            ApiResponse.Success(response)
        } catch (e: IOException) {
            Timber.e(e, "날씨 조회 네트워크 오류")
            ApiResponse.NetworkError
        } catch (e: Exception) {
            Timber.e(e, "날씨 조회 실패")
            ApiResponse.Error(
                code = 0,
                message = e.message ?: "날씨 조회 실패"
            )
        }
    }

    /**
     * 5일 예보 조회
     */
    suspend fun getForecast(
        lat: Double,
        lon: Double
    ): ApiResponse<ForecastResponse> {
        return try {
            val response = weatherApiService.getForecast(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.WEATHER_API_KEY,
                units = "metric",
                lang = "ko"
            )
            Timber.d("5일 예보 조회 성공: ${response.list.size}개 항목")
            ApiResponse.Success(response)
        } catch (e: IOException) {
            Timber.e(e, "5일 예보 네트워크 오류")
            ApiResponse.NetworkError
        } catch (e: Exception) {
            Timber.e(e, "5일 예보 조회 실패")
            ApiResponse.Error(
                code = 0,
                message = e.message ?: "5일 예보 조회 실패"
            )
        }
    }
}
