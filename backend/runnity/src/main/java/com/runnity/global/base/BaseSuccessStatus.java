package com.runnity.global.base;

import org.springframework.http.HttpStatus;

public interface BaseSuccessStatus {
    HttpStatus getHttpStatus();
    Integer getCode();
    String getMessage();
}
