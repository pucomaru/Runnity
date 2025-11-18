package com.runnity.global.base;

import org.springframework.http.HttpStatus;

public interface BaseErrorStatus {
    HttpStatus getHttpStatus();
    Integer getCode();
    String getMessage();
}
