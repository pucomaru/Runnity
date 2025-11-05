package com.runnity.global.exception;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // GeneralException 처리
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(GlobalException e) {
        log.warn(">>>>>>>>GlobalException: {}", e.getErrorStatus().getMessage());
        return ApiResponse.error(e.getErrorStatus());
    }

    // 기타 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error(">>>>>>>>Internal Server Error: {}", e.getMessage());
        e.printStackTrace();
        return ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
    }
}
