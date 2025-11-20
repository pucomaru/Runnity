package com.example.runnity.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * OpenWeatherMap Current Weather API 응답 모델
 */
data class WeatherResponse(
    @SerializedName("weather")
    val weather: List<Weather>,

    @SerializedName("main")
    val main: Main,

    @SerializedName("wind")
    val wind: Wind?,

    @SerializedName("clouds")
    val clouds: Clouds?,

    @SerializedName("sys")
    val sys: Sys?,

    @SerializedName("visibility")
    val visibility: Int?,

    @SerializedName("dt")
    val dt: Long,

    @SerializedName("name")
    val cityName: String
)

data class Weather(
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class Main(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int
)

data class Wind(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int?
)

data class Clouds(
    @SerializedName("all") val all: Int
)

data class Sys(
    @SerializedName("country") val country: String,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)

/**
 * UI에서 사용할 날씨 상세 모델
 */
data class WeatherUiModel(
    val cityName: String,
    val country: String,
    val weatherMain: String,
    val weatherDescription: String,
    val weatherIcon: String,
    val temperature: Int,
    val feelsLike: Int,
    val tempMin: Int,
    val tempMax: Int,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDeg: Int?,
    val clouds: Int,
    val visibility: Int?,
    val sunrise: Long,
    val sunset: Long,
    val timestamp: Long
)

/**
 * WeatherResponse → WeatherUiModel 변환
 */
fun WeatherResponse.toUiModel(): WeatherUiModel {
    val weatherItem = weather.firstOrNull()
    val countryName = when (sys?.country) {
        "KR" -> "Korea"
        "US" -> "USA"
        "JP" -> "Japan"
        else -> "Korea"
    }

    return WeatherUiModel(
        cityName = cityName,
        country = countryName,
        weatherMain = weatherItem?.main ?: "Clear",
        weatherDescription = weatherItem?.description ?: "",
        weatherIcon = weatherItem?.icon ?: "01d",
        temperature = main.temp.toInt(),
        feelsLike = main.feelsLike.toInt(),
        tempMin = main.tempMin.toInt(),
        tempMax = main.tempMax.toInt(),
        humidity = main.humidity,
        pressure = main.pressure,
        windSpeed = wind?.speed ?: 0.0,
        windDeg = wind?.deg,
        clouds = clouds?.all ?: 0,
        visibility = visibility,
        sunrise = sys?.sunrise ?: 0,
        sunset = sys?.sunset ?: 0,
        timestamp = dt
    )
}
