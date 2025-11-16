package com.example.runnity.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * OpenWeatherMap 5일 예보 API 응답
 */
data class ForecastResponse(
    @SerializedName("list") val list: List<ForecastItem>
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("clouds") val clouds: Clouds?,
    @SerializedName("wind") val wind: Wind?,
    @SerializedName("dt_txt") val dtTxt: String
)

/**
 * 5일 예보를 일별로 그룹화한 UI 모델
 */
data class DailyForecast(
    val date: String,           // "11월 17일 (월)"
    val weatherMain: String,    // "Clear", "Rain" 등
    val tempMax: Int,           // 최고 온도
    val tempMin: Int,           // 최저 온도
    val pop: Int = 0            // 강수 확률 (0-100)
)

/**
 * ForecastResponse를 DailyForecast 리스트로 변환
 */
fun ForecastResponse.toDailyForecasts(): List<DailyForecast> {
    val groupedByDate = list.groupBy { item ->
        // "2025-01-17 12:00:00" -> "2025-01-17"
        item.dtTxt.split(" ")[0]
    }

    return groupedByDate.map { (dateStr, items) ->
        // 최고/최저 온도 계산
        val temps = items.map { it.main.temp.toInt() }
        val tempMax = temps.maxOrNull() ?: 0
        val tempMin = temps.minOrNull() ?: 0

        // 가장 많이 나타나는 날씨 상태 선택
        val weatherMain = items
            .groupBy { it.weather.firstOrNull()?.main ?: "Clear" }
            .maxByOrNull { it.value.size }
            ?.key ?: "Clear"

        // 날짜 포맷 (yyyy-MM-dd -> M월 d일 (요일))
        val formattedDate = try {
            val parts = dateStr.split("-")
            val month = parts[1].toInt()
            val day = parts[2].toInt()

            val localDate = java.time.LocalDate.parse(dateStr)
            val dayOfWeek = when (localDate.dayOfWeek) {
                java.time.DayOfWeek.MONDAY -> "월"
                java.time.DayOfWeek.TUESDAY -> "화"
                java.time.DayOfWeek.WEDNESDAY -> "수"
                java.time.DayOfWeek.THURSDAY -> "목"
                java.time.DayOfWeek.FRIDAY -> "금"
                java.time.DayOfWeek.SATURDAY -> "토"
                java.time.DayOfWeek.SUNDAY -> "일"
                else -> ""
            }
            "${month}월 ${day}일 ($dayOfWeek)"
        } catch (e: Exception) {
            dateStr
        }

        DailyForecast(
            date = formattedDate,
            weatherMain = weatherMain,
            tempMax = tempMax,
            tempMin = tempMin
        )
    }.take(5) // 5일치만
}
