package com.example.runnity.data.util

import com.example.runnity.data.model.common.ApiResponse
import com.example.runnity.data.model.common.BaseResponse
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

/**
 * API 호출을 안전하게 처리하는 확장 함수
 * - 네트워크 예외 처리
 * - HTTP 상태 코드 처리
 * - BaseResponse 래퍼 언래핑
 *
 * @param apiCall Retrofit suspend 함수
 * @return ApiResponse (Success, Error, NetworkError)
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<BaseResponse<T>>
): ApiResponse<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()

            if (body != null && body.isSuccess && body.data != null) {
                // 성공 케이스
                ApiResponse.Success(body.data)
            } else {
                // 서버에서 isSuccess=false를 반환한 경우
                ApiResponse.Error(
                    code = body?.code ?: response.code(),
                    message = body?.message ?: "Unknown error"
                )
            }
        } else {
            // HTTP 에러 (401, 404, 500 등)
            // 에러 body에서 상세 메시지 추출 시도
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                // errorBody를 파싱하여 message 필드 추출 (간단한 JSON 파싱)
                errorBody?.let {
                    val messageStart = it.indexOf("\"message\"")
                    if (messageStart != -1) {
                        val valueStart = it.indexOf(":", messageStart) + 1
                        val valueEnd = it.indexOf(",", valueStart).takeIf { it != -1 } ?: it.indexOf("}", valueStart)
                        it.substring(valueStart, valueEnd).trim().removeSurrounding("\"")
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to parse error message from response body")
                null
            }

            ApiResponse.Error(
                code = response.code(),
                message = errorMessage ?: response.message() ?: "HTTP Error ${response.code()}"
            )
        }
    } catch (e: IOException) {
        // 네트워크 오류 (연결 끊김, 타임아웃 등)
        Timber.e(e, "Network error occurred")
        ApiResponse.NetworkError
    } catch (e: Exception) {
        // 기타 예외 (JSON 파싱 오류 등)
        Timber.e(e, "Unexpected error occurred")
        ApiResponse.Error(
            code = 0,
            message = e.message ?: "Unknown error",
            exception = e
        )
    }
}

/**
 * Unit 타입 응답을 위한 safeApiCall (데이터가 중요하지 않은 경우)
 * 예: 로그아웃, 삭제 등
 *
 * @param T 응답 데이터 타입 (무시되고 Unit으로 반환됨)
 */
suspend fun <T> safeApiCallUnit(
    apiCall: suspend () -> Response<BaseResponse<T>>
): ApiResponse<Unit> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()

            if (body != null && body.isSuccess) {
                ApiResponse.Success(Unit)
            } else {
                ApiResponse.Error(
                    code = body?.code ?: response.code(),
                    message = body?.message ?: "Unknown error"
                )
            }
        } else {
            // HTTP 에러 (401, 404, 500 등)
            // 에러 body에서 상세 메시지 추출 시도
            val errorBody = response.errorBody()?.string()
            val errorMessage = try {
                errorBody?.let {
                    val messageStart = it.indexOf("\"message\"")
                    if (messageStart != -1) {
                        val valueStart = it.indexOf(":", messageStart) + 1
                        val valueEnd = it.indexOf(",", valueStart).takeIf { it != -1 } ?: it.indexOf("}", valueStart)
                        it.substring(valueStart, valueEnd).trim().removeSurrounding("\"")
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to parse error message from response body")
                null
            }

            ApiResponse.Error(
                code = response.code(),
                message = errorMessage ?: response.message() ?: "HTTP Error ${response.code()}"
            )
        }
    } catch (e: IOException) {
        Timber.e(e, "Network error occurred")
        ApiResponse.NetworkError
    } catch (e: Exception) {
        Timber.e(e, "Unexpected error occurred")
        ApiResponse.Error(
            code = 0,
            message = e.message ?: "Unknown error",
            exception = e
        )
    }
}
