package com.runnity.global.exception;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
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

    // ClientAbortException 처리 (클라이언트 연결 끊김 - 무시)
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException e) {
        log.debug("Client aborted connection: {}", e.getMessage());
        // 응답을 반환하지 않음 - 클라이언트가 이미 연결을 끊었으므로
    }

    // 기타 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // ClientAbortException의 하위 예외도 무시
        if (e instanceof java.io.IOException && e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
            log.debug("Client connection broken: {}", e.getMessage());
            return null;
        }
        
        log.error(">>>>>>>>Internal Server Error: {}", e.getMessage());
        e.printStackTrace();
        return ApiResponse.error(ErrorStatus.INTERNAL_SERVER_ERROR);
    }
}
