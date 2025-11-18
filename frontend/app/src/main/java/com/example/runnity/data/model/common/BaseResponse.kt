package com.example.runnity.data.model.common

/**
 * 백엔드 API 공통 응답 래퍼
 * 모든 API 응답은 이 구조로 래핑됨
 *
 * @param T 실제 데이터 타입
 */
data class BaseResponse<T>(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val data: T?
)

/**
 * API 호출 결과를 표현하는 Sealed Class
 * Repository에서 ViewModel로 결과를 전달할 때 사용
 */
sealed class ApiResponse<out T> {
    /**
     * 성공 - 데이터 포함
     */
    data class Success<T>(val data: T) : ApiResponse<T>()

    /**
     * 에러 - HTTP 상태 코드, 메시지, 예외 포함
     */
    data class Error(
        val code: Int,
        val message: String,
        val exception: Exception? = null
    ) : ApiResponse<Nothing>()

    /**
     * 네트워크 오류 (인터넷 연결 끊김 등)
     */
    object NetworkError : ApiResponse<Nothing>()
}
