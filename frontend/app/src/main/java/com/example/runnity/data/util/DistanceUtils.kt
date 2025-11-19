package com.example.runnity.data.util

/**
 * 거리 코드 <-> 레이블 변환 유틸리티
 */
object DistanceUtils {

    /**
     * 거리 코드를 사람이 읽을 수 있는 레이블로 변환
     *
     * @param code 거리 코드 (예: "FIVE", "M100")
     * @return 레이블 (예: "5 km", "100 m")
     */
    fun codeToLabel(code: String?): String = when (code) {
        // 미터 단위 (100m, 500m)
        "M100" -> "100 m"
        "M500" -> "500 m"

        // 킬로미터 단위 (1km ~ 10km)
        "ONE" -> "1 km"
        "TWO" -> "2 km"
        "THREE" -> "3 km"
        "FOUR" -> "4 km"
        "FIVE" -> "5 km"
        "SIX" -> "6 km"
        "SEVEN" -> "7 km"
        "EIGHT" -> "8 km"
        "NINE" -> "9 km"
        "TEN" -> "10 km"

        // 장거리
        "FIFTEEN" -> "15 km"
        "HALF" -> "하프 (21.1 km)"
        "FULL" -> "풀코스 (42.195 km)"

        else -> "알 수 없음"
    }

    /**
     * 거리 코드를 미터 단위로 변환
     *
     * @param code 거리 코드
     * @return 미터 단위 거리
     */
    fun codeToMeter(code: String?): Int = when (code) {
        "M100" -> 100
        "M500" -> 500
        "ONE" -> 1000
        "TWO" -> 2000
        "THREE" -> 3000
        "FOUR" -> 4000
        "FIVE" -> 5000
        "SIX" -> 6000
        "SEVEN" -> 7000
        "EIGHT" -> 8000
        "NINE" -> 9000
        "TEN" -> 10000
        "FIFTEEN" -> 15000
        "HALF" -> 21097  // 21.097 km
        "FULL" -> 42195  // 42.195 km
        else -> 0
    }

    /**
     * 미터를 킬로미터 문자열로 변환
     *
     * @param meter 미터 단위 거리
     * @return "1.5 km" 형식 문자열
     */
    fun meterToKmString(meter: Int): String {
        return if (meter < 1000) {
            "$meter m"
        } else {
            String.format("%.1f km", meter / 1000f)
        }
    }
}
