package com.runnity.global.exception;

import com.runnity.global.response.ApiResponse;
import com.runnity.global.response.ValidationErrorResponse;
import com.runnity.global.response.ValidationErrorResponse.FieldErrorDetail;
import com.runnity.global.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

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

    // @Valid @RequestBody 검증 실패 처리
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.warn(">>>>>>>>Validation Failed - {} error(s): {}", 
                ex.getBindingResult().getErrorCount(),
                ex.getBindingResult().getAllErrors());

        List<FieldErrorDetail> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        // 필드 에러
                        return new FieldErrorDetail(
                                fieldError.getField(),
                                fieldError.getDefaultMessage(),
                                fieldError.getRejectedValue()
                        );
                    } else {
                        // 객체 레벨 에러 (@AssertTrue 등)
                        return new FieldErrorDetail(
                                "", // 또는 null
                                error.getDefaultMessage(),
                                null
                        );
                    }
                })
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(errors);

        return ResponseEntity
                .status(ErrorStatus.VALIDATION_FAILED.getHttpStatus())
                .body(new ApiResponse<>(
                        false,
                        ErrorStatus.VALIDATION_FAILED.getCode(),
                        ErrorStatus.VALIDATION_FAILED.getMessage(),
                        errorResponse
                ));
    }

    // @ModelAttribute 검증 실패 처리
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<Object> handleBindException(
            org.springframework.validation.BindException ex
    ) {
        log.warn(">>>>>>>>Bind Validation Failed - {} error(s): {}", 
                ex.getBindingResult().getErrorCount(),
                ex.getBindingResult().getAllErrors());

        List<FieldErrorDetail> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return new FieldErrorDetail(
                                fieldError.getField(),
                                fieldError.getDefaultMessage(),
                                fieldError.getRejectedValue()
                        );
                    } else {
                        return new FieldErrorDetail(
                                "",
                                error.getDefaultMessage(),
                                null
                        );
                    }
                })
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(errors);

        return ResponseEntity
                .status(ErrorStatus.VALIDATION_FAILED.getHttpStatus())
                .body(new ApiResponse<>(
                        false,
                        ErrorStatus.VALIDATION_FAILED.getCode(),
                        ErrorStatus.VALIDATION_FAILED.getMessage(),
                        errorResponse
                ));
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
